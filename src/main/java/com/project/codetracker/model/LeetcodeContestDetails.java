package com.project.codetracker.model;

import java.util.Map;

public record LeetcodeContestDetails(
        Map<String, Object> contest,
        Double rating,
        Long ranking,
        Integer problemsSolved,
        Integer totalProblems,
        String trendDirection

) {
}
