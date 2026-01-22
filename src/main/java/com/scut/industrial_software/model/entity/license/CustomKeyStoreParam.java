package com.scut.industrial_software.model.entity.license;

import de.schlichtherle.license.AbstractKeyStoreParam;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * 自定义密钥库参数类，用于指定公钥/私钥存储路径
 */
@Getter
@Setter
public class CustomKeyStoreParam extends AbstractKeyStoreParam {
    /**
     * 公钥/私钥在磁盘上的存储路径
     */
    private String storePath;
    private String alias;
    private String storePwd;
    private String keyPwd;

    public CustomKeyStoreParam(Class clazz, String resource,String alias,String storePwd,String keyPwd) {
        super(clazz, resource);
        this.storePath = resource;
        this.alias = alias;
        this.storePwd = storePwd;
        this.keyPwd = keyPwd;
    }

    /**
     * 用于将公私钥存储文件存放到其他磁盘位置而不是项目中
     * @return java.io.InputStream
     */
    @Override
    public InputStream getStream() throws IOException {
        try {
            if (storePath == null || storePath.trim().isEmpty()) {
                throw new FileNotFoundException("Keystore path is empty");
            }
            // 支持classpath:前缀，否则回落为文件系统路径
            if (storePath.startsWith("classpath:")) {
                String cp = storePath.substring("classpath:".length());
                if (!cp.startsWith("/")) {
                    cp = "/" + cp;
                }
                InputStream is = getClass().getResourceAsStream(cp);
                if (is == null) {
                    throw new FileNotFoundException("Keystore not found in classpath: " + storePath);
                }
                return is;
            }
            File file = new File(storePath);
            if (!file.exists()) {
                throw new FileNotFoundException("Keystore file not found: " + storePath);
            }
            return new FileInputStream(file);
        } catch (IOException ex) {
            throw ex; // 保留原始IO异常
        } catch (Exception ex) {
            throw new IOException("Failed to load keystore from path: " + storePath, ex);
        }
    }
}
