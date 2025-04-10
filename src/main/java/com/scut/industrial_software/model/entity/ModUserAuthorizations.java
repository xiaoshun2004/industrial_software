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
@TableName("mod_user_authorizations")
public class ModUserAuthorizations implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "authorization_id", type = IdType.AUTO)
    private Integer authorizationId;

    private Integer userId;

    private String role;

    private String permittedTables;

    private Boolean createPermission;

    private Boolean readPermission;

    private Boolean updatePermission;

    private Boolean deletePermission;


}
