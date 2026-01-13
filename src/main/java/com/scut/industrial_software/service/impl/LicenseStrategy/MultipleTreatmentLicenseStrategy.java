package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;

/**
 * MultipleTreatment策略类，用于处理多体动力学前后处理模块相关的License管理逻辑
 */
public class MultipleTreatmentLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "MultipleTreatment";
    }

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的多体动力学前后处理模块License生成逻辑
        // 返回生成的License结果
        return null;
    }
}
