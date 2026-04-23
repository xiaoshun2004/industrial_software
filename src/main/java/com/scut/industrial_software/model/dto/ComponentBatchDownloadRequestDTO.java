package com.scut.industrial_software.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ComponentBatchDownloadRequestDTO {

    private List<Integer> componentIds;
}
