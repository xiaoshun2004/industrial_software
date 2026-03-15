package com.scut.industrial_software.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.mapper.ModTasksMapper;
import com.scut.industrial_software.mapper.MonitorServerMapper;
import com.scut.industrial_software.model.constant.RedisConstants;
import com.scut.industrial_software.model.dto.ProcessInfoDTO;
import com.scut.industrial_software.model.dto.TaskRuntimeSnapshotDTO;
import com.scut.industrial_software.model.entity.ModTasks;
import com.scut.industrial_software.model.entity.Server;
import com.scut.industrial_software.model.vo.MonitorVO;
import com.scut.industrial_software.service.IMonitorService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class MonitorServiceImpl extends ServiceImpl<ModTasksMapper, ModTasks> implements IMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    private static final long INITIAL_IDLE_BACKOFF_SECONDS = 1L;
    private static final long MAX_IDLE_BACKOFF_SECONDS = 128L;

    @Value("${monitor.program.path:D:/exe/GETexe4/dist/main/main.exe}")
    private String programPath;

    @Value("${monitor.maxParallel.default:2}")
    private int defaultMaxParallel;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private MonitorServerMapper monitorServerMapper;

    private final Map<String, ProcessInfoDTO> processMap = new ConcurrentHashMap<>();
    private final Object idleDispatchBackoffLock = new Object();
    private volatile long nextIdleDispatchAtMillis = 0L;
    private volatile long idleDispatchBackoffSeconds = INITIAL_IDLE_BACKOFF_SECONDS;

    @Override
    public ApiResult<MonitorVO> startProgram(String taskId) {
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }
        if (isNotPending(task.getStatus())) {
            return ApiResult.failed("仅 pending 状态任务可启动");
        }

        String type = StringUtils.hasText(task.getType()) ? task.getType() : "default";
        RLock solverLock = redissonClient.getLock(RedisConstants.TASK_SCHEDULE_LOCK_PREFIX + type);
        RLock taskLock = redissonClient.getLock(RedisConstants.TASK_RUN_LOCK_PREFIX + taskId);

        boolean solverLocked = false;
        boolean taskLocked = false;
        try {
            // 1. 任务幂等性锁，防止单个任务被重复启动
            taskLocked = taskLock.tryLock(2, 30, TimeUnit.SECONDS);
            if (!taskLocked) {
                return ApiResult.failed("任务正在处理中，请稍后重试");
            }

            // 2. 抢占求解器的锁，等待3秒，拿到后60秒释放，watchdog机制防止锁提前释放和死锁
            // TODO: 兴许可以不需要这个锁呢（考虑ing）
            solverLocked = solverLock.tryLock(3, 60, TimeUnit.SECONDS);
            if (!solverLocked) {
                return ApiResult.failed("资源调度繁忙，请稍后重试");
            }

            // 3. 查找这个任务是否存在并且能否被启动
            ModTasks latest = this.getById(taskIdInt);
            if (latest == null) {
                return ApiResult.failed("任务不存在");
            }
            if (isNotPending(latest.getStatus())) {
                return ApiResult.failed("仅 pending 状态任务可启动");
            }

            // 4. 获取当前类型为type且正在执行中的任务，获取当前服务器的并行任务数
            int runningCount = baseMapper.countRunningByType(type);
            int maxParallel = resolveMaxParallel(type);
            // TODO：正在运行的数量大于求解器的最大并发数时，可以考虑将该任务塞入消息队列（必须使用）
            if (runningCount >= maxParallel) {
                return ApiResult.success(new MonitorVO(taskId, STATUS_PENDING), "任务进入等待队列，待资源可用后自动调度");
            }

            // TODO：服务器怎么选？使用负载均衡策略
            // 5. 选择一台能够运行该任务的服务器
            Server server = selectTargetServer(latest);
            if (server == null) {
                return ApiResult.failed("暂无可用服务器资源，任务已保持 pending");
            }

            // 6. 将该任务的状态转成执行中
            int updated = baseMapper.updateTaskStatusConditionally(taskIdInt, latest.getStatus(), STATUS_RUNNING);
            if (updated <= 0) {
                return ApiResult.failed("任务状态已变化，请刷新后重试");
            }

            // 7. 任务开始执行前先将任务的serverId、serverName、startTime等信息更新到数据库，做上下文数据库落库
            LambdaUpdateWrapper<ModTasks> startUpdate = new LambdaUpdateWrapper<>();
            startUpdate.eq(ModTasks::getTaskId, taskIdInt)
                    .set(ModTasks::getServerId, server.getId())
                    .set(ModTasks::getServerName, server.getName())
                    .set(ModTasks::getStartTime, LocalDateTime.now())
                    .set(ModTasks::getProgress, 0)
                    .set(ModTasks::getErrorMsg, null);
            this.update(startUpdate);

            // TODO: 任务在不同服务器下的异步执行，可能需要RPC框架
            // 8. 任务的异步执行
            launchProgramAsync(taskId, taskIdInt, server);
            return ApiResult.success(new MonitorVO(taskId, STATUS_RUNNING));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ApiResult.failed("任务启动被中断");
        } catch (Exception e) {
            logger.error("启动任务失败 taskId={}", taskId, e);
            return ApiResult.failed("任务启动失败: " + e.getMessage());
        } finally {
            if (taskLocked) {
                taskLock.unlock();
            }
            if (solverLocked) {
                solverLock.unlock();
            }
        }
    }

    private void launchProgramAsync(String taskId, Integer taskIdInt, Server server) {
        CompletableFuture.runAsync(() -> {
            // 1. 查看应用程序的路径是否存在，不存在直接失败
            File file = new File(programPath);
            if (!file.exists()) {
                failTask(taskIdInt, taskId, "程序路径不正确: " + programPath);
                return;
            }

            Process process = null;
            try {
                // TODO: 这里直接启动本地程序，后续改为通过SSH或者RPC在远程服务器上启动
                // 2. 启动一个进程和进程快照来记录此次任务的监控信息
                process = new ProcessBuilder(programPath).start();
                ProcessInfoDTO processInfo = new ProcessInfoDTO(process, process.pid(), LocalDateTime.now());
                processInfo.setStatus(STATUS_RUNNING);
                processInfo.setProgress(0);
                processInfo.setServerId(server.getId());
                processInfo.setServerName(server.getName());
                processMap.put(taskId, processInfo);

                // 3. 等待应用进程的结束与退出
                int exitCode = process.waitFor();
                ProcessInfoDTO latestInfo = processMap.get(taskId);
                // 3.1 如果没加入应用进程池，就当没执行，直接返回，当然不太会出现进程快照没放进去的情况
                if (latestInfo == null) {
                    return;
                }

                // 4. 这个时候没有被用户中断或者调度器中断，属于正常结束或运行出错
                if (STATUS_RUNNING.equals(latestInfo.getStatus())) {
                    latestInfo.setExitCode(exitCode);
                    latestInfo.setEndTime(LocalDateTime.now());
                    // 4.1 以退出码判断任务的结束方式
                    if (exitCode == 0) {
                        latestInfo.setStatus(STATUS_COMPLETED);
                        latestInfo.setProgress(100);
                        // 4.1.1 任务执行成功落库处理
                        completeTask(taskIdInt);
                    } else {
                        latestInfo.setStatus(STATUS_FAILED);
                        latestInfo.setErrorMsg("程序退出码=" + exitCode);
                        // 4.1.2 任务执行异常退出落库处理
                        failTask(taskIdInt, taskId, latestInfo.getErrorMsg());
                    }
                }
            } catch (Exception e) {
                failTask(taskIdInt, taskId, "启动程序失败: " + e.getMessage());
            } finally {
                // 5. 记得杀死进程
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
                // 6. 从应用进程池里删掉这个快照
                processMap.remove(taskId);
                dispatchPendingTasks();
            }
        }, taskExecutor);
    }

    @Override
    public ApiResult<MonitorVO> stopProgram(String taskId) {
        // 1. 解析出任务ID
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }
        // 2. 获取应用进程池中的任务进程快照
        ProcessInfoDTO processInfo = processMap.get(taskId);
        if (processInfo != null) {
            // 2.1 切断任务进程
            Process process = processInfo.getProcess();
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            processInfo.setStatus(STATUS_FAILED);
            processInfo.setErrorMsg("用户手动停止");
            processInfo.setEndTime(LocalDateTime.now());
            // 2.2 先落库再删快照
            failTask(taskIdInt, taskId, "用户手动停止");
            processMap.remove(taskId);
            dispatchPendingTasks();
            return ApiResult.success(new MonitorVO(taskId, STATUS_FAILED));
        }

        // 3. 这个时候可能是任务抢先已经完成了，结束就没有意义了，直接从库里获取任务的状态吧
        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }
        if (STATUS_COMPLETED.equals(task.getStatus()) || STATUS_FAILED.equals(task.getStatus())) {
            return ApiResult.success(new MonitorVO(taskId, task.getStatus()));
        }

        // 4. 兜底策略，任务启动后异步开启的线程断了但又没抛异常，用户手动停止就能走到这
        failTask(taskIdInt, taskId, "用户手动停止");
        dispatchPendingTasks();
        return ApiResult.success(new MonitorVO(taskId, STATUS_FAILED));
    }

    @Override
    public ApiResult<MonitorVO> getProgramStatus(String taskId) {
        /*
          用于客户端只需要返回状态字段的查询情况
         */
        Integer taskIdInt = parseTaskId(taskId);
        if (taskIdInt == null) {
            return ApiResult.failed("任务ID格式不正确");
        }

        // 先从应用进程Map中获取，如果有则返回这个进程快照的运行状态，这时候大概率正在运行，也有可能被中断结束还未从map中移除
        ProcessInfoDTO processInfo = processMap.get(taskId);
        if (processInfo != null) {
            return ApiResult.success(new MonitorVO(taskId, processInfo.getStatus()));
        }

        // 如果在进程Map中没找到，可能是还未启动或者已经结束了，这时再查询数据库中的状态
        ModTasks task = this.getById(taskIdInt);
        if (task == null) {
            return ApiResult.failed("任务不存在");
        }
        return ApiResult.success(new MonitorVO(taskId, normalizeStatus(task.getStatus())));
    }

    @Override
    @Scheduled(fixedDelay = 1000)
    public void scheduledMonitor() {
        /*
        if (processMap.isEmpty()) {
            dispatchPendingTasksWithBackoff();
            return;
        }
        resetIdleDispatchBackoff();*/

        // 1. 循环监控应用进程池中的每个进程，更新它的运行状态和进度快照
        // TODO: 这种方法并不能保证每个进程的进度实时性，后续需要使用更高级的方案解决
        for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
            String taskId = entry.getKey();
            ProcessInfoDTO info = entry.getValue();
            if (!STATUS_RUNNING.equals(info.getStatus())) {
                continue;
            }

            Process process = info.getProcess();
            if (process == null || !process.isAlive()) {
                Integer taskIdInt = parseTaskId(taskId);
                if (taskIdInt != null) {
                    failTask(taskIdInt, taskId, "进程异常退出");
                }
                info.setStatus(STATUS_FAILED);
                info.setErrorMsg("进程异常退出");
                continue;
            }

            // 这里只是模拟任务执行进度变更，每次定时增加5%的进度
            int progress = info.getProgress() == null ? 0 : info.getProgress();
            if (progress < 95) {
                progress = Math.min(progress + 5, 95);
                info.setProgress(progress);
                Integer taskIdInt = parseTaskId(taskId);
                if (taskIdInt != null) {
                    // TODO: 每次进度更新时都要更新一次数据库，妥妥的数据库性能杀手，其实进度信息可以不保存进数据表的字段里面
                    LambdaUpdateWrapper<ModTasks> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(ModTasks::getTaskId, taskIdInt)
                            .set(ModTasks::getProgress, progress);
                    this.update(wrapper);
                }
            }
        }
        /*if (processMap.isEmpty()) {
            dispatchPendingTasksWithBackoff();
            return;
        }
        resetIdleDispatchBackoff();
        dispatchPendingTasks();*/
    }

    @Override
    public Map<String, TaskRuntimeSnapshotDTO> getRuntimeSnapshots() {
        Map<String, TaskRuntimeSnapshotDTO> snapshots = new HashMap<>();
        for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
            snapshots.put(entry.getKey(), toSnapshot(entry.getKey(), entry.getValue()));
        }
        return snapshots;
    }

    @Override
    public TaskRuntimeSnapshotDTO getRuntimeSnapshot(String taskId) {
        // 从进程Map中获取taskInfo，一定要保证任务先更新落库后再从Map中移除这个任务
        ProcessInfoDTO info = processMap.get(taskId);
        if (info == null) {
            return null;
        }
        return toSnapshot(taskId, info);
    }

    private TaskRuntimeSnapshotDTO toSnapshot(String taskId, ProcessInfoDTO info) {
        return new TaskRuntimeSnapshotDTO(taskId, info.getStatus(), info.getProgress(), info.getServerId(), info.getServerName(), info.getStartTime(), info.getErrorMsg());
    }

    private void completeTask(Integer taskIdInt) {
        LambdaUpdateWrapper<ModTasks> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ModTasks::getTaskId, taskIdInt)
                .set(ModTasks::getStatus, STATUS_COMPLETED)
                .set(ModTasks::getProgress, 100)
                .set(ModTasks::getEndTime, LocalDateTime.now())
                .set(ModTasks::getErrorMsg, null);
        this.update(updateWrapper);
    }

    private void failTask(Integer taskIdInt, String taskId, String errorMsg) {
        LambdaUpdateWrapper<ModTasks> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ModTasks::getTaskId, taskIdInt)
                .set(ModTasks::getStatus, STATUS_FAILED)
                .set(ModTasks::getEndTime, LocalDateTime.now())
                .set(ModTasks::getErrorMsg, errorMsg);
        this.update(updateWrapper);
        logger.warn("任务失败 taskId={}, error={}", taskId, errorMsg);
    }

    private Server selectTargetServer(ModTasks task) {
        List<Server> servers = monitorServerMapper.selectList(new LambdaQueryWrapper<>());
        if (servers == null || servers.isEmpty()) {
            return null;
        }

        int requiredCpu = task.getCpuCoreNeed() == null ? 1 : task.getCpuCoreNeed();
        int requiredMemory = task.getMemoryNeed() == null ? 1 : task.getMemoryNeed();

        return servers.stream()
                .filter(server -> isServerActive(server.getStatus()))
                .filter(server -> server.getCpuCores() == null || server.getCpuCores() >= requiredCpu)
                .filter(server -> server.getMemory() == null || server.getMemory() >= requiredMemory)
                // 获取服务器当前CPU负载，负载未知或无法解析时视为最重负载，优先分配已知负载较轻的服务器
                .min(Comparator.comparingDouble(this::cpuLoadOrMax))
                .orElse(null);
    }

    private boolean isServerActive(String status) {
        if (!StringUtils.hasText(status)) {
            return false;
        }
        return "running".equalsIgnoreCase(status)
                || "idle".equalsIgnoreCase(status)
                || "Running".equalsIgnoreCase(status);
    }

    private double cpuLoadOrMax(Server server) {
        // 如果服务器不存在，服务器CPU负载为空则返回最大负载
        if (server == null || !StringUtils.hasText(server.getCpuUsage()) || "N/A".equalsIgnoreCase(server.getCpuUsage())) {
            return Double.MAX_VALUE;
        }
        try {
            return Double.parseDouble(server.getCpuUsage());
        } catch (NumberFormatException e) {
            return Double.MAX_VALUE;
        }
    }

    private boolean dispatchPendingTasks() {
        Page<ModTasks> page = new Page<>(1, 20);
        // 1. 从数据库中拉取20条未被执行的任务，尝试重新启动pending的任务
        // TODO: 这里的调度策略非常简单，直接拉取前20条pending任务尝试启动，后续可以改成更智能的调度算法，比如优先级调度、基于资源需求和服务器负载的调度等
        // TODO: 这里并未按照任务类型进行拉取，如果后续有不同类型的求解器资源限制，可以改成按照求解器类型分别拉取待调度任务
        // TODO: 当出现大量任务执行时会重复刷库，后续必须改用MQ来替代
        // TODO: 如果一个任务执行所需的资源太大，服务器无法支撑时就会造成任务调用的死循环，这个必须优化
        List<ModTasks> pendingTasks = baseMapper.selectPendingTasksForSchedule(page, null).getRecords();
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return false;
        }
        boolean launched = false;
        // 2. 循环启动拉取到的任务
        for (ModTasks pendingTask : pendingTasks) {
            if (pendingTask.getTaskId() == null) {
                continue;
            }
            ApiResult<MonitorVO> result = startProgram("task_" + pendingTask.getTaskId());
            if (result.getCode() == 200 && result.getData() != null && STATUS_RUNNING.equals(result.getData().getStatus())) {
                launched = true;
            }
            if (result.getCode() != 200) {
                logger.debug("调度待执行任务未启动: taskId={}, msg={}", pendingTask.getTaskId(), result.getMessage());
            }
        }
        return launched;
    }

    private void dispatchPendingTasksWithBackoff() {
        long now = System.currentTimeMillis();
        synchronized (idleDispatchBackoffLock) {
            if (now < nextIdleDispatchAtMillis) {
                return;
            }
        }

        boolean launched = dispatchPendingTasks();
        if (launched || !processMap.isEmpty()) {
            resetIdleDispatchBackoff();
            return;
        }

        synchronized (idleDispatchBackoffLock) {
            long currentBackoffSeconds = idleDispatchBackoffSeconds;
            nextIdleDispatchAtMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(currentBackoffSeconds);
            idleDispatchBackoffSeconds = Math.min(currentBackoffSeconds * 2, MAX_IDLE_BACKOFF_SECONDS);
            logger.debug("processMap 为空且未调度到待执行任务，下次将在 {} 秒后重试调度", currentBackoffSeconds);
        }
    }

    private void resetIdleDispatchBackoff() {
        synchronized (idleDispatchBackoffLock) {
            nextIdleDispatchAtMillis = 0L;
            idleDispatchBackoffSeconds = INITIAL_IDLE_BACKOFF_SECONDS;
        }
    }

    private boolean isNotPending(String status) {
        return !(STATUS_PENDING.equals(status) || "未启动".equals(status));
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_FAILED;
        }
        return switch (status) {
            case "未启动" -> STATUS_PENDING;
            case "仿真中" -> STATUS_RUNNING;
            case "已结束" -> STATUS_COMPLETED;
            default -> status;
        };
    }

    private int resolveMaxParallel(String solverType) {
        StringUtils.hasText(solverType);
        return Math.max(defaultMaxParallel, 1);
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
