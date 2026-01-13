package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;

/**
 * ImpactTreatment策略类，用于处理冲击动力学预处理模块相关的License管理逻辑
 */
public class ImpactTreatmentLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "ImpactTreatment";
    }

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的冲击动力学预处理模块License生成逻辑
        // 返回生成的License结果
        return null;
    }
}
