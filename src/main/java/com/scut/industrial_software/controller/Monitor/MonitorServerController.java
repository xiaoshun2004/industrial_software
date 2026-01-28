package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.service.IMonitorServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/monitor/servers")
public class MonitorServerController {

    @Autowired
    private IMonitorServerService serverService;

    @PostMapping
    public ApiResult<?> getServerPage(@RequestParam Integer pageNum,
                                      @RequestParam Integer pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String type){
        log.info("获取服务器列表: pageNum={}, pageSize={}, keyword={}, status={}, type={}", pageNum, pageSize, keyword, status, type);
        return serverService.getServerPage(pageNum, pageSize, keyword, status, type);
    }

    @PutMapping("/{serverId}/resources")
    public ApiResult<?> adjustServerResources(@PathVariable String serverId,
                                              @RequestParam Integer cpuCores,
                                              @RequestParam Integer memory){
        return serverService.adjustServerResources(serverId, cpuCores, memory);
    }

    @PostMapping("/allocate")
    public ApiResult<?> allocateResources(@RequestBody Integer[] serverIds){
        return ApiResult.success();
    }
}
