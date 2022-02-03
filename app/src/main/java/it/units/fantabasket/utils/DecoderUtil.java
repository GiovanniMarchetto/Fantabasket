package it.units.fantabasket.utils;

import it.units.fantabasket.entities.Game;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.enums.LegaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.utils.AssetDecoderUtil.calendarListOfRoundStart;

@SuppressWarnings("unchecked")
public class DecoderUtil {

    public static final String GIORNATA_ = "giornata_";
    public static final String CALENDARIO = "calendario";
    public static final String CLASSIFICA = "classifica";
    public static final String TEAM_NAME = "teamName";
    public static final String GIORNATA_INIZIO = "giornataInizio";
    public static final String LAST_ROUND_CALCULATED = "lastRoundCalculated";
    public static final String TOTAL_POINTS_SCORED = "totalPointsScored";
    public static final String TOTAL_POINTS_ALLOWED = "totalPointsAllowed";
    public static final String POINTS_OF_VICTORIES = "pointsOfVictories";

    public static Lega getLegaFromHashMapOfDB(Object snapshot) {
        HashMap<String, Object> legaParams = (HashMap<String, Object>) snapshot;

        Object numPartecipanti = legaParams.get("numPartecipanti");
        Object giornataInizio = legaParams.get(GIORNATA_INIZIO);
        Object lastRoundCalculated = legaParams.get(LAST_ROUND_CALCULATED);
        Object startedObject = legaParams.get("started");
        boolean started = (startedObject != null) && (boolean) startedObject;
        LegaType legaType = LegaType.valueOf((String) legaParams.get("tipologia"));
        Object latitude = legaParams.get("latitude");
        Object longitude = legaParams.get("longitude");

        List<HashMap<String, Object>> classifica = null;
        HashMap<String, List<Game>> calendario = null;
        if (started) {
            classifica = (List<HashMap<String, Object>>) legaParams.get(CLASSIFICA);
        }
        if (legaType == LegaType.CALENDARIO && started) {
            calendario = (HashMap<String, List<Game>>) legaParams.get(CALENDARIO);
        }

        return new Lega(
                (String) legaParams.get("name"),
                (String) legaParams.get("location"),
                (latitude != null) ? (Double) latitude : 0,
                (longitude != null) ? (Double) longitude : 0,
                started,
                (int) ((giornataInizio != null) ? (long) giornataInizio : 0),
                (int) ((lastRoundCalculated != null) ? (long) lastRoundCalculated : 0),
                (String) legaParams.get("admin"),
                (List<String>) legaParams.get("partecipanti"),
                (int) ((numPartecipanti != null) ? (long) numPartecipanti : 0),
                legaType,
                classifica,
                calendario
        );
    }

    public static User getUserFromHashMapOfDB(Object snapshotOfUser) {
        HashMap<String, Object> userParams = (HashMap<String, Object>) snapshotOfUser;

        String id = (String) userParams.get("id");
        String nickname = (String) userParams.get("nickname");
        String teamName = (String) userParams.get("teamName");
        String teamLogo = (String) userParams.get("teamLogo");
        String legaSelezionata = (String) userParams.get("legaSelezionata");
        List<String> roster = (List<String>) userParams.get("roster");
        List<HashMap<FieldPositions, String>> formazioniPerGiornata = null;

        HashMap<String, HashMap<String, String>> formazioniRawMap = (HashMap<String, HashMap<String, String>>) userParams.get("formazioniPerGiornata");
        if (formazioniRawMap != null) {
            formazioniPerGiornata = new ArrayList<>(calendarListOfRoundStart.size());
            List<Integer> indexArray = new ArrayList<>(formazioniRawMap.size());
            for (String stringIndex : formazioniRawMap.keySet()) {
                int index = Integer.parseInt(stringIndex);
                indexArray.add(index);
            }
            for (int i = 0; i < calendarListOfRoundStart.size(); i++) {
                if (indexArray.contains(i)) {
                    HashMap<String, String> stringHashMap = formazioniRawMap.get(String.valueOf(i));
                    HashMap<FieldPositions, String> correctHashMap = new HashMap<>();
                    assert stringHashMap != null;
                    for (String key : stringHashMap.keySet()) {
                        correctHashMap.put(FieldPositions.valueOf(key), stringHashMap.get(key));
                    }
                    formazioniPerGiornata.add(i, correctHashMap);
                } else {
                    formazioniPerGiornata.add(i, null);
                }
            }
        }

        return new User(id, nickname, teamName, teamLogo, legaSelezionata, roster, formazioniPerGiornata);
    }

    public static List<Game> getGamesFromHashmap(List<HashMap<String, Object>> hashMapsList) {
        List<Game> gameList = new ArrayList<>(hashMapsList.size());
        for (HashMap<String, Object> hashMap : hashMapsList) {
            Object homePoints = hashMap.get("homePoints");
            Object awayPoints = hashMap.get("awayPoints");
            gameList.add(
                    new Game((String) hashMap.get("homeUserId"), (String) hashMap.get("awayUserId"),
                            homePoints != null ? (int) homePoints : 0,
                            awayPoints != null ? (int) awayPoints : 0));
        }
        return gameList;
    }
}
