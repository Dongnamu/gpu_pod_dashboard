package gpu_dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.kubernetes.client.openapi.apis.CoreV1Api;
// import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
// import io.kubernetes.client.openapi.models.V1Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import gpu_dashboard.dto.DeletePodResponseDto;
import gpu_dashboard.dto.PodInfoDto;
import gpu_dashboard.dto.PodResponseDto;

/**
 * 쿠버네티스 Pod 관련 서비스를 처리하는 클래스
 */
@Service
public class PodService {

    private static final Logger logger = LoggerFactory.getLogger(PodService.class);

    private final CoreV1Api coreV1Api;

    /**
     * PodService 생성자
     * @param coreV1Api 쿠버네티스 Core V1 API 클라이언트
     */
    @Autowired
    public PodService(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    /**
     * 특정 네임스페이스의 모든 Pod 목록을 조회
     * @param namespace 조회할 네임스페이스
     * @return PodResponseDto Pod 정보 목록을 포함한 응답 객체
     * @throws Exception API 호출 실패시 발생
     */
    public PodResponseDto listPods(String namespace) throws Exception {
        try {
            logger.debug("Attempting to list pods in namespace: {}", namespace);
            V1PodList podList = coreV1Api.listNamespacedPod(
                namespace != null ? namespace.trim() : "",
                null, null, null, null, null, null, null, null, null, false
            );

            List<PodInfoDto> podInfos = podList.getItems().stream()
                .map(pod -> {
                    String uptime = "";
                    if (pod.getStatus() != null) {
                        OffsetDateTime startTime = pod.getStatus().getStartTime();
                        if (startTime != null) {
                            OffsetDateTime now = OffsetDateTime.now();
                            Duration duration = Duration.between(startTime, now);
                            long days = duration.toDays();
                            long hours = duration.toHours() % 24;
                            uptime = String.format("%dd %dh", days, hours);
                        }
                    }
                    
                    String podName = pod.getMetadata() != null ? pod.getMetadata().getName() : "unknown";
                    String podPhase = pod.getStatus() != null ? pod.getStatus().getPhase() : "unknown";
                    
                    return new PodInfoDto(
                        podName,
                        podPhase,
                        uptime
                    );
                })
                .collect(Collectors.toList());

            return new PodResponseDto(podInfos);
            
        } catch (Exception e) {
            logger.error("Failed to list pods in namespace '{}': {}", namespace, e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 Pod 삭제
     * @param namespace 조회할 pod 네임스페이스
     * @param podName 삭제할 Pod 이름
     * @return DeletePodResponseDto 삭제 결과를 포함한 응답 객체
     */
    public DeletePodResponseDto deletePod(String namespace, String podName) {
        try {
            logger.debug("Pod 삭제 시도 - namespace: {}, podName: {}", namespace, podName);
            
            coreV1Api.deleteNamespacedPod(
                podName,
                namespace,
                null,  // pretty
                null,  // dryRun
                30,  // gracePeriodSeconds
                null,  // orphanDependents
                "Background",  // propagationPolicy
                null   // body
            );
            
            logger.info("Pod 삭제 완료");
            return new DeletePodResponseDto("success");
            
        } catch (Exception e) {
            logger.error("Pod 삭제 실패 - namespace: {}, podName: {}, error: {}", 
                namespace, podName, e.getMessage());
            return new DeletePodResponseDto("fail");
        }
    }
} 