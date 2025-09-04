
package com.transcoding.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video Transcoding Application
 * 
 * This application provides comprehensive video transcoding functionality with:
 * - CPU/GPU processing mode selection
 * - System information logging
 * - Configurable video parameters (bitrate, resolution, frame rate, codecs)
 * - Multiple output format support
 * - Progress monitoring and error handling
 */
@SpringBootApplication
public class VideoTranscodingApplication {

    private static final Logger logger = LoggerFactory.getLogger(VideoTranscodingApplication.class);

    public static void main(String[] args) {
        // Print system information before starting
        SystemInfoService.printSystemInfo();
        
        SpringApplication.run(VideoTranscodingApplication.class, args);
    }

    @Component
    public static class TranscodingRunner implements CommandLineRunner {
        
        private final TranscodingService transcodingService;
        
        public TranscodingRunner(TranscodingService transcodingService) {
            this.transcodingService = transcodingService;
        }

        @Override
        public void run(String... args) throws Exception {
            if (args.length < 2) {
                printUsage();
                return;
            }

            String inputFile = args[0];
            String outputFile = args[1];
            
            // Parse additional arguments or use defaults
            TranscodingConfig config = parseArguments(args);
            
            logger.info("Starting transcoding process...");
            logger.info("Input: {}", inputFile);
            logger.info("Output: {}", outputFile);
            logger.info("Configuration: {}", config);
            
            try {
                boolean success = transcodingService.transcodeVideo(inputFile, outputFile, config);
                if (success) {
                    logger.info("Transcoding completed successfully!");
                } else {
                    logger.error("Transcoding failed!");
                    System.exit(1);
                }
            } catch (Exception e) {
                logger.error("Error during transcoding: ", e);
                System.exit(1);
            }
        }

        private void printUsage() {
            System.out.println("\nUsage: java -jar transcoding-service.jar <input_file> <output_file> [options]");
            System.out.println("\nOptions:");
            System.out.println("  --video-codec <codec>      Video codec (libx264, libx265, libvpx-vp9, libaom-av1) [default: libx264]");
            System.out.println("  --audio-codec <codec>      Audio codec (aac, mp3, opus, libvorbis) [default: aac]");
            System.out.println("  --video-bitrate <bitrate>  Video bitrate (e.g., 2000k, 5M) [default: 2000k]");
            System.out.println("  --audio-bitrate <bitrate>  Audio bitrate (e.g., 128k, 256k) [default: 128k]");
            System.out.println("  --resolution <resolution>  Output resolution (e.g., 1920x1080, 1280x720) [default: original]");
            System.out.println("  --frame-rate <fps>         Frame rate (e.g., 24, 30, 60) [default: original]");
            System.out.println("  --format <format>          Output format (mp4, webm, avi, mkv) [default: mp4]");
            System.out.println("  --processing-mode <mode>   Processing mode (CPU, GPU) [default: CPU]");
            System.out.println("  --quality <preset>         Quality preset (ultrafast, fast, medium, slow, veryslow) [default: medium]");
            System.out.println("  --threads <number>         Number of threads for CPU processing [default: auto]");
            System.out.println("\nExamples:");
            System.out.println("  java -jar app.jar input.mp4 output.mp4");
            System.out.println("  java -jar app.jar input.mp4 output.mp4 --video-codec libx265 --resolution 1280x720");
            System.out.println("  java -jar app.jar input.mp4 output.mp4 --processing-mode GPU --video-bitrate 5M");
        }

        private TranscodingConfig parseArguments(String[] args) {
            TranscodingConfig.TranscodingConfigBuilder builder = TranscodingConfig.builder();
            
            for (int i = 2; i < args.length; i += 2) {
                if (i + 1 >= args.length) break;
                
                String option = args[i];
                String value = args[i + 1];
                
                switch (option) {
                    case "--video-codec":
                        builder.videoCodec(value);
                        break;
                    case "--audio-codec":
                        builder.audioCodec(value);
                        break;
                    case "--video-bitrate":
                        builder.videoBitrate(value);
                        break;
                    case "--audio-bitrate":
                        builder.audioBitrate(value);
                        break;
                    case "--resolution":
                        builder.resolution(value);
                        break;
                    case "--frame-rate":
                        builder.frameRate(Integer.parseInt(value));
                        break;
                    case "--format":
                        builder.outputFormat(value);
                        break;
                    case "--processing-mode":
                        builder.processingMode(ProcessingMode.valueOf(value.toUpperCase()));
                        break;
                    case "--quality":
                        builder.qualityPreset(value);
                        break;
                    case "--threads":
                        builder.threads(Integer.parseInt(value));
                        break;
                }
            }
            
            return builder.build();
        }
    }
}

/**
 * Transcoding configuration class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TranscodingConfig {
    @Builder.Default
    private String videoCodec = "libx264";
    
    @Builder.Default
    private String audioCodec = "aac";
    
    @Builder.Default
    private String videoBitrate = "2000k";
    
    @Builder.Default
    private String audioBitrate = "128k";
    
    private String resolution; // null means keep original
    
    private Integer frameRate; // null means keep original
    
    @Builder.Default
    private String outputFormat = "mp4";
    
    @Builder.Default
    private ProcessingMode processingMode = ProcessingMode.CPU;
    
    @Builder.Default
    private String qualityPreset = "medium";
    
    @Builder.Default
    private Integer threads = null; // null means auto
}

/**
 * Processing mode enum
 */
enum ProcessingMode {
    CPU, GPU
}

/**
 * System information data class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SystemInfo {
    private String osName;
    private String osVersion;
    private String osArch;
    private String javaVersion;
    private long totalMemory;
    private long freeMemory;
    private long maxMemory;
    private int availableProcessors;
    private String cpuInfo;
    private List<String> gpuInfo;
    private boolean ffmpegAvailable;
    private String ffmpegVersion;
    private boolean cudaAvailable;
    private String cudaVersion;
}

/**
 * Video information data class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class VideoInfo {
    private String format;
    private long duration; // in seconds
    private long bitrate;
    private int width;
    private int height;
    private double frameRate;
    private String videoCodec;
    private String audioCodec;
    private long fileSize;
}

/**
 * Transcoding progress data class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TranscodingProgress {
    private int percentage;
    private long processedFrames;
    private long totalFrames;
    private String currentSpeed;
    private long elapsedTime;
    private long estimatedTime;
    private String status;
}

/**
 * System information service
 */
@Service
class SystemInfoService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemInfoService.class);

    public static void printSystemInfo() {
        logger.info("=".repeat(80));
        logger.info("SYSTEM INFORMATION");
        logger.info("=".repeat(80));
        
        SystemInfo systemInfo = getSystemInfo();
        
        // Operating System
        logger.info("Operating System:");
        logger.info("  Name: {}", systemInfo.getOsName());
        logger.info("  Version: {}", systemInfo.getOsVersion());
        logger.info("  Architecture: {}", systemInfo.getOsArch());
        
        // Java Runtime
        logger.info("Java Runtime:");
        logger.info("  Version: {}", systemInfo.getJavaVersion());
        
        // Memory Information
        logger.info("Memory Information:");
        logger.info("  Total Memory: {} MB", systemInfo.getTotalMemory() / 1024 / 1024);
        logger.info("  Free Memory: {} MB", systemInfo.getFreeMemory() / 1024 / 1024);
        logger.info("  Max Memory: {} MB", systemInfo.getMaxMemory() / 1024 / 1024);
        logger.info("  Used Memory: {} MB", (systemInfo.getTotalMemory() - systemInfo.getFreeMemory()) / 1024 / 1024);
        
        // CPU Information
        logger.info("CPU Information:");
        logger.info("  Available Processors: {}", systemInfo.getAvailableProcessors());
        logger.info("  CPU Details: {}", systemInfo.getCpuInfo());
        
        // GPU Information
        if (systemInfo.getGpuInfo() != null && !systemInfo.getGpuInfo().isEmpty()) {
            logger.info("GPU Information:");
            systemInfo.getGpuInfo().forEach(gpu -> logger.info("  GPU: {}", gpu));
        } else {
            logger.info("GPU Information: No GPU detected or GPU info unavailable");
        }
        
        // CUDA Information
        logger.info("CUDA Support:");
        logger.info("  CUDA Available: {}", systemInfo.isCudaAvailable());
        if (systemInfo.isCudaAvailable()) {
            logger.info("  CUDA Version: {}", systemInfo.getCudaVersion());
        }
        
        // FFmpeg Information
        logger.info("FFmpeg Information:");
        logger.info("  FFmpeg Available: {}", systemInfo.isFfmpegAvailable());
        if (systemInfo.isFfmpegAvailable()) {
            logger.info("  FFmpeg Version: {}", systemInfo.getFfmpegVersion());
        }
        
        logger.info("=".repeat(80));
    }

    public static SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        return SystemInfo.builder()
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArch(System.getProperty("os.arch"))
                .javaVersion(System.getProperty("java.version"))
                .totalMemory(runtime.totalMemory())
                .freeMemory(runtime.freeMemory())
                .maxMemory(runtime.maxMemory())
                .availableProcessors(runtime.availableProcessors())
                .cpuInfo(getCpuInfo())
                .gpuInfo(getGpuInfo())
                .ffmpegAvailable(isFFmpegAvailable())
                .ffmpegVersion(getFFmpegVersion())
                .cudaAvailable(isCudaAvailable())
                .cudaVersion(getCudaVersion())
                .build();
    }

    private static String getCpuInfo() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                return executeCommand("cat /proc/cpuinfo | grep 'model name' | head -1 | cut -d':' -f2").trim();
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                return executeCommand("sysctl -n machdep.cpu.brand_string").trim();
            } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                return executeCommand("wmic cpu get name /value | findstr Name=").replace("Name=", "").trim();
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve CPU information: {}", e.getMessage());
        }
        return "Unknown CPU";
    }

    private static List<String> getGpuInfo() {
        List<String> gpus = new ArrayList<>();
        try {
            // Try NVIDIA first
            String nvidiaOutput = executeCommand("nvidia-smi --query-gpu=name --format=csv,noheader,nounits");
            if (!nvidiaOutput.isEmpty()) {
                gpus.addAll(Arrays.asList(nvidiaOutput.split("\n")));
            }
            
            // Try AMD
            if (gpus.isEmpty()) {
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    String amdOutput = executeCommand("lspci | grep -i vga | grep -i amd");
                    if (!amdOutput.isEmpty()) {
                        gpus.add("AMD GPU detected: " + amdOutput);
                    }
                }
            }
            
            // Try Intel integrated graphics
            if (gpus.isEmpty()) {
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    String intelOutput = executeCommand("lspci | grep -i vga | grep -i intel");
                    if (!intelOutput.isEmpty()) {
                        gpus.add("Intel GPU detected: " + intelOutput);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Could not retrieve GPU information: {}", e.getMessage());
        }
        return gpus;
    }

    private static boolean isFFmpegAvailable() {
        try {
            executeCommand("ffmpeg -version");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getFFmpegVersion() {
        try {
            String output = executeCommand("ffmpeg -version");
            Pattern pattern = Pattern.compile("ffmpeg version ([^\\s]+)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve FFmpeg version: {}", e.getMessage());
        }
        return "Unknown";
    }

    private static boolean isCudaAvailable() {
        try {
            executeCommand("nvcc --version");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getCudaVersion() {
        try {
            String output = executeCommand("nvcc --version");
            Pattern pattern = Pattern.compile("release ([^,]+)");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            logger.warn("Could not retrieve CUDA version: {}", e.getMessage());
        }
        return "Unknown";
    }

    private static String executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        process.waitFor(5, TimeUnit.SECONDS);
        return output.toString();
    }
}

/**
 * Video analysis service
 */
@Service
class VideoAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoAnalysisService.class);

    public VideoInfo analyzeVideo(String videoPath) throws IOException, InterruptedException {
        logger.info("Analyzing video: {}", videoPath);
        
        String command = String.format(
            "ffprobe -v quiet -print_format json -show_format -show_streams \"%s\"",
            videoPath
        );
        
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }
        
        Process process = processBuilder.start();
        
        StringBuilder jsonOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFprobe failed with exit code: " + exitCode);
        }
        
        return parseVideoInfo(jsonOutput.toString(), videoPath);
    }

    private VideoInfo parseVideoInfo(String jsonOutput, String videoPath) {
        try {
            // Simple JSON parsing - in production, use Jackson or Gson
            VideoInfo.VideoInfoBuilder builder = VideoInfo.builder();
            
            // Extract basic information
            if (jsonOutput.contains("\"format\"")) {
                Pattern formatPattern = Pattern.compile("\"format_name\":\\s*\"([^\"]+)\"");
                Matcher matcher = formatPattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.format(matcher.group(1));
                }
                
                Pattern durationPattern = Pattern.compile("\"duration\":\\s*\"([^\"]+)\"");
                matcher = durationPattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.duration((long) Double.parseDouble(matcher.group(1)));
                }
                
                Pattern bitratePattern = Pattern.compile("\"bit_rate\":\\s*\"([^\"]+)\"");
                matcher = bitratePattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.bitrate(Long.parseLong(matcher.group(1)));
                }
            }
            
            // Extract video stream information
            if (jsonOutput.contains("\"codec_type\": \"video\"")) {
                Pattern widthPattern = Pattern.compile("\"width\":\\s*(\\d+)");
                Matcher matcher = widthPattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.width(Integer.parseInt(matcher.group(1)));
                }
                
                Pattern heightPattern = Pattern.compile("\"height\":\\s*(\\d+)");
                matcher = heightPattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.height(Integer.parseInt(matcher.group(1)));
                }
                
                Pattern frameRatePattern = Pattern.compile("\"r_frame_rate\":\\s*\"([^\"]+)\"");
                matcher = frameRatePattern.matcher(jsonOutput);
                if (matcher.find()) {
                    String[] parts = matcher.group(1).split("/");
                    if (parts.length == 2) {
                        double frameRate = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                        builder.frameRate(frameRate);
                    }
                }
                
                Pattern videoCodecPattern = Pattern.compile("\"codec_name\":\\s*\"([^\"]+)\"");
                matcher = videoCodecPattern.matcher(jsonOutput);
                if (matcher.find()) {
                    builder.videoCodec(matcher.group(1));
                }
            }
            
            // File size
            Path path = Paths.get(videoPath);
            if (Files.exists(path)) {
                builder.fileSize(Files.size(path));
            }
            
            VideoInfo info = builder.build();
            logger.info("Video analysis complete: {}x{}, {} seconds, {} codec", 
                       info.getWidth(), info.getHeight(), info.getDuration(), info.getVideoCodec());
            
            return info;
            
        } catch (Exception e) {
            logger.error("Error parsing video information: {}", e.getMessage());
            throw new RuntimeException("Failed to parse video information", e);
        }
    }
}

/**
 * Main transcoding service
 */
@Service
class TranscodingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TranscodingService.class);
    
    private final VideoAnalysisService videoAnalysisService;
    private final ExecutorService executorService;

    public TranscodingService(VideoAnalysisService videoAnalysisService) {
        this.videoAnalysisService = videoAnalysisService;
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
    }

    public boolean transcodeVideo(String inputPath, String outputPath, TranscodingConfig config) {
        try {
            // Validate input file
            if (!Files.exists(Paths.get(inputPath))) {
                throw new IllegalArgumentException("Input file does not exist: " + inputPath);
            }
            
            // Analyze input video
            VideoInfo inputInfo = videoAnalysisService.analyzeVideo(inputPath);
            logger.info("Input video info: {}", inputInfo);
            
            // Create output directory if needed
            Path outputDir = Paths.get(outputPath).getParent();
            if (outputDir != null && !Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            // Build FFmpeg command
            List<String> command = buildFFmpegCommand(inputPath, outputPath, config, inputInfo);
            logger.info("Executing command: {}", String.join(" ", command));
            
            // Execute transcoding
            return executeTranscoding(command, inputInfo);
            
        } catch (Exception e) {
            logger.error("Transcoding failed: ", e);
            return false;
        }
    }

    private List<String> buildFFmpegCommand(String inputPath, String outputPath, 
                                          TranscodingConfig config, VideoInfo inputInfo) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-i");
        command.add(inputPath);
        
        // Hardware acceleration settings
        if (config.getProcessingMode() == ProcessingMode.GPU) {
            addGpuAcceleration(command, config);
        }
        
        // Video codec and settings
        command.add("-c:v");
        command.add(config.getVideoCodec());
        
        // Video bitrate
        command.add("-b:v");
        command.add(config.getVideoBitrate());
        
        // Resolution scaling
        if (config.getResolution() != null && !config.getResolution().isEmpty()) {
            command.add("-vf");
            command.add("scale=" + config.getResolution().replace("x", ":"));
        }
        
        // Frame rate
        if (config.getFrameRate() != null) {
            command.add("-r");
            command.add(String.valueOf(config.getFrameRate()));
        }
        
        // Audio codec and settings
        command.add("-c:a");
        command.add(config.getAudioCodec());
        command.add("-b:a");
        command.add(config.getAudioBitrate());
        
        // Quality preset for x264/x265
        if (config.getVideoCodec().contains("264") || config.getVideoCodec().contains("265")) {
            command.add("-preset");
            command.add(config.getQualityPreset());
        }
        
        // Threading
        if (config.getProcessingMode() == ProcessingMode.CPU) {
            if (config.getThreads() != null) {
                command.add("-threads");
                command.add(String.valueOf(config.getThreads()));
            }
        }
        
        // Output format
        command.add("-f");
        command.add(config.getOutputFormat());
        
        // Progress and overwrite settings
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y"); // Overwrite output file
        
        command.add(outputPath);
        
        return command;
    }

    private void addGpuAcceleration(List<String> command, TranscodingConfig config) {
        String codec = config.getVideoCodec().toLowerCase();
        
        if (codec.contains("264")) {
            // NVIDIA NVENC H.264
            command.add("-hwaccel");
            command.add("cuda");
            command.add("-hwaccel_output_format");
            command.add("cuda");
            // Replace libx264 with h264_nvenc
            config.setVideoCodec("h264_nvenc");
        } else if (codec.contains("265") || codec.contains("hevc")) {
            // NVIDIA NVENC H.265
            command.add("-hwaccel");
            command.add("cuda");
            command.add("-hwaccel_output_format");
            command.add("cuda");
            // Replace libx265 with hevc_nvenc
            config.setVideoCodec("hevc_nvenc");
        }
        
        logger.info("GPU acceleration enabled for codec: {}", config.getVideoCodec());
    }

    private boolean executeTranscoding(List<String> command, VideoInfo inputInfo) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(false);
            
            Process process = processBuilder.start();
            
            // Create progress monitor
            ProgressMonitor monitor = new ProgressMonitor(inputInfo, process.getInputStream());
            Future<Void> progressFuture = executorService.submit(monitor);
            
            // Monitor stderr for errors
            ErrorMonitor errorMonitor = new ErrorMonitor(process.getErrorStream());
            Future<Void> errorFuture = executorService.submit(errorMonitor);
            
            // Wait for process completion
            int exitCode = process.waitFor();
            
            // Wait for monitors to complete
            try {
                progressFuture.get(5, TimeUnit.SECONDS);
                errorFuture.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                logger.warn("Monitor threads did not complete within timeout");
            }
            
            if (exitCode == 0) {
                logger.info("Transcoding completed successfully");
                return true;
            } else {
                logger.error("Transcoding failed with exit code: {}", exitCode);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error executing transcoding: ", e);
            return false;
        }
    }

    /**
     * Progress monitor for FFmpeg output
     */
    private static class ProgressMonitor implements Callable<Void> {
        private final VideoInfo inputInfo;
        private final InputStream inputStream;
        private final Logger logger = LoggerFactory.getLogger(ProgressMonitor.class);

        public ProgressMonitor(VideoInfo inputInfo, InputStream inputStream) {
            this.inputInfo = inputInfo;
            this.inputStream = inputStream;
        }

        @Override
        public Void call() throws Exception {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                long startTime = System.currentTimeMillis();
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("out_time_ms=")) {
                        long timeMs = Long.parseLong(line.substring(12)) / 1000; // Convert to milliseconds
                        long durationMs = inputInfo.getDuration() * 1000;
                        
                        if (durationMs > 0) {
                            int percentage = (int) ((timeMs * 100) / durationMs);
                            long elapsed = System.currentTimeMillis() - startTime;
                            
                            if (percentage > 0) {
                                long estimated = (elapsed * 100) / percentage;
                                long remaining = estimated - elapsed;
                                
                                logger.info("Progress: {}% | Elapsed: {}s | Remaining: {}s", 
                                           percentage, elapsed / 1000, remaining / 1000);
                            }
                        }
                    } else if (line.startsWith("speed=")) {
                        String speed = line.substring(6).trim();
                        logger.debug("Processing speed: {}", speed);
                    }
                }
            } catch (Exception e) {
                logger.warn("Progress monitoring error: {}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * Error monitor for FFmpeg stderr
     */
    private static class ErrorMonitor implements Callable<Void> {
        private final InputStream errorStream;
        private final Logger logger = LoggerFactory.getLogger(ErrorMonitor.class);

        public ErrorMonitor(InputStream errorStream) {
            this.errorStream = errorStream;
        }

        @Override
        public Void call() throws Exception {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Filter out common FFmpeg info messages
                    if (line.contains("Configuration:") || 
                        line.contains("built with") ||
                        line.contains("Stream #") ||
                        line.contains("Input #") ||
                        line.contains("Output #")) {
                        logger.debug("FFmpeg: {}", line);
                    } else if (line.contains("Error") || line.contains("error")) {
                        logger.error("FFmpeg Error: {}", line);
                    } else if (line.contains("Warning") || line.contains("warning")) {
                        logger.warn("FFmpeg Warning: {}", line);
                    } else {
                        logger.trace("FFmpeg: {}", line);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error monitoring error: {}", e.getMessage());
            }
            return null;
        }
    }
}