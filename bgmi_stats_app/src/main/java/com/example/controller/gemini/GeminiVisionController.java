package com.example.controller.gemini;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.model.player.DualMatchResultModel;
import com.example.model.player.MatchExtractionResultModel;

public class GeminiVisionController {

    private static final String API_KEY = "AIzaSyCfaHXevra37pJAyyo7_B68K66AkgAzpeg";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    public static MatchExtractionResultModel sendGeminiRequestData(List<File> imageFiles) {

        try {

            if (imageFiles == null || imageFiles.isEmpty()) {
                return createErrorModel("No evidence screenshots were provided.");
            }

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Request body

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject contentObject = new JSONObject();
            contentObject.put("role", "user");

            JSONArray partsArray = new JSONArray();

            // COMPLETE PROMPT AS TEXT INPUT
            String promptText = """
                    You are an expert OCR and data extraction AI specialized in analyzing video game UI, specifically Battle Royale post-match statistics screens. Your objective is to scan the provided images and extract the data into a strictly structured JSON format.

                    ### CONTEXT
                    The attached images contain end-of-match statistics, including a general match summary, player-specific combat stats, damage distribution across body parts, and specific weapon performance metrics.

                    ### STRICT RULES & CONSTRAINTS
                    1. You must output ONLY a valid, parseable JSON object. Do not include markdown formatting (like ```json), conversational text, greetings, or explanations.
                    2. Ensure data types are strictly followed (Numbers must be integers/floats without quotes, Booleans must be true/false, Strings must be in quotes).
                    3. MISSING DATA FALLBACK: If a specific piece of data cannot be found, is illegible, or you are unsure of its value, you MUST use the following defaults:
                       - Use `0` for numerical fields (integers or floats).
                       - Use `false` for boolean fields.
                       - Use `"N/A"` for string fields.
                    4. "Finishes" on the screen corresponds to the `kills` field in the JSON.
                    5. For `damageDistribution`, extract the integer number of hits (e.g., if the screen says "1/100.0%", extract `1`).
                    6. The `confidenceScore` should be a float representing your overall certainty in the OCR extraction (e.g., 98.5).

                    ### JSON SCHEMA
                    Your output must exactly match this JSON structure:

                    {
                      "success": true,
                      "confidenceScore": 0.0,
                      "matchSummary": {
                        "placement": 0,
                        "totalPlayers": 0,
                        "matchResult": "N/A",
                        "mapName": "N/A",
                        "gameMode": "N/A",
                        "perspective": "N/A",
                        "rating": 0.0,
                        "kills": 0,
                        "assists": 0,
                        "damage": 0.0,
                        "survivalTime": "N/A",
                        "rescues": 0,
                        "recalls": 0
                      },
                      "combatStatistics": {
                        "travelDistanceKm": 0.0,
                        "survivalRating": 0.0,
                        "supportRating": 0.0,
                        "healthRestored": 0.0,
                        "suppliesRating": 0.0,
                        "totalSupplies": 0,
                        "advancedSupplies": 0
                      },
                      "damageDistribution": {
                        "head": 0,
                        "arms": 0,
                        "body": 0,
                        "hands": 0,
                        "feet": 0
                      },
                      "weaponStatistics": [
                        {
                          "weaponName": "N/A",
                          "weaponType": "N/A",
                          "damage": 0.0,
                          "accuracy": 0.0,
                          "headshotRate": 0.0,
                          "eliminations": 0,
                          "knockOuts": 0,
                          "shotsFired": 0,
                          "firearmPower": 0
                        }
                      ],
                      "meleeWeapon": {
                        "weaponName": "N/A",
                        "damage": 0.0
                      },
                      "shootingDistance": {
                        "0-30m": 0,
                        "30-120m": 0,
                        "120-300m": 0,
                        "300-1000m": 0
                      },
                      "imageVerification": {
                        "resultScreenDetected": false,
                        "detailedStatisticsDetected": false,
                        "weaponStatisticsDetected": false
                      },
                      "warnings": [],
                      "missingFields": []
                    }

                    Analyze the images carefully and populate the JSON.
                                        """;

            JSONObject textPart = new JSONObject();
            textPart.put("text", promptText);
            partsArray.put(textPart);

            // INPUT AS IMAGES FILE
            for (File file : imageFiles) {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                String base64Image = Base64.getEncoder().encodeToString(fileContent);

                JSONObject inlineData = new JSONObject();
                inlineData.put("mimeType", getMimeType(file));
                inlineData.put("data", base64Image);

                JSONObject imagePart = new JSONObject();
                imagePart.put("inlineData", inlineData);
                partsArray.put(imagePart);
            }

            contentObject.put("parts", partsArray);
            contents.put(contentObject);
            requestBody.put("contents", contents);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // OUTPUT OF GEMINI IS FROM HERE BELOW

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(response.toString());
                }

                // Extract the AI's answer text from the response structure
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    String aiJsonString = firstCandidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0)
                            .getString("text");
                    return parseGeminiJsonToModel(aiJsonString);
                }
                return createErrorModel("AI returned empty content.");
            } else {
                StringBuilder errorBody = new StringBuilder();
                if (conn.getErrorStream() != null) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null)
                            errorBody.append(line);
                    }
                }
                System.err.println("API Error " + responseCode + ": " + errorBody);
                return createErrorModel("API Error: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorModel("Exception: " + e.getMessage());
        }
    }

    // Helper to determine the proper image MIME type
    // This method tels which file is uploaded as input to gemini, whether it is
    // png, jpg, jpeg, webp
    private static String getMimeType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg"; // Default for .jpg / .jpeg
    }

    // Maps the AI's JSON output directly into your Model using org.json
    // JSON TO MODEL CONVERTER TO SAVE INTO FIRESTORE DATABASE
    private static MatchExtractionResultModel parseGeminiJsonToModel(String aiJsonString) {
        MatchExtractionResultModel model = new MatchExtractionResultModel();
        try {
            String cleanJson = aiJsonString.replaceAll("(?i)^```json\\s*", "").replaceAll("\\s*```$", "").trim();
            JSONObject data = new JSONObject(cleanJson);

            model.setExtractionSuccessful(data.optBoolean("success", false));
            model.setConfidenceScore(data.optDouble("confidenceScore", 0.0));

            JSONObject summary = data.optJSONObject("matchSummary");
            if (summary != null) {
                model.setTeamPlacement(summary.optInt("placement", 0));
                model.setTotalPlayers(summary.optInt("totalPlayers", 0));
                model.setMatchResult(summary.optString("matchResult", "N/A"));
                model.setMap(summary.optString("mapName", "N/A"));
                model.setGameMode(summary.optString("gameMode", "N/A"));
                model.setPerspective(summary.optString("perspective", "N/A"));
                model.setRating(summary.optDouble("rating", 0.0));

                model.setKills(summary.optInt("kills", 0));
                model.setAssists(summary.optInt("assists", 0));
                model.setDamage((int) summary.optDouble("damage", 0.0));
                model.setSurvivalTime(summary.optString("survivalTime", "00:00"));

                model.setRescues(summary.optInt("rescues", 0));
                model.setRecalls(summary.optInt("recalls", 0));

                model.setMatchDate(summary.optString("matchDate", "N/A"));
                model.setRole(summary.optString("role", "N/A"));
                model.setMainWeapon(summary.optString("mainWeapon", "N/A"));
                model.setMvp(summary.optBoolean("mvp", false));
                model.setTeamGrade(summary.optDouble("teamGrade", 0.0));
            }

            JSONObject combat = data.optJSONObject("combatStatistics");
            if (combat != null) {
                model.setTravelDistance(combat.optDouble("travelDistanceKm", 0.0));
                model.setSurvivalScore(combat.optDouble("survivalRating", 0.0));
                model.setSupportScore(combat.optDouble("supportRating", 0.0));
                model.setHealthRestored((int) combat.optDouble("healthRestored", 0.0));
                model.setSuppliesScore(combat.optDouble("suppliesRating", 0.0));
                model.setTotalSupplies(combat.optInt("totalSupplies", 0));
                model.setAdvancedSupplies(combat.optInt("advancedSupplies", 0));
            }

            JSONArray weapons = data.optJSONArray("weaponStatistics");
            if (weapons != null && weapons.length() > 0) {
                JSONObject firstWeapon = weapons.getJSONObject(0);
                model.setWeaponName(firstWeapon.optString("weaponName", "N/A"));
                model.setWeaponType(firstWeapon.optString("weaponType", "N/A"));
                model.setWeaponDamage(firstWeapon.optDouble("damage", 0.0));
                model.setWeaponEliminations(firstWeapon.optInt("eliminations", 0));
                model.setWeaponKnockdowns(firstWeapon.optInt("knockOuts", 0));
                model.setWeaponUses(firstWeapon.optInt("shotsFired", 0));

                model.setCloseRangeThrows(data.optInt("closeRangeThrows", 0));
                model.setLongRangeThrows(data.optInt("longRangeThrows", 0));
            }
            return model;

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorModel("Failed to map AI JSON to Model: " + e.getMessage());
        }
    }

    // ERROR MODEL
    /*
     * This method will be helping out with the error model that is no data state
     * with Extraction false and warning so UI will showcase in that way.
     */
    private static MatchExtractionResultModel createErrorModel(String warning) {
        MatchExtractionResultModel model = new MatchExtractionResultModel();
        model.setExtractionSuccessful(false);
        model.addWarning(warning);
        return model;
    }

    // ==========================================
    // LIGHTWEIGHT TDM BATTLE VERIFICATION
    // ==========================================

    public static class BattleResultExtraction {
        public String winnerSide; // "A", "B", or "UNCLEAR"
        public double confidence;
    }

    public static BattleResultExtraction extractBattleWinner(File screenshot, String gameTitle) {
        BattleResultExtraction result = new BattleResultExtraction();
        try {
            String prompt = "";

            // DYNAMIC PROMPTS BASED ON GAME TITLE
            switch (gameTitle != null ? gameTitle.toUpperCase() : "BGMI") {
                case "VALORANT":
                    prompt = "This is a Valorant match result screen. Look for the 'MATCH WON' or 'MATCH LOST' text, " +
                            "or check the final scoreboard team colors (Blue vs Red). " +
                            "If the perspective player's team won, respond with {\"winnerSide\": \"A\", \"confidence\": 0.99}. "
                            +
                            "If they lost, use 'B'. Use 'UNCLEAR' if missing.";
                    break;
                case "FREE_FIRE":
                    prompt = "This is a Free Fire match result screen (Booyah screen). Look for the 'BOOYAH' or 'DEFEAT' text. "
                            +
                            "If the perspective player's team won (Booyah), respond with {\"winnerSide\": \"A\", \"confidence\": 0.99}. "
                            +
                            "If they lost, use 'B'. Use 'UNCLEAR' if missing.";
                    break;
                case "BGMI":
                default:
                    prompt = "This is a BGMI team deathmatch result screen. Look at the top center of the image. " +
                            "If you see the word 'VICTORY' in large gold/yellow text, the perspective player won. " +
                            "If you see the word 'DEFEAT' in large red text, the perspective player lost. " +
                            "Respond ONLY with a JSON object exactly like this: {\"winnerSide\": \"A\", \"confidence\": 0.99}. "
                            +
                            "Use 'A' if the perspective player won, 'B' if the perspective player lost, or 'UNCLEAR' if the text is missing.";
                    break;
            }

            // Call the lightweight helper method
            String jsonResponse = analyzeBattleScreenshot(screenshot, prompt);

            // Parse the response
            if (jsonResponse.contains("\"winnerSide\": \"A\"") || jsonResponse.contains("\"winnerSide\":\"A\"")) {
                result.winnerSide = "A";
            } else if (jsonResponse.contains("\"winnerSide\": \"B\"")
                    || jsonResponse.contains("\"winnerSide\":\"B\"")) {
                result.winnerSide = "B";
            } else {
                result.winnerSide = "UNCLEAR";
            }

            result.confidence = 0.90;
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.winnerSide = "UNCLEAR";
            return result;
        }
    }

    // Helper method to make the HTTP call specifically for short prompts
    private static String analyzeBattleScreenshot(File screenshot, String promptText) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObject = new JSONObject();
        contentObject.put("role", "user");

        JSONArray partsArray = new JSONArray();

        JSONObject textPart = new JSONObject();
        textPart.put("text", promptText);
        partsArray.put(textPart);

        byte[] fileContent = Files.readAllBytes(screenshot.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);

        JSONObject inlineData = new JSONObject();
        inlineData.put("mimeType", getMimeType(screenshot)); // Reuses your existing getMimeType helper
        inlineData.put("data", base64Image);

        JSONObject imagePart = new JSONObject();
        imagePart.put("inlineData", inlineData);
        partsArray.put(imagePart);

        contentObject.put("parts", partsArray);
        contents.put(contentObject);
        requestBody.put("contents", contents);

        JSONObject generationConfig = new JSONObject();
        generationConfig.put("responseMimeType", "application/json");
        requestBody.put("generationConfig", generationConfig);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null)
                    response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("candidates").getJSONObject(0).getJSONObject("content")
                    .getJSONArray("parts").getJSONObject(0).getString("text");
        } else {
            throw new Exception("API Error: " + conn.getResponseCode());
        }
    }

    /**
     * Sends TWO images to Gemini simultaneously to cross-reference and verify the
     * match.
     */
    public static DualMatchResultModel analyzeDualMatchImages(byte[] imageABytes, byte[] imageBBytes,
            String gameTitle) {
        DualMatchResultModel result = new DualMatchResultModel();

        try {
            String base64ImageA = Base64.getEncoder().encodeToString(imageABytes);
            String base64ImageB = Base64.getEncoder().encodeToString(imageBBytes);

            String gameContext = (gameTitle != null) ? gameTitle : "BGMI";

            // The strict referee prompt mapped to the screenshot layout
            String prompt = "You are an eSports referee verifying a " + gameContext + " match. " +
                    "You are provided two screenshots. Image 1 is from Team A's perspective. Image 2 is from Team B's perspective. "
                    +
                    "1. Verify if both images are from the exact same match by cross-referencing final scores and IGNs. "
                    +
                    "2. Determine the winning team based ONLY on the perspective of Image 1 (Team A). " +
                    "3. Extract the final score (top corners) and the MVP stats (Finishes, F/D, Assists) for both teams. "
                    +
                    "Respond ONLY with a valid JSON object matching this exact structure, nothing else: " +
                    "{\"winnerSide\": \"A\", \"isSameMatch\": true, \"confidence\": 0.99, " +
                    "\"teamA\": {\"score\": 23, \"mvpIgn\": \"CLUMSYog\", \"kills\": 23, \"fdRatio\": 1.5, \"assists\": 0}, "
                    +
                    "\"teamB\": {\"score\": 16, \"mvpIgn\": \"JORDJOHNNYxYT\", \"kills\": 16, \"fdRatio\": 0.7, \"assists\": 0}}";

            // Assuming you have a standard HTTP client setup for Gemini in this class:
            // Build the multi-part payload with BOTH images
            String jsonPayload = "{" +
                    "\"contents\": [{" +
                    "\"parts\": [" +
                    "{\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}," +
                    "{\"inline_data\": {\"mime_type\": \"image/jpeg\", \"data\": \"" + base64ImageA + "\"}}," +
                    "{\"inline_data\": {\"mime_type\": \"image/jpeg\", \"data\": \"" + base64ImageB + "\"}}" +
                    "]" +
                    "}]" +
                    "}";

            // Make the HTTP POST request to the Gemini API
            String jsonResponse = sendHttpRequestToGemini(jsonPayload); // Replace with your actual internal HTTP call
                                                                        // method

            // Safely parse the JSON response
            if (jsonResponse.contains("\"winnerSide\": \"A\"") || jsonResponse.contains("\"winnerSide\":\"A\"")) {
                result.winnerSide = "A";
            } else if (jsonResponse.contains("\"winnerSide\": \"B\"")
                    || jsonResponse.contains("\"winnerSide\":\"B\"")) {
                result.winnerSide = "B";
            } else {
                result.winnerSide = "UNCLEAR";
            }

            result.isSameMatch = jsonResponse.contains("\"isSameMatch\": true")
                    || jsonResponse.contains("\"isSameMatch\":true");

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.winnerSide = "UNCLEAR";
            result.isSameMatch = false;
            return result;
        }
    }

    private static String sendHttpRequestToGemini(String jsonPayload) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null)
                    response.append(line);
            }
            return response.toString();
        } else {
            throw new Exception("Gemini API Error: HTTP " + conn.getResponseCode());
        }
    }

    /**
     * Validates a single image when the opponent times out to prevent fake-upload
     * forfeits.
     */
    public static String validateSingleMatchImage(byte[] imageBytes, String gameTitle) {
        try {
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String gameContext = (gameTitle != null) ? gameTitle : "BGMI";

            String prompt = "You are an eSports referee verifying a " + gameContext + " match forfeit claim. " +
                    "Look at this single screenshot. Determine if it is a legitimate victory screen. " +
                    "Respond ONLY with a JSON object: {\"winnerSide\": \"A\"} if the perspective player clearly won, " +
                    "{\"winnerSide\": \"B\"} if they lost, or {\"winnerSide\": \"UNCLEAR\"} if invalid/unrelated.";

            String jsonPayload = "{" +
                    "\"contents\": [{" +
                    "\"parts\": [" +
                    "{\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}," +
                    "{\"inline_data\": {\"mime_type\": \"image/jpeg\", \"data\": \"" + base64Image + "\"}}" +
                    "]" +
                    "}]" +
                    "}";

            String jsonResponse = sendHttpRequestToGemini(jsonPayload);

            if (jsonResponse.contains("\"winnerSide\": \"A\"") || jsonResponse.contains("\"winnerSide\":\"A\""))
                return "A";
            if (jsonResponse.contains("\"winnerSide\": \"B\"") || jsonResponse.contains("\"winnerSide\":\"B\""))
                return "B";
            return "UNCLEAR";

        } catch (Exception e) {
            e.printStackTrace();
            return "UNCLEAR";
        }
    }
}