package com.scut.industrial_software.common.exception;

import com.scut.industrial_software.common.api.IErrorCode;

/**
 * 专用于证书生成失败的业务异常
 */
public class CertificateGenerationException extends ApiException {

    public CertificateGenerationException(IErrorCode errorCode) {
        super(errorCode);
    }

    public CertificateGenerationException(String message) {
        super(message);
    }
}

