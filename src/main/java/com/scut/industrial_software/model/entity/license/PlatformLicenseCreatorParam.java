package com.scut.industrial_software.model.entity.license;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import oshi.SystemInfo;

import java.util.Date;
import java.util.Map;

/**
 * @author Ming
 * 平台License证书生成参数实体类
 */
@Data
public class PlatformLicenseCreatorParam {

    /**
     * 证书主题
     */
    private String subject;

    /**
     * 证书生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date issuedTime;

    /**
     * 证书失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expiryTime;

    /**
     * 服务器系统信息
     */
    private SystemInfo systemInfo;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 描述信息
     */
    private String description = "生成的License证书";

    /**
     * 额外的扩展属性
     */
    private Map<String, Object> properties;
}
