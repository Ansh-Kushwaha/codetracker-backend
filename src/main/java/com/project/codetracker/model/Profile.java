package com.project.codetracker.model;


import java.util.Map;

public record Profile (
    String username,
    String name,
    String avatar,
    Long Ranking,
    String country,
    String school,
    Map<String, String> links
) {
}
