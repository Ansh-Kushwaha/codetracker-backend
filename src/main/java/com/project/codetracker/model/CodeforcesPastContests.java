package com.project.codetracker.model;

import java.util.List;

public record CodeforcesPastContests(
        Integer contestAttended,
        Integer contestRating,
        String rank,
        List<CodeforcesContestDetails> contestDetails
) {
}
