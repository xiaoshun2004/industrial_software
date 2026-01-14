package com.scut.industrial_software.service.impl.LicenseStrategy;

import com.scut.industrial_software.config.LicenseStoreProperties;
import com.scut.industrial_software.model.entity.license.LicenseResult;
import com.scut.industrial_software.service.ILicenseStrategy;
import com.scut.industrial_software.utils.MachineInfoUtils;
import io.lettuce.core.dynamic.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ImpactTreatment策略类，用于处理冲击动力学预处理模块相关的License管理逻辑
 */
@Service
public class ImpactTreatmentLicenseStrategy implements ILicenseStrategy {
    @Override
    public String getLicenseCreateType() {
        return "ImpactTreatment";
    }

    @Autowired
    private LicenseStoreProperties licenseStoreProperties;

    @Override
    public LicenseResult generateLicense() throws Exception {
        // 这里实现具体的冲击动力学预处理模块License生成逻辑
        // Mac地址
        String macAddress = MachineInfoUtils.getMachineAddr();
        // 主机名称
        String hostName = MachineInfoUtils.getHostName();
        // 许可时间(默认一年)(字符串)
        String expirationTime = "1y";

        // 生成文件名以及完整存储路径
        String storePath = licenseStoreProperties.getImpactTreatmentStorePath();
        String fileName = "ImpactTreatment" + "_" + "license" + "_" + System.currentTimeMillis() + ".lic";
        String fullPath  = storePath + File.separator + fileName;

        ProcessBuilder pb = new ProcessBuilder(
                "src/main/resources/bin/ImpactTreatment.exe",
                "--store",
                fullPath,
                "--mac",
                macAddress,
                "--host",
                hostName,
                "--expire",
                expirationTime
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            // 原始密钥
            String rawSecretKey = "your-raw-secret-key";
            return new LicenseResult(fullPath, rawSecretKey);
        } else {
            throw new Exception("Failed to generate license.");
        }
    }
}
