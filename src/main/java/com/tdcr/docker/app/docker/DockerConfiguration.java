package com.tdcr.docker.app.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
public class DockerConfiguration {

    DefaultDockerClientConfig defaultDockerClientConfig(String host){
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://"+host+":5555").build();
        return config;
    }



    @Bean
    public Map<String,DockerClient> dockerClientMap (DockerProps dp){
        Map <String,DockerClient> dockerClientMap = new HashMap<>();
        Map<String, String> dockerDaemonMap = new HashMap<>();
        for (String key:
                dp.getDockerDaemonMap().keySet()) {
            if(StringUtils.isEmpty(dp.getDockerDaemonMap().get(key))){
                continue;
            }
            dockerDaemonMap.put(key,dp.getDockerDaemonMap().get(key));
            dockerClientMap.put(key,
                    DockerClientBuilder.getInstance(
                            defaultDockerClientConfig(dp.getDockerDaemonMap().get(key))).build());
        }
        dp.setDockerDaemonMap(dockerDaemonMap);
        return dockerClientMap;
    }

    @Bean
    @ConfigurationProperties
    public DockerProps DockerProps() {
        return new DockerProps();
    }

    public static class DockerProps {
        private Map<String, String> dockerDaemonMap = new HashMap<String, String>();
        public Map<String, String> getDockerDaemonMap() {
            return this.dockerDaemonMap;
        }

        public void setDockerDaemonMap(Map<String, String> dockerDaemonMap) {
            this.dockerDaemonMap = dockerDaemonMap;
        }
    }

}
