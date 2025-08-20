package com.scut.industrial_software.controller.External;

import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * 外部程序执行控制器
 */
@RestController
@RequestMapping("/external")
public class ExternalProgramController {

    /**
     * 生成run.bat文件
     * 根据前端请求，生成包含exe路径、输出目录和thudynamics命令的bat文件
     */
    @PostMapping("/generateBat")
    public Map<String, Object> generateRunBat(@RequestBody Map<String, String> params) {
        String taskId = params.get("taskId");          // 任务ID，用于区分不同任务
        String insPath = params.get("insPath");        // main.ins文件路径
        String outputDir = params.get("outputDir");    // 求解器产生文件的地址
        
        if (taskId == null || insPath == null || outputDir == null) {
            return Map.of("code", -1, "message", "缺少必要参数：taskId、insPath和outputDir");
        }
        
        // 硬编码的求解器exe地址（后续可配置化）
        String exePath = "D:\\IDEA_Project\\industrial_software_0813\\industrial_software\\GETexe\\dist\\main.exe";
        
        // 生成bat内容：exe地址 + 输出目录 + thudynamics main.ins
        String batContent = "@echo off\n\n" +
                           "REM Task ID: " + taskId + "\n" +
                           "set PATH=" + new File(exePath).getParent() + ";%PATH%\n" +
                           "cd /d \"" + outputDir + "\"\n" +
                           "thudynamics main.ins";
        
        // bat文件名包含任务ID，放在main.ins文件所在的目录
        File insFile = new File(insPath);
        String batFileName = "run_task_" + taskId + ".bat";
        File batFile = new File(insFile.getParent(), batFileName);
        
        try (FileWriter writer = new FileWriter(batFile)) {
            writer.write(batContent);
            return Map.of(
                "code", 200, 
                "message", "run.bat生成成功", 
                "data", Map.of(
                    "taskId", taskId,
                    "batFilePath", batFile.getAbsolutePath(),
                    "batFileName", batFileName
                )
            );
        } catch (IOException e) {
            return Map.of("code", -1, "message", "生成run.bat失败: " + e.getMessage());
        }
    }
}
