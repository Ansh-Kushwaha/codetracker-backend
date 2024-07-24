package com.project.codetracker.service;

import com.project.codetracker.model.CodeChefContestDetails;
import com.project.codetracker.model.CodeChefPastContests;
import com.project.codetracker.model.CodeChefProfile;
import com.project.codetracker.model.Contest;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CodeChefService {

    @Value("${codechef.base.url}")
    private String baseUrl;

    private final OkHttpClient client;

    public CodeChefService() {
        this.client = new OkHttpClient();
    }

    public Optional<CodeChefProfile> getProfile(String username) {
        String profileUrl = String.format("%s/users/%s", baseUrl, username);
        try {
            Document doc = Jsoup.connect(profileUrl).get();
            return parseUserProfile(doc);
        } catch (Exception e) {
            System.out.println("User profile not found");
        }

        return Optional.empty();
    }

    private Optional<CodeChefProfile> parseUserProfile(Document doc) {
        try {
            String username = Objects.requireNonNull(doc.select("span.m-username--link").first()).text();
            String name = Objects.requireNonNull(doc.select("div.user-details-container > header > h1.h2-style").first()).text();

            Element avatarElem = doc.select("div.user-details-container > header > img.profileImage").first();
            Element rankingElem = doc.select("a[href=/ratings/all] > strong").first();
            Element starsElem = doc.select("span.rating").first();
            Element ratingElem = doc.select("div.rating-number").first();
            Element countryElem = doc.select("span.user-country-name").first();
            Elements schoolElem = doc.select("section.user-details > ul.side-nav > li:contains(Institution:)");
            Element problemsSolvedElem = doc.select("section.rating-data-section > h3:contains(Total Problems Solved:)").first();

            String avatar = avatarElem == null ? "" : avatarElem.attr("src");
            Long ranking = rankingElem == null || rankingElem.text().equals("Inactive") ? -1 : Long.parseLong(rankingElem.text());
            Integer rating = ratingElem == null ? -1 : Integer.parseInt(ratingElem.text());
            Integer stars = starsElem == null ? 0 : Integer.parseInt(starsElem.text().charAt(0) + "");
            String country = countryElem == null ? "" : countryElem.text();
            String school = schoolElem.first() == null ? "" : schoolElem.select("span").text();
            Integer problemsSolved = problemsSolvedElem == null ? 0 : Integer.parseInt(problemsSolvedElem.text().split(" ")[3]);

            return Optional.of(new CodeChefProfile(username, name, avatar, ranking, rating, problemsSolved, stars, country, school));
        } catch(Exception e) {
            System.out.println("Can't load user data");
        }
        return Optional.empty();
    }

    public Optional<List<Contest>> getUpcomingContests() {
        String allContestsUrl = String.format("%s/api/list/contests/all", baseUrl);
        String response = fetchData(allContestsUrl);
        if (response.isEmpty())
            return Optional.empty();

        return parseUpcomingContests(response);
    }

    private Optional<List<Contest>> parseUpcomingContests(String response) {
        try {
            JSONArray upcomingContests = new JSONObject(response)
                    .getJSONArray("future_contests");
            List<Contest> contestList = new ArrayList<>();

            for (int i = 0; i < upcomingContests.length(); i++) {
                JSONObject contest = upcomingContests.getJSONObject(i);
                String id = contest.optString("contest_code");
                String title = contest.optString("contest_name");
                Long startTime = Instant.parse(contest.optString("contest_start_date_iso")).getEpochSecond();
                Long duration = contest.optLong("contest_duration") * 60; // Minutes -> Seconds

                contestList.add(new Contest(id, title, startTime, duration));
            }

            contestList.sort((a, b) -> (int) (a.startTime() - b.startTime()));
            return Optional.of(contestList);
        } catch (Exception e) {
            System.out.println("Error parsing upcoming contests: " + e);
        }

        return Optional.empty();
    }

    public Optional<CodeChefPastContests> getPastContests(String username) {
        String profileUrl = String.format("%s/users/%s", baseUrl, username);
        try {
            Document doc = Jsoup.connect(profileUrl).get();
            return parsePastContest(doc);
        } catch (Exception e) {
            System.out.println("User contest data not found");
        }

        return Optional.empty();
    }

    private Optional<CodeChefPastContests> parsePastContest(Document doc) {
        try {
            String document = doc.toString();
            int idxPre = document.indexOf("all_rating");
            if (idxPre == -1) return Optional.empty();
            int start = document.indexOf("[", idxPre), end = document.indexOf("];", idxPre);
            String contests = document.substring(start, end + 1);
            JSONArray contestData = new JSONArray(contests);

            Element contestAttendedElem = doc.select("div.contest-participated-count > b").first();
            Element currentRatingElem = doc.select("div.rating-number").first();
            Element highestRatingElem = doc.select("div.rating-header > small").first();
            Element countryRankingElem = doc.select("div.rating-ranks > ul > li > a").get(1);
            Element globalRankingElem = doc.select("div.rating-ranks > ul > li > a").get(0);

            Integer contestAttended = contestAttendedElem == null ? 0 : Integer.parseInt(contestAttendedElem.text());
            Integer currentRating = currentRatingElem == null ? -1 : Integer.parseInt(currentRatingElem.text());
            Integer highestRating = highestRatingElem == null ? -1 : Integer.parseInt(highestRatingElem.text().split(" ")[2].replaceAll(".$", ""));
            Long countryRanking = countryRankingElem.text().equals("Inactive") ? -1 : Long.parseLong(countryRankingElem.text());
            Long globalRanking = globalRankingElem.text().equals("Inactive") ? -1 : Long.parseLong(globalRankingElem.text());
            List<CodeChefContestDetails> contestDetailsList = new ArrayList<>();

            for (int i = 0; i < contestData.length(); i++) {
                JSONObject contestDetails = contestData.getJSONObject(i);
                String endDate = contestDetails.optString("end_date");
                Long endTime = LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toEpochSecond(ZoneOffset.of("+05:30"));
                Map<String, Object> contest = Map.of("title", contestDetails.optString("name"), "endTime", endTime);
                Integer rating = contestDetails.optInt("rating");
                Integer ranking = contestDetails.optInt("rank");

                contestDetailsList.add(new CodeChefContestDetails(contest, rating, ranking));
            }

            return Optional.of(new CodeChefPastContests(contestAttended, currentRating, highestRating, countryRanking, globalRanking, contestDetailsList));
        } catch (Exception e) {
            System.out.println("Error parsing past contests: " + e);
        }

        return Optional.empty();
    }

    @NotNull
    private Request getRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://codechef.com")
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
