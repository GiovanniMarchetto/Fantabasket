package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.databinding.ActivityAccessBinding;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;

import java.util.HashMap;

public class AccessActivity extends AppCompatActivity {

    private ActivityAccessBinding accessBinding;
    private FirebaseAuth mAuth;
    private Fragment loginFragment;
    private Fragment registrationFragment;
    private FragmentTransaction transaction;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();//inizializzo
        passToMainActivityIfUserNotNull();
    }

    private void passToMainActivityIfUserNotNull() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.i("MIO", "not null user: " + user.getDisplayName());
            Intent intent = new Intent(AccessActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessBinding = ActivityAccessBinding.inflate(getLayoutInflater());
        setContentView(accessBinding.getRoot());

        loginFragment = new LoginFragment();
        registrationFragment = new RegistrationFragment();

        accessBinding.userEmail.addTextChangedListener(
                (TextWatcherAfterChange) editable -> {
                    if (!editable.toString().contains("@")) {
                        accessBinding.userEmail.setError("Not an email");
                    }
                });

        accessBinding.password.addTextChangedListener(
                (TextWatcherAfterChange) editable -> {
                    if (editable.toString().length() < 6) {
                        accessBinding.password.setError("At least of 6");
                    }
                });

        accessBinding.actionButton.setOnClickListener(view -> {
            if (accessBinding.userEmail.getError() != null
                    || accessBinding.password.getError() != null
                    || accessBinding.userEmail.getText().toString().equals("")
                    || accessBinding.password.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "Error on user data inputs", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = accessBinding.userEmail.getText().toString();
            String password = accessBinding.password.getText().toString();

            String actionButtonText = accessBinding.actionButton.getText().toString();
            if (getString(R.string.login).equals(actionButtonText)) {
                login(email, password);
            } else if (getString(R.string.register).equals(actionButtonText)) {

                if (RegistrationFragment.nickname.equals("")
                        || RegistrationFragment.teamName.equals("")
                        || Utils.teamLogoBase64.equals("")) {
                    Toast.makeText(getApplicationContext(), "Error on team inputs", Toast.LENGTH_SHORT).show();
                    return;
                }

                registration(email, password,
                        RegistrationFragment.nickname, RegistrationFragment.teamName,
                        Utils.teamLogoBase64);
            } else {
                Toast.makeText(getApplicationContext(), "Unexpected Error", Toast.LENGTH_SHORT).show();
            }
        });

        accessBinding.switch1.setOnCheckedChangeListener((compoundButton, b) -> {
            transaction = getSupportFragmentManager().beginTransaction();
            if (b) {
                accessBinding.actionButton.setText(getString(R.string.register));
                transaction.replace(R.id.access_fragment_container, registrationFragment);
            } else {
                accessBinding.actionButton.setText(getString(R.string.login));
                transaction.replace(R.id.access_fragment_container, loginFragment);
            }
            transaction.commit();
        });
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        passToMainActivityIfUserNotNull();
                    } else {
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registration(String email, String password, String name, String teamName, String teamLogoBase64) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("teamName", teamName);
                        userData.put("teamLogo", teamLogoBase64);

                        //noinspection ConstantConditions
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(mAuth.getCurrentUser().getUid()).setValue(userData);

                        passToMainActivityIfUserNotNull();
                    } else {
                        Log.w("ERROR", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}