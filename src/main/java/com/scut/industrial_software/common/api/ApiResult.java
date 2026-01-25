package com.scut.industrial_software.common.api;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;

@Data
@NoArgsConstructor
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = -4153430394359594346L;
    private static final Logger logger = LoggerFactory.getLogger(ApiResult.class);

    /**
     * 业务状态码
     */
    private long code;

    /**
     * 结果集
     */
    private T data;

    /**
     * 接口描述
     */
    private String message;

    /**
     * 通用响应结构全参
     *
     * @param code    业务状态码
     * @param message 描述
     * @param data    结果集
     */
    public ApiResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiResult(IErrorCode errorCode) {
        errorCode = Optional.ofNullable(errorCode).orElse(ApiErrorCode.FAILED);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 成功
     *
     * @return {code:200,message:操作成功,data:自定义}
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<T>(ApiErrorCode.SUCCESS.getCode(), ApiErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功
     *
     * @param data 结果集
     * @return {code:200,message:操作成功,data:自定义}
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<T>(ApiErrorCode.SUCCESS.getCode(), ApiErrorCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功
     *
     * @param data    结果集
     * @param message 自定义提示信息
     * @return {code:200,message:自定义,data:自定义}
     */
    public static <T> ApiResult<T> success(T data, String message) {
        return new ApiResult<T>(ApiErrorCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> ApiResult<T> failed() {
        return failed(ApiErrorCode.FAILED);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     * @return {code:枚举ApiErrorCode取,message:自定义,data:null}
     */
    public static <T> ApiResult<T> failed(String message) {
        logger.error("API 请求失败: {}", message);
        return new ApiResult<T>(ApiErrorCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return {code:封装接口取,message:封装接口取,data:null}
     */
    public static <T> ApiResult<T> failed(IErrorCode errorCode) {
        logger.error("API 请求失败: 错误码 {}, 错误信息 {}", errorCode.getCode(), errorCode.getMessage());
        return new ApiResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @return {code:枚举ApiErrorCode取,message:自定义,data:null}
     */
    public static <T> ApiResult<T> failed(IErrorCode errorCode, String message) {
        logger.error("API 请求失败: 错误码 {}, 错误信息 {}", errorCode.getCode(), message);
        return new ApiResult<T>(errorCode.getCode(), message, null);
    }

    /**
     * 资源不存在返回结果
     */
    public static <T> ApiResult<T> resourceNotFound() {
        return failed(ApiErrorCode.RESOURCE_NOT_FOUND);
    }

    /**
     * 资源不存在返回结果
     *
     * @param message 提示信息
     */
    public static <T> ApiResult<T> resourceNotFound(String message) {
        logger.error("资源不存在: {}", message);
        return new ApiResult<T>(ApiErrorCode.RESOURCE_NOT_FOUND.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> ApiResult<T> unauthorized(T data) {
        logger.error("未登录: {}", data);
        return new ApiResult<T>(ApiErrorCode.UNAUTHORIZED.getCode(), ApiErrorCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> ApiResult<T> forbidden(T data) {
        logger.error("未授权: {}", data);
        return new ApiResult<T>(ApiErrorCode.FORBIDDEN.getCode(), ApiErrorCode.FORBIDDEN.getMessage(), data);
    }

    /**
     * 内部服务器错误返回结果
     */
    public static <T> ApiResult<T> internalServerError() {
        return failed(ApiErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 内部服务器错误返回结果
     *
     * @param message 提示信息
     */
    public static <T> ApiResult<T> internalServerError(String message) {
        logger.error("服务器内部错误: {}", message);
        return new ApiResult<T>(ApiErrorCode.INTERNAL_SERVER_ERROR.getCode(), message, null);
    }

    /**
     * 文件上传失败返回结果
     */
    public static <T> ApiResult<T> fileUploadFailed() {
        return failed(ApiErrorCode.FILE_UPLOAD_FAILED);
    }

    /**
     * 文件下载失败返回结果
     */
    public static <T> ApiResult<T> fileDownloadFailed() {
        return failed(ApiErrorCode.FILE_DOWNLOAD_FAILED);
    }

    /**
     * 数据库类型无效返回结果
     */
    public static <T> ApiResult<T> invalidDatabaseType() {
        return failed(ApiErrorCode.INVALID_DATABASE_TYPE);
    }

    /**
     * 文件存储空间不足返回结果
     */
    public static <T> ApiResult<T> storageSpaceInsufficient() {
        return failed(ApiErrorCode.STORAGE_SPACE_INSUFFICIENT);
    }

    /**
     * 不支持的文件类型返回结果
     */
    public static <T> ApiResult<T> unsupportedFileType() {
        return failed(ApiErrorCode.UNSUPPORTED_FILE_TYPE);
    }

}