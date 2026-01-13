package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;

/**
 * ImpactSolver策略类，用于处理冲击动力学求解器相关的License管理逻辑
 */
public class ImpactSolverLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "ImpactSolver";
    }

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的冲击动力学求解器License生成逻辑
        // 返回生成的License结果
        return null;
    }
}
