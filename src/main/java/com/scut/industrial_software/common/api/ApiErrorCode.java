package com.scut.industrial_software.common.api;


public enum ApiErrorCode implements IErrorCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    /**
     * 失败
     */
    FAILED(-1, "操作失败"),
    /**
     * 参数验证失败
     */
    VALIDATE_FAILED(400, "参数验证失败"),
    /**
     * 未登录，Token过期
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足"),
    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND(404, "资源不存在"),
    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(1001, "文件上传失败"),
    /**
     * 文件下载失败
     */
    FILE_DOWNLOAD_FAILED(1002, "文件下载失败"),
    /**
     * 数据库类型无效
     */
    INVALID_DATABASE_TYPE(1003, "数据库类型无效"),
    /**
     * 文件存储空间不足
     */
    STORAGE_SPACE_INSUFFICIENT(1005, "文件存储空间不足"),
    /**
     * 不支持的文件类型
     */
    UNSUPPORTED_FILE_TYPE(1006, "不支持的文件类型"),
    /**
     * 证书生成失败
     */
    CERTIFICATE_GENERATION_FAILED(1007, "证书生成失败"),
    /**
     * 任务创建失败
     */
    TASK_CREATION_FAILED(2001, "任务创建失败"),
    /**
     * 任务删除失败
     */
    TASK_DELETION_FAILED(2002, "任务删除失败"),
    /**
     * 任务开始失败
     */
    TASK_START_FAILED(2003, "任务开始失败"),
    /**
     * 任务结束失败
     */
    TASK_STOP_FAILED(2004, "任务结束失败"),
    /**
     * 项目不存在
     */
    PROJECT_NOT_FOUND(2005, "项目不存在"),
    /**
     * 任务状态不允许此操作
     */
    INVALID_TASK_STATUS(2006, "任务状态不允许此操作");

    private final Integer code;
    private final String message;

    ApiErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ApiErrorCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
