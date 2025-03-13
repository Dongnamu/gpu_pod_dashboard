package com.example.gpu_dashboard.dto;


public class PodInfoDto {
    private String namespace;
    private String podname;
    private String podstatus;
    private String poduptime;
    private String gpuDevices;
    private String username;

    public PodInfoDto(String namespace, String podname, String podstatus, String poduptime, String gpuDevices, String username) {
        this.namespace = namespace;
        this.podname = podname;
        this.podstatus = podstatus;
        this.poduptime = poduptime;
        this.gpuDevices = gpuDevices;
        this.username = username;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPodname() {
        return podname;
    }

    public String getPodstatus() {
        return podstatus;
    }

    public String getGpuDevices() {
        return gpuDevices;
    }

    public String getUsername() {
        return username;
    }
    
    public String getPoduptime() {
        return poduptime;
    }
} 