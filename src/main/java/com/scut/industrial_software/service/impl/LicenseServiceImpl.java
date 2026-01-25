package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.LicenseResultMapper;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.model.entity.license.LicenseResultInfo;
import com.scut.industrial_software.model.vo.LicenseResultVO;
import com.scut.industrial_software.service.ILicenseService;
import com.scut.industrial_software.service.ILicenseStrategy;
import com.scut.industrial_software.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.baomidou.mybatisplus.extension.toolkit.Db.save;

@Service
public class LicenseServiceImpl implements ILicenseService {

    @Autowired
    private LicenseFactory licenseFactory;

    @Autowired
    private LicenseResultMapper licenseResultMapper;

    @Autowired
    private RedissonClient redissonClient;

    private static final Logger log = LoggerFactory.getLogger(LicenseServiceImpl.class);

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

}
