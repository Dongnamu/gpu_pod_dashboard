package gpu_dashboard.controller;

import gpu_dashboard.dto.DeletePodRequest;
import gpu_dashboard.dto.DeletePodResponseDto;
import gpu_dashboard.dto.NamespaceDto;
import gpu_dashboard.dto.PodResponseDto;
import gpu_dashboard.service.PodService;
import io.kubernetes.client.openapi.models.V1PodList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 쿠버네티스 Pod 관련 컨트롤러
 */
@RestController
public class PodController {

    private final PodService podService;
    private static final Logger logger = LoggerFactory.getLogger(PodController.class);

    /**
     * PodController 생성자
     * @param podService Pod 관련 서비스
     */
    @Autowired
    public PodController(PodService podService) {
        this.podService = podService;
    }

    /**
     * 특정 네임스페이스의 Pod 목록을 조회
     * @param request 네임스페이스 정보를 담은 DTO
     * @return Pod 정보 목록을 담은 응답 DTO
     */
    @PostMapping("/pods")
    public PodResponseDto getPods(@RequestBody NamespaceDto request) {
        try {
            String namespace = request.getNamespace();
            logger.debug("Received namespace request: {}", namespace);
            return podService.listPods(namespace);
        } catch (Exception e) {
            logger.error("Error getting pods: ", e);
            return new PodResponseDto(Collections.emptyList());
        }
    }

    /**
     * 특정 네임스페이스의 Pod를 삭제
     * @param request Pod 삭제 요청 정보를 담은 DTO (네임스페이스와 Pod 이름 포함)
     * @return Pod 삭제 결과를 담은 응답 DTO (성공 시 "success", 실패 시 "fail" 반환)
     */
    @PostMapping("/pods/delete")
    public DeletePodResponseDto deletePod(@RequestBody DeletePodRequest request) {
        try {
            logger.debug("Pod 삭제 요청 - namespace: {}, podName: {}", 
                request.getNamespace(), request.getPodName());
            return podService.deletePod(request.getNamespace(), request.getPodName());
        } catch (Exception e) {
            logger.error("Pod 삭제 중 오류 발생: ", e);
            return new DeletePodResponseDto("fail");
        }
    }
}

