package com.tdcr.docker.app.consul;

import com.orbitz.consul.Consul;
import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.app.docker.DockerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ConsulClientConfig implements HasLogger {


    public Consul getConsul(String host){
        Consul consul = null;
        Consul.Builder consulBuilder = null;
        String url = "http://"+host+":8500";
        try{
             consulBuilder =Consul.builder().withUrl(url);
             consul = consulBuilder.build();
        }catch(Exception e){
            getLogger().error("Consul not reachable for provided URL {}",url);
        }
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
