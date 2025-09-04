# Video Transcoding Standalone Application

A standalone Spring Boot application for video transcoding using FFmpeg. This application provides a command-line interface for transcoding videos with various codecs, quality settings, and hardware acceleration options.

## Features

- **Multiple Video Codecs**: Support for H.264 (libx264), H.265/HEVC (libx265), VP9 (libvpx-vp9), and AV1 (libaom-av1)
- **Audio Codecs**: AAC, MP3, Opus, and Vorbis support
- **Hardware Acceleration**: GPU acceleration using NVIDIA NVENC (when available)
- **Quality Presets**: From ultrafast to veryslow for optimal quality/speed balance
- **Progress Monitoring**: Real-time transcoding progress with ETA
- **System Information**: Comprehensive system diagnostics and FFmpeg detection
- **Flexible Output**: Support for MP4, WebM, AVI, and MKV formats

## Prerequisites

- **Java 11** or higher
- **Maven 3.6** or higher
- **FFmpeg** installed and available in PATH
- **FFprobe** (usually comes with FFmpeg)

### Installing FFmpeg

#### Windows
1. Download from [https://ffmpeg.org/download.html](https://ffmpeg.org/download.html)
2. Extract to a folder (e.g., `C:\ffmpeg`)
3. Add `C:\ffmpeg\bin` to your PATH environment variable

#### macOS
```bash
brew install ffmpeg
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install ffmpeg
```

## Quick Start

### 1. Build the Application

```bash
# Navigate to the test directory
cd test

# Build the project
mvn clean package -DskipTests
```

Or use the provided batch script:
```bash
build-and-run.bat
```

### 2. Run Basic Transcoding

```bash
# Basic transcoding (MP4 to MP4)
java -jar target/video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4

# With custom settings
java -jar target/video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4 --video-codec libx265 --resolution 1280x720

# GPU acceleration
java -jar target/video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4 --processing-mode GPU --video-bitrate 5M
```

## Command Line Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `--video-codec` | Video codec | libx264 | libx265, libvpx-vp9, libaom-av1 |
| `--audio-codec` | Audio codec | aac | mp3, opus, libvorbis |
| `--video-bitrate` | Video bitrate | 2000k | 5M, 1000k |
| `--audio-bitrate` | Audio bitrate | 128k | 256k, 320k |
| `--resolution` | Output resolution | original | 1920x1080, 1280x720 |
| `--frame-rate` | Frame rate | original | 24, 30, 60 |
| `--format` | Output format | mp4 | webm, avi, mkv |
| `--processing-mode` | Processing mode | CPU | GPU |
| `--quality` | Quality preset | medium | ultrafast, fast, slow, veryslow |
| `--threads` | CPU threads | auto | 4, 8, 16 |

## Examples

### High Quality H.265 Transcoding
```bash
java -jar target/video-transcoding-standalone-1.0.0.jar \
  input.mp4 \
  output_h265.mp4 \
  --video-codec libx265 \
  --video-bitrate 8M \
  --quality slow \
  --resolution 1920x1080
```

### GPU Accelerated Transcoding
```bash
java -jar target/video-transcoding-standalone-1.0.0.jar \
  input.mp4 \
  output_gpu.mp4 \
  --processing-mode GPU \
  --video-codec h264_nvenc \
  --video-bitrate 5M \
  --quality fast
```

### WebM for Web Streaming
```bash
java -jar target/video-transcoding-standalone-1.0.0.jar \
  input.mp4 \
  output_webm.webm \
  --video-codec libvpx-vp9 \
  --audio-codec libvorbis \
  --format webm \
  --video-bitrate 2M \
  --resolution 1280x720
```

### Batch Processing Script
```bash
# Windows batch script for multiple files
for %%f in (*.mp4) do (
    java -jar target/video-transcoding-standalone-1.0.0.jar "%%f" "converted\%%~nf_h265.mp4" --video-codec libx265 --quality medium
)
```

## System Requirements

- **CPU**: Multi-core processor recommended for CPU transcoding
- **RAM**: Minimum 4GB, 8GB+ recommended for large files
- **GPU**: NVIDIA GPU with NVENC support for hardware acceleration
- **Storage**: Sufficient space for input/output files
- **OS**: Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)

## Troubleshooting

### Common Issues

1. **FFmpeg not found**
   - Ensure FFmpeg is installed and in PATH
   - Test with: `ffmpeg -version`

2. **Out of memory errors**
   - Increase JVM heap size: `java -Xmx4g -jar app.jar ...`
   - Use lower resolution or bitrate settings

3. **GPU acceleration not working**
   - Verify NVIDIA drivers are installed
   - Check CUDA availability: `nvcc --version`
   - Use CPU mode as fallback

4. **Poor performance**
   - Use appropriate quality preset (fast vs slow)
   - Enable GPU acceleration if available
   - Adjust thread count for your CPU

### Performance Tips

- **CPU Mode**: Use `--quality fast` for quick transcoding, `--quality slow` for best quality
- **GPU Mode**: Automatically selects optimal settings for hardware acceleration
- **Threading**: Let the app auto-detect optimal thread count, or specify manually
- **Batch Processing**: Process multiple files sequentially to avoid memory issues

## Development

### Project Structure
```
test/
├── pom.xml                          # Maven configuration
├── build-and-run.bat               # Windows build script
├── README.md                       # This file
└── src/main/java/
    └── com/transcoding/service/
        └── VideoTranscodingApplication.java  # Main application
```

### Building from Source
```bash
mvn clean compile
mvn package
```

### Running Tests
```bash
mvn test
```

## License

This project is part of the Video Transcoding Java microservices architecture.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Verify FFmpeg installation and PATH
3. Review system requirements
4. Check log output for detailed error messages
