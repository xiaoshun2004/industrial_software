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
 * 子程序文件实体类
 * </p>
 *
 * @since 2025-04-23
 */
@Getter
@Setter
@Builder
@TableName("subprogram_file")  // 表名：subprogram_file
public class SubprogramFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)  // 主键：id
    private Long id;

    private String fileName;  // 文件名（如 Mises_JC_RateDependentOnly.for）
    private String filePath;  // 文件存储路径（绝对路径）
    private String uploadTime; // 文件上传时间
}

