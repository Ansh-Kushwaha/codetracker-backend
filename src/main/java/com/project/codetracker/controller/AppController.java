package com.project.codetracker.controller;

import com.project.codetracker.service.AppService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class AppController {

    private final AppService appService;

    public AppController(AppService appService) {
        this.appService = appService;
    }
    @GetMapping("/")
    public Map<String, Object> routes() {
        return appService.getRoutes()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Routes not found"));
    }
}
