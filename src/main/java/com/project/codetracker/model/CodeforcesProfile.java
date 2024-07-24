package com.project.codetracker.model;

public record CodeforcesProfile(
        String username,
        String name,
        String avatar,
        String rank,
        Integer rating,
        String country,
        String school
) {
}
