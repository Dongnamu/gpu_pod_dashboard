package com.example.gpu_dashboard.dto;

public class PodUpdateUserDto {
    private String namespace;
    private String podname;
    private String username;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPodname() {
        return podname;
    }  

    public void setPodname(String podname) {
        this.podname = podname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
