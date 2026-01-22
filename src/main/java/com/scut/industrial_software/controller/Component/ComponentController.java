package com.scut.industrial_software.controller.Component;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IComponentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/components")
public class ComponentController {

    @Autowired
    private IComponentService moduleDownloadService;

    // 下载前处理和求解器
    @GetMapping("/install")
    public ResponseEntity<Resource> installModule(@RequestParam String dynamicsDirection,
                                                  @RequestParam String moduleType,
                                                  @RequestParam(defaultValue = "CPU") String resourceType){
        return moduleDownloadService.downloadModule(dynamicsDirection, moduleType, resourceType);
    }

    // 下载后处理
    @GetMapping("/install/postprocessing")
    public ResponseEntity<Resource> installPostprocessingModule(){
        return moduleDownloadService.downloadPostprocessingModule();
    }

    // 获取组件列表
    @GetMapping
    public ApiResult<?> getModuleList(){
        return moduleDownloadService.getModuleList();
    }

}
