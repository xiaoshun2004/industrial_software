package com.scut.industrial_software.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控返回信息元数据视图对象
 */
@Data
public class MonitorVO {

    /**
     * 进程ID
     */
    // private Long pid;

    /**
     * 程序CPU使用率
     */
    // private double cpuUsage;             // 单位: %

    /**
     * 程序内存使用率
     */
    // private double memUsage;             // 单位: MB

    /**
     * 当前时间戳
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime timestamp;

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

    /*
    @Override
    public String toString(){
        return String.format("PID: %d, CPU使用率: %.2f%%, 内存使用率: %.2fMB, 时间: %s, Status: %s",
                pid, cpuUsage, memUsage, timestamp.toString(), status);
    }
    */

}
