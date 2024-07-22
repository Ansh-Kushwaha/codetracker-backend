package com.project.codetracker.controller;

import com.project.codetracker.model.CodeChefPastContests;
import com.project.codetracker.model.CodeChefProfile;
import com.project.codetracker.model.Contest;
import com.project.codetracker.service.CodeChefService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/codechef")
public class CodeChefController {
    private final CodeChefService codeChefService;

    public CodeChefController(CodeChefService codeChefService) {
        this.codeChefService = codeChefService;
    }

    @GetMapping("/profile")
    public CodeChefProfile getProfile(@RequestParam("user") String username) {
        return codeChefService.getProfile(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/upcomingContests")
    public List<Contest> getUpcomingContests() {
        return codeChefService.getUpcomingContests()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest data not found"));
    }

    @GetMapping("pastContests")
    public CodeChefPastContests getPastContests(@RequestParam("user") String username) {
        return codeChefService.getPastContests(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User contest data not found"));
    }
}
