package com.scut.industrial_software.service;

import com.scut.industrial_software.model.entity.license.PlatformLicenseCreatorParam;

import java.io.File;

public interface IPlatformLicenseService {
    /**
     * 生成License证书全流程主体实现
     */
    void create() throws Exception;
    /**
     * 生成License证书并保存
     * @param param 证书生成参数实体
     */
    boolean generateLicense(PlatformLicenseCreatorParam param, File licenseFile);

}
