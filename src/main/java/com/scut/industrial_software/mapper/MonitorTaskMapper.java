package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scut.industrial_software.model.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MonitorTaskMapper extends BaseMapper<Task> {

    IPage<Task> selectTaskPage(Page<Task> page,
                               @Param("keyword") String keyword,
                               @Param("status") String status,
                               @Param("serverId") Integer serverId);
}
