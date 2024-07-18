package com.project.codetracker.service;

import com.project.codetracker.model.*;
import okhttp3.*;
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

    public Optional<Profile> getProfile(String username) {
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

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com")
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<Profile> parseUserProfile(String response) {
        try {
            JSONObject data = new JSONObject(response)
                    .getJSONObject("data")
                    .getJSONObject("matchedUser");
            JSONObject profile = data.getJSONObject("profile");

            String userName = data.optString("username", "");
            String githubUrl = data.optString("githubUrl", "");
            String twitterUrl = data.optString("twitterUrl", "");
            String linkedinUrl = data.optString("linkedinUrl", "");

            Map<String, String> links = Map.of("githubUrl", githubUrl, "twitterUrl", twitterUrl, "linkedInUrl", linkedinUrl);

            String realName = profile.optString("realName", "");
            String userAvatar = profile.optString("userAvatar", "");
            Long ranking = profile.optLong("ranking", -1L);
            String countryName = profile.optString("countryName", "");
            String school = profile.optString("school", "");

            return Optional.of(new Profile(userName, realName, userAvatar, ranking, countryName, school, links));
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

        RequestBody body = RequestBody.create(
            jsonObject.toString(),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com")
                .build();


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

            return Optional.of(contestList);
        } catch (Exception e) {
            System.err.println("Error parsing user profile: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<LeetcodeSolved> getSolved(String username) {
        String response = fetchProblemsSolved(username);
        System.out.println(response);
        if (response.isEmpty())
            return Optional.empty();

        return parseProblemsSolved(response);
    }

    private String fetchProblemsSolved(String username) {
        String query = loadQuery("getProblemsSolved");
        JSONObject jsonObject = new JSONObject(), variables = new JSONObject();
        jsonObject.put("query", query);
        variables.put("username", username);
        jsonObject.put("variables", variables);

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<LeetcodeSolved> parseProblemsSolved(String response) {
        try {
            JSONArray statistics = new JSONObject(response)
                    .getJSONObject("data")
                    .getJSONObject("matchedUser")
                    .getJSONObject("submitStatsGlobal")
                    .getJSONArray("acSubmissionNum");

            int totalSolved = statistics.getJSONObject(0).optInt("count");
            int easySolved = statistics.getJSONObject(1).optInt("count");
            int mediumSolved = statistics.getJSONObject(2).optInt("count");
            int hardSolved = statistics.getJSONObject(3).optInt("count");

            return Optional.of(new LeetcodeSolved(totalSolved, easySolved, mediumSolved, hardSolved));
        } catch (Exception e) {
            System.err.println("Error parsing user profile: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<LeetcodeUserContests> getPastContests(String username) {
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

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com")
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    private Optional<LeetcodeUserContests> parsePastContests(String response) {
        try {
            JSONObject data = new JSONObject(response)
                    .getJSONObject("data");
            JSONObject userData = data.getJSONObject("userContestRanking");
            JSONArray contestData = data.getJSONArray("userContestRankingHistory");

            int contestAttended = userData.optInt("attendedContestsCount");
            double contestRating = userData.optDouble("rating");
            long globalRanking = userData.optLong("globalRanking");
            List<LeetcodeParticipationDetails> participationDetails = new ArrayList<>();

            for (int i = 0; i < contestData.length(); i++) {
                JSONObject contestDetails = contestData.getJSONObject(i);
                boolean attended = contestDetails.optBoolean("attended");
                if (!attended) continue;
                double rating = contestDetails.optDouble("rating");
                long ranking = contestDetails.optLong("ranking");
                String trend = contestDetails.optString("trendDirection");
                int problemsSolved = contestDetails.optInt("problemsSolved");
                int totalProblems = contestDetails.optInt("totalProblems");
                Map<String, Object> contest = Map.of("title", contestDetails.getJSONObject("contest").optString("title"), "startTime", contestDetails.getJSONObject("contest").optLong("startTime"));

                participationDetails.add(new LeetcodeParticipationDetails(rating, ranking, trend, problemsSolved, totalProblems, contest));
            }
            return Optional.of(new LeetcodeUserContests(contestAttended, contestRating, globalRanking, participationDetails));
        } catch (Exception e) {
            System.err.println("Error parsing user profile: " + e.getMessage());
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
}
