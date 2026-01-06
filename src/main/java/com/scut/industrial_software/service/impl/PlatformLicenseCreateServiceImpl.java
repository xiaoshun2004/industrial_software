package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.common.exception.CertificateGenerationException;
import com.scut.industrial_software.config.LicenseProperties;
import com.scut.industrial_software.model.entity.license.CustomKeyStoreParam;
import com.scut.industrial_software.model.entity.license.PlatformLicenseCreatorParam;
import com.scut.industrial_software.service.ILicenseCreateService;
import de.schlichtherle.license.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * license使用流程：
 * 1、密钥生成对：公钥和私钥对的生成，使用Keytool命令行工具生成（开发环境下使用自签名证书，生产环境下使用CA颁发的证书）
 * 2、证书生成：使用私钥对生成license证书
 * 3、证书应用：将公钥集成到应用中
 * 4、分发：将license证书分发给最终用户
 * 5、验证：应用使用公钥验证许可证
 * @author Ming
 */
@Slf4j
@Service
public class PlatformLicenseCreateServiceImpl implements ILicenseCreateService {

    @Resource
    private LicenseProperties licenseProperties;

    private final static X500Principal DEFAULT_HOLDER_AND_ISSUER = new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");

    @Override
    public void create() throws Exception {
        /*
        log.info("License creation logic goes here.");
        // Implementation of license creation would be added here.
        PlatformLicenseCreatorParam param = new PlatformLicenseCreatorParam();
        // 证书主题
        param.setSubject(LicenseConstants.LICENSE_SUBJECT);
        // 密钥别称
        param.setPrivateAlias(LicenseConstants.PRIVATE_ALIAS);
        // 私钥库的密码
        param.setStorePass(LicenseConstants.KEY_STORE_PASSWORD);
        // 证书生成路径
        param.setLicensePath(LicenseConstants.LICENSE_PATH);
        // 私钥存储路径
        param.setPrivateKeysStorePath(LicenseConstants.PRIVATE_KEYS_STORE_PATH);
        // 证书生成时间-当前时间（统一使用UTC时间）
        Instant now = Instant.now();
        Date nowDate = Date.from(now);
        param.setIssuedTime(nowDate);
        // 证书10天后过期
        Instant expirationInstant = now.plus(10, ChronoUnit.DAYS);
        Date expirationDate = Date.from(expirationInstant);
        // 设置证书过期时间
        param.setExpiryTime(expirationDate);
        // 证书描述
        param.setDescription("证书描述信息");
        // 生成证书
        PlatformLicenseCreateServiceImpl licenseCreator = new PlatformLicenseCreateServiceImpl();
        licenseCreator.generateLicense(param);*/
    }

    /**
     * 生成License证书
     * @param param 证书生成参数实体
     */
    @Override
    public boolean generateLicense(PlatformLicenseCreatorParam param, File licenseFile) {
        try {
            LicenseManager licenseManager = new LicenseManager(initLicenseParam(param));
            LicenseContent licenseContent = initLicenseContent(param);
            licenseManager.store(licenseContent, licenseFile);
            return true;
        } catch (Exception e) {
            log.error("证书生成失败", e);
            throw new CertificateGenerationException("生成license证书失败");
        }
    }

    // 初始化一个LicenseParam实例
    private LicenseParam initLicenseParam(PlatformLicenseCreatorParam param){
        Preferences preferences = Preferences.userNodeForPackage(PlatformLicenseCreateServiceImpl.class);
        // 设置对证书内容加密的私钥
        CipherParam cipherParam = new DefaultCipherParam(licenseProperties.getStorePass());
        // 这里使用的是PKCS12格式的密钥库，私钥口令和密钥库口令相同，这里使用自定义的私钥库、私钥别名、私钥库密码、私钥密码
        KeyStoreParam privateStoreParam = new CustomKeyStoreParam(PlatformLicenseCreateServiceImpl.class
                , licenseProperties.getPrivateKeysStorePath()        // 私钥库存储路径
                , licenseProperties.getPrivateAlias()                // 私钥别称
                , licenseProperties.getStorePass()                   // 私钥库密码
                , licenseProperties.getKeyPass());                   // 私钥密码

        // 组织License参数
        return new DefaultLicenseParam(param.getSubject()
                , preferences
                , privateStoreParam
                , cipherParam);
    }

    // 设置证书生成正文信息
    private static LicenseContent initLicenseContent(PlatformLicenseCreatorParam param) {
        LicenseContent licenseContent = new LicenseContent();
        licenseContent.setHolder(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setIssuer(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setSubject(param.getSubject());         // 这里插个眼，为什么能取到subject？应该要从param里取
        licenseContent.setIssued(param.getIssuedTime());
        licenseContent.setNotBefore(param.getIssuedTime());
        licenseContent.setNotAfter(param.getExpiryTime());     // TODO：这里设一层保障，如果过期时间没有呢
        licenseContent.setConsumerType(param.getUserType());
        licenseContent.setConsumerAmount(param.getUserCount());
        licenseContent.setInfo(param.getDescription());
        // 这里插入额外校验信息，例如服务器硬件信息等
        Map<String, Object> extra = param.getProperties();
        extra.put("systemInfo", param.getSystemInfo());         // TODO: 插入系统信息用于后续校验，硬编码与系统信息太多是否是个问题
        licenseContent.setExtra(extra);
        return licenseContent;
    }
}
