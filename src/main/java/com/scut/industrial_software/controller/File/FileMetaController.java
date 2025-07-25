package com.scut.industrial_software.controller.File;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.FileQueryDTO;
import com.scut.industrial_software.model.vo.FileMetaVO;
import com.scut.industrial_software.model.vo.PageVO;
import com.scut.industrial_software.service.IFileMetaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 文件管理控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/dataManagement")
public class FileMetaController {

    @Autowired
    private IFileMetaService fileMetaService;

    /**
     * 上传并保存文件√
     *
     * @param dbType 文件隶属的数据库类型标识
     * @param fileName 用户指定的文件名
     * @param file 文件内容
     * @return 文件元数据
     */
    @PostMapping("/upload")
    public ApiResult<FileMetaVO> uploadFile(
            @RequestParam("dbType") String dbType,
            @RequestParam("fileName") String fileName,
            @RequestParam("file") MultipartFile file) {
        log.info("上传文件: {}, dbType: {}", file.getOriginalFilename(), dbType);
        FileMetaVO fileMetaVO = fileMetaService.uploadFile(dbType, fileName, file);
        return ApiResult.success(fileMetaVO);
    }

    /**
     * 下载文件√
     *
     * @param dbType 文件隶属的数据库类型
     * @param field 要下载的文件ID
     * @return 文件字节流
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("dbType") String dbType,
            @RequestParam("field") String field) {
        log.info("下载文件: {}", field);
        
        // 获取文件信息
        FileMetaVO fileInfo = fileMetaService.getFileInfo(field);
        
        // 获取文件内容
        byte[] fileContent = fileMetaService.downloadFile(field);
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        
        // 对文件名进行URL编码，解决中文文件名问题
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileInfo.getFileName(), StandardCharsets.UTF_8.toString());
            // 替换空格为%20，确保兼容性
            encodedFileName = encodedFileName.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encodedFileName = "file";
            log.error("文件名编码失败", e);
        }
        
        // 使用RFC 5987编码，支持国际化字符
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFileName);

        log.info("文件下载完成: {}", fileInfo.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    /**
     * 获取当前用户的文件列表√
     *
     * @param dbType 文件隶属的数据库类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 文件列表
     */
    @GetMapping("/files")
    public ApiResult<PageVO<FileMetaVO>> getMyFiles(
            @RequestParam(value = "dbType") String dbType,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("获取我的文件列表: dbType={}, pageNum={}, pageSize={}", dbType,pageNum, pageSize);
        
        FileQueryDTO queryDTO = new FileQueryDTO();
        queryDTO.setDbType(dbType);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        PageVO<FileMetaVO> pageResult = fileMetaService.getMyFiles(queryDTO);
        return ApiResult.success(pageResult);
    }


//    /**
//     * 根据关键字搜索文件
//     *
//     * @param pageNum 页码
//     * @param pageSize 每页大小
//     * @param keyword 关键字
//     * @return 文件列表
//     */
//
//    @GetMapping("/search/page")
//    public ApiResult<PageVO<FileMetaVO>> searchFiles(
//            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
//            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
//            @RequestParam(value = "keyword", required = false) String keyword) {
//        log.info("搜索文件: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);
//
//        FileQueryDTO queryDTO = new FileQueryDTO();
//        queryDTO.setPageNum(pageNum);
//        queryDTO.setPageSize(pageSize);
//        queryDTO.setKeyword(keyword);
//
//        PageVO<FileMetaVO> pageResult = fileMetaService.searchFiles(queryDTO);
//        return ApiResult.success(pageResult);
//    }
//
//    /**
//     * 删除文件
//     *
//     * @param id 文件ID
//     * @return 操作结果
//     */
//    @DeleteMapping("/delete/{id}")
//    public ApiResult<Object> deleteFile(@PathVariable Long id) {
//        log.info("删除文件: {}", id);
//        boolean result = fileMetaService.deleteFile(id);
//        return result ? ApiResult.success(null) : ApiResult.failed("删除失败");
//    }

}