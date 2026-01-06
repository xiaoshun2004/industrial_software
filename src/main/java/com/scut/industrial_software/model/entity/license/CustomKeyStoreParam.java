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
        return new FileInputStream(storePath);
    }
}
