package com.scut.industrial_software.model.vo;

import lombok.Data;

/**
 * 监控返回信息元数据视图对象
 */
@Data
public class MonitorVO {

    /**
     * 当前任务的字符串ID
     */
    private String taskId;

    /**
     * 进程状态
     */
    private String status;

    public MonitorVO(String taskId, String status){
        this.taskId = taskId;
        this.status = status;
    }

}
