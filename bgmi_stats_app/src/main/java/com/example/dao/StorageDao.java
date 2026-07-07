package com.example.dao;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;

public class StorageDao {

    static String BUCKET_NAME = "gamervaultfx-3ccf9.firebasestorage.app";

    /**
     * Used for uploading the screenshots to the firebase storage.
     * This method is called in the UploadMatchController which gives all the data
     * and passes it to the Firebase Storage.
     */
    public String uploadMatchImage(File imageFile, String userId, String matchId) throws Exception {

        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("Please select a valid image.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User session is missing.");
        }
        if (matchId == null || matchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Match ID is missing.");
        }

        // Sanitize filename to prevent URL/Storage breaking characters
        String safeName = imageFile.getName().replaceAll("[^a-zA-Z0-9.-]", "");

        // Structure: matches / userId / matchId / timestamp-filename.png
        String objectName = "matches/" + userId + "/" + matchId + "/" + Instant.now().toEpochMilli() + "-" + safeName;

        // Safely probe MIME type
        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = "image/jpeg";
        }

        Bucket bucket = StorageClient.getInstance().bucket(BUCKET_NAME);
        Blob blob = bucket.create(objectName, Files.readAllBytes(imageFile.toPath()), contentType);

        return "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName()
                + "/o/" + URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8).replace("+", "%20")
                + "?alt=media";
    }

    /**
     * Uploads Tournament Promotional Images to Firebase Storage
     */
    public String uploadTournamentImage(File imageFile) throws Exception {
        if (imageFile == null || !imageFile.exists()) {
            throw new IllegalArgumentException("Please select a valid image.");
        }

        String safeName = imageFile.getName().replaceAll("[^a-zA-Z0-9.-]", "");
        String objectName = "tournaments/" + Instant.now().toEpochMilli() + "-" + safeName;

        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            contentType = "image/jpeg";
        }

        Bucket bucket = StorageClient.getInstance().bucket(BUCKET_NAME);
        Blob blob = bucket.create(objectName, Files.readAllBytes(imageFile.toPath()), contentType);

        return "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName()
                + "/o/" + URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8).replace("+", "%20")
                + "?alt=media";
    }

    public static String uploadTemplateImage(File file, String fileName) {
        try {
            // Get the storage bucket from your Firebase initialization
            Bucket bucket = StorageClient.getInstance().bucket(BUCKET_NAME);
            if (bucket == null) {
                System.err.println("Error: Firebase Storage Bucket is not initialized.");
                return null;
            }

            // Define the storage path
            String blobName = "templates/" + fileName;
            // byte[] fileBytes = Files.readAllBytes(file.toPath());

            // // Upload the file to Firebase Storage
            // Blob blob = bucket.create(blobName, fileBytes, "image/png");

            // Manually construct the public download URL (Standard for Firebase Admin SDK)
            String encodedPath = URLEncoder.encode(blobName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String downloadUrl = "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/" + encodedPath
                    + "?alt=media";

            return downloadUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}