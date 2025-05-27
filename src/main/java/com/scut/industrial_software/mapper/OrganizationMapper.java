package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.Organization;
import com.scut.industrial_software.model.vo.OrganizationVO;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 组织表 Mapper 接口
 * </p>
 *
 * @author zhou
 * @since 2025-01-27
 */
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 分页查询组织列表（包含创建人姓名）
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 组织列表
     */
    Page<OrganizationVO> selectOrganizationPage(Page<OrganizationVO> page, @Param("keyword") String keyword);
} 