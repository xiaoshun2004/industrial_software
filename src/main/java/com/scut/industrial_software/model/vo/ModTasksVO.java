package com.scut.industrial_software.model.vo;

import com.scut.industrial_software.model.entity.ModTasks;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModTasksVO {

    private String taskId;

    private String taskName;

    private String creator;

    private String simulationStage;

    private String type;

    private String computerResource;

    private LocalDateTime creation_time;

    private String status;

    public ModTasksVO(ModTasks modTasks) {
        this.taskId = String.format("task_%03d",modTasks.getTaskId());
        this.taskName = modTasks.getTaskName();
        this.creator = modTasks.getCreator();
        this.simulationStage = modTasks.getSimulationStage();
        this.type = modTasks.getType();
        this.computerResource = modTasks.getComputeResource();
        this.creation_time = modTasks.getCreationTime();
        this.status = modTasks.getStatus();
    }
}
