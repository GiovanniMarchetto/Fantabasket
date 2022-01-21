package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.databinding.ActivityRegistrationBinding;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.userEmail.addTextChangedListener(
                (TextWatcherAfterChange) editable -> {
                    if (!editable.toString().contains("@")) {
                        binding.userEmail.setError("Not an email");
                    }
                });

        binding.password.addTextChangedListener(
                (TextWatcherAfterChange) editable -> {
                    if (editable.toString().length() < 6) {
                        binding.password.setError("At least of 6");
                    }
                });
        binding.repeatPassword.addTextChangedListener(
                (TextWatcherAfterChange) editable -> {
                    if (!editable.toString().equals(binding.password.getText().toString())) {
                        binding.repeatPassword.setError("It's not equal to password");
                    }
                });

        ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                Utils.getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(getContentResolver(), binding.teamLogoImage, false));

        binding.teamLogoButton.setOnClickListener(view -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
        });

        Utils.teamLogoBase64 = "";
        binding.registrationConstraintLayout.addOnLayoutChangeListener(
                (view, i, i1, i2, i3, i4, i5, i6, i7) ->
                        binding.registrationButton.setEnabled(
                                binding.userEmail.getError() == null
                                        && binding.password.getError() == null
                                        && binding.repeatPassword.getError() == null
                                        && !binding.userEmail.getText().toString().equals("")
                                        && !binding.password.getText().toString().equals("")
                                        && !binding.repeatPassword.getText().toString().equals("")
                                        && !binding.nickname.getText().toString().equals("")
                                        && !binding.teamName.getText().toString().equals("")
                                        && !Utils.teamLogoBase64.equals("")
                        ));

        binding.registrationButton.setOnClickListener(view -> registration(binding.userEmail.getText().toString(),
                binding.password.getText().toString(),
                binding.nickname.getText().toString(),
                binding.teamName.getText().toString(),
                Utils.teamLogoBase64));
    }

    private void registration(String email, String password, String name, String teamName, String teamLogoBase64) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //noinspection ConstantConditions
                        createUserData(mAuth.getCurrentUser().getUid(), name, teamName, teamLogoBase64);

                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w("ERROR", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserData(String userId, String name, String teamName, String teamLogoBase64) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("teamName", teamName);
        userData.put("teamLogo", teamLogoBase64);

        FirebaseDatabase.getInstance().getReference("users").child(userId).setValue(userData);
    }

}