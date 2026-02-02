package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.LicenseApplyDTO;

public interface ILicenseService {

    /**
     * 生成许可证
     * @param toolType 工具类型
     * @return 许可证生成结果
     * @throws Exception 异常
     */
    ApiResult<?> createLicense(String toolType) throws Exception;

    /**
     * 审批/登记证书申请请求。
     * @param licenseApplyDTO 申请数据载体
     * @return 保存结果
     */
    ApiResult<?> approveLicense(LicenseApplyDTO licenseApplyDTO);

}
