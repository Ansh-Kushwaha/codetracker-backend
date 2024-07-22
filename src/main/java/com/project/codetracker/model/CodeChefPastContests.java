package com.project.codetracker.model;

import java.util.List;

public record CodeChefPastContests(
        Integer contestAttended,
        Integer currentRating,
        Integer highestRating,
        Long countryRanking,
        Long globalRanking,
        List<CodeChefContestDetails> contestDetails
) {
}
