package com.sanjay.transcoding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfo {
    private String cpuInfo;
    private String gpuInfo;
    private Long totalMemory;
    private Long availableMemory;
    private Integer cpuCores;
    private Double cpuUsage;
    private Double memoryUsage;
    private String ffmpegVersion;
    private String ffprobeVersion;
    private Boolean gpuAccelerationEnabled;
    private String gpuType;
    private Long activeJobs;
    private Integer maxConcurrentJobs;
    private String workerId;
}
