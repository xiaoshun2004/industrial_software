package com.scut.industrial_software.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Ming
 * License 生成参数实体类，目前未建表
 */
@Data
public class LicenseCreatorParam {
    /**
     * 证书subject
     */
    private String subject;

    /**
     * 密钥别称
     */
    private String privateAlias;

    /**
     * 私钥库的密码
     */
    private String storePass;

    /**
     * 证书生成路径
     */
    private String licensePath;

    /**
     * 私钥存储路径
     */
    private String privateKeysStorePath;

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
     * 用户类型
     */
    private String consumerType;

    /**
     * 用户数量
     */
    private Integer consumerAmount;

    /**
     * 描述信息
     */
    private String description;
}
