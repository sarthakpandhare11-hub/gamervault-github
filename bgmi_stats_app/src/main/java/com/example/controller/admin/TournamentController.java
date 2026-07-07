package com.example.controller.admin;

import java.util.List;
import java.util.UUID;
import com.example.dao.TournamentDao;
import com.example.model.admin.TournamentModel;

public class TournamentController {

    public static boolean createNewTournament(
            String title,
            String prize,
            int maxSlots,
            String startDate,
            String regBegin,
            String regEnd,
            List<String> imageUrls,
            String organizer,
            String instaLink) {

        TournamentModel model = new TournamentModel();
        model.setTournamentId(UUID.randomUUID().toString());
        model.setTitle(title);
        model.setGame("BGMI");
        model.setPrizePool(prize);
        model.setStatus("UPCOMING");
        model.setSlotsMax(maxSlots);
        model.setSlotsRegistered(0);

        model.setStartDate(startDate);
        model.setRegBeginDate(regBegin);
        model.setRegEndDate(regEnd);
        model.setImageUrls(imageUrls);
        model.setOrganizerName(organizer);
        model.setInstagramLink(instaLink);

        boolean isSaved = TournamentDao.saveTournament(model);
        if (isSaved) {
            // Push notification to all players and the admin activity log
            NotificationController.sendNotification(
                    "New Tournament: " + title,
                    "Registration is now open! Prize Pool: " + prize,
                    "TOURNAMENT",
                    "GLOBAL");
        }
        return isSaved;
    }

    public static List<TournamentModel> loadActiveTournaments() {
        return TournamentDao.fetchAllTournaments();
    }

    public static boolean deleteTournament(String id) {
        return TournamentDao.deleteTournament(id);
    }
}