package com.tdcr.docker.backend.controller;


import com.tdcr.docker.app.HasLogger;
import com.tdcr.docker.backend.data.entity.AgentFeed;
import com.tdcr.docker.backend.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class AgentFeedController implements HasLogger {
    @Autowired
    AgentService agentService;

    @Value("${application.message}")
    String message;

    @Value("${application.appname}")
    String appname;

    @RequestMapping("/app")
    String home() {
        return message + " " + appname;
    }

    @RequestMapping(value = "/feed", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addUser(@RequestBody AgentFeed feed ){
        getLogger().info("Feeding agent details for imageId: {}",feed.getImageId());
        if(feed.getIncident() == null){
            agentService.saveImageFeed(feed);
        }else{
            agentService.saveImageIncFeed(feed);
        }

    }
}
