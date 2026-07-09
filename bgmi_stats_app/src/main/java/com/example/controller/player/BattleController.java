package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.dao.BattleDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.UserModel;
import com.example.model.player.BattleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BattleController {

    // 1. HOST A NEW BATTLE
    public static String hostBattle(String mode, String format, double entryFee) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return null;

        BattleModel battle = new BattleModel();
        battle.setHostType("PLAYER");
        battle.setHostUserId(currentUser.getUserId());
        battle.setMode(mode);
        battle.setFormat(format);
        battle.setStatus(BattleFirebaseKeys.STATUS_OPEN);
        battle.setEntryFeeCoins(entryFee);

        // Calculate max participants based on mode
        int max = 2; // Default 1v1
        if (mode.equals("2V2"))
            max = 4;
        if (mode.equals("4V4"))
            max = 8;
        battle.setMaxParticipants(max);

        // Host automatically joins Team A
        HashMap<String, String> players = new HashMap<>();
        players.put(currentUser.getUserId(), "A");
        battle.setParticipants(players);

        // Initialize empty rounds
        battle.setRounds(new ArrayList<>());
        battle.setCreatedAt(System.currentTimeMillis());

        // Note: In MVP, we are skipping Wallet Deduction here.
        // In the future, deduct entryFee from currentUser's wallet before saving.

        boolean success = BattleDao.saveBattle(battle);

        // Return the ID on success, null on failure
        return success ? battle.getBattleId() : null;
    }

    // 2. JOIN AN EXISTING BATTLE
    public static boolean joinBattle(BattleModel battle) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return false;

        // Prevent joining if full
        if (battle.getParticipants().size() >= battle.getMaxParticipants()) {
            return false;
        }

        // Prevent joining if already in the lobby
        if (battle.getParticipants().containsKey(currentUser.getUserId())) {
            return false;
        }

        // Logic to assign to Team A or Team B
        // E.g., if 4v4, first 4 go to A, next 4 go to B
        int currentSize = battle.getParticipants().size();
        int halfSize = battle.getMaxParticipants() / 2;
        String assignedTeam = (currentSize < halfSize) ? "A" : "B";

        // Update local model
        battle.getParticipants().put(currentUser.getUserId(), assignedTeam);

        // Check if Lobby is now full
        if (battle.getParticipants().size() == battle.getMaxParticipants()) {
            battle.setStatus(BattleFirebaseKeys.STATUS_LOCKED);
            // Note: This is where you would normally generate/fetch the Room ID and
            // Password
            // and attach it to the model right before it locks.
        }

        // Save updated lobby back to Firebase
        return BattleDao.saveBattle(battle);
    }

    // 3. FETCH OPEN LOBBIES
    public static List<BattleModel> getOpenLobbies() {
        return BattleDao.getOpenBattles();
    }
}