package gpu_dashboard.dto;

import java.util.List;

public class PodResponseDto {
    private List<PodInfoDto> result;

    public PodResponseDto(List<PodInfoDto> result) {
        this.result = result;
    }

    public List<PodInfoDto> getResult() {
        return result;
    }
} 