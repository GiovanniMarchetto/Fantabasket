package it.units.fantabasket.ui.access;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.ActivityAccessBinding;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.ui.MainActivity;
import it.units.fantabasket.ui.NoConnectionActivity;
import it.units.fantabasket.utils.NetworkChangeReceiver;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import static it.units.fantabasket.utils.Utils.ERROR;

public class AccessActivity extends AppCompatActivity {

    private ActivityAccessBinding accessBinding;
    private FirebaseAuth mAuth;
    private Fragment loginFragment;
    private Fragment registrationFragment;
    private FragmentTransaction transaction;

    @NotNull
    public static TextWatcherAfterChange getTextWatcherForValidatePassword(EditText editText) {
        return editable -> {
            if (editable.toString().length() < 6) {
                editText.setError(editText.getContext().getString(R.string.at_least_6_chars));
            }
        };
    }

    @NotNull
    public static TextWatcherAfterChange getTextWatcherForValidateEmailField(EditText editText) {
        return editable -> {
            if (!editable.toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                editText.setError(editText.getContext().getString(R.string.invalid_email_address));
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accessBinding = ActivityAccessBinding.inflate(getLayoutInflater());
        setContentView(accessBinding.getRoot());

        loginFragment = new LoginFragment();
        registrationFragment = new RegistrationFragment();

        accessBinding.userEmail.addTextChangedListener(
                getTextWatcherForValidateEmailField(accessBinding.userEmail));

        accessBinding.password.addTextChangedListener(
                getTextWatcherForValidatePassword(accessBinding.password));

        accessBinding.actionButton.setOnClickListener(view -> {
            if (!areFieldsValid()) {
                Utils.showSnackbar(view, getString(R.string.input_fields_are_not_valid), ERROR);
                return;
            }

            String email = accessBinding.userEmail.getText().toString();
            String password = accessBinding.password.getText().toString();

            if (isRegistration()) {
                registration(email, password, RegistrationFragment.nickname, RegistrationFragment.teamName, Utils.teamLogoBase64);
            } else {
                login(email, password);
            }
        });

        accessBinding.loginRegistrationSwitch.setOnCheckedChangeListener((compoundButton, fromLoginToRegister) -> {
            transaction = getSupportFragmentManager().beginTransaction();
            if (fromLoginToRegister) {
                accessBinding.actionButton.setText(getString(R.string.register));
                transaction.replace(R.id.access_fragment_container, registrationFragment);
            } else {
                accessBinding.actionButton.setText(getString(R.string.login));
                transaction.replace(R.id.access_fragment_container, loginFragment);
            }
            transaction.commit();
        });
    }

    private boolean isRegistration() {
        String actionButtonText = accessBinding.actionButton.getText().toString();
        return getString(R.string.register).equals(actionButtonText);
    }

    private boolean areFieldsValid() {

        if (isRegistration() && (
                RegistrationFragment.nickname.equals("") || RegistrationFragment.teamName.equals("")
                        || Utils.teamLogoBase64.equals(""))) {
            return false;
        }

        return accessBinding.userEmail.getError() == null && accessBinding.password.getError() == null
                && !accessBinding.userEmail.getText().toString().equals("")
                && !accessBinding.password.getText().toString().equals("");
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();//inizializzo

        boolean isConnect = NetworkChangeReceiver.isNetworkAvailable(getApplicationContext());
        Log.i("MIO", "Connessione: " + isConnect);

        if (isConnect) {
            passToMainActivityIfUserNotNull();
        } else {
            Intent intent = new Intent(this, NoConnectionActivity.class);
            startActivity(intent);
        }
    }

    private void passToMainActivityIfUserNotNull() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(AccessActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        passToMainActivityIfUserNotNull();
                    } else {
                        Utils.showSnackbar(accessBinding.accessActivityLayout,
                                getString(R.string.authentication_failed), ERROR);
                    }
                });
    }

    private void registration(String email, String password, String name, String teamName, String teamLogoBase64) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        @SuppressWarnings("ConstantConditions")
                        String userId = mAuth.getCurrentUser().getUid();
                        User thisUser = new User(userId, name, teamName, teamLogoBase64);

                        FirebaseDatabase.getInstance().getReference("users")
                                .child(userId).setValue(thisUser);

                        passToMainActivityIfUserNotNull();
                    } else {
                        Utils.showSnackbar(accessBinding.accessActivityLayout,
                                getString(R.string.registration_failed), ERROR);
                    }
                });
    }
}