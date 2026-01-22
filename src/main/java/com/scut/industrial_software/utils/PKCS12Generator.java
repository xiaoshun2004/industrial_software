package com.scut.industrial_software.utils;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class PKCS12Generator {
    public static void main(String[] args) throws Exception {
        String storePath = "src/main/resources/keystore.p12";
        char[] password = "Ming".toCharArray();
        String alias = "TestKey";

        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();     // 生成一对公私钥

        X509Certificate cert = selfSign("CN=demo", kp, 365);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);     // 初始化一个空的密钥库
        // 将私钥及其证书链以指定alias和password存入密钥库
        ks.setKeyEntry(alias, kp.getPrivate(), password, new Certificate[]{cert});
        try (FileOutputStream fos = new FileOutputStream(storePath)) {
            ks.store(fos, password);         // 打开输出流写入指定路径storePath，将密钥库内容持久化进文件
        }
    }

    // 简单自签名证书
    private static X509Certificate selfSign(String dn, KeyPair kp, int days) throws Exception {
        long now = System.currentTimeMillis();
        Date from = new Date(now);
        Date to = new Date(now + days * 86400000L);
        X500Name subject = new X500Name(dn);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(kp.getPrivate());

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(now),
                from,
                to,
                subject,
                kp.getPublic());

        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);
    }
}
