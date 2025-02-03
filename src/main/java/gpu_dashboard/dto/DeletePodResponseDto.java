package gpu_dashboard.dto;

public class DeletePodResponseDto {
    private String status;

    public DeletePodResponseDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
} 