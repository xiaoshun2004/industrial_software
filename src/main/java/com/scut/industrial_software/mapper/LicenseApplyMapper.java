package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.license.LicenseApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis-Plus mapper for license_apply table.
 */
@Mapper
public interface LicenseApplyMapper extends BaseMapper<LicenseApply> {

    /**
     * 按模块名称关键字（模糊）与状态过滤证书申请记录并分页。
     * @param page 分页参数
     * @param moduleKeyword 模块名称关键词
     * @param status 申请状态
     * @return 申请记录列表
     */
    Page<LicenseApply> selectByModuleAndStatus(Page<LicenseApply> page,
                                               @Param("moduleKeyword") String moduleKeyword,
                                               @Param("status") String status);

    /**
     * 更新证书编号及文件存储路径。
     * @param requestId 申请编号
     * @param licenseNo 证书编号
     * @param licensePath 文件存储路径
     * @return 影响行数
     */
    int updateLicenseFileInfo(@Param("requestId") String requestId,
                              @Param("licenseNo") String licenseNo,
                              @Param("licensePath") String licensePath);
}
