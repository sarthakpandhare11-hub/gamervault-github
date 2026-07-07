package com.example.controller.gemini;

import com.example.model.UserModel;
import com.example.model.admin.TemplateModel;
import com.example.model.player.MatchModel;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class PortfolioGeminiController {

        public static String compileStrongPrompt(String templateType, UserModel user, MatchModel match) {

                String baseVisuals = "";
                switch (templateType) {
                        case "OVERALL":
                                // Extract Profile Data
                                String overallName = (user != null && user.getPlayerName() != null)
                                                ? user.getPlayerName()
                                                : "Unknown";
                                String overallIgn = (user != null && user.getIgn() != null) ? user.getIgn() : "PLAYER";
                                String overallRole = (user != null && user.getPrimaryRole() != null)
                                                ? user.getPrimaryRole()
                                                : "Assaulter";
                                String overallSince = (user != null && user.getCreatedAt() != null)
                                                ? user.getCreatedAt()
                                                : "Recent";

                                // Extract Overall Performance Stats
                                String totalMatches = (user != null) ? String.valueOf(user.getTotalMatches()) : "0";
                                String totalKills = (user != null) ? String.valueOf(user.getTotalKills()) : "0";
                                String kdRatio = (user != null)
                                                ? String.format("%.2f",
                                                                (double) user.getTotalKills() / user.getTotalMatches())
                                                : "0.00";

                                // Dynamic win calculation based on win rate percentage
                                int computedWins = 0;
                                if (user != null && user.getTotalMatches() > 0) {
                                        computedWins = (int) Math
                                                        .round((user.getWinRate() / 100.0) * user.getTotalMatches());
                                }
                                String totalWins = String.valueOf(computedWins);

                                return baseVisuals
                                                + "# ROLE\r\n" + //
                                                "\r\n" + //
                                                "You are a professional AI image editor specializing in preserving game UI designs with near pixel-perfect consistency while updating dynamic player information.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# OBJECTIVE\r\n" + //
                                                "\r\n" + //
                                                "Use the uploaded reference image as the ONLY template.\r\n" + //
                                                "\r\n" + //
                                                "Recreate the image so that it matches the reference as closely as possible while replacing only the specified text values.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Do NOT redesign, enhance, restyle, reinterpret, or modify any visual element.\r\n"
                                                + //
                                                "\r\n" + //
                                                "The reference image is the authoritative source for every visual detail.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# REFERENCE IMAGE\r\n" + //
                                                "\r\n" + //
                                                "Treat the uploaded image as immutable.\r\n" + //
                                                "\r\n" + //
                                                "Preserve exactly:\r\n" + //
                                                "\r\n" + //
                                                "• Canvas size\r\n" + //
                                                "• Aspect ratio\r\n" + //
                                                "• Entire layout\r\n" + //
                                                "• Profile panel\r\n" + //
                                                "• Character placement\r\n" + //
                                                "• Character artwork\r\n" + //
                                                "• Weapon\r\n" + //
                                                "• Hologram effects\r\n" + //
                                                "• Blue lighting\r\n" + //
                                                "• Background\r\n" + //
                                                "• Decorative graphics\r\n" + //
                                                "• Borders\r\n" + //
                                                "• Icons\r\n" + //
                                                "• Typography style\r\n" + //
                                                "• Font sizes\r\n" + //
                                                "• Font colors\r\n" + //
                                                "• Text alignment\r\n" + //
                                                "• Spacing\r\n" + //
                                                "• Margins\r\n" + //
                                                "• Shadows\r\n" + //
                                                "• Glow effects\r\n" + //
                                                "• Overall visual hierarchy\r\n" + //
                                                "\r\n" + //
                                                "Do NOT:\r\n" + //
                                                "\r\n" + //
                                                "- Move any object\r\n" + //
                                                "- Resize any object\r\n" + //
                                                "- Replace the character\r\n" + //
                                                "- Modify the weapon\r\n" + //
                                                "- Change the background\r\n" + //
                                                "- Change lighting\r\n" + //
                                                "- Change colors\r\n" + //
                                                "- Replace icons\r\n" + //
                                                "- Remove UI elements\r\n" + //
                                                "- Add UI elements\r\n" + //
                                                "- Alter panel shapes\r\n" + //
                                                "- Modify decorative graphics\r\n" + //
                                                "\r\n" + //
                                                "Only replace the values listed below.\r\n" + //
                                                "\r\n" + //
                                                "# UPDATE ONLY THESE FIELDS\r\n" + //
                                                "\r\n" + //
                                                "## PROFILE\r\n" + //
                                                "\r\n" + //
                                                "PLAYER NAME:\r\n" + //
                                                "{playerName}\r\n" + //
                                                "\r\n" + //
                                                "IGN:\r\n" + //
                                                "{ign}\r\n" + //
                                                "\r\n" + //
                                                "ROLE:\r\n" + //
                                                "{role}\r\n" + //
                                                "\r\n" + //
                                                "MEMBER SINCE:\r\n" + //
                                                "{memberSince}\r\n" + //
                                                "\r\n" + //
                                                "---\r\n" + //
                                                "\r\n" + //
                                                "## OVERALL STATS\r\n" + //
                                                "\r\n" + //
                                                "MATCHES:\r\n" + //
                                                "{matches}\r\n" + //
                                                "\r\n" + //
                                                "KILLS:\r\n" + //
                                                "{kills}\r\n" + //
                                                "\r\n" + //
                                                "K/D RATIO:\r\n" + //
                                                "{kdRatio}\r\n" + //
                                                "\r\n" + //
                                                "WINS:\r\n" + //
                                                "{wins}\r\n" + //
                                                "\r\n" + //
                                                "# TEXT PLACEMENT RULES\r\n" + //
                                                "\r\n" + //
                                                "Replace only the values shown in the reference image.\r\n" + //
                                                "\r\n" + //
                                                "Preserve exactly:\r\n" + //
                                                "\r\n" + //
                                                "• Text position\r\n" + //
                                                "• Font family/style\r\n" + //
                                                "• Font weight\r\n" + //
                                                "• Font color\r\n" + //
                                                "• Capitalization\r\n" + //
                                                "• Alignment\r\n" + //
                                                "• Padding\r\n" + //
                                                "• Margins\r\n" + //
                                                "• Spacing\r\n" + //
                                                "• Visual hierarchy\r\n" + //
                                                "\r\n" + //
                                                "If a value is longer than the available space:\r\n" + //
                                                "\r\n" + //
                                                "- Reduce the font size only enough to fit.\r\n" + //
                                                "- Do not wrap text unless the original design wraps it.\r\n" + //
                                                "- Do not move any surrounding UI elements.\r\n" + //
                                                "\r\n" + //
                                                "# STRICT CONSTRAINTS\r\n" + //
                                                "\r\n" + //
                                                "Everything not listed above must remain unchanged.\r\n" + //
                                                "\r\n" + //
                                                "Do not modify:\r\n" + //
                                                "\r\n" + //
                                                "- Character\r\n" + //
                                                "- Outfit\r\n" + //
                                                "- Hair\r\n" + //
                                                "- Weapon\r\n" + //
                                                "- Background\r\n" + //
                                                "- Hologram platform\r\n" + //
                                                "- UI frame\r\n" + //
                                                "- Decorative elements\r\n" + //
                                                "- Connected status\r\n" + //
                                                "- Precision label\r\n" + //
                                                "- Victory label\r\n" + //
                                                "- Dominance label\r\n" + //
                                                "- Icons\r\n" + //
                                                "- Borders\r\n" + //
                                                "- Colors\r\n" + //
                                                "- Lighting\r\n" + //
                                                "- Glow\r\n" + //
                                                "- Shadows\r\n" + //
                                                "- Overall composition\r\n" + //
                                                "\r\n" + //
                                                "The result should appear as if the original image was directly edited rather than recreated.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# OUTPUT\r\n" + //
                                                "\r\n" + //
                                                "Generate a single completed image.\r\n" + //
                                                "\r\n" + //
                                                "The final image should be visually indistinguishable from the reference image, with only the specified text fields updated."
                                                + "[PROFILE DASHBOARD PANEL]\n"
                                                + "PLAYER NAME: " + overallName + "\n"
                                                + "IGN: " + overallIgn + "\n"
                                                + "ROLE: " + overallRole + "\n"
                                                + "MEMBER SINCE: " + overallSince + "\n\n"

                                                + "[OVERALL STATS GRID BOXES]\n"
                                                + "MATCHES Box Value: " + totalMatches + "\n"
                                                + "KILLS Box Value: " + totalKills + "\n"
                                                + "K/D RATIO Box Value: " + kdRatio + "\n"
                                                + "WINS Box Value: " + totalWins + "\n\n"

                                                + "Keep the floating overlay frames for 'VICTORY', 'PRECISION', and 'DOMINANCE' exactly where they are placed in relation to the pedestal graphic. Ensure all text values perfectly match the layout boxes cleanly.";

                        case "MATCH":
                                // 1. Extract Player Overview Data
                                String matchMap = (match != null && match.getMap() != null) ? match.getMap()
                                                : "Erangel";
                                String matchType = (match != null && match.getGameMode() != null) ? match.getGameMode()
                                                : "Classic - Squad";
                                String matchDate = (match != null && match.getMatchDate() != null)
                                                ? match.getMatchDate()
                                                : "RECENT";
                                String matchPlacement = (match != null)
                                                ? "#" + match.getTeamPlacement() + " / " + match.getTotalPlayers()
                                                : "#-- / 100";

                                boolean matchIsWinner = (match != null && match.getTeamPlacement() == 1) ||
                                                (match != null && "Victory".equalsIgnoreCase(match.getMatchResult()));
                                String matchWinnerWinner = matchIsWinner ? "Yes" : "No";

                                String matchKills = (match != null) ? String.valueOf(match.getKills()) : "0";
                                String matchAssists = (match != null) ? String.valueOf(match.getAssists()) : "0";
                                String matchDamage = (match != null) ? String.valueOf(match.getDamage()) : "0";
                                String matchSurvival = (match != null && match.getSurvivalTime() != null)
                                                ? match.getSurvivalTime()
                                                : "00:00";
                                String matchTravel = (match != null)
                                                ? String.format("%.2f km", match.getTravelDistance())
                                                : "0.00 km";
                                String matchRevives = (match != null) ? String.valueOf(match.getRescues()) : "0";

                                // 2. Extract Combat & Survival Summaries (Using placeholders for data not in
                                // MatchModel)
                                String combatHighestDmg = (match != null)
                                                ? String.valueOf((int) match.getWeaponDamage())
                                                : "0";
                                String combatShots = (match != null) ? String.valueOf(match.getWeaponUses()) : "0";
                                String combatKnocks = (match != null) ? String.valueOf(match.getWeaponKnockdowns())
                                                : "0";
                                String healsUsed = (match != null) ? String.valueOf(match.getTotalSupplies()) : "0";
                                String boostsUsed = (match != null) ? String.valueOf(match.getAdvancedSupplies()) : "0";

                                return baseVisuals
                                                + "# ROLE\r\n" + //
                                                "\r\n" + //
                                                "You are a professional image editing AI specializing in preserving game UI layouts with near pixel-perfect fidelity while updating dynamic match data.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# OBJECTIVE\r\n" + //
                                                "\r\n" + //
                                                "Use the uploaded reference image as the ONLY template.\r\n" + //
                                                "\r\n" + //
                                                "Recreate the image so it is visually identical to the reference while replacing only the specified data fields.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Do NOT redesign, improve, reinterpret, enhance, or modify any visual component.\r\n"
                                                + //
                                                "\r\n" + //
                                                "The reference image is the source of truth.\r\n" + //
                                                "\r\n" + //
                                                "# REFERENCE IMAGE\r\n" + //
                                                "\r\n" + //
                                                "The uploaded image is the master template.\r\n" + //
                                                "\r\n" + //
                                                "Preserve exactly:\r\n" + //
                                                "\r\n" + //
                                                "• Overall layout\r\n" + //
                                                "• Canvas size\r\n" + //
                                                "• Aspect ratio\r\n" + //
                                                "• Panel positions\r\n" + //
                                                "• Borders\r\n" + //
                                                "• Neon blue UI styling\r\n" + //
                                                "• Colors\r\n" + //
                                                "• Gradients\r\n" + //
                                                "• Lighting\r\n" + //
                                                "• Glow effects\r\n" + //
                                                "• Shadows\r\n" + //
                                                "• Character render\r\n" + //
                                                "• Equipment icons\r\n" + //
                                                "• MVP/Profile title\r\n" + //
                                                "• Decorative elements\r\n" + //
                                                "• Background\r\n" + //
                                                "• Typography style\r\n" + //
                                                "• Font hierarchy\r\n" + //
                                                "• Font colors\r\n" + //
                                                "• Icon positions\r\n" + //
                                                "• Alignment\r\n" + //
                                                "• Padding\r\n" + //
                                                "• Margins\r\n" + //
                                                "• Section spacing\r\n" + //
                                                "\r\n" + //
                                                "Do NOT:\r\n" + //
                                                "\r\n" + //
                                                "- Move any object\r\n" + //
                                                "- Resize any object\r\n" + //
                                                "- Change any icon\r\n" + //
                                                "- Change character equipment\r\n" + //
                                                "- Replace artwork\r\n" + //
                                                "- Modify background\r\n" + //
                                                "- Change borders\r\n" + //
                                                "- Add visual effects\r\n" + //
                                                "- Remove visual effects\r\n" + //
                                                "- Change colors\r\n" + //
                                                "- Change typography style\r\n" + //
                                                "- Alter any element that is not explicitly listed below.\r\n" + //
                                                "\r\n" + //
                                                "# UPDATE ONLY THESE FIELDS\r\n" + //
                                                "\r\n" + //
                                                "## PLAYER OVERVIEW\r\n" + //
                                                "\r\n" + //
                                                "MAP:\r\n" + //
                                                "{map}\r\n" + //
                                                "\r\n" + //
                                                "MATCH TYPE:\r\n" + //
                                                "{matchType}\r\n" + //
                                                "\r\n" + //
                                                "DATE & TIME:\r\n" + //
                                                "{dateTime}\r\n" + //
                                                "\r\n" + //
                                                "PLACEMENT:\r\n" + //
                                                "{placement}\r\n" + //
                                                "\r\n" + //
                                                "WINNER WINNER:\r\n" + //
                                                "{winnerWinner}\r\n" + //
                                                "\r\n" + //
                                                "TOTAL KILLS:\r\n" + //
                                                "{totalKills}\r\n" + //
                                                "\r\n" + //
                                                "ASSISTS:\r\n" + //
                                                "{assists}\r\n" + //
                                                "\r\n" + //
                                                "DAMAGE DEALT:\r\n" + //
                                                "{damageDealt}\r\n" + //
                                                "\r\n" + //
                                                "SURVIVAL TIME:\r\n" + //
                                                "{survivalTime}\r\n" + //
                                                "\r\n" + //
                                                "TRAVEL DISTANCE:\r\n" + //
                                                "{travelDistance}\r\n" + //
                                                "\r\n" + //
                                                "REVIVES:\r\n" + //
                                                "{revives}\r\n" + //
                                                "\r\n" + //
                                                "---\r\n" + //
                                                "\r\n" + //
                                                "## COMBAT SUMMARY\r\n" + //
                                                "\r\n" + //
                                                "HEADSHOT KILLS:\r\n" + //
                                                "{headshotKills}\r\n" + //
                                                "\r\n" + //
                                                "LONGEST KILL (M):\r\n" + //
                                                "{longestKill}\r\n" + //
                                                "\r\n" + //
                                                "HIGHEST DAMAGE IN MATCH:\r\n" + //
                                                "{highestDamage}\r\n" + //
                                                "\r\n" + //
                                                "SHOTS FIRED:\r\n" + //
                                                "{shotsFired}\r\n" + //
                                                "\r\n" + //
                                                "HIT ACCURACY:\r\n" + //
                                                "{hitAccuracy}\r\n" + //
                                                "\r\n" + //
                                                "KNOCKDOWNS:\r\n" + //
                                                "{knockdowns}\r\n" + //
                                                "\r\n" + //
                                                "---\r\n" + //
                                                "\r\n" + //
                                                "## SURVIVAL SUMMARY\r\n" + //
                                                "\r\n" + //
                                                "SURVIVAL TIME:\r\n" + //
                                                "{survivalTime}\r\n" + //
                                                "\r\n" + //
                                                "TRAVEL DISTANCE:\r\n" + //
                                                "{travelDistance}\r\n" + //
                                                "\r\n" + //
                                                "VEHICLES USED:\r\n" + //
                                                "{vehiclesUsed}\r\n" + //
                                                "\r\n" + //
                                                "HEALS USED:\r\n" + //
                                                "{healsUsed}\r\n" + //
                                                "\r\n" + //
                                                "BOOSTS USED:\r\n" + //
                                                "{boostsUsed}\r\n" + //
                                                "\r\n" + //
                                                "REVIVES:\r\n" + //
                                                "{revives}\r\n" + //
                                                "\r\n" + //
                                                "# TEXT PLACEMENT RULES\r\n" + //
                                                "\r\n" + //
                                                "Insert each value into the exact corresponding field shown in the reference image.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Maintain:\r\n" + //
                                                "\r\n" + //
                                                "• Identical text position\r\n" + //
                                                "• Identical alignment\r\n" + //
                                                "• Identical padding\r\n" + //
                                                "• Identical spacing\r\n" + //
                                                "• Identical font style\r\n" + //
                                                "• Identical font weight\r\n" + //
                                                "• Identical capitalization\r\n" + //
                                                "• Identical font color\r\n" + //
                                                "• Identical visual hierarchy\r\n" + //
                                                "\r\n" + //
                                                "If any value exceeds the available space:\r\n" + //
                                                "\r\n" + //
                                                "- Reduce the font size only enough to fit.\r\n" + //
                                                "- Never move surrounding elements.\r\n" + //
                                                "- Never wrap text unless the original design already wraps it.\r\n" + //
                                                "\r\n" + //
                                                "# STRICT CONSTRAINTS\r\n" + //
                                                "\r\n" + //
                                                "Everything except the supplied values must remain unchanged.\r\n" + //
                                                "\r\n" + //
                                                "Do not edit:\r\n" + //
                                                "\r\n" + //
                                                "- Character model\r\n" + //
                                                "- Armor\r\n" + //
                                                "- Helmet\r\n" + //
                                                "- Backpack\r\n" + //
                                                "- Weapon\r\n" + //
                                                "- Frying pan\r\n" + //
                                                "- Clothing\r\n" + //
                                                "- Equipment icons\r\n" + //
                                                "- Progress meter\r\n" + //
                                                "- UI panels\r\n" + //
                                                "- Borders\r\n" + //
                                                "- Background\r\n" + //
                                                "- Decorative graphics\r\n" + //
                                                "- Header\r\n" + //
                                                "- Logos\r\n" + //
                                                "- Close button\r\n" + //
                                                "- Icons\r\n" + //
                                                "- Colors\r\n" + //
                                                "- Lighting\r\n" + //
                                                "- Shadows\r\n" + //
                                                "\r\n" + //
                                                "The generated image should appear as if the original image was edited directly.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# OUTPUT\r\n" + //
                                                "\r\n" + //
                                                "Return a single completed image.\r\n" + //
                                                "\r\n" + //
                                                "The final result should be visually indistinguishable from the reference image, with only the specified data fields updated."

                                                + "[PLAYER OVERVIEW PANEL]\n"
                                                + "MAP: " + matchMap + "\n"
                                                + "MATCH TYPE: " + matchType + "\n"
                                                + "DATE & TIME: " + matchDate + "\n"
                                                + "PLACEMENT: " + matchPlacement + "\n"
                                                + "WINNER WINNER: " + matchWinnerWinner + "\n"
                                                + "TOTAL KILLS: " + matchKills + "\n"
                                                + "ASSISTS: " + matchAssists + "\n"
                                                + "DAMAGE DEALT: " + matchDamage + "\n"
                                                + "SURVIVAL TIME: " + matchSurvival + "\n"
                                                + "TRAVEL DISTANCE: " + matchTravel + "\n"
                                                + "REVIVES: " + matchRevives + "\n\n"

                                                + "[COMBAT SUMMARY PANEL]\n"
                                                + "HEADSHOT KILLS: 2\n"
                                                + "LONGEST KILL (M): 145\n"
                                                + "HIGHEST DAMAGE IN MATCH: " + combatHighestDmg + "\n"
                                                + "SHOTS FIRED: " + combatShots + "\n"
                                                + "HIT ACCURACY: 21.5%\n"
                                                + "KNOCKDOWNS: " + combatKnocks + "\n\n"

                                                + "[SURVIVAL SUMMARY PANEL]\n"
                                                + "SURVIVAL TIME: " + matchSurvival + "\n"
                                                + "TRAVEL DISTANCE: " + matchTravel + "\n"
                                                + "VEHICLES USED: 1\n"
                                                + "HEALS USED: " + healsUsed + "\n"
                                                + "BOOSTS USED: " + boostsUsed + "\n"
                                                + "REVIVES: " + matchRevives + "\n\n"

                                                + "Ensure all values align perfectly inside the template's neon blue grid layouts.";

                        case "MVP":
                                // 2. Extract every detail needed for the MVP Template
                                String mvpIgn = (user != null && user.getIgn() != null) ? user.getIgn() : "UNKNOWN";
                                String mode = (match != null && match.getGameMode() != null) ? match.getGameMode()
                                                : "Ranked Squad";
                                String mvpPlacement = (match != null)
                                                ? "#" + match.getTeamPlacement() + " / " + match.getTotalPlayers()
                                                : "#1 / 100";
                                String mvpKills = (match != null) ? String.valueOf(match.getKills()) : "0";
                                String mvpDamage = (match != null) ? String.valueOf(match.getDamage()) : "0";
                                String survival = (match != null && match.getSurvivalTime() != null)
                                                ? match.getSurvivalTime()
                                                : "00:00";
                                String assists = (match != null) ? String.valueOf(match.getAssists()) : "0";

                                // 3. Force the layout: Left side Character, Right side Data UI
                                return baseVisuals
                                                + "# ROLE\r\n" + //
                                                "You are a professional image editing AI specializing in preserving UI layouts with pixel-level consistency while updating dynamic text content.\r\n"
                                                + //
                                                "\r\n" + //
                                                "# TASK\r\n" + //
                                                "Use the uploaded reference image as the ONLY visual template.\r\n" + //
                                                "\r\n" + //
                                                "Recreate the image so that it matches the reference as closely as possible. Do not redesign, reinterpret, improve, restyle, or modify any visual element.\r\n"
                                                + //
                                                "\r\n" + //
                                                "The objective is to preserve every part of the design while replacing only the specified text values.\r\n"
                                                + //
                                                "\r\n" + //
                                                "## REFERENCE IMAGE\r\n" + //
                                                "The uploaded image is the master template.\r\n" + //
                                                "\r\n" + //
                                                "Treat it as immutable.\r\n" + //
                                                "\r\n" + //
                                                "Maintain exactly:\r\n" + //
                                                "- overall composition\r\n" + //
                                                "- aspect ratio\r\n" + //
                                                "- dimensions\r\n" + //
                                                "- metallic frame\r\n" + //
                                                "- MVP logo\r\n" + //
                                                "- icons\r\n" + //
                                                "- portrait placement\r\n" + //
                                                "- borders\r\n" + //
                                                "- spacing\r\n" + //
                                                "- margins\r\n" + //
                                                "- alignment\r\n" + //
                                                "- colors\r\n" + //
                                                "- gradients\r\n" + //
                                                "- shadows\r\n" + //
                                                "- lighting\r\n" + //
                                                "- textures\r\n" + //
                                                "- decorative elements\r\n" + //
                                                "- typography style\r\n" + //
                                                "- font weight\r\n" + //
                                                "- capitalization\r\n" + //
                                                "- text alignment\r\n" + //
                                                "- visual hierarchy\r\n" + //
                                                "\r\n" + //
                                                "Do NOT:\r\n" + //
                                                "- move any element\r\n" + //
                                                "- resize any element\r\n" + //
                                                "- crop anything\r\n" + //
                                                "- replace icons\r\n" + //
                                                "- change colors\r\n" + //
                                                "- change fonts\r\n" + //
                                                "- add new graphics\r\n" + //
                                                "- remove graphics\r\n" + //
                                                "- modify the portrait\r\n" + //
                                                "- modify the background\r\n" + //
                                                "- modify the metallic UI\r\n" + //
                                                "\r\n" + //
                                                "Only update the values listed below.\r\n" + //
                                                "\r\n" + //
                                                "# UPDATE THESE FIELDS\r\n" + //
                                                "\r\n" + //
                                                "MVP PLAYER: {mvpIgn}\r\n" + //
                                                "\r\n" + //
                                                "MATCH MODE: {mode}\r\n" + //
                                                "\r\n" + //
                                                "PLACEMENT: {mvpPlacement}\r\n" + //
                                                "\r\n" + //
                                                "TOTAL KILLS: {mvpKills}\r\n" + //
                                                "\r\n" + //
                                                "DAMAGE DEALT: {mvpDamage}\r\n" + //
                                                "\r\n" + //
                                                "SURVIVAL TIME: {survival}\r\n" + //
                                                "\r\n" + //
                                                "ASSISTS: {assists}\r\n" + //
                                                "\r\n" + //
                                                "# PLACEMENT RULES\r\n" + //
                                                "\r\n" + //
                                                "Insert each value into the exact corresponding field shown in the reference image.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Keep:\r\n" + //
                                                "- identical text position\r\n" + //
                                                "- identical padding\r\n" + //
                                                "- identical spacing\r\n" + //
                                                "- identical alignment\r\n" + //
                                                "- identical font style\r\n" + //
                                                "- identical font color\r\n" + //
                                                "- identical font weight\r\n" + //
                                                "- identical font hierarchy\r\n" + //
                                                "\r\n" + //
                                                "If a value is longer than the available width, reduce the font size only enough to make it fit inside the original text box without changing the layout.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Do not wrap text unless the original design already wraps it.\r\n" + //
                                                "\r\n" + //
                                                "# OUTPUT REQUIREMENTS\r\n" + //
                                                "\r\n" + //
                                                "The final image should appear visually identical to the reference image except for the updated values.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Every graphical element that is not listed above must remain unchanged.\r\n"
                                                + //
                                                "\r\n" + //
                                                "The reference image is the source of truth for all layout, styling, positioning, and visual details.\r\n"
                                                + //
                                                "\r\n" + //
                                                "Return only the completed image."
                                                + "MVP PLAYER: " + mvpIgn + "\n"
                                                + "MATCH MODE: " + mode + "\n"
                                                + "PLACEMENT: " + mvpPlacement + "\n"
                                                + "TOTAL KILLS: " + mvpKills + "\n"
                                                + "DAMAGE DEALT: " + mvpDamage + "\n"
                                                + "SURVIVAL TIME: " + survival + "\n"
                                                + "ASSISTS: " + assists + "\n\n"

                                                + "Ensure all values align inside the template's dashboard boxes cleanly.";

                        default:
                                return baseVisuals;
                }
        }

        public static String encodeTemplateToBase64(String imageResourcePath) {
                try (InputStream is = PortfolioGeminiController.class.getResourceAsStream(imageResourcePath)) {
                        if (is == null)
                                return "";
                        byte[] bytes = is.readAllBytes();
                        return Base64.getEncoder().encodeToString(bytes);
                } catch (Exception e) {
                        return "";
                }
        }

        /**
         * Downloads an image from a Web URL (Firebase) and converts it to Base64 for
         * the Gemini API.
         */
        public static String downloadAndConvertUrlToBase64(String imageUrl) {
                try {
                        URL url = new URL(imageUrl);
                        InputStream is = url.openStream();
                        byte[] bytes = is.readAllBytes();
                        return Base64.getEncoder().encodeToString(bytes);
                } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                }
        }

        public static String compileDynamicAdminPrompt(TemplateModel dynamicTemplate,
                        UserModel user, MatchModel match) {
                if (dynamicTemplate == null || dynamicTemplate.getAiPrompt() == null) {
                        return "";
                }

                // Get the base structural layout rules written by the Admin
                String rawAdminPrompt = dynamicTemplate.getAiPrompt();

                // 1. Gather all possible live metrics from the models
                String playerName = (user != null && user.getPlayerName() != null) ? user.getPlayerName() : "Unknown";
                String ign = (user != null && user.getIgn() != null) ? user.getIgn() : "PLAYER";
                String role = (user != null && user.getPrimaryRole() != null) ? user.getPrimaryRole() : "Assaulter";
                String memberSince = (user != null && user.getCreatedAt() != null) ? user.getCreatedAt() : "Recent";

                String totalMatches = (user != null) ? String.valueOf(user.getTotalMatches()) : "0";
                String totalKills = (user != null) ? String.valueOf(user.getTotalKills()) : "0";
                String kdRatio = (user != null && user.getTotalMatches() > 0)
                                ? String.format("%.2f", (double) user.getTotalKills() / user.getTotalMatches())
                                : "0.00";

                String matchMap = (match != null && match.getMap() != null) ? match.getMap() : "Erangel";
                String matchType = (match != null && match.getGameMode() != null) ? match.getGameMode() : "Classic";
                String matchDate = (match != null && match.getMatchDate() != null) ? match.getMatchDate() : "RECENT";
                String matchPlacement = (match != null) ? "#" + match.getTeamPlacement() : "#--";
                String matchKills = (match != null) ? String.valueOf(match.getKills()) : "0";
                String matchDamage = (match != null) ? String.valueOf(match.getDamage()) : "0";
                String matchSurvival = (match != null && match.getSurvivalTime() != null) ? match.getSurvivalTime()
                                : "00:00";

                // 2. Perform the runtime token injections
                String compiledPrompt = rawAdminPrompt
                                .replace("{playerName}", playerName)
                                .replace("{ign}", ign)
                                .replace("{role}", role)
                                .replace("{memberSince}", memberSince)
                                .replace("{matches}", totalMatches)
                                .replace("{kills}", totalKills)
                                .replace("{kdRatio}", kdRatio)
                                .replace("{map}", matchMap)
                                .replace("{matchType}", matchType)
                                .replace("{dateTime}", matchDate)
                                .replace("{placement}", matchPlacement)
                                .replace("{totalKills}", matchKills)
                                .replace("{damageDealt}", matchDamage)
                                .replace("{survivalTime}", matchSurvival);

                return compiledPrompt;
        }
}