package com.project.codetracker.model;

import java.util.Map;

public record CodeforcesContestDetails(
        Map<String, Object> contest,
        Integer rating,
        Long ranking,
        String trendDirection
) {
}
