package com.scut.industrial_software.model.dto;

import cn.hutool.core.date.DateTime;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 进程信息封装类
 */
@Data
public class ProcessInfoDTO {

    /**
     * 本地exe文件进程对象
     */
    private Process process;

    /**
     * 进程ID
     */
    private Long pid;

    /**
     * 进程退出码
     */
    private Integer exitCode;

    /**
     * 进程状态 (running, interrupted, completed, error)
     */
    private volatile String status = "running";      // volatile 保证线程间可见性

    /**
     * 进程启动时间
     */
    private LocalDateTime startTime;

    /**
     * 进程结束时间
     */
    private LocalDateTime endTime;

    public ProcessInfoDTO(Process process, Long pid, LocalDateTime startTime) {
        this.process = process;
        this.pid = pid;
        this.startTime = startTime;
    }

    // 原子性地更新状态
    public boolean updateStatus(String expectedStatus, String newStatus){
        synchronized (this){
            if (this.status.equals(expectedStatus)){
                this.status = newStatus;
                return true;
            }
            return false;
        }
    }

}
