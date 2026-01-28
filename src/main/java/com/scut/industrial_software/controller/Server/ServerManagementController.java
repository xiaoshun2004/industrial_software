package com.scut.industrial_software.controller.Server;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.entity.Server;
import com.scut.industrial_software.model.vo.PageVO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器管理接口
 * 用于获取当前所有服务器信息，修改服务器配置，动态分配服务器集群资源的功能
 */
@RestController
@Api(tags = "服务器管理")
@RequestMapping("/monitoring/servers")
public class ServerManagementController {

    /**
     * 获取服务器列表，支持分页和过滤
     * @param pageRequestDTO 分页请求参数（关键词可选）
     * @param status         服务器状态过滤条件（可选）
     * @param type           服务器类型过滤条件（可选）
     * @return 服务器分页列表
     */
    @GetMapping
    public ApiResult<PageVO<Server>> getServerList(@RequestBody PageRequestDTO pageRequestDTO,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "type", required = false) String type) {
        return ApiResult.success();
    }

    /**
     * 更新服务器配置，如CPU核心数和内存大小
     * @param serverId  服务器的唯一标识符ID
     * @param cpuCores  服务器CPU核心数
     * @param memory    服务器内存大小（GB）
     * @return 操作结果（成功或失败）
     */
    @PutMapping("/{serverId}/resources")
    public ApiResult<?> updateServerConfig(@PathVariable Long serverId,
                                           @RequestParam("cpuCores") Integer cpuCores,
                                           @RequestParam("memory") Integer memory) {
        return ApiResult.success();
    }

    /**
     * 动态分配服务器资源给特定任务或用户
     * @param serverIds 需要分配资源的服务器ID数组
     * @return 操作结果（成功或失败）
     */
    @PostMapping("/allocate")
    public ApiResult<?> allocateServerResources(@RequestBody Long[] serverIds) {
        return ApiResult.success();
    }

}
