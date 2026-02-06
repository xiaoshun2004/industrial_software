package com.scut.industrial_software.service;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.LicenseApplyDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 查询证书申请请求列表，支持根据模块名称模糊匹配及状态过滤并分页。
     * @param moduleKeyword 模块名称关键词
     * @param status 申请状态
     * @param page 当前页，从1开始
     * @param size 每页大小
     * @return 查询结果
     */
    ApiResult<?> getApplyRequests(String moduleKeyword, String status, long page, long size);

    /**
     * 根据申请编号审批通过证书申请。
     * @param requestId 申请编号
     * @return 审批结果
     */
    ApiResult<?> approveApplyRequest(String requestId);

    /**
     * 根据申请编号拒绝证书申请。
     * @param requestId 申请编号
     * @return 拒绝结果
     */
    ApiResult<?> rejectApplyRequest(String requestId);

    /**
     * 上传证书文件并更新编号与存储路径。
     * @param requestId 申请编号
     * @param file 上传的证书文件
     * @return 处理结果
     */
    ApiResult<?> uploadLicenseFile(String requestId, MultipartFile file);

    /**
     * 根据申请编号下载证书文件。
     * @param requestId 申请编号
     * @return 文件响应
     */
    ResponseEntity<byte[]> downloadLicenseFile(String requestId);

}
