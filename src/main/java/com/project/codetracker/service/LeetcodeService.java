package com.project.codetracker.service;

import com.project.codetracker.model.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class LeetcodeService {

    @Value("${leetcode.api.url}")
    private String apiUrl;
    private final OkHttpClient client;

    public LeetcodeService() {
        this.client = new OkHttpClient();
    }

    public Optional<LeetcodeProfile> getProfile(String username) {
        String response = fetchUserProfile(username);
        if (response.isEmpty())
            return Optional.empty();

        return parseUserProfile(response);
    }

    private String fetchUserProfile(String username) {
        String query = loadQuery("getUserProfile");
        JSONObject jsonObject = new JSONObject(), variables = new JSONObject();
        jsonObject.put("query", query);
        variables.put("username", username);
        jsonObject.put("variables", variables);
        Request request = getRequest(jsonObject);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<LeetcodeProfile> parseUserProfile(String response) {
        try {
            JSONObject data = new JSONObject(response)
                    .getJSONObject("data")
                    .getJSONObject("matchedUser");
            JSONObject profile = data.getJSONObject("profile");
            JSONArray submitStats = data.getJSONObject("submitStatsGlobal")
                    .getJSONArray("acSubmissionNum");

            Integer solvedProblem = submitStats.getJSONObject(0).optInt("count");
            Integer easySolved = submitStats.getJSONObject(1).optInt("count");
            Integer mediumSolved = submitStats.getJSONObject(2).optInt("count");
            Integer hardSolved = submitStats.getJSONObject(3).optInt("count");

            String userName = data.optString("username", "");
            String githubUrl = data.optString("githubUrl", "");
            String twitterUrl = data.optString("twitterUrl", "");
            String linkedinUrl = data.optString("linkedinUrl", "");

            Map<String, Object> problemsSolved = Map.of("all", solvedProblem, "easySolved", easySolved, "mediumSolved", mediumSolved, "hardSolved", hardSolved);
            Map<String, Object> links = Map.of("githubUrl", githubUrl, "twitterUrl", twitterUrl, "linkedInUrl", linkedinUrl);

            String realName = profile.optString("realName", "");
            String userAvatar = profile.optString("userAvatar", "");
            Long ranking = profile.optLong("ranking", -1L);
            String countryName = profile.optString("countryName", "");
            String school = profile.optString("school", "");

            return Optional.of(new LeetcodeProfile(userName, realName, userAvatar, ranking, problemsSolved, countryName, school, links));
        } catch (Exception e) {
            System.err.println("Error parsing user profile: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<List<Contest>> getUpcomingContests() {
        String response = fetchUpcomingContests();
        if (response.isEmpty())
            return Optional.empty();

        return parseUpcomingContests(response);
    }

    private String fetchUpcomingContests() {
        String query = loadQuery("getUpComingContests");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", query);
        Request request = getRequest(jsonObject);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<List<Contest>> parseUpcomingContests(String response) {
        try {
            JSONArray upcomingContests = new JSONObject(response)
                    .getJSONObject("data")
                    .getJSONArray("upcomingContests");
            List<Contest> contestList = new ArrayList<>();

            for (int i = 0; i < upcomingContests.length(); i++) {
                JSONObject contest = upcomingContests.getJSONObject(i);
                String id = contest.optString("titleSlug");
                String title = contest.optString("title");
                Long startTime = contest.optLong("startTime");
                Long duration = contest.optLong("duration");

                contestList.add(new Contest(id, title, startTime, duration));
            }

            contestList.sort((a, b) -> (int) (a.startTime() - b.startTime()));
            return Optional.of(contestList);
        } catch (Exception e) {
            System.err.println("Error parsing upcoming contests: " + e);
        }
        return Optional.empty();
    }

    public Optional<LeetcodePastContests> getPastContests(String username) {
        String response = fetchPastContests(username);
        if (response.isEmpty())
            return Optional.empty();

        return parsePastContests(response);
    }

    private String fetchPastContests(String username) {
        String query = loadQuery("getUserContestInfo");
        JSONObject jsonObject = new JSONObject(), variables = new JSONObject();
        jsonObject.put("query", query);
        variables.put("username", username);
        jsonObject.put("variables", variables);
        Request request = getRequest(jsonObject);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<LeetcodePastContests> parsePastContests(String response) {
        try {
            JSONObject data = new JSONObject(response)
                    .getJSONObject("data");
            JSONObject userData = data.getJSONObject("userContestRanking");
            JSONArray contestData = data.getJSONArray("userContestRankingHistory");

            Integer contestAttended = userData.optInt("attendedContestsCount");
            Double contestRating = userData.optDouble("rating");
            Long globalRanking = userData.optLong("globalRanking");
            List<LeetcodeContestDetails> contestDetailsList = new ArrayList<>();

            for (int i = 0; i < contestData.length(); i++) {
                JSONObject contestDetails = contestData.getJSONObject(i);
                boolean attended = contestDetails.optBoolean("attended");
                if (!attended) continue;
                Map<String, Object> contest = Map.of("title", contestDetails.getJSONObject("contest").optString("title"), "startTime", contestDetails.getJSONObject("contest").optLong("startTime"));
                Double rating = contestDetails.optDouble("rating");
                Long ranking = contestDetails.optLong("ranking");
                Integer problemsSolved = contestDetails.optInt("problemsSolved");
                Integer totalProblems = contestDetails.optInt("totalProblems");
                String trend = contestDetails.optString("trendDirection");

                contestDetailsList.add(new LeetcodeContestDetails(contest, rating, ranking, problemsSolved, totalProblems, trend));
            }
            return Optional.of(new LeetcodePastContests(contestAttended, contestRating, globalRanking, contestDetailsList));
        } catch (Exception e) {
            System.err.println("Error parsing past contests: " + e.getMessage());
        }
        return Optional.empty();
    }

    private String loadQuery(String queryName) {
        try {
            return new String(Files.readAllBytes(Paths.get(new ClassPathResource("graphql-queries/" + queryName + ".graphql").getURI())));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read GraphQL query file", e);
        }
    }

    @NotNull
    private Request getRequest(JSONObject jsonObject) {
        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        return new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com")
                .build();
    }
}
