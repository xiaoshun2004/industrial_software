package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.common.api.ApiErrorCode;
import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.common.exception.ApiException;
import com.scut.industrial_software.model.dto.ProcessInfoDTO;
import com.scut.industrial_software.model.vo.MonitorVO;
import com.scut.industrial_software.service.IMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

import static com.scut.industrial_software.utils.WindowFocusUtil.bringProcessWindowToFront;

@Service
public class MonitorServiceImpl implements IMonitorService {

    // 使用Sigar获取系统信息
    private final String programPath = "D:/exe/GETexe4/dist/main/main.exe";
    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    // 使用ConcurrentHashMap存储进程信息，支持多线程访问
    private final Map<String, ProcessInfoDTO> processMap = new ConcurrentHashMap<>();
    // private final List<MonitorVO> monitoringData = new CopyOnWriteArrayList<>();        // CopyOnWriteArrayList通过写时复制实现线程安全

    @Override
    public ApiResult<MonitorVO> startProgram(String taskId) {
        try{
            // 清空之前的监控数据
            // monitoringData.clear();

            // 检查程序路径是否存在
            File file = new File(programPath);
            if (!file.exists()) {
                logger.error("程序路径不正确: {}", programPath);
                return ApiResult.failed("程序路径不正确: " + programPath);
            }

            // 启动exe程序（异步），不需要立即等待其返回值
            launchExe(taskId, programPath);

            MonitorVO data = new MonitorVO(taskId,"仿真中");

            return ApiResult.success(data);

        }  catch(Exception e){
            logger.error("程序启动失败: {}", e.getMessage());
            return ApiResult.failed("程序调用失败: " + e.getMessage());
        }
    }

    /**
     * 使用自定义线程池启动外部程序
     */
    public CompletableFuture<String> launchExe(String taskId, String programPath){
        return CompletableFuture.supplyAsync(() -> {
            try{
                ProcessBuilder pb = new ProcessBuilder(programPath);
                Process process = pb.start();

                Long pid = process.pid();

                // 双重验证进程是否仍在运行
                if (!process.isAlive()){
                    logger.error("程序启动后立即退出，可能启动失败");
                    throw new ApiException(ApiErrorCode.TASK_START_FAILED);
                }

                // 存储进程信息
                ProcessInfoDTO processInfo = new ProcessInfoDTO(process, pid, LocalDateTime.now());

                processMap.put(taskId, processInfo);

                logger.info("程序{}已启动，PID={}", programPath, pid);

                // 使用新的窗口管理工具类将窗口置顶
                boolean windowShown = bringProcessWindowToFront(pid,5000);
                if (!windowShown) {
                    logger.warn("未能成功将程序窗口置顶，PID={}", pid);
                } else {
                    logger.info("程序窗口已置顶显示，PID={}", pid);
                }

                // 使用自定义线程池监控进程执行
                monitorProcessCompletion(process, taskId);

                return "success";

            }catch(IOException e){
                logger.error("启动程序失败: {}", e.getMessage());
                throw new ApiException(ApiErrorCode.TASK_START_FAILED);
            }
        }, taskExecutor);
    }

    /**
     * 监控进程执行完成
     */
    private void monitorProcessCompletion(Process process, String taskId){
        CompletableFuture.runAsync(() -> {
            try {
                int exitCode = process.waitFor();
                ProcessInfoDTO processInfo = processMap.get(taskId);
                // 如果退出码为1，则认为程序是被中断的，由于在stopProgram中已经设置状态为interrupted，并且是原子性的，所以不会执行这里的completed逻辑
                if (processInfo != null && processInfo.updateStatus("running", "completed")) {
                    processInfo.setEndTime(LocalDateTime.now());
                    processInfo.setExitCode(exitCode);
                    processInfo.setStatus("completed");
                    logger.info("进程执行完成，PID: {}, 退出码: {}", processInfo.getPid(), exitCode);
                }
                // 处理已经被中断的进程信息
                if (processInfo != null && processInfo.getStatus().equals("interrupted")) {
                    logger.info("进程已被中断，PID: {}", processInfo.getPid());
                }
                // TODO:可以在这里添加执行完成后的回调逻辑

            } catch (InterruptedException e) {
                logger.warn("进程监控被中断，PID: {}", process.pid());
                Thread.currentThread().interrupt();
            } finally {
                // 清理资源
                if (process != null) {
                    process.destroy();
                }
            }
        }, taskExecutor);
    }

    @Override
    public ApiResult<MonitorVO> stopProgram(String taskId) {
        try{
            ProcessInfoDTO processInfo = processMap.get(taskId);
            if (processInfo == null){
                return ApiResult.failed("进程不存在");
            }
            // 因为前端轮询存在延迟，可能进程已经结束，这里需要检查进程状态
            if (processInfo.getStatus().equals("completed") || processInfo.getStatus().equals("interrupted")){
                return ApiResult.success(new MonitorVO(taskId,"已结束"));
            }
            Process process = processInfo.getProcess();
            if (process.isAlive()){
                if (processInfo.updateStatus("running", "interrupted")){
                    process.destroy();
                    processInfo.setEndTime(LocalDateTime.now());
                    logger.info("进程已终止，PID={}", processInfo.getPid());
                    return ApiResult.success(new MonitorVO(taskId,"已结束"));
                }else{
                    return ApiResult.success(new MonitorVO(taskId,"已结束"));
                }
            } else {
                return ApiResult.success(new MonitorVO(taskId,"已结束"));
            }
        } catch (Exception e) {
            logger.error("中断进程失败, 错误: {}", e.getMessage());
            return ApiResult.failed("中断进程失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<MonitorVO> getProgramStatus(String taskId) {
        ProcessInfoDTO processInfo = processMap.get(taskId);
        if (processInfo == null){
            return ApiResult.failed("进程不存在");
        }

        String status = processInfo.getStatus();

        if (status.equals("running")){
            return ApiResult.success(new MonitorVO(taskId,"仿真中"));
        }

        return ApiResult.success(new MonitorVO(taskId,"已结束"));      // status: running, interrupted, completed
    }

    // 每2秒执行一次监控
    @Scheduled(fixedRate = 2000)
    public void scheduledMonitor(){
        // 挑选processMap中第一个正在运行的进程进行监控（仅作测试）
        Long currentPid = null;
        for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
            ProcessInfoDTO processInfo = entry.getValue();
            if (processInfo.getStatus().equals("running")) {
                currentPid = processInfo.getPid();
                break;
            }
        }
        if (currentPid != null){
            try{
                monitorProgram(currentPid);
            }catch(Exception e){
                logger.error("监控失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void monitorProgram(Long pid) {
        // 使用OSHI监控库获取系统信息
        try {
            // 获取进程信息
            SystemInfo systemInfo = new SystemInfo();
            OperatingSystem os = systemInfo.getOperatingSystem();

            // 获取进程信息
            OSProcess process = os.getProcess(pid.intValue());

            // 先判断进程是否存在（OSHI 在找不到进程时会返回 null）
            if (process == null) {
                logger.warn("监控：进程 {} 不存在或已结束", pid);
                // 如果我们在 processMap 中有对应的记录，标记为已完成以保持状态一致
                for (Map.Entry<String, ProcessInfoDTO> entry : processMap.entrySet()) {
                    ProcessInfoDTO info = entry.getValue();
                    if (info != null && info.getPid() != null && info.getPid().equals(pid)) {
                        // 尝试将 running -> completed（与 monitorProcessCompletion 中的逻辑一致）
                        if (info.updateStatus("running", "completed")) {
                            info.setEndTime(LocalDateTime.now());
                            info.setExitCode(-1); // 未获取到真实退出码，使用 -1 表示未知
                            info.setStatus("completed");
                            logger.info("将 processMap 中的任务标记为 completed，taskId={}, pid={}", entry.getKey(), pid);
                        }
                        break;
                    }
                }
                return;
            }

            // 从 OSHI 读取 CPU 与内存（注意返回值范围和可能的 -1 值）
            double cpuLoad = process.getProcessCpuLoadCumulative(); // 可能返回 -1 或 0..1
            if (cpuLoad >= 0 && cpuLoad <= 1) {
                cpuLoad = cpuLoad * 100.0; // 转换为百分比
            } else if (cpuLoad < 0) {
                cpuLoad = Double.NaN; // 无效值
            }

            long rssBytes = process.getResidentSetSize();
            double memMB = rssBytes / 1024.0 / 1024.0;

            // 日志记录并格式化输出
            String cpuStr = Double.isNaN(cpuLoad) ? "N/A" : String.format("%.2f", cpuLoad);
            String memStr = String.format("%.2f", memMB);
            String procName = process.getName() != null ? process.getName() : "<unknown>";

            logger.info("监控结果: PID={}, 名称={}, CPU={}%, 内存={}MB, 更新时间={}", pid, procName, cpuStr, memStr, LocalDateTime.now());

        } catch (Exception e) {
            logger.error("监控进程{}失败: {}", pid, e.getMessage());
            throw e;
        }
    }

    // 辅助方法：获取进程PID (JDK 8兼容)
    /*
    private Long getPidFromProcess(Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            return (Long) pidField.get(process);
        } catch (Exception e) {
            return null;
        }
    }
    */
}
