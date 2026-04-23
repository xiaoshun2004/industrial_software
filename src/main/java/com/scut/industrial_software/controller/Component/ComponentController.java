package com.scut.industrial_software.controller.Component;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.ComponentBatchDownloadRequestDTO;
import com.scut.industrial_software.service.IComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/components")
public class ComponentController {

    @Autowired
    private IComponentService moduleDownloadService;

    @PostMapping("/install/{componentId}/download-token")
    public ApiResult<?> createInstallModuleToken(@PathVariable Integer componentId) {
        return moduleDownloadService.createSingleDownloadToken(componentId);
    }

    @GetMapping("/install/{componentId}/download")
    public ResponseEntity<Resource> installModuleByToken(@PathVariable Integer componentId,
                                                         @RequestParam String token,
                                                         @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return moduleDownloadService.downloadModuleByToken(componentId, token, rangeHeader);
    }

    @PostMapping("/install/batch/stream-token")
    public ApiResult<?> createBatchInstallModulesToken(@RequestBody ComponentBatchDownloadRequestDTO requestDTO) {
        return moduleDownloadService.createBatchStreamToken(requestDTO);
    }

    @GetMapping("/install/batch/stream")
    public ResponseEntity<StreamingResponseBody> installModulesByToken(@RequestParam String token) {
        return moduleDownloadService.downloadModulesByToken(token);
    }

    @GetMapping
    public ApiResult<?> getModuleList() {
        return moduleDownloadService.getModuleList();
    }
}
