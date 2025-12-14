package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.model.constant.LicenseConstants;
import com.scut.industrial_software.model.entity.LicenseCreatorParam;
import com.scut.industrial_software.service.ILicenseCreateService;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.prefs.Preferences;

@Slf4j
@Service
public class LicenseCreateServiceImpl implements ILicenseCreateService {
    private final static X500Principal DEFAULT_HOLDER_AND_ISSUER = new X500Principal("CN=localhost, OU=localhost, O=localhost, L=SH, ST=SH, C=CN");

    /**
     * 生成License证书主体实现
     */
    @Override
    public void create() throws Exception {
        log.info("License creation logic goes here.");
        // Implementation of license creation would be added here.
        LicenseCreatorParam param = new LicenseCreatorParam();
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
        // 用户类型
        param.setConsumerType("user");
        // 用户数量
        param.setConsumerAmount(1);
        // 证书描述
        param.setDescription("证书描述信息");
        // 生成证书
        LicenseCreateServiceImpl licenseCreator = new LicenseCreateServiceImpl();
        licenseCreator.generateLicense(param);
    }

    /**
     * 生成License证书
     * @param param 证书生成参数实体
     */
    @Override
    public void generateLicense(LicenseCreatorParam param) throws Exception {
        try {
            LicenseManager licenseManager = new LicenseManager(initLicenseParam(param));
            LicenseContent licenseContent = initLicenseContent(param);
            licenseManager.store(licenseContent, new File(param.getLicensePath()));
        } catch (Exception e) {
            log.error("证书生成失败", e);
        }
    }

    // 初始化一个LicenseParam实例
    private static LicenseParam initLicenseParam(LicenseCreatorParam param){
        Preferences preferences = Preferences.userNodeForPackage(LicenseCreateServiceImpl.class);
        // 设置对证书内容加密的私钥
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());
        // 这里使用的是PKCS12格式的密钥库，私钥口令和密钥库口令相同
        KeyStoreParam privateStoreParam = new DefaultKeyStoreParam(LicenseCreateServiceImpl.class
                , param.getPrivateKeysStorePath()        // 私钥库存储路径
                , param.getPrivateAlias()                // 私钥别称
                , param.getStorePass()                   // 私钥库密码
                , param.getStorePass());                 // 私钥密码

        // 组织License参数
        return new DefaultLicenseParam(param.getSubject()
                , preferences
                , privateStoreParam
                , cipherParam);
    }

    // 设置证书生成正文信息
    private static LicenseContent initLicenseContent(LicenseCreatorParam param) {
        LicenseContent licenseContent = new LicenseContent();
        licenseContent.setHolder(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setIssuer(DEFAULT_HOLDER_AND_ISSUER);
        licenseContent.setSubject(param.getSubject());
        licenseContent.setIssued(param.getIssuedTime());
        licenseContent.setNotBefore(param.getIssuedTime());
        licenseContent.setNotAfter(param.getExpiryTime());
        licenseContent.setConsumerType(param.getConsumerType());
        licenseContent.setConsumerAmount(param.getConsumerAmount());
        licenseContent.setInfo(param.getDescription());
        return licenseContent;
    }
}
