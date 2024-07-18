package com.project.codetracker.service;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Map;

@Service
public class AppService {
    public Optional<Map<String, Object>> getRoutes() {
        try {
            String json = new String(Files.readAllBytes(Paths.get(new ClassPathResource("routes.json").getURI())));
            JSONObject routes = new JSONObject(json);
            return Optional.of(routes.toMap());
        } catch (IOException e) {
            System.out.println("Unable to read routes.json file");
        }

        return Optional.empty();
    }
}
