package it.units.fantabasket.utils;

import it.units.fantabasket.entities.Game;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.enums.LegaType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.utils.AssetDecoderUtil.numberOfGamesInTheSeason;

@SuppressWarnings({"unchecked", "ConstantConditions"})
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
            classifica = decodeClassifica(legaParams.get(CLASSIFICA), legaType);
        }
        if (legaType == LegaType.CALENDARIO && started) {
            calendario = decodeCalendario(legaParams.get(CALENDARIO));
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

    @NotNull
    private static HashMap<String, List<Game>> decodeCalendario(Object calendarioToDecode) {
        HashMap<String, List<Game>> calendario;
        calendario = new HashMap<>();

        HashMap<String, List<HashMap<String, Object>>> calendarioOfObject =
                (HashMap<String, List<HashMap<String, Object>>>) calendarioToDecode;

        for (String key : calendarioOfObject.keySet()) {
            List<Game> gameList = getGamesFromHashmap(calendarioOfObject.get(key));
            calendario.put(key, gameList);
        }
        return calendario;
    }

    private static List<HashMap<String, Object>> decodeClassifica(Object classificaToDecode, LegaType legaType) {
        List<HashMap<String, Object>> classificaDecodificate = new ArrayList<>();
        for (HashMap<String, Object> elementOfClassifica : (List<HashMap<String, Object>>) classificaToDecode) {
            elementOfClassifica.put(TOTAL_POINTS_SCORED, (int) ((long) elementOfClassifica.get(TOTAL_POINTS_SCORED)));

            if (legaType == LegaType.CALENDARIO) {
                elementOfClassifica.put(TOTAL_POINTS_ALLOWED, (int) ((long) elementOfClassifica.get(TOTAL_POINTS_ALLOWED)));
                elementOfClassifica.put(POINTS_OF_VICTORIES, (int) ((long) elementOfClassifica.get(POINTS_OF_VICTORIES)));
            }
            classificaDecodificate.add(elementOfClassifica);
        }
        return classificaDecodificate;
    }

    public static User getUserFromHashMapOfDB(Object snapshotOfUser) {
        HashMap<String, Object> userParams = (HashMap<String, Object>) snapshotOfUser;

        String id = (String) userParams.get("id");
        String nickname = (String) userParams.get("nickname");
        String teamName = (String) userParams.get("teamName");
        String teamLogo = (String) userParams.get("teamLogo");
        String legaSelezionata = (String) userParams.get("legaSelezionata");
        List<String> roster = (List<String>) userParams.get("roster");
        HashMap<String, HashMap<FieldPositions, String>> formazioniPerGiornata = null;

        HashMap<String, HashMap<String, String>> formazioniRawMap = (HashMap<String, HashMap<String, String>>) userParams.get("formazioniPerGiornata");
        if (formazioniRawMap != null) {
            formazioniPerGiornata = new HashMap<>(numberOfGamesInTheSeason);

            for (String giornataString : formazioniRawMap.keySet()) {
                HashMap<String, String> stringHashMap = formazioniRawMap.get(giornataString);
                HashMap<FieldPositions, String> correctHashMap = null;
                if (stringHashMap != null) {
                    correctHashMap = new HashMap<>();
                    for (String key : stringHashMap.keySet()) {
                        correctHashMap.put(FieldPositions.valueOf(key), stringHashMap.get(key));
                    }
                }
                formazioniPerGiornata.put(giornataString, correctHashMap);
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
                            homePoints != null ? (int) ((long) homePoints) : 0,
                            awayPoints != null ? (int) ((long) awayPoints) : 0));
        }
        return gameList;
    }
}
