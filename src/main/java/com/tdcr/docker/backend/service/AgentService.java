package com.tdcr.docker.backend.service;

public interface AgentService {

    void saveImageFeed(String dockerDeamon, String containerId, int errorCount, String errorType);
}
