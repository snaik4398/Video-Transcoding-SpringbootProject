Clean. Here's a summary of everything created and changed:

---

## What was created

### 1. `docker-compose.gpu.yml` - GPU-accelerated deployment with load distribution

This is a separate compose file you run instead of (not alongside) the base `docker-compose.yml`:

```bash
docker-compose -f docker-compose.gpu.yml up --build
```

**Architecture - 3 transcoding workers:**

| Worker | Type | Port | Max Jobs | Description |
|--------|------|------|----------|-------------|
| `transcoding-gpu-worker` | GPU (NVIDIA) | 8082 | 4 | Uses NVENC/NVDEC hardware encoding, 5-10x faster |
| `transcoding-cpu-worker-1` | CPU | 8092 | 2 | Software encoding (libx264/libx265) |
| `transcoding-cpu-worker-2` | CPU | 8093 | 2 | Software encoding (second instance) |

**Load distribution:** All 3 workers share the same Kafka consumer group (`transcode-service-group`). Kafka automatically distributes jobs across them via partition assignment. With 6 partitions and 3 consumers, each worker gets ~2 partitions. No custom routing needed - it's automatic.

**GPU passthrough** uses the `deploy.resources.reservations.devices` block:
```yaml
reservations:
  devices:
    - driver: nvidia
      count: 1
      capabilities: [gpu, video, compute]
```

### 2. `transcoding-service/Dockerfile.gpu` - NVIDIA CUDA-based image

Uses `nvidia/cuda:12.3.1-runtime-ubuntu22.04` as base instead of Alpine, since NVIDIA drivers require glibc. Installs FFmpeg with hardware acceleration support and JDK 21.

### 3. `FFmpegService.java` - Full GPU transcoding support

Key changes:
- **Automatic codec mapping:** `libx264` -> `h264_nvenc`, `libx265` -> `hevc_nvenc` when GPU is enabled
- **Hardware acceleration flags:** `-hwaccel cuda -hwaccel_device 0 -hwaccel_output_format cuda`
- **GPU-specific scaling:** Uses `scale_cuda` filter instead of `scale` for GPU-resident frames
- **NVENC quality presets:** `-preset p4 -tune hq -rc vbr` (optimized for quality)
- **Automatic CPU fallback:** If GPU transcoding fails (driver issue, unsupported codec), it automatically retries with software codecs
- **GPU detection:** `isGpuAvailable()` checks `nvidia-smi`, `getGpuInfo()` returns GPU name/memory/utilization

### Prerequisites to use GPU mode

1. **NVIDIA GPU** with updated drivers on the host
2. **NVIDIA Container Toolkit** installed:
   - Windows: Docker Desktop with WSL2 backend and GPU support enabled
   - Linux: `sudo apt install nvidia-container-toolkit && sudo systemctl restart docker`
3. **Verify:** `docker run --rm --gpus all nvidia/cuda:12.3.1-base-ubuntu22.04 nvidia-smi`

If you don't have a GPU or don't want GPU acceleration, just use the original `docker-compose.yml` - it still works with CPU-only transcoding.