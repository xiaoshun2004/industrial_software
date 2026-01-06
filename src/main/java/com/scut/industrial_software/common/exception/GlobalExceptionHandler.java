package com.scut.industrial_software.common.exception;//package com.knox.aurora.common.exception;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.api.ApiErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 捕获自定义异常
     */
    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ApiResult<Map<String, Object>> handle(ApiException e) {
        if (e.getErrorCode() != null) {
            return ApiResult.failed(e.getErrorCode());
        }
        return ApiResult.failed(e.getMessage());
    }

    /**
     * 捕获证书生成失败异常
     */
    @ResponseBody
    @ExceptionHandler(CertificateGenerationException.class)
    public ApiResult<Map<String, Object>> handleCertificateException(CertificateGenerationException e) {
        if (e.getErrorCode() != null) {
            return ApiResult.failed(e.getErrorCode());
        }
        // 默认使用证书生成失败的错误码
        return ApiResult.failed(ApiErrorCode.CERTIFICATE_GENERATION_FAILED, e.getMessage());
    }
}
