package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.config.LicenseStoreProperties;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;
import com.scut.industrial_software.utils.MachineInfoUtils;
import io.lettuce.core.dynamic.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ImpactSolver策略类，用于处理冲击动力学求解器相关的License管理逻辑
 */
@Service
public class ImpactSolverLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "ImpactSolver";
    }

    @Autowired
    private LicenseStoreProperties licenseStoreProperties;

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的冲击动力学求解器License生成逻辑

        // Mac地址
        String macAddress = MachineInfoUtils.getMachineAddr();
        // 单位名称
        String unitName = "SCUT";
        // 授权时长
        String licenseDuration = "1y";
        // 联系人
        String contact = "Contact";
        // 模块标识
        String moduleIdentifier = "ImpactSolver";
        //存储路径
        String storePath = licenseStoreProperties.getImpactSolverStorePath();
        String fileName = "ImpactSolver" + "_" + "license" + "_" + System.currentTimeMillis() + ".lic";
        String fullPath  = storePath + File.separator + fileName;

        ProcessBuilder pb = new ProcessBuilder(
                "src/main/resources/bin/ImpactSolver.exe",
                "--store",
                fullPath,
                "--mac",
                macAddress,
                "--unit",
                unitName,
                "--duration",
                licenseDuration,
                "--contact",
                contact,
                "--module",
                moduleIdentifier
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // 原始密钥
            String rawSecretKey = "your-raw-secret-key";
            // 返回结果
            return new LicenseResult(fullPath, rawSecretKey);
        } else {
            throw new Exception("Failed to generate license.");
        }
    }
}
