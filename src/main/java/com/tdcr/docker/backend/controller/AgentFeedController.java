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

/*    @RequestMapping(value = "/feed/{dd}/{containerId}/{errorCnt}/{errorType}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addUser(@PathVariable("dd") String dockerDeamon,
                        @PathVariable("containerId") String containerId,
                        @PathVariable("errorCnt") int errorCnt,@PathVariable("errorType") String errorType  ){
        getLogger().info("Feeding agent details for {}",containerId);
        agentService.saveImageFeed(dockerDeamon,containerId,errorCnt,errorType);
    }*/

    @RequestMapping(value = "/feed", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addUser(@RequestBody AgentFeed feed ){
        getLogger().info("Feeding agent details for imageId: {}",feed.getImageId());
        agentService.saveImageFeed(feed);
    }
}
