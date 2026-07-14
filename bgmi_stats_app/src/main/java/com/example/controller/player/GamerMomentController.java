package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.dao.GamerMomentDao;
import com.example.model.UserModel;
import com.example.model.player.GamerMomentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GamerMomentController {

    public static boolean publishPost(String content, String visibility) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null || content == null || content.trim().isEmpty())
            return false;

        GamerMomentModel post = new GamerMomentModel();
        post.setPostId(UUID.randomUUID().toString());
        post.setAuthorId(currentUser.getUserId());
        post.setAuthorName(currentUser.getPlayerName());
        post.setAuthorIgn(currentUser.getIgn());
        post.setContentType("TEXT");
        post.setTextContent(content.trim());
        post.setVisibility(visibility);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setCreatedAt(System.currentTimeMillis());

        return GamerMomentDao.savePost(post);
    }

    public static List<GamerMomentModel> getCuratedFeed() {
        UserModel currentUser = AuthController.currentUser;
        List<GamerMomentModel> rawFeed = GamerMomentDao.fetchGlobalTimeline();
        List<GamerMomentModel> curatedFeed = new ArrayList<>();

        if (currentUser == null)
            return curatedFeed;

        List<String> myConnections = currentUser.getConnectionIds() != null ? currentUser.getConnectionIds()
                : new ArrayList<>();

        for (GamerMomentModel post : rawFeed) {
            boolean isMine = post.getAuthorId().equals(currentUser.getUserId());
            boolean isPublic = "PUBLIC".equals(post.getVisibility());
            boolean isConnection = myConnections.contains(post.getAuthorId());

            if (isPublic || isMine || isConnection) {
                curatedFeed.add(post);
            }
        }
        return curatedFeed;
    }
}