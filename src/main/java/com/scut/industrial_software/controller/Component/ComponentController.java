package com.scut.industrial_software.controller.Component;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/components")
public class ComponentController {

    @Autowired
    private IComponentService moduleDownloadService;

    // 下载组件
    @GetMapping("/install")
    public ResponseEntity<Resource> installModule(@RequestParam Integer componentId,
                                                  @RequestHeader (value = "Range", required = false) String rangeHeader){
        return moduleDownloadService.downloadModule(componentId,rangeHeader);
    }

    // Batch download component installers
    @GetMapping("/install/batch")
    public ResponseEntity<StreamingResponseBody> installModules(@RequestParam List<Integer> componentIds) {
        return moduleDownloadService.downloadModules(componentIds);
    }

    // Get component list
    @GetMapping
    public ApiResult<?> getModuleList(){
        return moduleDownloadService.getModuleList();
    }

}
