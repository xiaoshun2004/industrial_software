package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.config.LicenseStoreProperties;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;
import com.scut.industrial_software.utils.MachineInfoUtils;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * MultipleTreatment策略类，用于处理多体动力学前后处理模块相关的License管理逻辑
 */
@Service
public class MultipleTreatmentLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "MultipleTreatment";
    }

    @Autowired
    private LicenseStoreProperties licenseStoreProperties;

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的多体动力学前后处理模块License生成逻辑

        // Mac地址
        String macAddress = MachineInfoUtils.getMachineAddr();
        // 许可时间
        String expirationTime = "1y";
        // 存储地址
        String storePath = licenseStoreProperties.getMultipleTreatmentStorePath();
        // 文件名
        String fileName = "MultipleTreatment" + "_" + "license" + "_" + System.currentTimeMillis() + ".lic";
        // 完整存储路径
        String fullPath  = storePath + File.separator + fileName;

        ProcessBuilder pb = new ProcessBuilder(
                "src/main/resources/bin/MultipleTreatment.exe",
                "--store",
                fullPath,
                "--mac",
                macAddress,
                "--expire",
                expirationTime
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // 原始密钥
            String rawSecretKey = "your-raw-secret-key";
            // 返回生成的License结果
            return new LicenseResult(fullPath, rawSecretKey);
        } else {
            throw new Exception("Failed to generate license.");
        }
    }
}
