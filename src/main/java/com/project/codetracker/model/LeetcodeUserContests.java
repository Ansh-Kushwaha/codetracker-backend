package com.project.codetracker.model;

import java.util.*;

public record LeetcodeUserContests(
        Integer contestAttended,
        Double contestRating,
        Long globalRanking,
        List<LeetcodeParticipationDetails> contestParticipation
) {
}
