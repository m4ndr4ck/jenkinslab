package com.tlf.jenkinslab.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GitlabController {

    String PROJECT_CREATE = "project_create";
    String PROJECT_DESTROY = "project_destroy";

    @PostMapping("/newJob")
    public void newJob(@RequestBody String test) {

        Map<String, Object> retMap = new Gson().fromJson(
                test, new TypeToken<HashMap<String, Object>>() {}.getType()
        );

        String eventName = (String)retMap.get("event_name");
        String projectName = (String)retMap.get("name");
        String ownerName = (String)retMap.get("owner_name");
        String jobName = ownerName+"-"+projectName;

        if(eventName.equals(PROJECT_CREATE)) {
            System.out.println("Project created on: " + retMap.get("path_with_namespace"));
            System.out.println("Will create Jenkins job name: "+jobName);
        }

        if(eventName.equals(PROJECT_DESTROY))
            System.out.println("Project destroyed on: "+retMap.get("path_with_namespace"));

    }
}


