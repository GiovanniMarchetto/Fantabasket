package it.units.fantabasket.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.R;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.layouts.LegaLayout;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static it.units.fantabasket.ui.MainActivity.userDataReference;

public class Utils {

    public static final String MIO_TAG = "MIO";
    public static final String GOOD = "good";
    public static final String WARNING = "warning";
    public static final String ERROR = "error";

    public static final ViewGroup.LayoutParams LAYOUT_PARAMS =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    public static String teamLogoBase64;
    private static LocationCallback locationCallback;

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
                            imageView.setVisibility(View.VISIBLE);
                            Log.i(MIO_TAG, "LOGO-- prima: " + original.getByteCount()
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
        String backgroundColor;
        switch (type) {
            case GOOD:
                backgroundColor = "#FFCCFF90";
                break;
            case WARNING:
                backgroundColor = "#FFFFFF8D";
                break;
            case ERROR:
                backgroundColor = "#FFD32F2F";
                break;
            default:
                backgroundColor = "#FFB1B1B1";
        }
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(Color.parseColor(backgroundColor));
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }

    public static void showSnackbar(View view, String message) {
        showSnackbar(view, message, "normal");
    }

    public static Task<Location> getLastLocation(Context context, Activity activity) {
        if (activity == null || context == null) {
            Log.e(MIO_TAG, "Something null: \n---> Activity-" + activity + "\n---> Context-" + context);
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

    public static void useLocalizationAndExecuteUpdate(
            Context context, Activity activity,
            UpdateLocationInterface updateLocationInterface, HashMap<Lega, LegaLayout> legaLinearLayoutHashMap) {

        FusedLocationProviderClient fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PackageManager.PERMISSION_GRANTED
            );
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                Location location = null;
                final List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    location = locationList.get(locationList.size() - 1);
                }

                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                updateLocationInterface.updateLocation(location, legaLinearLayoutHashMap);
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public static void sendEmailForResetPassword(@NotNull View view, String email) {
        if (email != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showSnackbar(view, view.getContext().getString(it.units.fantabasket.R.string.email_for_password_reset_sent), GOOD);
                        } else {
                            showSnackbar(view, view.getContext().getString(it.units.fantabasket.R.string.email_for_reset_password_not_sent), ERROR);
                        }
                    });
        } else {
            showSnackbar(view, view.getContext().getString(R.string.email_of_user_account_null), ERROR);
        }
    }
}
