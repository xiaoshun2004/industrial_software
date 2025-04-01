package com.scut.industrial_software.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhou
 * @since 2025-03-29
 */
@Getter
@Setter
@TableName("load_spectrum_attributes")
public class LoadSpectrumAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "load_spectrum_id", type = IdType.AUTO)
    private Integer loadSpectrumId;

    private String loadSpectrumName;

    private String loadSpectrumFilePath;

    private String loadSpectrumDescription;

    private String loadSpectrumType;


}
