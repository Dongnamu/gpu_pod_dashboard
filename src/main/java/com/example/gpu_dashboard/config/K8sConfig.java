package com.example.gpu_dashboard.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

// @Configuration
// public class K8sConfig {

//     @Value("${kubeconfig.path}")
//     private String kubeconfigPath;

//     @Bean
//     public CoreV1Api coreV1Api() throws IOException {
//         ClassPathResource resource = new ClassPathResource("kube_config.yaml");
//         InputStreamReader reader = new InputStreamReader(resource.getInputStream());
        
//         // ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeconfigPath))).build();
//         ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(reader)).build();
//         io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
//         return new CoreV1Api(client);
//     }
// }

@Configuration
public class K8sConfig {
    @Value("${kubeconfig.path:#{null}}")
    private String kubeconfigPath;
    
    @Bean
    public CoreV1Api coreV1Api() throws IOException {
        ApiClient client;

        try {
            client = ClientBuilder.cluster().build();
            System.out.println("Kubernetes 클러스터에 연결되었습니다.");
        } catch (Exception e) {
            System.out.println("클러스터 내부 인증 실패, 외부 구성으로 시도합니다: " + e.getMessage());
            try {
                // 클래스패스에서 설정 파일 시도
                ClassPathResource resource = new ClassPathResource("kube_config.yaml");
                if (resource.exists()) {
                    InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                    client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(reader)).build();
                    System.out.println("클래스패스에서 kube_config.yaml을 로드했습니다.");
                } else if (kubeconfigPath != null) {
                    // 설정된 경로에서 시도
                    client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeconfigPath))).build();
                    System.out.println("지정된 경로에서 kubeconfig를 로드했습니다: " + kubeconfigPath);
                } else {
                    // 기본 kubeconfig (~/.kube/config) 시도
                    client = ClientBuilder.defaultClient();
                    System.out.println("기본 kubeconfig를 로드했습니다.");
                }
            } catch (Exception ex) {
                System.out.println("모든 인증 방법 실패, 최후의 방법으로 defaultClient 시도: " + ex.getMessage());
                client = io.kubernetes.client.openapi.Configuration.getDefaultApiClient();
            }
        }
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return new CoreV1Api(client);
    }
}
