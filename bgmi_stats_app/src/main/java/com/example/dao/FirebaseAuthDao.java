package com.example.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseAuthDao {
    private static final String API_KEY = "AIzaSyDAI1H9FezWEubh-6Get-3WiEpNQx72VnM";

    /*
     * This method is called after all the verification of email and password.
     * Called from AuthController.
     * 
     * Here the method registers new user to Firebase.
     */
    public static String signUp(String email, String password) {

        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);

            String payloadJson = String.format("{\"email\":\"%s\",\"password\":\"%s\", \"returnSecureToken\": true}",
                    email, password);

            OutputStream os = connection.getOutputStream();

            os.write(payloadJson.getBytes());

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the JSON response from Firebase
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extract the UID (localId) from the JSON string
                String userId = extractLocalId(response.toString());
                System.out.println("DEBUG: User registered successfully. UID: " + userId);

                return userId;
            } else {
                System.out.println("User registration failed.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * This method is called after all the verification of email and password.
     * Called from AuthController.
     * 
     * Here the method logs in user to Firebase.
     */
    public static String loginWithEmailAndPassword(String email, String password) {

        try {
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);

            String payloadJson = String.format("{\"email\":\"%s\",\"password\":\"%s\", \"returnSecureToken\": true}",
                    email, password);

            OutputStream os = connection.getOutputStream();
            os.write(payloadJson.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the JSON response from Firebase
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extract the UID (localId) from the JSON string
                String userId = extractLocalId(response.toString());
                System.out.println("DEBUG: User logged in successfully. UID: " + userId);

                return userId;
            } else {
                System.out.println("DEBUG: User login failed.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Helper method to extract "localId" from the Firebase JSON response
     * without needing to install external JSON libraries like Gson or Jackson.
     */
    private static String extractLocalId(String jsonResponse) {
        String searchKey = "\"localId\": \"";
        int startIndex = jsonResponse.indexOf(searchKey);

        if (startIndex == -1) {
            searchKey = "\"localId\":\"";
            startIndex = jsonResponse.indexOf(searchKey);
        }

        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return jsonResponse.substring(startIndex, endIndex);
            }
        }
        return null;
    }
}
