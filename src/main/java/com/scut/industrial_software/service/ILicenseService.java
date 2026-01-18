package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;

public interface ILicenseService {

    /**
     * 生成许可证
     * @param toolType 工具类型
     * @return 许可证生成结果
     * @throws Exception 异常
     */
    ApiResult<?> createLicense(String toolType) throws Exception;

}
