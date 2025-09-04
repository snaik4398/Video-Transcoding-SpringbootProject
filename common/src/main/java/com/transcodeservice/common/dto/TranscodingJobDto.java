package com.transcodeservice.common.dto;

import com.transcodeservice.common.entity.TranscodingJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscodingJobDto {
    
    @NotBlank(message = "Input file ID is required")
    private String inputFileId;
    
    private String outputFilename;
    
    @NotNull(message = "Output settings are required")
    private OutputSettingsDto outputSettings;
    
    private TranscodingJob.JobPriority priority = TranscodingJob.JobPriority.NORMAL;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputSettingsDto {
        private String videoCodec = "libx264";
        private String audioCodec = "aac";
        private String outputFormat = "mp4";
        private String videoBitrate = "1500k";
        private String audioBitrate = "128k";
        private String resolution = "1280x720";
        private Integer frameRate = 30;
        private String processingMode = "CPU";
    }
}
