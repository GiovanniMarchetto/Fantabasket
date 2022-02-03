package it.units.fantabasket.utils;

import android.content.Context;
import android.util.Log;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class AssetDecoderUtil {
    public static int currentRound;
    public static Calendar calendarOfCurrentRoundStart;
    public static List<Calendar> calendarListOfRoundStart;

    public static HashMap<String, Player> completePlayersList;

    public static void loadRoundInfos(Context context) {
        Calendar currentCal = Utils.getCalendarNow();
        calendarListOfRoundStart = new ArrayList<>();

        try {
            InputStreamReader is = new InputStreamReader(context.getAssets().open("orariInizio.csv"));
            BufferedReader reader = new BufferedReader(is);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                int giornata = Integer.parseInt(values[0]) - 1;
                int year = Integer.parseInt(values[1]);
                int month = Integer.parseInt(values[2]) - 1;
                int day = Integer.parseInt(values[3]);
                int hour = Integer.parseInt(values[4]);
                int minute = Integer.parseInt(values[5]);
                Calendar orario = new GregorianCalendar(year, month, day, hour, minute, 0);
                calendarListOfRoundStart.add(giornata, orario);
                //siccome va in ordine di giornata la prima che non è già passata è la giornata corrente
            }
        } catch (Exception e) {
            Log.e("MIO", "Error loading asset files --> " + e.getMessage(), e);
        }

        for (int i = 0; i < calendarListOfRoundStart.size(); i++) {//sono in ordine
            Calendar cloneOfCalendar = (Calendar) calendarListOfRoundStart.get(i).clone();
            cloneOfCalendar.add(Calendar.DATE, 2);//si suppone che due giorni dopo siano finite le partite
            if (currentCal.before(cloneOfCalendar)) {
                currentRound = i + 1;
                calendarOfCurrentRoundStart = calendarListOfRoundStart.get(i);
                break;
            }
        }
    }

    public static void loadAllPlayersInfos(Context applicationContext) {
        completePlayersList = new HashMap<>();

        for (Team team : Team.values()) {
            try {
                InputStreamReader is = new InputStreamReader(
                        applicationContext.getAssets().open("giocatori/" + team.name().toLowerCase() + ".csv"));
                BufferedReader reader = new BufferedReader(is);
                reader.readLine();
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");

                    //parsing
                    final String numberOfPlayer = values[0];
                    final String name = values[1];
                    final String surname = values[2];
                    final String roles = values[3];
                    final String dateBirth = values[4];
                    final String heightOfPlayer = values[5];
                    final String weightOfPlayer = values[6];
                    final String nationality = values[7];
                    final int cost = Integer.parseInt(values[8]);

                    //id's must be all distinct
                    String id = surname;
                    int posName = 0;
                    while (completePlayersList.containsKey(id)) {
                        if (posName == 0) id = id + " ";
                        char nameChar = name.charAt(posName);
                        id = id + nameChar;
                        posName++;
                    }

                    Role role_1;
                    Role role_2 = null;
                    if (roles.contains("/")) {
                        String[] role = roles.split("/");
                        role_1 = Role.valueOf(role[0].toUpperCase());
                        role_2 = Role.valueOf(role[1].toUpperCase());
                    } else {
                        role_1 = Role.valueOf(roles.toUpperCase());
                    }

                    Player player = new Player(
                            id, numberOfPlayer, name, surname,
                            role_1, role_2, dateBirth, heightOfPlayer, weightOfPlayer,
                            nationality, team, cost);

                    completePlayersList.put(id, player);
                }
            } catch (IOException e) {
                Log.e("MIO", "Error loading asset files", e);
            } catch (Exception ee) {
                Log.e("MIO", ee.getMessage());
                ee.printStackTrace();
            }
        }
    }
}
