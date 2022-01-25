package it.units.fantabasket.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.fragment.app.FragmentActivity;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static it.units.fantabasket.MainActivity.userDataReference;

public class Utils {

    public static String teamLogoBase64;

    public static ActivityResultCallback<ActivityResult> getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(ContentResolver contentResolver, ImageView imageView, boolean saveInDB) {
        return result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (null != data) {
                    Uri selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        try {
                            //from uri to bitmap
                            Bitmap original = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                    ? ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, selectedImageUri))
                                    : MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri);

                            //compress bitmap
                            int maxSize = 200; //kB
                            Bitmap bitmap = Bitmap.createScaledBitmap(original, maxSize, maxSize, true);

                            //from bitmap to base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            teamLogoBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                            if (saveInDB) {
                                userDataReference.child("teamLogo").setValue(Utils.teamLogoBase64);
                            }

                            imageView.setImageBitmap(bitmap);
                            Log.i("MIO", "LOGO-- prima: " + original.getByteCount() + " --> " + bitmap.getByteCount() + " --> baos " + imageBytes.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public static Calendar getCalendarNow() {
        final String italyId = "Europe/Rome";
        return Calendar.getInstance(TimeZone.getTimeZone(italyId));
    }

    @SuppressWarnings("unchecked")
    public static Lega getLegaFromHashMapOfDB(HashMap<String, Object> legaParams) {
        Object numPartecipanti = legaParams.get("numPartecipanti");
        Object giornataInizio = legaParams.get("giornataInizio");
        Object lastRoundCalculated = legaParams.get("lastRoundCalculated");
        Object startedObject = legaParams.get("started");
        boolean started = (startedObject != null) && (boolean) startedObject;
        LegaType legaType = LegaType.valueOf((String) legaParams.get("tipologia"));
        Object latitude = legaParams.get("latitude");
        Object longitude = legaParams.get("longitude");

        List<HashMap<String, Object>> classifica = null;
        HashMap<String, List<Game>> calendario = null;
        if (started) {
            classifica = (List<HashMap<String, Object>>) legaParams.get("classifica");
        }
        if (legaType == LegaType.CALENDARIO && started) {
            calendario = (HashMap<String, List<Game>>) legaParams.get("calendario");
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

    @SuppressWarnings("unchecked")
    public static User getUserFromHashMapOfDB(HashMap<String, Object> userParams) {
        String id = (String) userParams.get("id");
        String nickname = (String) userParams.get("nickname");
        String teamName = (String) userParams.get("teamName");
        String teamLogo = (String) userParams.get("teamLogo");
        String legaSelezionata = (String) userParams.get("legaSelezionata");
        List<String> roster = (List<String>) userParams.get("roster");
        List<HashMap<FieldPositions, String>> formazioniPerGiornata = (List<HashMap<FieldPositions, String>>) userParams.get("formazioniPerGiornata");

        return new User(id, nickname, teamName, teamLogo, legaSelezionata, roster, formazioniPerGiornata);
    }

    public static List<Game> getGamesFromHashmap(List<HashMap<String, Object>> hashMapsList) {
        List<Game> gameList = new ArrayList<>(hashMapsList.size());
        for (HashMap<String, Object> hashMap : hashMapsList) {
            gameList.add(
                    new Game((String) hashMap.get("homeUserId"), (String) hashMap.get("awayUserId"),
                            (Integer) hashMap.get("homePoints"), (Integer) hashMap.get("awayPoints")));
        }
        return gameList;
    }

    public static Bitmap getBitmapFromBase64(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static void setCompletePlayerList(FragmentActivity fragmentActivity, List<Player> playerList) {
        for (Team team : Team.values()) {
            try {
                InputStreamReader is = new InputStreamReader(
                        fragmentActivity.getAssets()
                                .open("giocatori/" + team.name().toLowerCase() + ".csv"));
                BufferedReader reader = new BufferedReader(is);
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    Role role_1;
                    Role role_2 = null;
                    if (values[3].contains("/")) {
                        String[] roles = values[3].split("/");
                        role_1 = Role.valueOf(roles[0].toUpperCase());
                        role_2 = Role.valueOf(roles[1].toUpperCase());
                    } else {
                        role_1 = Role.valueOf(values[3].toUpperCase());
                    }


                    playerList.add(new Player(
                            values[0], values[1], values[2],
                            role_1, role_2,
                            values[4], values[5], values[6],
                            values[7], team
                    ));
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
