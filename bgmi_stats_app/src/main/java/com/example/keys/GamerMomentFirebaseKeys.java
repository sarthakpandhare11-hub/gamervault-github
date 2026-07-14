package com.example.keys;

public interface GamerMomentFirebaseKeys {
    String COLLECTION_GAMER_MOMENTS = "GamerMoments";

    String FIELD_POST_ID = "postId";
    String FIELD_AUTHOR_ID = "authorId";
    String FIELD_AUTHOR_NAME = "authorName";
    String FIELD_AUTHOR_IGN = "authorIgn";

    String FIELD_CONTENT_TYPE = "contentType"; // "TEXT", "IMAGE", "MATCH_HIGHLIGHT"
    String FIELD_TEXT_CONTENT = "textContent";
    String FIELD_VISIBILITY = "visibility"; // "PUBLIC" or "CONNECTIONS_ONLY"

    String FIELD_LIKES_COUNT = "likesCount";
    String FIELD_COMMENTS_COUNT = "commentsCount";
    String FIELD_CREATED_AT = "createdAt";
}