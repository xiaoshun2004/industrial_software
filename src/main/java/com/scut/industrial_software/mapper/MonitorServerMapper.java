package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MonitorServerMapper extends BaseMapper<Server> {

    /**
     * 分页查询服务器列表
     *
     * @param page 分页参数
     * @param keyword 搜索关键字
     * @param status 状态
     * @param type 类型
     * @return 分页结果
     */
    IPage<Server> selectServerPage(Page<Server> page,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("type") String type);

}
