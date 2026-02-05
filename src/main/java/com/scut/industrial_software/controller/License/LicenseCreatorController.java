package com.scut.industrial_software.controller.License;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.constant.Constant;
import com.scut.industrial_software.model.dto.LicenseApplyDTO;
import com.scut.industrial_software.service.ILicenseService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 求解器处理器工具License证书生成接口
 */
@RestController
@Api(tags = "license管理")
@RequestMapping("/license")             // 映射地址后续修改
public class LicenseCreatorController {

    @Autowired
    private ILicenseService licenseService;

    // TODO: 后续增加参数，指定工具类型等，当前只是个测试
    @GetMapping("/create")
    public ApiResult<?> createLicense() throws Exception {
        return licenseService.createLicense("Structure");
    }

    // 获取分类下阶段列表
    @GetMapping("/categories")
    public ApiResult<Map<String,String>> getModuleCategories(){
        return ApiResult.success(Constant.moduleCategories);
    }

    // 根据阶段分类获取模块列表
    @GetMapping("/modules")
    public ApiResult<Map<String,String>> getModulesByCategory(@RequestParam String categoryId){
        Map<String, String> modules = Constant.moduleLibrary.get(categoryId);
        return ApiResult.success(modules);
    }

    // 用户申请证书
    @PostMapping("/requests")
    public ApiResult<?> approveLicense(@RequestBody LicenseApplyDTO dto){
        return licenseService.approveLicense(dto);
    }

    // 获取所有证书申请请求列表（管理员功能）
    @GetMapping("/requests")
    public ApiResult<?> getApplyRequests(@RequestParam(required = false) String moduleKeyword,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size){
        return licenseService.getApplyRequests(moduleKeyword, status, page, size);
    }

    // 审批通过申请
    @PostMapping("/requests/{requestId}/approve")
    public ApiResult<?> approveApplyRequest(@PathVariable String requestId){
        return licenseService.approveApplyRequest(requestId);
    }

    // 拒绝证书申请
    @PostMapping("/requests/{requestId}/reject")
    public ApiResult<?> rejectApplyRequest(@PathVariable String requestId){
        return licenseService.rejectApplyRequest(requestId);
    }

    // 上传证书文件
    @PostMapping("/requests/{requestId}/upload")
    public ApiResult<?> uploadLicenseFile(@PathVariable String requestId, @RequestParam MultipartFile file){
        return licenseService.uploadLicenseFile(requestId, file);
    }

    // 下载证书文件（普通用户）
    @GetMapping("/requests/{requestId}/download")
    public ResponseEntity<byte[]> downloadLicenseFile(@PathVariable String requestId){
        return licenseService.downloadLicenseFile(requestId);
    }



}
