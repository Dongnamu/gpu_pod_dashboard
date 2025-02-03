package gpu_dashboard.dto;


public class PodInfoDto {
    private String name;
    private String status;
    private String uptime;
    private String gpuDevices;

    public PodInfoDto(String name, String status, String uptime, String gpuDevices) {
        this.name = name;
        this.status = status;
        this.uptime = uptime;
        this.gpuDevices = gpuDevices;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getUptime() {
        return uptime;
    }

    public String getGpuDevices() {
        return gpuDevices;
    }
} 