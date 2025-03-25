package com.example.gpu_dashboard.service;

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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

import com.example.gpu_dashboard.dto.DeletePodResponseDto;
import com.example.gpu_dashboard.dto.PodInfoDto;
import com.example.gpu_dashboard.dto.PodResponseDto;
import com.example.gpu_dashboard.repository.PodInfoRepository;
import com.example.gpu_dashboard.entity.PodInfoEntity;

/**
 * 쿠버네티스 Pod 관련 서비스를 처리하는 클래스
 */
@Service
public class PodService {

    private static final Logger logger = LoggerFactory.getLogger(PodService.class);

    private final CoreV1Api coreV1Api;
    private final PodInfoRepository podInfoRepository;

    /**
     * PodService 생성자
     * @param coreV1Api 쿠버네티스 Core V1 API 클라이언트
     */
    @Autowired
    public PodService(CoreV1Api coreV1Api, PodInfoRepository podInfoRepository) {
        this.coreV1Api = coreV1Api;
        this.podInfoRepository = podInfoRepository;
    }

    /**
     * Pod 정보를 DB에 저장
     * @param namespace 네임스페이스
     * @param podName Pod 이름
     * @param podStatus Pod 상태
     * @param podUptime Pod 가동 시간
     * @param gpuDevices GPU 장치 정보
     */
    private void savePodInfo(String namespace, String podName, String podStatus, String podUptime, String gpuDevices) {
        try {
            // 기존 Pod 정보 조회 (namespace와 podName으로 식별)
            List<PodInfoEntity> existingPods = podInfoRepository.findByNamespace(namespace);
            PodInfoEntity existingPod = null;
            
            for (PodInfoEntity pod : existingPods) {
                if (pod.getPodName().equals(podName)) {
                    existingPod = pod;
                    break;
                }
            }
            
            if (existingPod != null) {
                // 기존 정보 업데이트
                existingPod.setPodStatus(podStatus);
                existingPod.setPodUptime(podUptime);
                existingPod.setGpuDevices(gpuDevices);
                podInfoRepository.save(existingPod);
                logger.debug("Updated pod info in DB: {}/{}", namespace, podName);
            } else {
                // 새 정보 생성
                PodInfoEntity newPod = new PodInfoEntity();
                newPod.setNamespace(namespace);
                newPod.setPodName(podName);
                newPod.setPodStatus(podStatus);
                newPod.setPodUptime(podUptime);
                newPod.setGpuDevices(gpuDevices);
                podInfoRepository.save(newPod);
                logger.debug("Saved new pod info to DB: {}/{}", namespace, podName);
            }
        } catch (Exception e) {
            logger.error("Failed to save pod info to DB: {}", e.getMessage(), e);
        }
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
            // 한국 시간대(KST)로 현재 시간 설정
            ZoneId koreaZoneId = ZoneId.of("Asia/Seoul");
            ZonedDateTime koreaTime = ZonedDateTime.now(koreaZoneId);
            OffsetDateTime now = koreaTime.toOffsetDateTime();
            logger.debug("Current time (KST): {}", koreaTime);

            // 현재 쿠버네티스에 존재하는 Pod 이름들을 Set으로 수집
            Set<String> currentPodNames = podList.getItems().stream()
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toSet());
            logger.debug("Current pod names: {}", currentPodNames);

            // DB에서 해당 네임스페이스의 모든 Pod 조회
            List<PodInfoEntity> dbPods = podInfoRepository.findByNamespace(namespace);

            // 현재 쿠버네티스에 없는 Pod는 DB에서 삭제
            for (PodInfoEntity dbPod : dbPods) {
                if (!currentPodNames.contains(dbPod.getPodName())) {
                    podInfoRepository.delete(dbPod);
                    logger.debug("Deleted pod from DB: {}/{}", namespace, dbPod.getPodName());
                }
            }


            List<PodInfoDto> podInfos = podList.getItems().stream()
                .map(pod -> {
                    String poduptime = "";
                    if (pod.getStatus() != null) {
                        OffsetDateTime startTime = pod.getStatus().getStartTime();
                        if (startTime != null) {
                            Duration duration = Duration.between(startTime, now);
                            long days = duration.toDays();
                            long hours = duration.toHours() % 24;
                            poduptime = String.format("%dd %dh", days, hours);
                        }
                    }
                    
                    String podName = (pod.getMetadata() != null) ? pod.getMetadata().getName() : "unknown";
                    String podPhase = (pod.getStatus() != null) ? pod.getStatus().getPhase() : "unknown";
                    // 각 container의 "CUDA_VISIBLE_DEVICES" 환경변수를 확인하여 GPU 장비번호를 가져옴
                    String gpuDevices = "";
                    // logger.debug("PodSpec: {}", pod.getSpec());
                    if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
                        gpuDevices = pod.getSpec().getContainers().stream()
                            .map(container -> {
                                String device = "Not GPU";
                                if (container.getEnv() != null) {
                                    device = container.getEnv().stream()
                                              .filter(envVar -> "NVIDIA_VISIBLE_DEVICES".equals(envVar.getName()))
                                              .map(envVar -> envVar.getValue())
                                              .findFirst().orElse("Not GPU");
                                }
                                return device;
                            })
                            .collect(Collectors.joining(", "));
                    }

                    savePodInfo(namespace, podName, podPhase, poduptime, gpuDevices);

                    String username = "";

                    return new PodInfoDto(namespace, podName, podPhase, poduptime, gpuDevices, username);
                })
                .collect(Collectors.toList());
                

            // // // 이상한 Pod 처리 - DB에는 있지만 쿠버네티스에는 없는 Pod
            // List<PodInfoEntity> dbPods = podInfoRepository.findByNamespace(namespace);
            // for (PodInfoEntity pod : dbPods) {
            //     if (!currentPodNames.contains(pod.getPodName())) {
            //         // 방법 1: 상태만 변경
            //         pod.setPodStatus("Deleted");
            //         podInfoRepository.save(pod);
            //         logger.debug("Pod가 삭제됨, 상태 업데이트: {}/{}", namespace, pod.getPodName());
                    
            //         // 방법 2: DB에서 완전히 삭제 (원하는 경우)
            //         // podInfoRepository.delete(pod);
            //         // logger.debug("Pod가 삭제됨, DB에서 제거: {}/{}", namespace, pod.getPodName());
            //     }
            // }

            return new PodResponseDto(podInfos);
            
        } catch (Exception e) {
            logger.error("Failed to list pods in namespace '{}': {}", namespace, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * DB에서 특정 네임스페이스의 Pod 정보 조회
     * @param namespace 조회할 네임스페이스
     * @return PodResponseDto Pod 정보 목록을 포함한 응답 객체
     */
    public PodResponseDto getPodsFromDb(String namespace) {
        try {
            logger.debug("Retrieving pod info from DB for namespace: {}", namespace);
            
            List<PodInfoEntity> podEntities = podInfoRepository.findByNamespace(namespace);
            
            List<PodInfoDto> podInfos = podEntities.stream()
                .map(entity -> new PodInfoDto(
                    entity.getNamespace(),
                    entity.getPodName(),
                    entity.getPodStatus(),
                    entity.getPodUptime(),
                    entity.getGpuDevices(),
                    entity.getUsername()
                ))
                .collect(Collectors.toList());
            
            logger.debug("Retrieved {} pod records from DB for namespace: {}", podInfos.size(), namespace);
            return new PodResponseDto(podInfos);
        } catch (Exception e) {
            logger.error("Failed to retrieve pod info from DB for namespace '{}': {}", namespace, e.getMessage(), e);
            return new PodResponseDto(Collections.emptyList());
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
            
            // DB에서 Pod 상태 업데이트
            updatePodStatusInDb(namespace, podName, "Deleted");


            logger.info("Pod 삭제 완료");
            return new DeletePodResponseDto("success");
            
        } catch (Exception e) {
            logger.error("Pod 삭제 실패 - namespace: {}, podName: {}, error: {}", 
                namespace, podName, e.getMessage());
            return new DeletePodResponseDto("fail");
        }
    }
    /**
     * DB에서 Pod 상태 업데이트
     * @param namespace 네임스페이스
     * @param podName Pod 이름
     * @param status 새 상태
     */
    private void updatePodStatusInDb(String namespace, String podName, String status) {
        try {
            // DB에서 Pod 정보 조회
            List<PodInfoEntity> existingPods = podInfoRepository.findByNamespace(namespace);
            
            for (PodInfoEntity pod : existingPods) {
                if (pod.getPodName().equals(podName)) {
                    pod.setPodStatus(status);
                    podInfoRepository.save(pod);
                    logger.debug("Pod 상태 업데이트: {}/{} -> {}", namespace, podName, status);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Pod 상태 업데이트 실패: {}/{} -> {}, error: {}", 
                namespace, podName, status, e.getMessage());
        }
    }

    public PodResponseDto updateUsername(String namespace, String podName, String username) {
        try {
            logger.debug("Pod 사용자 이름 업데이트 시도 - namespace: {}, podName: {}, username: {}", namespace, podName, username);
            
            Optional<PodInfoEntity> pod = podInfoRepository.findByNamespaceAndPodName(namespace, podName);
            if (pod.isPresent()) {
                pod.get().setUsername(username);
                podInfoRepository.save(pod.get());
            }
        } catch (Exception e) {
            logger.error("Pod 사용자 이름 업데이트 실패: {}/{} -> {}, error: {}", 
                namespace, podName, username, e.getMessage());
        }
        return new PodResponseDto(Collections.emptyList());
    }
}
