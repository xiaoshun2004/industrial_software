package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.config.LicenseStoreProperties;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 结构模块策略类，用于处理结构模块相关的License管理逻辑
 */
@Service
public class StructureLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "Structure";
    }

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的结构模块License生成逻辑
        // 返回生成的License结果
        return null;
    }
}
