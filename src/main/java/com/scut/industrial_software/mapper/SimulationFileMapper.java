package com.scut.industrial_software.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scut.industrial_software.model.entity.SimulationFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 仿真文件 Mapper 接口
 * </p>
 *
 * @since 2025-03-29
 */
@Mapper
public interface SimulationFileMapper extends BaseMapper<SimulationFile> {
    // 你可以在这里定义自定义查询方法
}
