package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface IComponentService {

    public ResponseEntity<Resource> downloadModule(Integer componentId,@RequestHeader(value = "Range", required = false) String rangeHeader);

    public ResponseEntity<StreamingResponseBody> downloadModules(List<Integer> componentIds);

    public ApiResult<?> getModuleList();
}
