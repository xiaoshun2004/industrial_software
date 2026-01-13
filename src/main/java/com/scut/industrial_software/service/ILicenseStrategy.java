package com.scut.industrial_software.service;

import com.scut.industrial_software.model.entity.license.LicenseResult;

/**
 * License管理工厂接口，用于将不同的License管理实现进行抽象和统一
 */
public interface ILicenseStrategy {
    /**
     * 获取当前策略，生成License时所需的工具
     */
    String getLicenseCreateType();

    /**
     * 执行证书生成逻辑
     */
    LicenseResult generateLicense() throws Exception;
}
