package com.scut.industrial_software.model.vo;

import lombok.Data;

/**
 * 证书生成结果的返回视图对象
 */
@Data
public class LicenseResultVO {

    /**
     * 证书生成的存储编号，用于找到原始.lic文件
     */
    private Integer licenseId;

    /**
     * 证书文件的原始二进制数据（传送给客户端）
     */
    private byte[] licenseData;

}
