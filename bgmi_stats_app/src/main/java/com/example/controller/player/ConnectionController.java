package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.dao.ConnectionDao;
import com.example.model.player.ConnectionModel;
import java.util.Arrays;

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

        return ConnectionDao.sendConnectionRequest(conn);
    }
}