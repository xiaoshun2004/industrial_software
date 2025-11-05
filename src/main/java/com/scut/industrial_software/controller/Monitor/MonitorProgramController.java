package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IMonitorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/taskMonitor")
public class MonitorProgramController {

    @Resource
    private IMonitorService monitorService;

    /**
     * 启动exe程序
     */
    /*
    @PostMapping("/start")
    public ApiResult<String> start(){
        // 启动exe程序
        return monitorService.startProgram();
    }
    */

    /**
     * 停止exe程序
     */
    /*
    @PostMapping("/stop")
    public ApiResult<String> stopProgram(Long pid) {
        // 获取当前时间调用的exe程序的监控资源
        return monitorService.stopProgram(pid);
    }*/

    /**
     * 获取程序状态
     */
    /*
    @GetMapping("/status")
    public ApiResult<String> getStatus(Long pid){
        return monitorService.getProgramStatus(pid);
    }*/


}
