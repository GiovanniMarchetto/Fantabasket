package it.units.fantabasket;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import it.units.fantabasket.databinding.ActivityMainBinding;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static FirebaseUser user;
    public static DatabaseReference userDataReference;
    public static List<String> roster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userDataReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        roster = new ArrayList<>(16);
        loadCurrentRoster();

        it.units.fantabasket.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void loadCurrentRoster() {
        Log.i("DATI", ".............sto caricando i giocatori");
        userDataReference.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                @SuppressWarnings("unchecked") List<String> currentRoster = (List<String>) dataSnapshot.getValue();
//                Log.i("DATI", "RECUPERO ROSTER");

                if (currentRoster != null) {
                    roster = currentRoster;
//                    for (String userPlayer : roster) {
//                        Log.i("DATI", userPlayer);
//                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

}