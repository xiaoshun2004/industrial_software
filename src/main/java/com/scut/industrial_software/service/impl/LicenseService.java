package com.scut.industrial_software.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LicenseService {

    @Autowired
    private LicenseFactory licenseFactory;

    // 向客户端返回生成的License文件路径和密钥

}
