package com.project.codetracker.controller;

import com.project.codetracker.model.Contest;
import com.project.codetracker.model.LeetcodeProfile;
import com.project.codetracker.model.LeetcodePastContests;
import com.project.codetracker.service.LeetcodeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/leetcode")
public class LeetcodeController {
    private final LeetcodeService leetcodeService;

    public LeetcodeController(LeetcodeService leetcodeService) {
        this.leetcodeService = leetcodeService;
    }

    @GetMapping("/profile")
    public LeetcodeProfile getProfile(@RequestParam("user") String username) {
        return leetcodeService.getProfile(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/upcomingContests")
    public List<Contest> getUpcomingContests() {
        return leetcodeService.getUpcomingContests()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest data not found"));
    }

    @GetMapping("pastContests")
    public LeetcodePastContests getPastContests(@RequestParam("user") String username) {
        return leetcodeService.getPastContests(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User contest data not found"));
    }
}
