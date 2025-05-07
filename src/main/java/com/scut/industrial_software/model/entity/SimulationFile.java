package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 仿真文件实体类
 * </p>
 *
 * @since 2025-03-29
 */
@Getter
@Setter
@Builder
@TableName("simulation_file")  // 表名：simulation_file
public class SimulationFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)  // 主键：id
    private Long id;

    private String inpName;   // 输入文件名（如 PP_100rate.inp）
    private String filePath;  // 文件存储路径（绝对路径）
    private String uploadTime; // 文件上传时间
}
