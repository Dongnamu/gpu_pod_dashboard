package com.example.gpu_dashboard.config;

import com.example.gpu_dashboard.service.PodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    
    private final PodService podService;
    
    // 모니터링할 네임스페이스 목록
    private final List<String> monitoredNamespaces = Arrays.asList("aidx", "abclab", "mattermost");
    
    @Autowired
    public SchedulerConfig(PodService podService) {
        this.podService = podService;
    }
    
    // 1분마다 Pod 정보 업데이트
    @Scheduled(fixedRate = 60000)
    public void updatePodInfo() {
        logger.info("Scheduled pod info update started");
        
        for (String namespace : monitoredNamespaces) {
            try {
                // podService.listAndSavePods(namespace, null);
                podService.listPods(namespace);
                logger.debug("Updated pod info for namespace: {}", namespace);
            } catch (Exception e) {
                logger.error("Failed to update pod info for namespace {}: {}", namespace, e.getMessage());
            }
        }
        
        logger.info("Scheduled pod info update completed");
    }
}
