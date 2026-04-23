package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface IComponentService {

    ResponseEntity<Resource> downloadModule(Integer componentId, String rangeHeader);

    ApiResult<?> createSingleDownloadToken(Integer componentId);

    ResponseEntity<Resource> downloadModuleByToken(Integer componentId, String token, String rangeHeader);

    ResponseEntity<StreamingResponseBody> downloadModules(List<Integer> componentIds);

    ApiResult<?> getModuleList();
}
