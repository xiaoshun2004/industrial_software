package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.ComponentBatchDownloadRequestDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface IComponentService {

    ApiResult<?> createSingleDownloadToken(Integer componentId);

    ResponseEntity<Resource> downloadModuleByToken(Integer componentId, String token, String rangeHeader);

    ApiResult<?> createBatchStreamToken(ComponentBatchDownloadRequestDTO requestDTO);

    ResponseEntity<StreamingResponseBody> downloadModulesByToken(String token);

    ApiResult<?> getModuleList();
}
