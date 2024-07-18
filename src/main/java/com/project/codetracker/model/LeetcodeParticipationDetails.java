package com.project.codetracker.model;

import java.util.Map;

public record LeetcodeParticipationDetails(
        Double rating,
        Long ranking,
        String trendDirection,
        Integer problemsSolved,
        Integer totalProblems,

        Map<String, Object> contest
) {
}
