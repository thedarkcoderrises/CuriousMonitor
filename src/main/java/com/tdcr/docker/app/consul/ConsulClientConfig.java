package com.tdcr.docker.app.consul;

import com.orbitz.consul.Consul;
import com.tdcr.docker.app.docker.DockerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConsulClientConfig {


    public Consul getConsul(String host){
        Consul.Builder consulBuilder = Consul.builder().withUrl("http://"+host+":8500");
        Consul consul = consulBuilder.build();
        return consul;
    }

    @Bean
    public Map<String,Consul> dockerConsulClienttMap (DockerConfiguration.DockerProps dp){
        Map <String,Consul> dockerConsulClienttMap = new HashMap<>();
        for (String key:
                dp.getDockerDaemonMap().keySet()) {
            if(StringUtils.isEmpty(dp.getDockerDaemonMap().get(key))){
                continue;
            }
            dockerConsulClienttMap.put(key,getConsul(dp.getDockerDaemonMap().get(key)));
        }
        return dockerConsulClienttMap;
    }
}
