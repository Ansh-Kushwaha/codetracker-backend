package com.project.codetracker.controller;

import com.project.codetracker.model.CodeforcesPastContests;
import com.project.codetracker.model.CodeforcesProfile;
import com.project.codetracker.model.Contest;
import com.project.codetracker.service.CodeforcesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/codeforces")
public class CodeforcesController {

    private final CodeforcesService codeforcesService;

    public CodeforcesController(CodeforcesService codeforcesService) {
        this.codeforcesService = codeforcesService;
    }

    @GetMapping("/profile")
    public CodeforcesProfile getUserProfile(@RequestParam("user") String username) {
        return codeforcesService.getUserProfile(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/upcomingContests")
    public List<Contest> getUpcomingContests() {
        return codeforcesService.getUpcomingContests()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest data not found"));
    }

    @GetMapping("pastContests")
    public CodeforcesPastContests getPastContests(@RequestParam("user") String username) {
        return codeforcesService.getPastContests(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User contest data not found"));
    }
}
