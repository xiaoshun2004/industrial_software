package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.vo.MonitorVO;
import com.scut.industrial_software.service.IMonitorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/taskMonitor")
public class MonitorProgramController {

    @Resource
    private IMonitorService monitorService;

    /**
     * 启动exe程序
     */
    @PostMapping("/start")
    public ApiResult<String> start(){
        // 启动exe程序
        CompletableFuture<String> future = monitorService.startProgram();
        return ApiResult.success(future.join());
    }

    /**
     * 使用Sigar监控本地exe程序
     */
    /*
    @PostMapping("/monitorProgram")
    public ApiResult<MonitorVO> monitorProgram(Long pid) {
        // 获取当前时间调用的exe程序的监控资源
        return monitorService.monitorProgram(pid);
    }*/

}
