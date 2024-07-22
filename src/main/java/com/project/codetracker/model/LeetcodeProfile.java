package com.project.codetracker.model;


import java.util.Map;

public record LeetcodeProfile(
    String username,
    String name,
    String avatar,
    Long ranking,
    Map<String, Object> problemsSolved,
    String country,
    String school,
    Map<String, Object> links
) {
}
