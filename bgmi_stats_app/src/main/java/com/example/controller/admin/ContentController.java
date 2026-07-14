package com.example.controller.admin;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.dao.ContentDao;
import com.example.model.admin.ContentModel;

public class ContentController {

    public static boolean publishContent(String title, String desc, String url, String manualThumbnailUrl) {
        ContentModel model = new ContentModel();
        model.setContentId(UUID.randomUUID().toString());
        model.setTitle(title);
        model.setDescription(desc);
        model.setUrl(url);
        model.setCreatedAt(System.currentTimeMillis());

        // SMART ROUTING LOGIC
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")) {
            model.setPlatform("YouTube");
            String videoId = extractYouTubeId(url);
            if (!videoId.isEmpty() && (manualThumbnailUrl == null || manualThumbnailUrl.isEmpty())) {
                // Auto-fetch maximum resolution YouTube thumbnail
                model.setThumbnailUrl("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg");
            } else {
                model.setThumbnailUrl(manualThumbnailUrl);
            }
        } else if (lowerUrl.contains("instagram.com")) {
            model.setPlatform("Instagram");
            model.setThumbnailUrl(manualThumbnailUrl); // Requires manual upload via UI
        } else {
            model.setPlatform("Other");
            model.setThumbnailUrl(manualThumbnailUrl);
        }

        boolean isSaved = ContentDao.saveContent(model);
        // if (isSaved) {
        // // Push notification so players check out the new video/post
        // NotificationController.sendNotification(
        // "New Content Dropped",
        // "Watch now: " + title,
        // "CONTENT",
        // "GLOBAL");
        // }
        return isSaved;
    }

    public static List<ContentModel> loadContent() {
        return ContentDao.fetchAllContent();
    }

    public static boolean deleteContent(String id) {
        return ContentDao.deleteContent(id);
    }

    // REGEX to extract YouTube Video ID from any standard YT link format
    private static String extractYouTubeId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}