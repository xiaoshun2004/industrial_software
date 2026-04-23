package com.scut.industrial_software.controller.Component;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/components")
public class ComponentController {

    @Autowired
    private IComponentService moduleDownloadService;

    // 直接安装接口，适用于小型组件的安装包，支持断点续传，需要鉴权
    @GetMapping("/install")
    public ResponseEntity<Resource> installModule(@RequestParam Integer componentId,
                                                  @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return moduleDownloadService.downloadModule(componentId, rangeHeader);
    }

    // 生成单组件下载 Token 的接口，适用于大型组件的安装包，前端可以先调用该接口获取一个临时有效的 Token，然后使用该 Token 调用下载接口进行安装，支持断点续传
    @PostMapping("/install/{componentId}/download-token")
    public ApiResult<?> createInstallModuleToken(@PathVariable Integer componentId) {
        return moduleDownloadService.createSingleDownloadToken(componentId);
    }

    // 使用 Token 进行组件安装的接口，前端需要提供之前生成的 Token，后端会验证 Token 的有效性后允许下载，适用于大型组件的安装包，支持断点续传
    @GetMapping("/install/{componentId}/download")
    public ResponseEntity<Resource> installModuleByToken(@PathVariable Integer componentId,
                                                         @RequestParam String token,
                                                         @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return moduleDownloadService.downloadModuleByToken(componentId, token, rangeHeader);
    }

    @GetMapping("/install/batch")
    public ResponseEntity<StreamingResponseBody> installModules(@RequestParam List<Integer> componentIds) {
        return moduleDownloadService.downloadModules(componentIds);
    }

    // 获取组件列表的接口，前端可以调用该接口获取所有可用组件的信息，供用户选择安装，支持分页和搜索功能，需要鉴权
    @GetMapping
    public ApiResult<?> getModuleList() {
        return moduleDownloadService.getModuleList();
    }
}
