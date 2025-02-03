package gpu_dashboard.dto;

import java.time.OffsetDateTime;

public class PodInfoDto {
    private String name;
    private String status;
    private String uptime;

    public PodInfoDto(String name, String status, String uptime) {
        this.name = name;
        this.status = status;
        this.uptime = uptime;
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
} 