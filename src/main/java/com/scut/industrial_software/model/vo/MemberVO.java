package com.scut.industrial_software.model.vo;

import lombok.Data;

/**
 * 成员信息VO
 */
@Data
public class MemberVO {
    
    /**
     * 成员ID
     */
    private String userId;
    
    /**
     * 成员姓名
     */
    private String userName;

    /**
     * 是否为组管理员：0-否，1-是
     */
    private Integer isGroupAdmin;
}
