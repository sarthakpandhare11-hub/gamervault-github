package com.example.controller.gemini;

import com.example.view.util.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.io.InputStream;
import java.util.Map;

public class GeminiImageClient {

    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/interactions";
    private static final String API_KEY = ConfigManager.get("GEMINI_API_KEY");

    public static byte[] generateHolographicCardBytes(String prompt, String base64ReferenceImage) throws Exception {

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "image");

        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gemini-3-pro-image-preview");
        payload.add("response_format", responseFormat);

        if (base64ReferenceImage != null && !base64ReferenceImage.isEmpty()) {

            JsonObject textPart = new JsonObject();
            textPart.addProperty("type", "text");
            textPart.addProperty("text", prompt);

            JsonObject imagePart = new JsonObject();
            imagePart.addProperty("type", "image");

            imagePart.addProperty("mime_type", "image/png");
            imagePart.addProperty("data", base64ReferenceImage.replaceAll("[\\n\\r\\t\\s]", ""));

            JsonArray inputContentArray = new JsonArray();
            inputContentArray.add(textPart);
            inputContentArray.add(imagePart);

            payload.add("input", inputContentArray);

        } else {
            payload.addProperty("input", prompt);
        }

        String jsonPayload = payload.toString();

        System.out.println("Dispatching secure JSON payload to Gemini 3 Pro Image...");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            String extractedData = extractBase64FromJson(responseBody);

            if (extractedData != null) {

                System.out.println("--- API EXTRACTION SUCCESS ---");
                System.out.println("Payload Length: " + extractedData.length() + " characters.");
                System.out.println("Starts with: " + extractedData.substring(0, Math.min(100, extractedData.length())));
                System.out.println("------------------------------");

                if (extractedData.startsWith("http://") || extractedData.startsWith("https://")) {
                    System.out.println("Action: URL Detected! Downloading raw image bytes directly...");
                    try (InputStream in = URI.create(extractedData).toURL().openStream()) {
                        return in.readAllBytes();
                    }
                }

                if (extractedData.length() < 1000 && !extractedData.startsWith("http")) {
                    throw new Exception("API returned text, not an image! Text: " + extractedData);
                }

                System.out.println("Action: Base64 Image Detected. Decoding to binary...");
                return decodeAndSanitizeBase64(extractedData);

            } else {
                throw new Exception("Success 200 OK, but failed to extract image data.\nRaw Response: " + responseBody);
            }
        } else {
            throw new Exception(
                    "API Request Failed! Status: " + response.statusCode() + "\nRaw Error: " + response.body());
        }
    }

    private static String extractBase64FromJson(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("steps")) {
                JsonArray steps = root.getAsJsonArray("steps");
                for (JsonElement stepElement : steps) {
                    JsonObject step = stepElement.getAsJsonObject();
                    if (step.has("type") && "model_output".equals(step.get("type").getAsString())) {
                        JsonArray contentArray = step.getAsJsonArray("content");
                        for (JsonElement contentElement : contentArray) {
                            JsonObject content = contentElement.getAsJsonObject();
                            if (content.has("type") && "image".equals(content.get("type").getAsString())) {
                                if (content.has("text"))
                                    return content.get("text").getAsString();
                                if (content.has("data"))
                                    return content.get("data").getAsString();
                                if (content.has("imageBytes"))
                                    return content.get("imageBytes").getAsString();
                                if (content.has("bytesBase64Encoded"))
                                    return content.get("bytesBase64Encoded").getAsString();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Targeted parsing failed: " + e.getMessage());
        }

        System.err.println("Targeted parsing failed, falling back to deep scan...");
        try {
            return scanForPureBase64(JsonParser.parseString(json), "");
        } catch (Exception ex) {
            return null;
        }
    }

    private static byte[] decodeAndSanitizeBase64(String rawInput) throws Exception {
        String cleanText = rawInput.replaceAll("^\"|\"$", "");
        if (cleanText.contains(",")) {
            cleanText = cleanText.substring(cleanText.indexOf(",") + 1);
        }

        cleanText = cleanText.replaceAll("[^A-Za-z0-9+/=_-]", "");
        cleanText = cleanText.replace('-', '+').replace('_', '/');

        int parityRemainder = cleanText.length() % 4;
        if (parityRemainder > 0) {
            StringBuilder padBuilder = new StringBuilder();
            for (int i = 0; i < (4 - parityRemainder); i++) {
                padBuilder.append("=");
            }
            cleanText += padBuilder.toString();
        }

        return Base64.getDecoder().decode(cleanText);
    }

    private static String scanForPureBase64(JsonElement node, String currentKey) {
        if (node.isJsonPrimitive() && node.getAsJsonPrimitive().isString()) {

            if ("signature".equalsIgnoreCase(currentKey)) {
                return null;
            }

            String val = node.getAsString();
            if (val.length() > 5000 || val.startsWith("http"))
                return val;
        }

        if (node.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
                String check = scanForPureBase64(entry.getValue(), entry.getKey());
                if (check != null)
                    return check;
            }
        } else if (node.isJsonArray()) {
            for (JsonElement entry : node.getAsJsonArray()) {
                String check = scanForPureBase64(entry, currentKey);
                if (check != null)
                    return check;
            }
        }
        return null;
    }
}