package com.scut.industrial_software.controller.Monitor;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.MonitorServersPageRequestDTO;
import com.scut.industrial_software.service.IMonitorServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/monitoring/servers")
public class MonitorServerController {

    @Autowired
    private IMonitorServerService serverService;

    @GetMapping
    public ApiResult<?> getServerPage(@ModelAttribute @Validated MonitorServersPageRequestDTO requestDTO){
        log.info("获取服务器列表: pageNum={}, pageSize={}, keyword={}, status={}, type={}", requestDTO.getPageNum(), requestDTO.getPageSize(), requestDTO.getKeyword(), requestDTO.getStatus(), requestDTO.getType());
        return serverService.getServerPage(requestDTO);
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
