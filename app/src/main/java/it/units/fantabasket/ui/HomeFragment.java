package it.units.fantabasket.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import it.units.fantabasket.databinding.FragmentHomeBinding;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (null != data) {
                            Uri selectedImageUri = data.getData();
                            if (null != selectedImageUri) {
                                try {
                                    //from uri to bitmap
                                    Bitmap original = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                            ? ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().getContentResolver(), selectedImageUri))
                                            : MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);

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

                                    //set bitmap in the window
                                    binding.teamLogo.setImageBitmap(bitmap);

                                    //from bitmap to base64
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] imageBytes = baos.toByteArray();
                                    String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                                    Log.i("DATI", "prima: "+original.getByteCount() + " --> "+bitmap.getByteCount() +" --> baos "+imageBytes.length);

                                    //save file in db
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    assert user != null;
                                    DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                                    userDataReference.child("teamLogo").setValue(base64String);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            DatabaseReference userDataReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userDataReference.child("teamName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    String teamName = dataSnapshot.getValue(String.class);
                    binding.teamName.setText(teamName);
                    Log.i("DATI", teamName);
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
                }
            });

            userDataReference.child("teamLogo").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    String teamLogo = dataSnapshot.getValue(String.class);
                    byte[] decodedString = Base64.decode(teamLogo, Base64.NO_WRAP);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.teamLogo.setImageBitmap(decodedByte);

                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
                }
            });
        } else {
            Log.e("ERROR", "KIM POSSIBLE");
        }

        binding.modifyButton.setOnClickListener(view -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}