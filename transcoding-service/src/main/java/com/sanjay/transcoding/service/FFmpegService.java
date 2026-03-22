package com.sanjay.transcoding.service;

import com.sanjay.common.dto.TranscodingJobDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FFmpegService {

    @Value("${transcoding.ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${transcoding.ffmpeg.probe-path:/usr/bin/ffprobe}")
    private String ffprobePath;

    @Value("${transcoding.gpu.enabled:false}")
    private boolean gpuEnabled;

    @Value("${transcoding.gpu.type:none}")
    private String gpuType;

    @Value("${transcoding.gpu.cuda-devices:0}")
    private String cudaDevices;

    @Value("${transcoding.gpu.render-device:/dev/dri/renderD128}")
    private String renderDevice;

    private static final long TRANSCODING_TIMEOUT_HOURS = 4;

    private static final Map<String, String> CPU_TO_NVIDIA_CODEC = Map.of(
            "libx264", "h264_nvenc",
            "libx265", "hevc_nvenc",
            "h264", "h264_nvenc",
            "hevc", "hevc_nvenc"
    );

    private static final Map<String, String> CPU_TO_QSV_CODEC = Map.of(
            "libx264", "h264_qsv",
            "libx265", "hevc_qsv",
            "h264", "h264_qsv",
            "hevc", "hevc_qsv"
    );

    private boolean isNvidia() {
        return "nvidia".equalsIgnoreCase(gpuType);
    }

    private boolean isIntel() {
        return "intel".equalsIgnoreCase(gpuType);
    }

    private Map<String, String> getGpuCodecMap() {
        if (isNvidia()) return CPU_TO_NVIDIA_CODEC;
        if (isIntel()) return CPU_TO_QSV_CODEC;
        return Map.of();
    }

    public boolean transcode(File inputFile, File outputFile, TranscodingJobDto.OutputSettingsDto settings) {
        try {
            if (!inputFile.exists()) {
                log.error("Input file does not exist: {}", inputFile.getAbsolutePath());
                return false;
            }

            boolean useGpu = gpuEnabled && isGpuModeRequested(settings);
            List<String> command = buildFFmpegCommand(inputFile, outputFile, settings, useGpu);

            log.info("Executing FFmpeg command (GPU={}): {}", useGpu, String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                    if (line.contains("frame=") || line.contains("time=")) {
                        log.debug("FFmpeg progress: {}", line.trim());
                    }
                }
            }

            boolean finished = process.waitFor(TRANSCODING_TIMEOUT_HOURS, TimeUnit.HOURS);
            if (!finished) {
                log.error("FFmpeg process timed out after {} hours for file: {}", TRANSCODING_TIMEOUT_HOURS, inputFile.getName());
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                if (outputFile.exists() && outputFile.length() > 0) {
                    log.info("Transcoding completed (GPU={}): {} -> {} (output size: {} bytes)",
                            useGpu, inputFile.getName(), outputFile.getName(), outputFile.length());
                    return true;
                } else {
                    log.error("FFmpeg exited successfully but output file is missing or empty: {}", outputFile.getAbsolutePath());
                    return false;
                }
            } else {
                String truncatedOutput = outputLog.substring(0, Math.min(outputLog.length(), 2000));

                if (useGpu && exitCode != 0) {
                    log.warn("GPU transcoding failed (exit code {}), falling back to CPU for file: {}", exitCode, inputFile.getName());
                    return transcodeCpuFallback(inputFile, outputFile, settings);
                }

                log.error("FFmpeg failed with exit code {} for file: {}\nOutput:\n{}", exitCode, inputFile.getName(), truncatedOutput);
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FFmpeg transcoding interrupted for file: {}", inputFile.getName());
            return false;
        } catch (Exception e) {
            log.error("Error during transcoding of {}: {}", inputFile.getName(), e.getMessage(), e);
            return false;
        }
    }

    private boolean transcodeCpuFallback(File inputFile, File outputFile, TranscodingJobDto.OutputSettingsDto settings) {
        try {
            log.info("CPU fallback: re-running transcoding with software codecs for {}", inputFile.getName());
            List<String> command = buildFFmpegCommand(inputFile, outputFile, settings, false);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("frame=") || line.contains("time=")) {
                        log.debug("FFmpeg CPU fallback progress: {}", line.trim());
                    }
                }
            }

            boolean finished = process.waitFor(TRANSCODING_TIMEOUT_HOURS, TimeUnit.HOURS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0 && outputFile.exists() && outputFile.length() > 0;

        } catch (Exception e) {
            log.error("CPU fallback also failed for {}: {}", inputFile.getName(), e.getMessage(), e);
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

                boolean finished = process.waitFor(30, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    return null;
                }

                return output.toString();
            }

        } catch (Exception e) {
            log.error("Error getting video info for {}: {}", inputFile.getName(), e.getMessage(), e);
            return null;
        }
    }

    private List<String> buildFFmpegCommand(File inputFile, File outputFile,
                                             TranscodingJobDto.OutputSettingsDto settings,
                                             boolean useGpu) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-y");

        if (useGpu) {
            if (isNvidia()) {
                command.add("-hwaccel");
                command.add("cuda");
                command.add("-hwaccel_device");
                command.add(cudaDevices);
                command.add("-hwaccel_output_format");
                command.add("cuda");
            } else if (isIntel()) {
                command.add("-hwaccel");
                command.add("qsv");
                command.add("-hwaccel_output_format");
                command.add("qsv");
                command.add("-init_hw_device");
                command.add("qsv=hw,child_device=" + renderDevice);
            }
        }

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        String requestedVideoCodec = settings.getVideoCodec();
        if (requestedVideoCodec != null && !requestedVideoCodec.isEmpty()) {
            if (useGpu) {
                Map<String, String> codecMap = getGpuCodecMap();
                String gpuCodec = codecMap.getOrDefault(requestedVideoCodec, requestedVideoCodec);
                command.add("-c:v");
                command.add(gpuCodec);
                log.info("Using {} GPU video codec: {} (requested: {})", gpuType, gpuCodec, requestedVideoCodec);
            } else {
                command.add("-c:v");
                command.add(requestedVideoCodec);
            }
        }

        if (settings.getVideoBitrate() != null && !settings.getVideoBitrate().isEmpty()) {
            command.add("-b:v");
            command.add(settings.getVideoBitrate());
        }

        if (settings.getResolution() != null && !settings.getResolution().isEmpty()) {
            String res = settings.getResolution();
            if (res.contains("x")) {
                String[] parts = res.split("x");
                if (useGpu && isNvidia()) {
                    command.add("-vf");
                    command.add("scale_cuda=" + parts[0] + ":" + parts[1]);
                } else if (useGpu && isIntel()) {
                    command.add("-vf");
                    command.add("scale_qsv=w=" + parts[0] + ":h=" + parts[1]);
                } else {
                    command.add("-vf");
                    command.add("scale=" + parts[0] + ":" + parts[1]);
                }
            }
        }

        if (settings.getFrameRate() != null && settings.getFrameRate() > 0) {
            command.add("-r");
            command.add(settings.getFrameRate().toString());
        }

        if (settings.getAudioCodec() != null && !settings.getAudioCodec().isEmpty()) {
            command.add("-c:a");
            command.add(settings.getAudioCodec());
        }

        if (settings.getAudioBitrate() != null && !settings.getAudioBitrate().isEmpty()) {
            command.add("-b:a");
            command.add(settings.getAudioBitrate());
        }

        if (settings.getOutputFormat() != null && !settings.getOutputFormat().isEmpty()) {
            command.add("-f");
            command.add(settings.getOutputFormat());
        }

        if (useGpu && isNvidia()) {
            command.add("-preset");
            command.add("p4");
            command.add("-tune");
            command.add("hq");
            command.add("-rc");
            command.add("vbr");
        } else if (useGpu && isIntel()) {
            command.add("-preset");
            command.add("medium");
            command.add("-global_quality");
            command.add("25");
        } else {
            command.add("-preset");
            command.add("medium");
        }

        command.add("-movflags");
        command.add("+faststart");

        command.add(outputFile.getAbsolutePath());

        return command;
    }

    private boolean isGpuModeRequested(TranscodingJobDto.OutputSettingsDto settings) {
        String mode = settings.getProcessingMode();
        if (mode != null && mode.equalsIgnoreCase("GPU")) {
            return true;
        }
        String codec = settings.getVideoCodec();
        if (codec != null && !getGpuCodecMap().isEmpty() && getGpuCodecMap().containsKey(codec)) {
            return true;
        }
        return false;
    }

    public boolean isGpuAvailable() {
        if (!gpuEnabled) {
            return false;
        }
        if (isNvidia()) {
            return checkNvidiaGpu();
        } else if (isIntel()) {
            return checkIntelGpu();
        }
        return false;
    }

    private boolean checkNvidiaGpu() {
        try {
            ProcessBuilder pb = new ProcessBuilder("nvidia-smi", "--query-gpu=name", "--format=csv,noheader");
            Process process = pb.start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkIntelGpu() {
        try {
            ProcessBuilder pb = new ProcessBuilder("vainfo");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0 && output.toString().contains("VAEntrypoint");
        } catch (Exception e) {
            return false;
        }
    }

    public String getGpuInfo() {
        if (!gpuEnabled) {
            return "GPU acceleration disabled";
        }
        if (isNvidia()) {
            return getNvidiaGpuInfo();
        } else if (isIntel()) {
            return getIntelGpuInfo();
        }
        return "Unknown GPU type: " + gpuType;
    }

    private String getNvidiaGpuInfo() {
        try {
            ProcessBuilder pb = new ProcessBuilder("nvidia-smi",
                    "--query-gpu=name,memory.total,memory.free,utilization.gpu,driver_version",
                    "--format=csv,noheader");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                boolean finished = process.waitFor(10, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                }
                return line != null ? "NVIDIA: " + line : "NVIDIA GPU info not available";
            }
        } catch (Exception e) {
            return "NVIDIA GPU not detected: " + e.getMessage();
        }
    }

    private String getIntelGpuInfo() {
        try {
            ProcessBuilder pb = new ProcessBuilder("vainfo");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder info = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Driver version") || line.contains("vainfo")) {
                        info.append(line.trim()).append("; ");
                    }
                }
            }
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
            }
            String result = info.toString().trim();
            return result.isEmpty() ? "Intel QSV: device at " + renderDevice : "Intel QSV: " + result;
        } catch (Exception e) {
            return "Intel GPU not detected: " + e.getMessage();
        }
    }

    public String getGpuType() {
        return gpuType;
    }

    public String getFFmpegVersion() {
        return getToolVersion(ffmpegPath);
    }

    public String getFFprobeVersion() {
        return getToolVersion(ffprobePath);
    }

    private String getToolVersion(String toolPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(toolPath, "-version");
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                boolean finished = process.waitFor(10, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                }
                return line != null ? line : "Unknown";
            }

        } catch (Exception e) {
            log.warn("Could not get version for {}: {}", toolPath, e.getMessage());
            return "Not available";
        }
    }
}
