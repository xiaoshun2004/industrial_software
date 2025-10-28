package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.common.api.ApiErrorCode;
import com.scut.industrial_software.model.dto.PageRequestDTO;
import com.scut.industrial_software.model.dto.TaskCreateDTO;
import com.scut.industrial_software.model.entity.ModProjects;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.entity.ModUsers;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.model.vo.ModTasksVO;
import com.scut.industrial_software.service.IModProjectsService;
import com.scut.industrial_software.service.IModTasksService;
import com.scut.industrial_software.service.IModUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.service.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jhx1301
 * @since 2025-05-28
 */
@Service
public class ModTasksServiceImpl extends ServiceImpl<ModTasksMapper, ModTasks> implements IModTasksService {

    @Autowired
    private IModProjectsService modProjectsService;
    
    @Autowired
    private IModUsersService modUsersService;

    @Autowired
    private IMonitorService monitorService;

    private static final List<String> STAGE_TYPES = Arrays.asList("前处理", "后处理", "求解器");
    private static final List<String> PREPROCESSING_SOLVER_TYPES = Arrays.asList("多体", "结构", "冲击");
    private static final List<String> POSTPROCESSING_TYPES = Arrays.asList("通用后处理");
    private static final List<String> STATUS_TYPES = Arrays.asList("未启动", "仿真中", "已结束");

    @Override
    public ApiResult<?> getSharedTasksPage(Integer projectId, PageRequestDTO requestDTO) {
        // 先检查项目是否存在且为共享项目
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        
        if (project.getProjectStatus() != 0) {
            return ApiResult.failed("不是共享项目");
        }
        
        Page<ModTasks> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<ModTasks> pageResult = baseMapper.selectSharedTasksByPage(page, projectId, requestDTO.getKeyword());

        List<ModTasksVO> modTasksVO = pageResult.getRecords().stream().map(ModTasksVO::new).toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", modTasksVO);
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> getPrivateTasksPage(Integer projectId, PageRequestDTO requestDTO) {
        // 先检查项目是否存在且为私人项目
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        
        if (project.getProjectStatus() != 1) {
            return ApiResult.failed("不是私人项目");
        }
        
        Page<ModTasks> page = new Page<>(requestDTO.getPageNum(), requestDTO.getPageSize());
        IPage<ModTasks> pageResult = baseMapper.selectPrivateTasksByPage(page, projectId, requestDTO.getKeyword());

        List<ModTasksVO> modTasksVO = pageResult.getRecords().stream().map(ModTasksVO::new).toList();
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", modTasksVO);
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        
        return ApiResult.success(result);
    }

    @Override
    public ApiResult<?> createSharedTask(Integer projectId, TaskCreateDTO createDTO) {
        // 先检查项目是否存在且为共享项目
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        
        if (project.getProjectStatus() != 0) {
            return ApiResult.failed("不是共享项目");
        }
        
        // 校验输入参数（包括用户是否存在）
        ApiResult<?> validateResult = validateTaskParams(createDTO);
        if (validateResult != null) {
            return validateResult;
        }
        
        // 通过用户名查询用户ID
        Integer creatorId = getUserIdByUsername(createDTO.getCreator());
        
        ModTasks task = new ModTasks();
        task.setTaskName(createDTO.getTaskName());
        task.setCreatorId(creatorId);
        task.setCreator(createDTO.getCreator());
        task.setCreationTime(LocalDateTime.now());
        task.setProjectId(projectId);
        task.setSimulationStage(createDTO.getSimulationStage());
        task.setType(createDTO.getType());
        task.setStatus("未启动"); // 默认状态为未启动
        task.setComputeResource(createDTO.getComputeResource());

        boolean IsSave = this.save(task);

        if (!IsSave){
            throw new ApiException(ApiErrorCode.TASK_CREATION_FAILED);
        }

        ModTasksVO modTasksVO = new ModTasksVO(task);

        return ApiResult.success(modTasksVO, "任务创建成功");
    }

    @Override
    public ApiResult<?> createPrivateTask(Integer projectId, TaskCreateDTO createDTO) {
        // 先检查项目是否存在且为私人项目
        ModProjects project = modProjectsService.getById(projectId);
        if (project == null) {
            throw new ApiException(ApiErrorCode.PROJECT_NOT_FOUND);
        }
        
        if (project.getProjectStatus() != 1) {
            return ApiResult.failed("不是私人项目");
        }
        
        // 校验输入参数（包括用户是否存在）
        ApiResult<?> validateResult = validateTaskParams(createDTO);
        if (validateResult != null) {
            return validateResult;
        }
        
        // 通过用户名查询用户ID
        Integer creatorId = getUserIdByUsername(createDTO.getCreator());
        
        ModTasks task = new ModTasks();
        task.setTaskName(createDTO.getTaskName());
        task.setCreatorId(creatorId);
        task.setCreator(createDTO.getCreator());
        task.setCreationTime(LocalDateTime.now());
        task.setProjectId(projectId);
        task.setSimulationStage(createDTO.getSimulationStage());
        task.setType(createDTO.getType());
        task.setStatus("未启动"); // 默认状态为未启动
        task.setComputeResource(createDTO.getComputeResource());

        boolean IsSave = this.save(task);

        if (!IsSave){
            throw new ApiException(ApiErrorCode.TASK_CREATION_FAILED);
        }

        ModTasksVO modTasksVO = new ModTasksVO(task);

        return ApiResult.success(modTasksVO, "任务创建成功");

    }

    @Override
    public ApiResult<?> deleteTask(String taskId) {
        // 将task_%3d格式的taskId转换为整数ID
        Integer taskIdInt = null;

        if (taskId.startsWith("task_")) {
            taskIdInt = Integer.parseInt(taskId.substring(5));
        }

        if (taskIdInt == null){
            return ApiResult.failed("任务ID格式不正确");
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            throw new ApiException(ApiErrorCode.RESOURCE_NOT_FOUND);
        }
        
        // 填充创建者用户名信息
        if (task.getCreatorId() != null) {
            ModUsers creator = modUsersService.getById(task.getCreatorId());
            if (creator != null) {
                task.setCreator(creator.getUsername());
            }
        }
        
        boolean result = this.removeById(taskIdInt);
        if (result) {
            return ApiResult.success(null, "删除成功");
        } else {
            throw new ApiException(ApiErrorCode.TASK_DELETION_FAILED);
        }
    }

    @Override
    public ApiResult<?> startTask(String taskId) {
        return monitorService.startProgram(taskId);
    }

    @Override
    public ApiResult<?> getTaskStatus(String taskId) {
        return monitorService.getProgramStatus(taskId);
    }

    @Override
    public ApiResult<?> stopTask(String taskId) {
        return monitorService.stopProgram(taskId);
    }

    /**
     * 通过用户名查询用户ID
     * 
     * @param username 用户名
     * @return 用户ID
     */
    private Integer getUserIdByUsername(String username) {
        LambdaQueryWrapper<ModUsers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModUsers::getUsername, username);
        ModUsers user = modUsersService.getOne(wrapper);
        return user != null ? user.getUserId() : null;
    }
    
    /**
     * 校验任务参数
     * 
     * @param createDTO 任务创建参数
     * @return 校验结果，null表示校验通过
     */
    private ApiResult<?> validateTaskParams(TaskCreateDTO createDTO) {
        if (!StringUtils.hasText(createDTO.getTaskName())) {
            return ApiResult.failed("任务名称不能为空");
        }
        
        // 校验创建者用户名
        if (!StringUtils.hasText(createDTO.getCreator())) {
            return ApiResult.failed("创建者不能为空");
        }
        
        // 校验用户是否存在
        Integer creatorId = getUserIdByUsername(createDTO.getCreator());
        if (creatorId == null) {
            return ApiResult.failed("创建者用户不存在");
        }
        
        // 校验仿真阶段
        if (!StringUtils.hasText(createDTO.getSimulationStage()) || 
                !STAGE_TYPES.contains(createDTO.getSimulationStage())) {
            return ApiResult.failed("仿真阶段不正确，应为：前处理、后处理、求解器");
        }
        
        // 校验任务类型与仿真阶段匹配
        if (!StringUtils.hasText(createDTO.getType())) {
            return ApiResult.failed("任务类型不能为空");
        }
        
        if ("后处理".equals(createDTO.getSimulationStage())) {
            if (!POSTPROCESSING_TYPES.contains(createDTO.getType())) {
                return ApiResult.failed("后处理阶段任务类型只能为：通用后处理");
            }
        } else if ("前处理".equals(createDTO.getSimulationStage()) || "求解器".equals(createDTO.getSimulationStage())) {
            if (!PREPROCESSING_SOLVER_TYPES.contains(createDTO.getType())) {
                return ApiResult.failed("前处理/求解器阶段任务类型只能为：多体、结构、冲击");
            }
        }
        
        return null;
    }
}
