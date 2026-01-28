package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.MonitorServerMapper;
import com.scut.industrial_software.model.entity.Server;
import com.scut.industrial_software.service.IMonitorServerService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitorServerServiceImpl extends ServiceImpl<MonitorServerMapper, Server> implements IMonitorServerService{

    @Override
    public ApiResult<?> getServerPage(Integer pageNum, Integer pageSize, String keyword, String status, String type) {
        Page<Server> page = new Page<>(pageNum, pageSize);
        IPage<Server> serverPage = baseMapper.selectServerPage(page, keyword, status, type);

        List<Server> servers = serverPage.getRecords();

        Map<String,Object> result = new HashMap<>();

        result.put("records", servers);
        result.put("total", serverPage.getTotal());
        result.put("size", serverPage.getSize());
        result.put("current", serverPage.getCurrent());
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> adjustServerResources(String serverId, Integer cpuCores, Integer memory) {
        Server server = this.getById(serverId);
        if (server == null) {
            return ApiResult.failed("服务器不存在");
        }

        server.setCpuCores(cpuCores);
        server.setMemory(memory);
        boolean result = this.updateById(server);
        if(!result){
            return ApiResult.failed("更新失败");
        } else{
            return ApiResult.success(true, "更新成功");
        }
    }

}
