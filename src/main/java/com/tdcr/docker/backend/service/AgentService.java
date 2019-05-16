package com.tdcr.docker.backend.service;

import com.tdcr.docker.backend.data.entity.AgentFeed;

public interface AgentService {

    void saveImageFeed(AgentFeed feed);

    void saveImageIncFeed(AgentFeed feed);
}
