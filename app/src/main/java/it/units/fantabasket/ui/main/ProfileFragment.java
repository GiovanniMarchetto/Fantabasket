package it.units.fantabasket.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.AccessActivity;
import it.units.fantabasket.LeaguesActivity;
import it.units.fantabasket.databinding.FragmentProfileBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import static it.units.fantabasket.MainActivity.*;
import static it.units.fantabasket.layouts.LegaLayout.addLegaParamsAtView;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Activity activity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Lega actualLega = leagueOn.get();
        if (actualLega != null) {
            addLegaParamsAtView(activity, actualLega, binding.leagueParams);
        }

        binding.changeLegaButton.setOnClickListener(viewListener -> {
            Intent intent = new Intent(activity, LeaguesActivity.class);
            startActivity(intent);
        });

        //TEAM params

        binding.nicknameProfile.setText(user.nickname);

        binding.nicknameProfile.addTextChangedListener((TextWatcherAfterChange) editable -> {
            if (editable.toString().equals("")) {
                binding.nicknameProfile.setError("Invalid Nickname");
            }
        });

        binding.nicknameChangeButton.setOnClickListener(viewListener -> {
            if (binding.nicknameProfile.getError() == null) {
                userDataReference.child("nickname").setValue(binding.nicknameProfile.getText().toString());
            }
        });

        binding.teamNameProfile.setText(user.teamName);

        binding.teamNameChangeButton.setOnClickListener(viewListener -> {
            String teamNameUpdate = binding.teamNameProfile.getText().toString();
            if (!teamNameUpdate.equals("")) {
                userDataReference.child("teamName").setValue(teamNameUpdate);
            }
        });


        binding.teamLogoProfile.setImageBitmap(Utils.getBitmapFromBase64(user.teamLogo));

        ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                Utils.getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(
                        requireContext().getContentResolver(), binding.teamLogoProfile, true));

        binding.logoChangeButton.setOnClickListener(viewListener -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
        });


        //profile params

        binding.emailProfile.setText(firebaseUser.getEmail());

        binding.emailProfile.addTextChangedListener((TextWatcherAfterChange) editable -> {
            if (!editable.toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                binding.emailProfile.setError("Invalid Email Address");
            }
        });

        binding.emailChangeButton.setOnClickListener(viewListener -> {
            String emailUpdate = binding.emailProfile.getText().toString();
            if (binding.emailProfile.getError() == null && !emailUpdate.equals(firebaseUser.getEmail())) {
                firebaseUser.updateEmail(emailUpdate)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Utils.showSnackbar(view, "Email changed", "good");
                            } else {
                                Utils.showSnackbar(view, "Email NOT changed", "error");
                            }
                        });
            }
        });

        binding.passwordChangeButton.setOnClickListener(viewListener -> {
            String emailAddress = firebaseUser.getEmail();
            if (emailAddress != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Utils.showSnackbar(view, "Email for password reset sent", "good");
                            } else {
                                Utils.showSnackbar(view, "It's not possible to sent email for reset password", "error");
                            }
                        });
            } else {
                Utils.showSnackbar(view, "Email of your account is null", "error");
            }
        });

        binding.exitButton.setOnClickListener(viewListener -> returnToLogin());
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(activity, AccessActivity.class);
        startActivity(intent);
        activity.finish();
    }
}