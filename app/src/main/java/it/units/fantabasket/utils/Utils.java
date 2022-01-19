package it.units.fantabasket.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {

    public static String teamLogoBase64;

    public static ActivityResultCallback<ActivityResult> getActivityResultCallbackForChangeTeamLogo(ContentResolver contentResolver, ImageView imageView) {
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
                            float width = original.getWidth();
                            float height = original.getHeight();
                            float bitmapRatio = width / height;
                            if (bitmapRatio > 1) {
                                width = maxSize;
                                height = (width / bitmapRatio);
                            } else {
                                height = maxSize;
                                width = (height * bitmapRatio);
                            }
                            Bitmap bitmap = Bitmap.createScaledBitmap(original,
                                    (int) width, (int) height, true);

                            //from bitmap to base64
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageBytes = baos.toByteArray();
                            teamLogoBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                            imageView.setImageBitmap(bitmap);
                            Log.i("DATI", "prima: " + original.getByteCount() + " --> " + bitmap.getByteCount() + " --> baos " + imageBytes.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }
}
