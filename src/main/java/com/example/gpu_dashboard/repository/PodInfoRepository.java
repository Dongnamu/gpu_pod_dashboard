package com.example.gpu_dashboard.repository;

import com.example.gpu_dashboard.entity.PodInfoEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PodInfoRepository extends JpaRepository<PodInfoEntity, Long> {
    //사용자 이름으로 작업 목록 조회
    List<PodInfoEntity> findByNamespace(String namespace);

    // 추가: namespace와 podName으로 Pod 조회
    Optional<PodInfoEntity> findByNamespaceAndPodName(String namespace, String podName);
}
