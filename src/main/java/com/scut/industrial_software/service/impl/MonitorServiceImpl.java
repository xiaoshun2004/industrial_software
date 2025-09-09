package com.scut.industrial_software.service.impl;

import com.scut.industrial_software.common.api.ApiResult;
import com.scut.industrial_software.model.vo.MonitorVO;
import com.scut.industrial_software.service.IMonitorService;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MonitorServiceImpl implements IMonitorService {

    // 使用Sigar获取系统信息
    private final String programPath = "D:/exe/GETexe4/dist/main.exe";
    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
    private Process process;
    private Long currentPid;
    private volatile boolean isMonitoring = false;                // volatile确保线程间可见性
    private List<MonitorVO> monitoringData = new CopyOnWriteArrayList<>();        // CopyOnWriteArrayList通过写时复制实现线程安全

    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> startProgram() {
        try{
            // 清空之前的监控数据
            monitoringData.clear();

            ProcessBuilder pb = new ProcessBuilder(programPath);
            process = pb.start();

            // 获取进程ID
            currentPid = process.pid();

            // 启动监控
            isMonitoring = true;
            logger.info("程序已启动: 进程ID=" + currentPid);

            // 等待程序执行完成
            int exitCode = process.waitFor();

            // 程序执行完毕后停止监控
            isMonitoring = false;
            return CompletableFuture.completedFuture("执行成功: 进程ID=" + currentPid + ", 退出状态=" + exitCode);

        }catch(IOException|InterruptedException e){
            isMonitoring = false;
            logger.error("程序执行失败: " + e.getMessage());
            return CompletableFuture.completedFuture("程序执行失败: " + e.getMessage());
        }
    }

    // 每2秒执行一次监控
    @Scheduled(fixedRate = 2000)
    public void scheduledMonitor(){
        if (isMonitoring && currentPid != null){
            try{
                MonitorVO data = monitorProgram(currentPid);
                if (data != null){
                    monitoringData.add(data);
                    logger.info("监控数据已记录: " + data);
                } else {
                    logger.warn("无法获取进程信息，可能进程已结束: PID=" + currentPid);
                }
            }catch(Exception e){
                logger.error("监控失败: " + e.getMessage());
            }
        }
    }

    @Override
    public MonitorVO monitorProgram(Long pid) {
        // 使用OSHI监控库获取系统信息
        try {
            // 获取进程信息
            /*Sigar sigar = new Sigar();
            ProcCpu procCpu = sigar.getProcCpu(pid);
            ProcMem procMem = sigar.getProcMem(pid);
            */
            SystemInfo systemInfo = new SystemInfo();
            OperatingSystem os = systemInfo.getOperatingSystem();

            // 获取进程信息
            OSProcess process = os.getProcess(pid.intValue());
            if (process == null) {
                isMonitoring = false;
                logger.warn("进程{}不存在，停止监控", pid);
                return null;
            }
            // 封装返回结果
            MonitorVO monitorVO = new MonitorVO();
            monitorVO.setPid(pid);
            //monitorVO.setCpuUsage(procCpu.getPercent());             // 单位：%
            monitorVO.setCpuUsage(process.getProcessCpuLoadCumulative());   // 单位：%
            //monitorVO.setMemUsage(procMem.getResident()/1024.0/1024.0);     // 单位：MB
            monitorVO.setMemUsage(process.getResidentSetSize() / 1024.0 / 1024.0);   // 获取主流集大小，单位：MB
            monitorVO.setTimestamp(LocalDateTime.now());

            logger.info("监控结果: " + monitorVO);
            return monitorVO;

        } catch (Exception e) {
            logger.error("监控进程{}失败: {}", pid, e.getMessage());
            return null;
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
