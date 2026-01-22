package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scut.industrial_software.model.entity.license.LicenseResultInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 持久化操作接口：负责对许可证生成结果（license_result_info 表）进行 CRUD。
 * 使用 MyBatis-Plus 的 BaseMapper 提供基础增删改查能力，可按需新增自定义查询。
 */
public interface LicenseResultMapper extends BaseMapper<LicenseResultInfo> {
    /**
     * 根据用户ID和工具类型查询有效的许可证生成结果。
     * @param userId  用户ID
     * @param toolType  工具类型
     * @return  对应的许可证生成结果实体，如果不存在则返回 null（一般只有1个）
     */
    LicenseResultInfo selectActiveByUserIdAndToolType(@Param("userId") Integer userId,
                                                     @Param("toolType") String toolType);

    /**
     * 将指定用户与工具类型的许可证状态置为无效（status = 0）。
     * 返回受影响行数。
     */
    int deactivateByUserIdAndToolType(@Param("userId") Integer userId,
                                      @Param("toolType") String toolType);
}
