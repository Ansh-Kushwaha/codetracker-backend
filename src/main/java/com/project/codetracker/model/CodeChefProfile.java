package com.project.codetracker.model;

import java.util.Map;

public record CodeChefProfile (
        String username,
        String name,
        String avatar,
        Long ranking,
        Integer rating,
        Integer problemsSolved,
        Integer stars,
        String country,
        String school
) {

}
