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
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentProfileBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.ui.access.AccessActivity;
import it.units.fantabasket.ui.leagues.LeaguesActivity;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;

import static it.units.fantabasket.layouts.LegaLayout.addLegaParamsAtView;
import static it.units.fantabasket.ui.MainActivity.*;
import static it.units.fantabasket.utils.DecoderUtil.TEAM_NAME;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (leagueOn != null) {
            Lega actualLega = leagueOn.get();
            if (actualLega != null) {
                addLegaParamsAtView(activity, actualLega, binding.leagueParams);
            }
        }

        binding.changeLegaButton.setOnClickListener(viewListener -> {
            Intent intent = new Intent(activity, LeaguesActivity.class);
            startActivity(intent);
        });

        //TEAM params
        if (user != null) {
            binding.nicknameProfile.setText(user.nickname);
            binding.teamNameProfile.setText(user.teamName);
            binding.teamLogoProfile.setImageBitmap(Utils.getBitmapFromBase64(user.teamLogo));
        }

        binding.nicknameProfile.addTextChangedListener((TextWatcherAfterChange) editable -> {
            if (editable.toString().equals("")) {
                binding.nicknameProfile.setError(getString(R.string.invalid_nickname));
            }
        });

        binding.nicknameChangeButton.setOnClickListener(viewListener -> {
            if (binding.nicknameProfile.getError() == null) {
                userDataReference.child("nickname").setValue(binding.nicknameProfile.getText().toString());
            }
        });


        binding.teamNameChangeButton.setOnClickListener(viewListener -> {
            String teamNameUpdate = binding.teamNameProfile.getText().toString();
            if (!teamNameUpdate.equals("")) {
                userDataReference.child(TEAM_NAME).setValue(teamNameUpdate);
            }
        });


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

        binding.emailProfile.addTextChangedListener(AccessActivity.getTextWatcherForValidateEmailField(binding.emailProfile));

        binding.emailChangeButton.setOnClickListener(viewListener -> {
            String emailUpdate = binding.emailProfile.getText().toString();
            if (binding.emailProfile.getError() == null && !emailUpdate.equals(firebaseUser.getEmail())) {
                firebaseUser.updateEmail(emailUpdate)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Utils.showSnackbar(view, getString(R.string.email_changed), Utils.GOOD);
                            } else {
                                Utils.showSnackbar(view, getString(R.string.email_not_changed), Utils.ERROR);
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
                                Utils.showSnackbar(view, getString(R.string.email_for_password_reset_sent), Utils.GOOD);
                            } else {
                                Utils.showSnackbar(view, getString(R.string.email_for_reset_password_not_sent), Utils.ERROR);
                            }
                        });
            } else {
                Utils.showSnackbar(view, getString(R.string.email_of_user_account_null), Utils.ERROR);
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