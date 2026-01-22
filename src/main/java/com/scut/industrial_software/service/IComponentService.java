package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface IComponentService {

    public ResponseEntity<Resource> downloadModule(String dynamicsDirection, String moduleType, String resourceType);

    public ResponseEntity<Resource> downloadPostprocessingModule();

    public ApiResult<?> getModuleList();
}
