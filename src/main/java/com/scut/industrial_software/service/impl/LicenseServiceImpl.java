package com.scut.industrial_software.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.LicenseApplyMapper;
import com.scut.industrial_software.mapper.LicenseResultMapper;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.LicenseApplyDTO;
import com.scut.industrial_software.model.entity.license.LicenseApply;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.model.entity.license.LicenseResultInfo;
import com.scut.industrial_software.model.vo.ApplyLicenseVO;
import com.scut.industrial_software.model.vo.LicenseResultVO;
import com.scut.industrial_software.service.ILicenseService;
import com.scut.industrial_software.service.ILicenseStrategy;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.extension.toolkit.Db.save;

@Service
public class LicenseServiceImpl implements ILicenseService {

    @Autowired
    private LicenseFactory licenseFactory;

    @Autowired
    private LicenseResultMapper licenseResultMapper;

    @Autowired
    private LicenseApplyMapper licenseApplyMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${license.upload.directory}")
    private String licenseUploadDir;

    private static final Logger log = LoggerFactory.getLogger(LicenseServiceImpl.class);

    private static final DateTimeFormatter FALLBACK_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    // 向客户端返回生成的License文件路径和密钥
    public ApiResult<Object> createLicense(String toolType) throws Exception {
        // 1. 生成证书时的幂等性设计，保证同一用户同一工具类型在同一时间只能生成一份证书，这里使用Redisson分布式锁实现
        // 1.ps01 这里采用Redisson的分布式锁的原因在于，因为调用第三方库生成证书可能时间比较长，如果使用普通的Redis分布式锁可能会因为锁过期而导致证书重复生成的问题
        // 1.1 获取当前用户ID
        // Integer userId = UserHolder.getUser().getId();
        Integer userId = 20; // TODO: 目前先写死，后续集成用户模块后放开，当前为了方便测试
        String lockKey = RedisConstants.TOOL_LICENSE_KEY_PREFIX + userId + ":" + toolType;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(2, -1, TimeUnit.SECONDS);       // 等待时间很大程序上取决于证书生成的时间
            if (!locked) {
                return ApiResult.failed("请稍后重试");
            }
            // 2. 如果获得锁，先检查数据库中是否存在该用户对该工具类型的未过期证书
            LicenseResultInfo existingLicense = licenseResultMapper.selectActiveByUserIdAndToolType(userId, toolType);
            if (existingLicense != null) {
                // 2.1 如果存在未过期证书，根据当前时间进行下一次判断是否生效
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(existingLicense.getExpireTime())) {
                    // 2.1.1 证书仍在有效期内，直接返回已有证书信息
                    log.info("An active license already exists for user ID {} and tool type {}. Returning existing license.", userId, toolType);
                    // 2.1.2 将已有证书信息封装成VO返回给前端
                    LicenseResultVO licenseResultVO = convertToVO(existingLicense);
                    return ApiResult.success(licenseResultVO);
                } else {
                    // 2.2 证书已过期，更新数据库里的状态为无效
                    log.info("The existing license for user ID {} and tool type {} has expired. Generating a new license.", userId, toolType);
                    licenseResultMapper.deactivateByUserIdAndToolType(userId, toolType);
                }
            }
            // 3. 调用工具执行证书生成逻辑并保存进数据库，返回给前端
            return generateAndPersistLicense(toolType, userId);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    private ApiResult<Object> generateAndPersistLicense(String toolType, Integer userId) throws Exception {
        log.info("Currently, a type {} certificate is being generated. The applicant for the certificate is {}.", toolType, userId);
        ILicenseStrategy strategy = licenseFactory.getStrategy(toolType);
        // 1. 调用工具执行证书生成逻辑
        LicenseResult result = strategy.generateLicense();
        if (result.getLicenseData() != null) {
            log.info("License file generated successfully for tool type {}. License path: {}, License name: {}.", toolType, result.getLicensePath(), result.getLicenseName());
        } else {
            log.error("Failed to generate license file for tool type {}.", toolType);
            throw new Exception("License generation failed, license data is null.");
        }
        // 2. 将result信息转换成LicenseResultInfo保存进数据库
        LicenseResultInfo licenseResultInfo = getLicenseResultInfo(toolType, result, userId);
        // 3. 保存进数据库
        save(licenseResultInfo);
        // 4. 构造返回给前端的LicenseResultVO
        LicenseResultVO licenseResultVO = new LicenseResultVO();
        licenseResultVO.setLicenseId(licenseResultInfo.getLicenseId());
        // 5. 安全读取生成的license文件为字节数组返回给前端
        byte[] licenseData = safeCopyLicenseBytes(result);
        licenseResultVO.setLicenseData(licenseData);
        return ApiResult.success(licenseResultVO);
    }

    /**
     * 将LicenseResult转换成LicenseResultInfo实体类
     */
    private static LicenseResultInfo getLicenseResultInfo(String toolType, LicenseResult result, Integer userId) {
        // 将LicenseResult转换成LicenseResultInfo实体类并保存进数据库
        LicenseResultInfo licenseResultInfo = new LicenseResultInfo();
        licenseResultInfo.setLicensePath(result.getLicensePath());
        licenseResultInfo.setLicenseName(result.getLicenseName());
        licenseResultInfo.setCreateTime(result.getCreateTime());
        licenseResultInfo.setExpireTime(result.getExpireTime());
        licenseResultInfo.setToolType(toolType);
        licenseResultInfo.setUserId(userId);
        licenseResultInfo.setStatus(true);
        return licenseResultInfo;
    }

    /**
     * 将LicenseResultInfo转换成LicenseResultVO返回给前端
     * @param info LicenseResultInfo实体类
     * @return 返回给前端的LicenseResultVO
     */
    private LicenseResultVO convertToVO(LicenseResultInfo info) throws IOException {
        LicenseResultVO vo = new LicenseResultVO();
        vo.setLicenseId(info.getLicenseId());
        vo.setLicenseData(readFileToByteArray(info.getLicensePath()));
        return vo;
    }

    /**
     * 安全读取生成的license文件为字节数组：
     * 1) 优先使用内存中的licenseData并克隆副本
     * 2) 否则回退到licensePath读取，校验存在、可读
     */
    private byte[] safeCopyLicenseBytes(LicenseResult result) throws IOException {
        if (result == null) {
            throw new FileNotFoundException("License result is null");
        }
        byte[] data = result.getLicenseData();
        if (data != null) {
            return data.clone(); // 防止外部修改原数组
        }
        String pathStr = result.getLicensePath();
        if (pathStr == null || pathStr.isBlank()) {
            throw new FileNotFoundException("License path is empty");
        }
        return readFileToByteArray(pathStr);
    }

    private byte[] readFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);
        Path path = file.toPath();
        if (!Files.exists(path)) {
            throw new FileNotFoundException("License file not found: " + path);
        }
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IOException("License file is not readable: " + path);
        }
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public ApiResult<?> approveLicense(LicenseApplyDTO licenseApplyDTO) {
        if (licenseApplyDTO == null) {
            return ApiResult.failed("申请数据不能为空");
        }
//        UserDTO currentUser = UserHolder.getUser();
//        if (currentUser == null) {
//            return ApiResult.failed("未登录用户无法申请证书");
//        }
        Integer testId = 10;
        String testName = "张三";
        LocalDateTime validFrom;
        LocalDateTime validTo;
        try {
            validFrom = parseDateTime(licenseApplyDTO.getValidFrom(), "validFrom");
            validTo = parseDateTime(licenseApplyDTO.getValidTo(), "validTo");
        } catch (IllegalArgumentException ex) {
            log.warn("License apply datetime parse error: {}", ex.getMessage());
            return ApiResult.failed(ex.getMessage());
        }
        LicenseApply entity = new LicenseApply();
        /*
        // 获取当前时间戳
        long currentTime = System.currentTimeMillis();
        // 获取证书的模块阶段名
        String moduleName = licenseApplyDTO.getModuleId();
        // 定义证书编号生成
        String licenseNo = moduleName.toUpperCase(Locale.ROOT) + "_" + String.valueOf(currentTime);
         */
        entity.setRequestId(generateApplyId(testId));
        entity.setMacAddress(licenseApplyDTO.getMacAddress());
        entity.setCategoryId(licenseApplyDTO.getCategoryId());
        entity.setModuleId(licenseApplyDTO.getModuleId());
        // entity.setLicenseNo(licenseNo);
        entity.setCustomerName(licenseApplyDTO.getCustomerName());
        entity.setUsageCount(licenseApplyDTO.getUsageCount());
        entity.setValidFrom(validFrom);
        entity.setValidTo(validTo);
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUserName(testName);
        int inserted = licenseApplyMapper.insert(entity);
        if (inserted <= 0) {
            return ApiResult.failed("证书申请保存失败");
        }
        // 这里需要将entity实体对象转换成返回视图对象
        ApplyLicenseVO applyLicenseVO = new ApplyLicenseVO();
        BeanUtils.copyProperties(entity, applyLicenseVO);
        return ApiResult.success(applyLicenseVO);
    }

    private String generateApplyId(Integer userId) {
        // 假设使用雪花算法生成一个 long
        long snowflakeId = IdUtil.getSnowflake().nextId();
        // 如果必须返回字符串，建议：业务前缀 + 用户ID后4位 + 雪花ID
        // 或者直接返回 snowflakeId 的字符串形式
        return String.format("%d%04d", snowflakeId, userId % 10000);
    }

    private LocalDateTime parseDateTime(String source, String fieldName) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }

        try {
            // 如果长度是 10，说明只有日期 (yyyy-MM-dd)
            if (source.length() == 10) {
                LocalDate date = LocalDate.parse(source);
                if(fieldName.equals("validFrom")) {
                    return date.atStartOfDay();
                }
                if(fieldName.equals("validTo")) {
                    return date.atStartOfDay().minusSeconds(1);
                }
            }
            // 尝试按照 yyyy-MM-dd HH:mm:ss 解析
            return LocalDateTime.parse(source, FALLBACK_DATETIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            try {
                // 最后尝试 ISO 格式
                return LocalDateTime.parse(source, ISO_FORMATTER);
            } catch (DateTimeParseException innerEx) {
                throw new IllegalArgumentException(fieldName + "格式不正确，期望 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss", innerEx);
            }
        }
    }

    @Override
    public ApiResult<?> getApplyRequests(String moduleKeyword, String status, long page, long size) {
        Page<LicenseApply> pageParam = new Page<>(Math.max(page, 1), Math.max(size, 1));
        Page<LicenseApply> applyPage = licenseApplyMapper.selectByModuleAndStatus(pageParam, moduleKeyword, status);
        if (applyPage == null || applyPage.getRecords().isEmpty()) {
            Page<ApplyLicenseVO> emptyPage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), 0);
            emptyPage.setRecords(Collections.emptyList());
            return ApiResult.success(emptyPage);
        }
        List<ApplyLicenseVO> voList = applyPage.getRecords().stream().map(apply -> {
            ApplyLicenseVO vo = new ApplyLicenseVO();
            BeanUtils.copyProperties(apply, vo);
            return vo;
        }).collect(Collectors.toList());
        Page<ApplyLicenseVO> voPage = new Page<>(applyPage.getCurrent(), applyPage.getSize(), applyPage.getTotal());
        voPage.setRecords(voList);
        return ApiResult.success(voPage);
    }

    @Override
    public ApiResult<?> approveApplyRequest(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return ApiResult.failed("申请编号不能为空");
        }
        LicenseApply apply = licenseApplyMapper.selectById(requestId);
        if (apply == null) {
            return ApiResult.failed("申请记录不存在");
        }
        if ("APPROVED".equalsIgnoreCase(apply.getStatus())) {
            ApplyLicenseVO vo = new ApplyLicenseVO();
            BeanUtils.copyProperties(apply, vo);
            return ApiResult.success(vo);
        }
        LicenseApply update = new LicenseApply();
        update.setRequestId(requestId);
        update.setStatus("APPROVED");
        int rows = licenseApplyMapper.updateById(update);
        if (rows <= 0) {
            return ApiResult.failed("审批更新失败");
        }
        apply.setStatus("APPROVED");
        ApplyLicenseVO vo = new ApplyLicenseVO();
        BeanUtils.copyProperties(apply, vo);
        return ApiResult.success(vo);
    }

    @Override
    public ApiResult<?> rejectApplyRequest(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return ApiResult.failed("申请编号不能为空");
        }
        LicenseApply apply = licenseApplyMapper.selectById(requestId);
        if (apply == null) {
            return ApiResult.failed("申请记录不存在");
        }
        if ("REJECTED".equalsIgnoreCase(apply.getStatus())) {
            ApplyLicenseVO vo = new ApplyLicenseVO();
            BeanUtils.copyProperties(apply, vo);
            return ApiResult.success(vo);
        }
        LicenseApply update = new LicenseApply();
        update.setRequestId(requestId);
        update.setStatus("REJECTED");
        int rows = licenseApplyMapper.updateById(update);
        if (rows <= 0) {
            return ApiResult.failed("拒绝申请失败");
        }
        apply.setStatus("REJECTED");
        ApplyLicenseVO vo = new ApplyLicenseVO();
        BeanUtils.copyProperties(apply, vo);
        return ApiResult.success(vo);
    }

    @Override
    public ApiResult<?> uploadLicenseFile(String requestId, MultipartFile file) {
        if (requestId == null || requestId.isBlank()) {
            return ApiResult.failed("申请编号不能为空");
        }
        if (file == null || file.isEmpty()) {
            return ApiResult.failed("上传文件不能为空");
        }
        LicenseApply apply = licenseApplyMapper.selectById(requestId);
        if (apply == null) {
            return ApiResult.failed("申请记录不存在");
        }
        if (!StringUtils.hasText(licenseUploadDir)) {
            return ApiResult.failed("未配置证书上传目录");
        }
        try {
            Path baseDir = Paths.get(licenseUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "license.lic" : file.getOriginalFilename());
            String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".lic";
            String uniqueFileName = "license_" + IdUtil.getSnowflake().nextId() + extension;
            Path targetPath = baseDir.resolve(uniqueFileName).normalize();
            if (!targetPath.startsWith(baseDir)) {
                return ApiResult.failed("非法的文件路径");
            }
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            String licenseNo = "LIC-" + IdUtil.getSnowflake().nextId();
            int rows = licenseApplyMapper.updateLicenseFileInfo(requestId, licenseNo, targetPath.toString());
            if (rows <= 0) {
                return ApiResult.failed("更新证书信息失败");
            }
            apply.setLicenseNo(licenseNo);
            apply.setLicensePath(targetPath.toString());
            ApplyLicenseVO vo = new ApplyLicenseVO();
            BeanUtils.copyProperties(apply, vo);
            return ApiResult.success(vo);
        } catch (IOException ex) {
            log.error("保存证书文件失败", ex);
            return ApiResult.failed("证书文件保存失败: " + ex.getMessage());
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadLicenseFile(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return ResponseEntity.badRequest().build();
        }
        LicenseApply apply = licenseApplyMapper.selectById(requestId);
        if (apply == null || !StringUtils.hasText(apply.getLicensePath())) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path filePath = Paths.get(apply.getLicensePath()).normalize().toAbsolutePath();
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.notFound().build();
            }
            if (StringUtils.hasText(licenseUploadDir)) {
                Path baseDir = Paths.get(licenseUploadDir).toAbsolutePath().normalize();
                if (!filePath.startsWith(baseDir)) {
                    log.warn("Denied download due to path escaping base dir: {}", filePath);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            byte[] data = Files.readAllBytes(filePath);
            String filename = filePath.getFileName().toString();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException ex) {
            log.error("读取证书文件失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
