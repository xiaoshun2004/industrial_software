package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiErrorCode;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.model.constant.TaskStatusConstants;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.TaskCreateDTO;
import com.scut.industrial_software.model.entity.ModProjects;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.model.vo.ModTasksVO;
import com.scut.industrial_software.service.IModProjectsService;
import com.scut.industrial_software.service.IModTasksService;
import com.scut.industrial_software.service.IModUsersService;
import com.scut.industrial_software.service.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModTasksServiceImpl extends ServiceImpl<ModTasksMapper, ModTasks> implements IModTasksService {

    @Autowired
    private IModProjectsService modProjectsService;

    @Autowired
    private IModUsersService modUsersService;

    @Autowired
    private IMonitorService monitorService;

    @Value("${monitor.local-server.id:1}")
    private Integer localServerId;

    @Value("${monitor.local-server.name:local-server}")
    private String localServerName;

    private static final List<String> STAGE_TYPES = Arrays.asList("前处理", "后处理", "求解器");
    private static final List<String> PREPROCESSING_SOLVER_TYPES = Arrays.asList("多体", "结构", "冲击");
    private static final List<String> POSTPROCESSING_TYPES = List.of("通用后处理", "多体");
    private static final int DEFAULT_PRIORITY = 2;
    private static final int DEFAULT_CPU_CORE_NEED = 1;
    private static final int DEFAULT_MEMORY_NEED = 4;

    @Override
    public ApiResult<?> getSharedTasksPage(Integer projectId, PageRequestDTO requestDTO) {
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        if (project.getProjectStatus() != 0) {
            return ApiResult.failed("不是共享项目");
        }

        Page<ModTasks> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<ModTasks> pageResult = baseMapper.selectSharedTasksByPage(page, projectId, requestDTO.getKeyword());

        List<ModTasksVO> records = pageResult.getRecords().stream()
                .map(this::toTaskVOWithDisplayStatus)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> getPrivateTasksPage(Integer projectId, PageRequestDTO requestDTO) {
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        if (project.getProjectStatus() != 1) {
            return ApiResult.failed("不是私有项目");
        }

        Page<ModTasks> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<ModTasks> pageResult = baseMapper.selectPrivateTasksByPage(page, projectId, requestDTO.getKeyword());

        List<ModTasksVO> records = pageResult.getRecords().stream()
                .map(this::toTaskVOWithDisplayStatus)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> createSharedTask(Integer projectId, TaskCreateDTO createDTO) {
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        if (project.getProjectStatus() != 0) {
            return ApiResult.failed("不是共享项目");
        }

        ApiResult<?> validateResult = validateTaskParams(createDTO);
        if (validateResult != null) {
            return validateResult;
        }

        ModTasks task = buildTask(projectId, createDTO);
        if (!this.save(task)) {
            throw new ApiException(ApiErrorCode.TASK_CREATION_FAILED);
        }
        return ApiResult.success(toTaskVOWithDisplayStatus(task), "任务创建成功");
    }

    @Override
    public ApiResult<?> createPrivateTask(Integer projectId, TaskCreateDTO createDTO) {
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        if (project.getProjectStatus() != 1) {
            return ApiResult.failed("不是私有项目");
        }

        ApiResult<?> validateResult = validateTaskParams(createDTO);
        if (validateResult != null) {
            return validateResult;
        }

        ModTasks task = buildTask(projectId, createDTO);
        if (!this.save(task)) {
            throw new ApiException(ApiErrorCode.TASK_CREATION_FAILED);
        }
        return ApiResult.success(toTaskVOWithDisplayStatus(task), "任务创建成功");
    }

    @Override
    public ApiResult<?> deleteTask(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }

        if (!this.removeById(taskIdInt)) {
            throw new ApiException(ApiErrorCode.TASK_DELETION_FAILED);
        }
        return ApiResult.success(null, "删除成功");
    }

    @Override
    public ApiResult<?> startRemoteTask(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }

        int updated = baseMapper.markTaskRemotePending(taskIdInt, localServerId, localServerName);
        if (updated <= 0) {
            return ApiResult.failed("仅 pending 且未选择其他执行模式的任务允许远程启动");
        }

        monitorService.dispatchPendingTasks();
        return monitorService.getProgramStatus(taskId);
    }

    @Override
    public ApiResult<?> getTaskStatus(String taskId) {
        return monitorService.getProgramStatus(taskId);
    }

    @Override
    public ApiResult<?> stopTask(String taskId) {
        return monitorService.stopProgram(taskId);
    }

    private ModTasks buildTask(Integer projectId, TaskCreateDTO createDTO) {
        Integer creatorId = getUserIdByUsername(createDTO.getCreator());

        ModTasks task = new ModTasks();
        task.setTaskName(createDTO.getTaskName());
        task.setCreatorId(creatorId);
        task.setCreator(createDTO.getCreator());
        task.setCreationTime(LocalDateTime.now());
        task.setProjectId(projectId);
        task.setSimulationStage(createDTO.getSimulationStage());
        task.setType(createDTO.getType());
        task.setStatus(TaskStatusConstants.PENDING);
        task.setExecutionMode(null);
        task.setPriority(resolveTaskPriority(createDTO));
        task.setCpuCoreNeed(DEFAULT_CPU_CORE_NEED);
        task.setMemoryNeed(DEFAULT_MEMORY_NEED);
        task.setProgress(0);
        task.setComputeResource(createDTO.getComputeResource());
        return task;
    }

    private ModTasksVO toTaskVOWithDisplayStatus(ModTasks task) {
        ModTasksVO taskVO = new ModTasksVO(task);
        taskVO.setStatus(convertTaskStatus(taskVO.getStatus()));
        return taskVO;
    }

    private String convertTaskStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return status;
        }
        return switch (status.toLowerCase()) {
            case "pending" -> "未启动";
            case "running" -> "仿真中";
            case "completed" -> "已完成";
            case "failed" -> "已失败";
            case "stopped" -> "已停止";
            default -> status;
        };
    }

    private Integer getUserIdByUsername(String username) {
        LambdaQueryWrapper<ModUsers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModUsers::getUsername, username);
        ModUsers user = modUsersService.getOne(wrapper);
        return user != null ? user.getUserId() : null;
    }

    private ApiResult<?> validateTaskParams(TaskCreateDTO createDTO) {
        if (createDTO == null) {
            return ApiResult.failed("任务参数不能为空");
        }
        if (!StringUtils.hasText(createDTO.getTaskName())) {
            return ApiResult.failed("任务名称不能为空");
        }
        if (!StringUtils.hasText(createDTO.getCreator())) {
            return ApiResult.failed("创建者不能为空");
        }
        if (getUserIdByUsername(createDTO.getCreator()) == null) {
            return ApiResult.failed("创建者用户不存在");
        }
        if (!StringUtils.hasText(createDTO.getSimulationStage())
                || !STAGE_TYPES.contains(createDTO.getSimulationStage())) {
            return ApiResult.failed("仿真阶段不正确，应为：前处理、后处理、求解器");
        }
        if (!StringUtils.hasText(createDTO.getType())) {
            return ApiResult.failed("任务类型不能为空");
        }
        if ("后处理".equals(createDTO.getSimulationStage())) {
            if (!POSTPROCESSING_TYPES.contains(createDTO.getType())) {
                return ApiResult.failed("后处理阶段任务类型只能为：通用后处理、多体");
            }
        } else if ("前处理".equals(createDTO.getSimulationStage()) || "求解器".equals(createDTO.getSimulationStage())) {
            if (!PREPROCESSING_SOLVER_TYPES.contains(createDTO.getType())) {
                return ApiResult.failed("前处理、求解器阶段任务类型只能为：多体、结构、冲击");
            }
        }
        if (createDTO.getPriority() != null && (createDTO.getPriority() < 1 || createDTO.getPriority() > 3)) {
            return ApiResult.failed("priority 仅支持 1|2|3");
        }
        return null;
    }

    private Integer resolveTaskPriority(TaskCreateDTO createDTO) {
        return createDTO.getPriority() == null ? DEFAULT_PRIORITY : createDTO.getPriority();
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
