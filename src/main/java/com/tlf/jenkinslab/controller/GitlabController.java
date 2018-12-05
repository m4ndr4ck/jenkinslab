package com.tlf.jenkinslab.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.ResourceLoader;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class GitlabController {

    String PROJECT_CREATE = "project_create";
    String PROJECT_DESTROY = "project_destroy";

    @Value( "${gitlab.host}" )
    private String gitlabHost;

    private String projectName;
    private String ownerName;
    private String configXMLString;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ApplicationContext context;


    @PostMapping("/newJob")
    public void newJob(@RequestBody String gitLabHook) throws Exception{

        System.out.println(gitLabHook);

        Map<String, Object> retMap = new Gson().fromJson(
                gitLabHook, new TypeToken<HashMap<String, Object>>() {}.getType()
        );

        String eventName = (String)retMap.get("event_name");
        projectName = (String)retMap.get("name");
        ownerName = (String)retMap.get("owner_name");
        String jobName = projectName+"-"+ownerName;

        if(eventName.equals(PROJECT_CREATE)) {
            System.out.println("Project created on: " + retMap.get("path_with_namespace"));
            System.out.println("Will create Jenkins job name: "+jobName);
            xmlConfigCreator(projectName, ownerName);
            createJob();
        }

        if(eventName.equals(PROJECT_DESTROY))
            System.out.println("Project destroyed on: "+retMap.get("path_with_namespace"));

    }

    @GetMapping("/test")
    public String xmlConfigCreator(String projectName, String ownerName) throws Exception{

        Resource resource = context.getResource("classpath:jenkinsjob.xml");

        String file = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        System.out.print(this.ownerName);

        configXMLString = file
                    .replace("HOST", gitlabHost)
                    .replace("PATH", this.ownerName)
                    .replace("JOB", this.projectName);

        return  configXMLString;
    }

    @PostMapping("/createJob")
    public void createJob() throws Exception{

        RestTemplate restTemplate =  new RestTemplate();
        //Create a list for the message converters
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        //Add the String Message converter
        messageConverters.add(new StringHttpMessageConverter());
        //Add the message converters to the restTemplate
        restTemplate.setMessageConverters(messageConverters);

        String plainCreds = "m4ndr4ck:1146b6d0b05b7c0e0f584f253550639365";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(configXMLString, headers);

        final ResponseEntity<String> response = restTemplate.postForEntity("http://129.213.136.120:8080/job/OSS/createItem?name="+projectName+"-"+ownerName, request, String.class);

    }



    public static void main (String... args) throws Exception{
        new GitlabController().createJob();
    }


}


