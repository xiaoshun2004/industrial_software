package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.model.constant.TaskStatusConstants;
import com.scut.industrial_software.model.dto.ProcessInfoDTO;
import com.scut.industrial_software.model.dto.TaskRuntimeSnapshotDTO;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.vo.MonitorVO;
import com.scut.industrial_software.service.IMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MonitorServiceImpl extends ServiceImpl<ModTasksMapper, ModTasks> implements IMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Value("${monitor.program.path:D:/exe/GETexe4/dist/main/main.exe}")
    private String programPath;

    @Value("${monitor.maxParallel.default:2}")
    private int defaultMaxParallel;

    @Value("${monitor.local-server.id:1}")
    private Integer localServerId;

    @Value("${monitor.local-server.name:local-server}")
    private String localServerName;

    private final Executor taskExecutor;

    private final Map<String, ProcessInfoDTO> processMap = new ConcurrentHashMap<>();

    private final AtomicBoolean dispatching = new AtomicBoolean(false);

    private volatile long nextIdleDispatchAtMillis = 0L;

    private volatile long idleDispatchBackoffSeconds = 1L;

    private static final long MAX_IDLE_DISPATCH_BACKOFF_SECONDS = 30L;

    public MonitorServiceImpl(@Qualifier("taskExecutor") Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 兼容旧启动接口：用户请求只触发一次调度扫描，不直接启动指定任务。
     */
    public ApiResult<MonitorVO> startProgram(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }

        dispatchPendingTasks();
        return ApiResult.success(new MonitorVO(normalizeTaskKey(taskIdInt), getLatestStatus(taskIdInt)),
                "任务已进入调度队列，后端将按优先级和资源情况自动启动");
    }

    /**
     * 调度任务的核心逻辑入口
     */
    @Override
    public boolean dispatchPendingTasks() {
        if (!dispatching.compareAndSet(false, true)) {
            return false;
        }

        try {
            // 获取最大并发量，判断是否可以继续调度
            int maxParallel = resolveMaxParallel();
            int runningCount = safeCount(baseMapper.countRunningTasks());
            int availableSlots = Math.max(maxParallel - runningCount, 0);
            if (availableSlots <= 0) {
                return false;
            }

            Page<ModTasks> page = new Page<>(1, availableSlots, false);
            List<ModTasks> pendingTasks = baseMapper.selectPendingTasksForSchedule(page).getRecords();
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                return false;
            }

            boolean launched = false;
            for (ModTasks pendingTask : pendingTasks) {
                if (tryLaunchScheduledTask(pendingTask)) {
                    launched = true;
                }
            }
            if (launched) {
                resetIdleDispatchBackoff();
            }
            return launched;
        } catch (Exception e) {
            logger.error("调度 pending 任务失败", e);
            return false;
        } finally {
            dispatching.set(false);
        }
    }

    /**
     * 恢复运行任务
     */
    @Override
    public void recoverRunningTasksOnStartup() {
        try {
            int recovered = baseMapper.failRunningTasksOnStartup("后端服务重启，运行态进程关系丢失");
            if (recovered > 0) {
                logger.warn("服务启动恢复：已将 {} 个遗留 running 任务标记为 failed", recovered);
            }
        } catch (Exception e) {
            logger.warn("服务启动恢复任务状态失败，可能是任务表尚未初始化: {}", e.getMessage());
        }
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        recoverRunningTasksOnStartup();
        dispatchPendingTasks();
    }

    private boolean tryLaunchScheduledTask(ModTasks task) {
        if (task == null || task.getTaskId() == null) {
            return false;
        }

        Integer taskIdInt = task.getTaskId();
        Integer serverId = task.getServerId() == null ? localServerId : task.getServerId();
        String serverName = StringUtils.hasText(task.getServerName()) ? task.getServerName() : localServerName;
        int updated = baseMapper.markTaskRunning(taskIdInt, serverId, serverName);
        if (updated <= 0) {
            return false;
        }

        launchProgramAsync(normalizeTaskKey(taskIdInt), taskIdInt, serverId, serverName);
        return true;
    }

    /**
     *  启动异步线程执行任务
     */
    private void launchProgramAsync(String taskId, Integer taskIdInt, Integer serverId, String serverName) {
        CompletableFuture.runAsync(() -> {
            Process process = null;
            try {
                File file = new File(programPath);
                if (!file.exists()) {
                    markFailedAfterRunning(taskIdInt, taskId, "程序路径不正确: " + programPath);
                    return;
                }

                process = new ProcessBuilder(programPath).start();
                ProcessInfoDTO processInfo = new ProcessInfoDTO(process, process.pid(), LocalDateTime.now());
                processInfo.setStatus(TaskStatusConstants.RUNNING);
                processInfo.setProgress(0);
                processInfo.setServerId(serverId);
                processInfo.setServerName(serverName);
                processMap.put(taskId, processInfo);

                int exitCode = process.waitFor();
                ProcessInfoDTO latestInfo = processMap.get(taskId);
                if (latestInfo == null) {
                    return;
                }

                latestInfo.setExitCode(exitCode);
                latestInfo.setEndTime(LocalDateTime.now());
                if (TaskStatusConstants.RUNNING.equals(latestInfo.getStatus())) {
                    if (exitCode == 0) {
                        latestInfo.setStatus(TaskStatusConstants.COMPLETED);
                        latestInfo.setProgress(100);
                        baseMapper.markTaskCompleted(taskIdInt);
                    } else {
                        String errorMsg = "程序退出码=" + exitCode;
                        latestInfo.setStatus(TaskStatusConstants.FAILED);
                        latestInfo.setErrorMsg(errorMsg);
                        baseMapper.markTaskFailed(taskIdInt, errorMsg);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                markFailedAfterRunning(taskIdInt, taskId, "任务执行线程被中断");
            } catch (Exception e) {
                markFailedAfterRunning(taskIdInt, taskId, "启动程序失败: " + e.getMessage());
            } finally {
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
                processMap.remove(taskId);
                dispatchPendingTasks();
            }
        }, taskExecutor);
    }

    /**
     * 标记运行后失败的任务
     */
    private void markFailedAfterRunning(Integer taskIdInt, String taskId, String errorMsg) {
        baseMapper.markTaskFailed(taskIdInt, errorMsg);
        ProcessInfoDTO info = processMap.get(taskId);
        if (info != null) {
            info.setStatus(TaskStatusConstants.FAILED);
            info.setErrorMsg(errorMsg);
            info.setEndTime(LocalDateTime.now());
        }
        logger.warn("任务失败 taskId={}, error={}", taskId, errorMsg);
    }

    /**
     *  用户手动停止任务
     */
    @Override
    public ApiResult<MonitorVO> stopProgram(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        String taskKey = normalizeTaskKey(taskIdInt);
        ProcessInfoDTO processInfo = processMap.get(taskKey);
        if (processInfo != null) {
            processInfo.setStatus(TaskStatusConstants.STOPPED);
            processInfo.setErrorMsg("用户手动停止");
            processInfo.setEndTime(LocalDateTime.now());
            Process process = processInfo.getProcess();
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            baseMapper.markTaskStopped(taskIdInt, "用户手动停止");
            processMap.remove(taskKey);
            dispatchPendingTasks();
            return ApiResult.success(new MonitorVO(taskKey, TaskStatusConstants.STOPPED));
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }
        String status = normalizeStatus(task.getStatus());
        if (TaskStatusConstants.RUNNING.equals(status)) {
            int updated = baseMapper.markTaskStopped(taskIdInt, "用户手动停止");
            dispatchPendingTasks();
            return updated > 0
                    ? ApiResult.success(new MonitorVO(taskKey, TaskStatusConstants.STOPPED))
                    : ApiResult.failed("任务状态已变化，请刷新后重试");
        }
        if (isTerminalStatus(status) || TaskStatusConstants.PENDING.equals(status)) {
            return ApiResult.success(new MonitorVO(taskKey, status));
        }
        return ApiResult.failed("任务状态不允许停止");
    }

    /**
     * 获取程序的状态
     * @param taskId
     * @return
     */
    @Override
    public ApiResult<MonitorVO> getProgramStatus(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        String taskKey = normalizeTaskKey(taskIdInt);
        ProcessInfoDTO processInfo = processMap.get(taskKey);
        if (processInfo != null) {
            return ApiResult.success(new MonitorVO(taskKey, processInfo.getStatus()));
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }
        return ApiResult.success(new MonitorVO(taskKey, normalizeStatus(task.getStatus())));
    }

    /**
     * 定时调度器，每秒执行一次任务监控以及任务调度
     */
    @Override
    @Scheduled(fixedDelayString = "${monitor.scheduler.fixed-delay-ms:1000}")
    public void scheduledMonitor() {
        monitorRunningProcesses();
        if (!processMap.isEmpty()) {
            resetIdleDispatchBackoff();
            dispatchPendingTasks();
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextIdleDispatchAtMillis) {
            return;
        }

        boolean launched = dispatchPendingTasks();
        if (!launched) {
            scheduleNextIdleDispatch(now);
        }
    }

    private void scheduleNextIdleDispatch(long nowMillis) {
        long delaySeconds = Math.max(1L, idleDispatchBackoffSeconds);
        nextIdleDispatchAtMillis = nowMillis + delaySeconds * 1000L;
        idleDispatchBackoffSeconds = Math.min(delaySeconds * 2L, MAX_IDLE_DISPATCH_BACKOFF_SECONDS);
    }

    private void resetIdleDispatchBackoff() {
        nextIdleDispatchAtMillis = 0L;
        idleDispatchBackoffSeconds = 1L;
    }

    /**
     * 监控运行任务
     */
    private void monitorRunningProcesses() {
        for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
            String taskId = entry.getKey();
            ProcessInfoDTO info = entry.getValue();
            if (!TaskStatusConstants.RUNNING.equals(info.getStatus())) {
                continue;
            }

            Process process = info.getProcess();
            if (process == null || !process.isAlive()) {
                continue;
            }

            int progress = info.getProgress() == null ? 0 : info.getProgress();
            if (progress < 95) {
                progress = Math.min(progress + 5, 95);
                info.setProgress(progress);
                Integer taskIdInt = parseTaskId(taskId);
                if (taskIdInt != null) {
                    LambdaUpdateWrapper<ModTasks> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(ModTasks::getTaskId, taskIdInt)
                            .eq(ModTasks::getStatus, TaskStatusConstants.RUNNING)
                            .set(ModTasks::getProgress, progress);
                    this.update(wrapper);
                }
            }
        }
    }

    /**
     *  获取运行快照
     */
    @Override
    public Map<String, TaskRuntimeSnapshotDTO> getRuntimeSnapshots() {
        Map<String, TaskRuntimeSnapshotDTO> snapshots = new HashMap<>();
        for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
            snapshots.put(entry.getKey(), toSnapshot(entry.getKey(), entry.getValue()));
        }
        return snapshots;
    }

    /**
     *  获取运行快照
     */
    @Override
    public TaskRuntimeSnapshotDTO getRuntimeSnapshot(String taskId) {
        ProcessInfoDTO info = processMap.get(taskId);
        if (info == null) {
            return null;
        }
        return toSnapshot(taskId, info);
    }

    private TaskRuntimeSnapshotDTO toSnapshot(String taskId, ProcessInfoDTO info) {
        return new TaskRuntimeSnapshotDTO(taskId, info.getStatus(), info.getProgress(), info.getServerId(),
                info.getServerName(), info.getStartTime(), info.getErrorMsg());
    }

    private String getLatestStatus(Integer taskIdInt) {
        ProcessInfoDTO info = processMap.get(normalizeTaskKey(taskIdInt));
        if (info != null) {
            return info.getStatus();
        }
        ModTasks latest = this.getById(taskIdInt);
        return latest == null ? TaskStatusConstants.FAILED : normalizeStatus(latest.getStatus());
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return TaskStatusConstants.FAILED;
        }
        return switch (status) {
            case "未启动" -> TaskStatusConstants.PENDING;
            case "仿真中" -> TaskStatusConstants.RUNNING;
            case "已结束" -> TaskStatusConstants.COMPLETED;
            default -> status;
        };
    }

    private boolean isTerminalStatus(String status) {
        return TaskStatusConstants.COMPLETED.equals(status)
                || TaskStatusConstants.FAILED.equals(status)
                || TaskStatusConstants.STOPPED.equals(status);
    }

    private int resolveMaxParallel() {
        return Math.max(defaultMaxParallel, 1);
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private String normalizeTaskKey(Integer taskIdInt) {
        return "task_" + taskIdInt;
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
