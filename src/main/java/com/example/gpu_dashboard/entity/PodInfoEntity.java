package com.example.gpu_dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gpu_dashboard")
public class PodInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String namespace;
    private String podName;
    private String podStatus;
    private String gpuDevices;
    private String podUptime;

    private LocalDateTime startDateTime;

    // Getter/Setter 메서드
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getPodName() {
        return podName;
    }
    
    public void setPodName(String podName) {
        this.podName = podName;
    }
    
    public String getPodStatus() {
        return podStatus;
    }
    
    public void setPodStatus(String podStatus) {
        this.podStatus = podStatus;
    }
    
    public String getGpuDevices() {
        return gpuDevices;
    }
    
    public void setGpuDevices(String gpuDevices) {
        this.gpuDevices = gpuDevices;
    }
    
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getPodUptime() {
        return podUptime;
    }

    public void setPodUptime(String podUptime) {
        this.podUptime = podUptime;
    }

}
