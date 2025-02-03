package gpu_dashboard.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;

@Configuration
public class K8sConfig {

    @Value("${kubeconfig.path}")
    private String kubeconfigPath;

    @Bean
    public CoreV1Api coreV1Api() throws IOException {
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeconfigPath))).build();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return new CoreV1Api(client);
    }
}
