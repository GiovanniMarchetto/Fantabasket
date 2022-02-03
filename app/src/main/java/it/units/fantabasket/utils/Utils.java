package it.units.fantabasket.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import static it.units.fantabasket.ui.MainActivity.userDataReference;

public class Utils {

    public static final ViewGroup.LayoutParams LAYOUT_PARAMS =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    public static String teamLogoBase64;

    public static ActivityResultCallback<ActivityResult> getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(
            ContentResolver contentResolver, ImageView imageView, boolean saveInDB) {
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
                                userDataReference.child("teamLogo").setValue(teamLogoBase64);
                            }

                            imageView.setImageBitmap(bitmap);
                            Log.i("MIO", "LOGO-- prima: " + original.getByteCount()
                                    + " --> " + bitmap.getByteCount() + " --> baos " + imageBytes.length);
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

    public static Bitmap getBitmapFromBase64(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.NO_WRAP);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static void showSnackbar(View view, String message, String type) {
        String textColor;
        String backgroundColor;
        switch (type) {
            case "good":
                textColor = "#FF000000";
                backgroundColor = "#FFCCFF90";
                break;
            case "warning":
                textColor = "#FF000000";
                backgroundColor = "#FFFFFF8D";
                break;
            case "error":
                textColor = "#FF000000";
                backgroundColor = "#FFD32F2F";
                break;
            default:
                textColor = "#FF000000";
                backgroundColor = "#FFB1B1B1";
        }
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.parseColor(backgroundColor));
        snackbar.setTextColor(Color.parseColor(textColor));
        snackbar.show();
    }

    public static void showSnackbar(View view, String message) {
        showSnackbar(view, message, "normal");
    }

    public static Task<Location> getLastLocation(Context context, Activity activity) {
        if (activity == null || context == null) {
            Log.e("MIO", "Something null: \n---> Activity-" + activity + "\n---> Context-" + context);
            return null;
        }

        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PackageManager.PERMISSION_GRANTED
            );
            return null;
        }

        return fusedLocationProviderClient.getLastLocation();
    }

    @NotNull
    public static String setTheme(SharedPreferences sharedPreferences) {
        String theme = sharedPreferences.getString("theme", null);

        if ("light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        return theme;
    }
}
