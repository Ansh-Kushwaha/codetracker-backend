package com.project.codetracker.model;

import java.util.*;

public record LeetcodePastContests(
        Integer contestAttended,
        Double contestRating,
        Long globalRanking,
        List<LeetcodeContestDetails> contestDetails
) {
}
