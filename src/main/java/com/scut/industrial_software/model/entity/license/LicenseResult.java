package com.scut.industrial_software.model.entity.license;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LicenseResult {
    private String licPath;                 // .lic文件的存储路径
    private String rawSecretKey;            // 生成的原始密钥（传送给客户端）
}
