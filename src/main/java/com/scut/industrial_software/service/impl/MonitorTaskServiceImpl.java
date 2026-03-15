package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.model.dto.MonitorTasksPageRequestDTO;
import com.scut.industrial_software.model.dto.TaskRuntimeSnapshotDTO;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.vo.MonitorTaskItemVO;
import com.scut.industrial_software.service.IMonitorService;
import com.scut.industrial_software.service.IMonitorTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MonitorTaskServiceImpl extends ServiceImpl<ModTasksMapper, ModTasks> implements IMonitorTaskService {

    private static final Set<String> ALLOWED_STATUS = Set.of("pending", "running", "completed", "failed", "未启动", "仿真中", "已结束");

    @Autowired
    private IMonitorService monitorService;

    @Override
    public ApiResult<?> getTasksPage(MonitorTasksPageRequestDTO requestDTO) {
        if (requestDTO == null) {
            return ApiResult.failed("分页参数不能为空");
        }
        if (requestDTO.getPageNum() == null || requestDTO.getPageNum() <= 0 || requestDTO.getPageSize() == null || requestDTO.getPageSize() <= 0) {
            return ApiResult.failed("分页参数必须为正整数");
        }
        if (StringUtils.hasText(requestDTO.getStatus()) && !ALLOWED_STATUS.contains(requestDTO.getStatus())) {
            return ApiResult.failed("状态筛选值非法");
        }

        Page<ModTasks> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<ModTasks> taskPage = baseMapper.selectMonitorTasksByPage(page, requestDTO.getKeyword(), requestDTO.getStatus(), requestDTO.getServerId());

        List<MonitorTaskItemVO> items = taskPage.getRecords().stream().map(task -> {
            MonitorTaskItemVO vo = MonitorTaskItemVO.from(task);
            // 如果任务的状态是running，正在运行的则先返回它的执行进度快照，后续如果升级为Websocket全双工协议可以实现实时更新
            if ("running".equals(task.getStatus()) || "仿真中".equals(task.getStatus())) {
                TaskRuntimeSnapshotDTO runtimeSnapshot = monitorService.getRuntimeSnapshot("task_" + task.getTaskId());
                if (runtimeSnapshot != null) {
                    vo.setStatus(runtimeSnapshot.getStatus());
                    vo.setProgress(runtimeSnapshot.getProgress());
                    vo.setServerId(runtimeSnapshot.getServerId());
                    vo.setServerName(runtimeSnapshot.getServerName());
                    vo.setStartTime(runtimeSnapshot.getStartTime());
                    vo.setErrorMsg(runtimeSnapshot.getErrorMsg());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", items);
        result.put("total", taskPage.getTotal());
        result.put("pageSize", taskPage.getSize());
        result.put("pageNum", taskPage.getCurrent());

        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> updateTaskPriority(String taskId, Integer priority) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }
        if (priority == null || priority < 1 || priority > 3) {
            return ApiResult.failed("priority 仅支持 1|2|3");
        }

        int updated = baseMapper.updateTaskPriorityWhenPending(taskIdInt, priority);
        if (updated > 0) {
            return ApiResult.success("优先级修改成功");
        }
        return ApiResult.failed("仅 pending 状态任务允许修改优先级");
    }

    private Integer parseTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        try {
            return taskId.startsWith("task_") ? Integer.parseInt(taskId.substring(5)) : Integer.parseInt(taskId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
