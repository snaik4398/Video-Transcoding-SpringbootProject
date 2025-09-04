package com.transcodeservice.transcoding.service;

import com.transcodeservice.common.dto.TranscodingJobDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FFmpegService {
    
    @Value("${transcoding.ffmpeg.path:/usr/local/bin/ffmpeg}")
    private String ffmpegPath;
    
    @Value("${transcoding.ffmpeg.probe-path:/usr/local/bin/ffprobe}")
    private String ffprobePath;
    
    public boolean transcode(File inputFile, File outputFile, TranscodingJobDto.OutputSettingsDto settings) {
        try {
            List<String> command = buildFFmpegCommand(inputFile, outputFile, settings);
            
            log.info("Executing FFmpeg command: {}", String.join(" ", command));
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // Read output for logging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Transcoding completed successfully for file: {}", inputFile.getName());
                return true;
            } else {
                log.error("Transcoding failed with exit code: {} for file: {}", exitCode, inputFile.getName());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during transcoding: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public String getVideoInfo(File inputFile) {
        try {
            List<String> command = List.of(
                ffprobePath,
                "-v", "quiet",
                "-print_format", "json",
                "-show_format",
                "-show_streams",
                inputFile.getAbsolutePath()
            );
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                return output.toString();
            }
            
        } catch (Exception e) {
            log.error("Error getting video info: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private List<String> buildFFmpegCommand(File inputFile, File outputFile, TranscodingJobDto.OutputSettingsDto settings) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(inputFile.getAbsolutePath());
        
        // Video settings
        if (settings.getVideoCodec() != null) {
            command.add("-c:v");
            command.add(settings.getVideoCodec());
        }
        
        if (settings.getVideoBitrate() != null) {
            command.add("-b:v");
            command.add(settings.getVideoBitrate());
        }
        
        if (settings.getResolution() != null) {
            command.add("-vf");
            command.add("scale=" + settings.getResolution());
        }
        
        if (settings.getFrameRate() != null) {
            command.add("-r");
            command.add(settings.getFrameRate().toString());
        }
        
        // Audio settings
        if (settings.getAudioCodec() != null) {
            command.add("-c:a");
            command.add(settings.getAudioCodec());
        }
        
        if (settings.getAudioBitrate() != null) {
            command.add("-b:a");
            command.add(settings.getAudioBitrate());
        }
        
        // Output format
        if (settings.getOutputFormat() != null) {
            command.add("-f");
            command.add(settings.getOutputFormat());
        }
        
        // Performance settings
        command.add("-preset");
        command.add("medium");
        
        command.add("-y"); // Overwrite output file
        
        command.add(outputFile.getAbsolutePath());
        
        return command;
    }
    
    public String getFFmpegVersion() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(ffmpegPath, "-version");
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                return line != null ? line : "Unknown";
            }
            
        } catch (Exception e) {
            log.error("Error getting FFmpeg version: {}", e.getMessage(), e);
            return "Error";
        }
    }
    
    public String getFFprobeVersion() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(ffprobePath, "-version");
            Process process = processBuilder.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                return line != null ? line : "Unknown";
            }
            
        } catch (Exception e) {
            log.error("Error getting FFprobe version: {}", e.getMessage(), e);
            return "Error";
        }
    }
}
