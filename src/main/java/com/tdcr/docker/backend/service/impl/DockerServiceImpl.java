package com.tdcr.docker.backend.service.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.tdcr.docker.backend.data.entity.DocContainer;
import com.tdcr.docker.backend.service.DockerService;
import com.tdcr.docker.backend.utils.ComputeStats;
import com.tdcr.docker.backend.utils.FirstObjectResultCallback;
import com.tdcr.docker.backend.utils.LogContainerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DockerServiceImpl implements DockerService {

    private static Logger LOG = LoggerFactory.getLogger(DockerServiceImpl.class);

    @Autowired
    Map<String,DockerClient> dockerClientMap;
    DockerClient dockerClient;

    @Override
    public String listRunningContainers() {
        return listAllContainers("running").toString();
    }

    @Override
    public String listStoppedContainers() {
        return listAllContainers("exited").toString();
    }

    @Override
    public List<DocContainer> listAllContainers(String status) {
        ListContainersCmd cmd = null;
        List<Container> lst = new ArrayList<>();
        if(StringUtils.isEmpty(status)){
            cmd = dockerClient.listContainersCmd().withShowSize(true)
                    .withShowAll(true);
        }else{
            cmd = dockerClient.listContainersCmd().withShowSize(true)
                    .withShowAll(true).withStatusFilter(status);
        }
         lst= cmd.exec(); //  docker ps -a -s -f status=${status}
        return covertContainerData(lst);
    }

    private List<DocContainer> covertContainerData(List<Container> lst) {
        List<DocContainer> list = new ArrayList<>();
        for (Container container :
                lst) {
            list.add(new DocContainer(container));
        }
        return list;
    }


    @Override
    public String inspectOnContainerId(String containerId) {
         InspectContainerResponse container = dockerClient.inspectContainerCmd(containerId).exec();
         return container.toString();
    }

    @Override
    public Map<String,String> getContainerStats(String containerId) throws Exception {
        try {
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            FirstObjectResultCallback<Statistics> resultCallback = new FirstObjectResultCallback<>();
            Statistics stats = statsCmd.exec(resultCallback).waitForObject();
            return ComputeStats.computeStats(stats);
        } catch(InterruptedException e){
            throw new Exception("Interrupted while waiting for statistics");
        }
    }

    @Override
    public String updateContainerStatus(String containerId, boolean status) {

        try{
            if(status){
                StartContainerCmd cmd = dockerClient.startContainerCmd(containerId);
                cmd.exec();
            }else{
                StopContainerCmd cmd = dockerClient.stopContainerCmd(containerId);
                cmd.exec();
            }
        }catch (Exception e){
            //TODO
            return null;
        }

        return containerId;
    }

    @Override
    public void updateDockerClient(String dockerDaemonName) {
        dockerClient = dockerClientMap.get(dockerDaemonName);
    }

    @Override
    public String getLogs(String containerId) throws InterruptedException {
        LogContainerCallback loggingCallback = new
                LogContainerCallback();

        LogContainerCmd cmd = dockerClient.logContainerCmd(containerId);
        cmd.withStdOut(true).withTimestamps(true).withTailAll().exec(loggingCallback);
        loggingCallback.awaitCompletion(3, TimeUnit.SECONDS);
        return loggingCallback.getEntries().toString();
    }

    @Override
    public String removeContainer(String containerId) {
        RemoveContainerCmd cmd = dockerClient.removeContainerCmd(containerId);
        try{
            cmd.exec();
        }catch(Exception e){
            return null;
        }
        return containerId;
    }

}
