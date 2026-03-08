package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.MonitorTaskMapper;
import com.scut.industrial_software.model.dto.MonitorTasksPageRequestDTO;
import com.scut.industrial_software.model.entity.Task;
import com.scut.industrial_software.service.IMonitorTaskService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonitorTaskServiceImpl extends ServiceImpl<MonitorTaskMapper, Task> implements IMonitorTaskService{
    @Override
    public ApiResult<?> getTasksPage(MonitorTasksPageRequestDTO requestDTO) {
        Page<Task> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<Task> taskPage = baseMapper.selectTaskPage(page, requestDTO.getKeyword(), requestDTO.getStatus(), requestDTO.getServerId());

        List<Task> tasks = taskPage.getRecords();

        Map<String,Object> result = new HashMap<>();
        result.put("records", tasks);
        result.put("total", taskPage.getTotal());
        result.put("size", taskPage.getSize());
        result.put("current", taskPage.getCurrent());

        return ApiResult.success(result);

    }
}
