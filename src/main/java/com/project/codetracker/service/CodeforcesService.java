package com.project.codetracker.service;

import com.project.codetracker.model.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CodeforcesService {

    @Value("${codeforces.api.url}")
    private String apiUrl;
    @Value("${codeforces.api.key}")
    private String apiKey;
    @Value("${codeforces.api.secret}")
    private String secret;
    private final OkHttpClient client;

    public CodeforcesService() {
        this.client = new OkHttpClient();
    }

    public Optional<CodeforcesProfile> getUserProfile(String username) {
        String response = fetchUserData(username);
        if (response.isEmpty())
            return Optional.empty();
        return parseUserProfile(response);
    }

    private String fetchUserData(String username) {
        Integer random = (new Random()).nextInt(999999 - 100000) + 100000;
        Long milliseconds = System.currentTimeMillis() / 1000;
        String input = String.format("%d/user.info?apiKey=%s&handles=%s&time=%d#%s", random, apiKey, username, milliseconds, secret);
        String hash = getHash(input);
        String url = String.format("%s/user.info?handles=%s&apiKey=%s&time=%d&apiSig=%d%s", apiUrl, username, apiKey, milliseconds, random, hash);
        return fetchData(url);
    }

    private Optional<CodeforcesProfile> parseUserProfile(String response) {
        try {
            JSONObject res = new JSONObject(response);
            if (!res.optString("status").equals("OK"))
                return Optional.empty();

            JSONObject result = res.getJSONArray("result").getJSONObject(0);
            String username = result.optString("handle");
            String name = result.optString("firstName");
            String avatar = result.optString("titlePhoto"); // more high res than avatar
            String rank = result.optString("rank");
            Integer rating = result.optInt("rating");
            String country = result.optString("country");
            String school = result.optString("organization");

            return Optional.of(new CodeforcesProfile(username, name, avatar, rank, rating, country, school));
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
        Integer random = (new Random()).nextInt(999999 - 100000) + 100000;
        Long milliseconds = System.currentTimeMillis() / 1000;
        String input = String.format("%d/contest.list?apiKey=%s&time=%d#%s", random, apiKey, milliseconds, secret);
        String hash = getHash(input);
        String url = String.format("%s/contest.list?&apiKey=%s&time=%d&apiSig=%d%s", apiUrl, apiKey, milliseconds, random, hash);
        return fetchData(url);
    }

    private Optional<List<Contest>> parseUpcomingContests(String response) {
        try {
            JSONObject res = new JSONObject(response);
            if (!res.optString("status").equals("OK"))
                return Optional.empty();

            JSONArray result = res.getJSONArray("result");
            List<Contest> contestList = new ArrayList<>();

            for (int i = 0; i < result.length(); i++) {
                JSONObject contest = result.getJSONObject(i);
                if (contest.optString("phase").equals("FINISHED")) break;
                String id = contest.optString("id");
                String title = contest.optString("name");
                Long startTime = contest.optLong("startTimeSeconds");
                Long duration = contest.optLong("durationSeconds");

                contestList.add(new Contest(id, title, startTime, duration));
            }

            contestList.sort((a, b) -> (int) (a.startTime() - b.startTime()));
            return Optional.of(contestList);
        } catch (Exception e) {
            System.err.println("Error parsing upcoming contests: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<CodeforcesPastContests> getPastContests(String username) {
        String userProfile = fetchUserData(username);
        String userContests = fetchPastContests(username);

        if (userProfile.isEmpty() || userContests.isEmpty())
            return Optional.empty();

        return parsePastContests(userProfile, userContests);
    }

    private String fetchPastContests(String username) {
        Integer random = (new Random()).nextInt(999999 - 100000) + 100000;
        Long milliseconds = System.currentTimeMillis() / 1000;
        String input = String.format("%d/user.rating?apiKey=%s&handle=%s&time=%d#%s", random, apiKey, username, milliseconds, secret);
        String hash = getHash(input);
        String url = String.format("%s/user.rating?handle=%s&apiKey=%s&time=%d&apiSig=%d%s", apiUrl, username, apiKey, milliseconds, random, hash);
        return fetchData(url);
    }

    private Optional<CodeforcesPastContests> parsePastContests(String userProfile, String pastContests) {
        try {
            JSONObject profileRes = new JSONObject(userProfile);
            JSONObject contestRes = new JSONObject(pastContests);
            if (!profileRes.optString("status").equals("OK") || !contestRes.optString("status").equals("OK"))
                return Optional.empty();

            JSONObject profileResult = profileRes.getJSONArray("result").getJSONObject(0);
            JSONArray contestResult = contestRes.getJSONArray("result");

            Integer contestRating = profileResult.optInt("rating");
            String rank = profileResult.optString("rank");
            Integer contestAttended = contestResult.length();
            List<CodeforcesContestDetails> contestDetailsList = new ArrayList<>();

            for (int i = 0; i < contestResult.length(); i++) {
                JSONObject contestDetails = contestResult.getJSONObject(i);
                String title = contestDetails.optString("contestName");
                Long updateTime = contestDetails.optLong("ratingUpdateTimeSeconds");
                Map<String, Object> contest = Map.of("title", title, "ratingUdpatedAt", updateTime);
                Integer newRating = contestDetails.optInt("newRating");
                Integer oldRating = contestDetails.optInt("oldRating");
                Long ranking = contestDetails.optLong("rank");
                String trend = newRating - oldRating >= 0 ? newRating - oldRating == 0 ? "NONE" : "UP" : "DOWN";
                contestDetailsList.add(new CodeforcesContestDetails(contest, newRating, ranking, trend));
            }

            return Optional.of(new CodeforcesPastContests(contestAttended, contestRating, rank, contestDetailsList));
        } catch (Exception e) {
            System.err.println("Error parsing past contests: " + e.getMessage());
        }
        return Optional.empty();
    }

    private String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            BigInteger num = new BigInteger(1, md.digest(input.getBytes()));
            StringBuilder hashText = new StringBuilder(num.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();

        } catch(NoSuchAlgorithmException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }

    @NotNull
    private Request getRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://codeforces.com")
                .build();
    }

    private String fetchData(String url) {
        Request request = getRequest(url);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body() == null ? "" : response.body().string();
        } catch (IOException e) {
            System.out.println("Exception: " + e);
        }

        return "";
    }
}
