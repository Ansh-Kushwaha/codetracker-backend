package com.project.codetracker.model;

public record Contest(
        String id,
        String title,
        Long startTime,
        Long duration
) {
}
