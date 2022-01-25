package it.units.fantabasket.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.AccessActivity;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentNotificationsBinding;
import it.units.fantabasket.utils.Utils;

import static it.units.fantabasket.MainActivity.firebaseUser;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.text.setText(firebaseUser.getEmail());

        binding.exitButton.setOnClickListener(view -> returnToLogin());

        ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                Utils.getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(
                        requireContext().getContentResolver(), binding.teamLogo, true));

        binding.changeLogoButton.setOnClickListener(view -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
        });

        binding.changeLegaButton.setOnClickListener(view ->
                NavHostFragment.findNavController(NotificationsFragment.this)
                        .navigate(R.id.action_NotificationFragment_to_LegheFragment)
        );
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(getContext(), AccessActivity.class);
        startActivity(intent);
//        finish();
    }
}