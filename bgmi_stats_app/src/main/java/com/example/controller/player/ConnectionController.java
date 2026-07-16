package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.dao.ConnectionDao;
import com.example.dao.ProfileDao;
import com.example.model.UserModel;
import com.example.model.player.ConnectionModel;

import java.util.Arrays;
import java.util.List;

public class ConnectionController {

    public static boolean sendRequest(String targetUserId) {
        if (AuthController.currentUser == null)
            return false;
        String myId = AuthController.currentUser.getUserId();

        ConnectionModel conn = new ConnectionModel();
        // Deterministic ID so they can't spam requests to the same person
        String[] ids = { myId, targetUserId };
        Arrays.sort(ids);
        conn.setConnectionId("CONN_" + ids[0] + "_" + ids[1]);

        conn.setRequesterId(myId);
        conn.setTargetId(targetUserId);
        conn.setStatus("PENDING");
        conn.setCreatedAt(System.currentTimeMillis());

        boolean success = ConnectionDao.sendConnectionRequest(conn);
        if (success) {
            NotificationController.sendNotification(
                    "New Connection Request",
                    AuthController.currentUser.getPlayerName() + " wants to connect with you.",
                    "CONNECTION",
                    targetUserId);
        }
        return success;
    }

    /**
     * Accepting is the step that was missing entirely before - it needs both
     * the requester and target IDs (not just the connection ID) since it has
     * to update connectionIds on both UserModel documents.
     */
    public static boolean acceptRequest(ConnectionModel request) {
        if (request == null)
            return false;
        boolean success = ConnectionDao.acceptConnectionRequest(
                request.getConnectionId(), request.getRequesterId(), request.getTargetId());
        if (success) {
            NotificationController.sendNotification(
                    "Connection Accepted",
                    (AuthController.currentUser != null ? AuthController.currentUser.getPlayerName() : "Someone")
                            + " accepted your connection request.",
                    "CONNECTION",
                    request.getRequesterId());
        }
        return success;
    }

    public static boolean declineRequest(String connectionId) {
        return ConnectionDao.declineConnectionRequest(connectionId);
    }

    public static List<ConnectionModel> fetchPendingRequests() {
        if (AuthController.currentUser == null)
            return new java.util.ArrayList<>();
        return ConnectionDao.getPendingRequestsFor(AuthController.currentUser.getUserId());
    }

    /**
     * "NONE", "PENDING", "ACCEPTED", or "DECLINED" - lets ProfileScreen show
     * the right button state instead of always offering "Send Request" even
     * after one is already pending or accepted.
     */
    public static String getConnectionStatusWith(String otherUserId) {
        if (AuthController.currentUser == null)
            return "NONE";
        return ConnectionDao.getConnectionStatus(AuthController.currentUser.getUserId(), otherUserId);
    }

    /**
     * Resolves the requester's display name for the requests inbox card,
     * since ConnectionModel only stores the ID.
     */
    public static String resolveRequesterName(String requesterId) {
        UserModel user = ProfileDao.fetchUserProfile(requesterId);
        return user != null && user.getPlayerName() != null ? user.getPlayerName() : "Unknown Player";
    }
}