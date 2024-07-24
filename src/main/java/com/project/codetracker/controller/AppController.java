package com.project.codetracker.controller;

import com.project.codetracker.model.Contest;
import com.project.codetracker.service.AppService;
import com.project.codetracker.service.CodeChefService;
import com.project.codetracker.service.LeetcodeService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)

@RestController
public class AppController {

    private final AppService appService;
    private final LeetcodeService leetcodeService;
    private final CodeChefService codeChefService;

    public AppController(AppService appService, LeetcodeService leetcodeService, CodeChefService codeChefService) {
        this.appService = appService;
        this.leetcodeService = leetcodeService;
        this.codeChefService = codeChefService;
    }
    @GetMapping("/")
    public Map<String, Object> routes() {
        return appService.getRoutes()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Routes not found"));
    }
}
