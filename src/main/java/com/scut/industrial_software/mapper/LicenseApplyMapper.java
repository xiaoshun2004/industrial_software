package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.license.LicenseApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

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
     * 将指定时间窗口内已过期但仍为VALID的申请单修正为OVERDUE。
     * @param startValidTo 起始 valid_to，可为空
     * @param endValidTo 截止 valid_to
     * @return 影响行数
     */
    int markExpiredValidAsOverdue(@Param("startValidTo") LocalDateTime startValidTo,
                                  @Param("endValidTo") LocalDateTime endValidTo);

    /**
     * 查询当前时间之后最早的一条 VALID 记录的过期时间。
     * @param current 当前时间
     * @return 下一条待过期记录的 valid_to
     */
    LocalDateTime selectNextValidToAfter(@Param("current") LocalDateTime current);

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

    /**
     * 根据用户ID分页查询证书申请记录。
     * @param page 分页参数
     * @param userId 用户ID
     * @return 该用户的申请记录列表
     */
    Page<LicenseApply> selectByUserId(Page<LicenseApply> page, @Param("userId") Integer userId);
}
