package it.units.fantabasket.ui.access;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentRegistrationBinding;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

public class RegistrationFragment extends Fragment {

    public static String nickname;
    public static String teamName;
    private FragmentRegistrationBinding registrationBinding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        registrationBinding = FragmentRegistrationBinding.inflate(inflater, container, false);
        registrationBinding.teamLogoImage.setVisibility(View.GONE);
        return registrationBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Utils.teamLogoBase64 = "";//resetto

        @SuppressWarnings("ConstantConditions")
        ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                Utils.getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(
                        getActivity().getContentResolver(), registrationBinding.teamLogoImage, false));

        registrationBinding.teamLogoButton.setOnClickListener(view1 -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
            registrationBinding.teamLogoImage.setVisibility(View.VISIBLE);
        });

        registrationBinding.nickname.addTextChangedListener((TextWatcherAfterChange) var -> nickname = var.toString());

        registrationBinding.teamName.addTextChangedListener((TextWatcherAfterChange) var -> teamName = var.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        registrationBinding = null;
    }

}