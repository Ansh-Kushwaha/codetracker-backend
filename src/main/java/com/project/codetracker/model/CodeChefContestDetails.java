package com.project.codetracker.model;

import java.util.Map;

public record CodeChefContestDetails(
        Map<String, Object> contest,
        Integer rating,
        Integer ranking
) {
}
