package it.units.fantabasket;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.databinding.ActivityLeaguesBinding;
import org.jetbrains.annotations.NotNull;

public class LeaguesActivity extends AppCompatActivity {

    public static FirebaseUser firebaseUserLeagues;
    public static DatabaseReference userDataReferenceInLeagues;
    public static DatabaseReference legheReferenceInLeagues;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUserLeagues = FirebaseAuth.getInstance().getCurrentUser();
        userDataReferenceInLeagues = FirebaseDatabase.getInstance().getReference("users").child(firebaseUserLeagues.getUid());
        legheReferenceInLeagues = FirebaseDatabase.getInstance().getReference("leghe");

        ActivityLeaguesBinding binding = ActivityLeaguesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_leagues);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_leagues);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public static class SelectLeagueDialogFragment extends DialogFragment {

        public static SelectLeagueDialogFragment newInstance(String legaName) {
            Bundle args = new Bundle();
            SelectLeagueDialogFragment frag = new SelectLeagueDialogFragment();
            args.putString("legaName", legaName);
            frag.setArguments(args);

            return frag;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Activity activity = getActivity();
            assert getArguments() != null;
            String legaName = getArguments().getString("legaName");

            assert activity != null;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.seleziona_lega)
                    .setPositiveButton(R.string.salva, (dialog, id) -> {
                        FirebaseDatabase.getInstance().getReference("users").child(firebaseUserLeagues.getUid()).child("legaSelezionata").setValue(legaName);
                        Intent intent = new Intent(activity, MainActivity.class);
                        startActivity(intent);
                        activity.finish();
                    })
                    .setNegativeButton("cancel", (dialog, id) -> {

                    });
            return builder.create();
        }
    }
}
