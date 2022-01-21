package it.units.fantabasket;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import it.units.fantabasket.databinding.ActivityMainBinding;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    public static FirebaseUser user;
    public static DatabaseReference userDataReference;
    public static DatabaseReference legheReference;
    public static int giornataCorrente;
    public static Calendar orarioInizio;
    public static List<String> roster;
    public static int ultimaGiornata;

    public static Calendar getCalendarNow() {
        final String italyId = "Europe/Rome";
        return Calendar.getInstance(TimeZone.getTimeZone(italyId));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PackageManager.PERMISSION_GRANTED
        );

        //dati fissi
        Calendar currentCal = getCalendarNow();
        List<Calendar> orariInizio = new ArrayList<>();

        try {
            InputStreamReader is = new InputStreamReader(getAssets().open("orariInizio.csv"));
            BufferedReader reader = new BufferedReader(is);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                int giornata = Integer.parseInt(values[0]);
                int year = Integer.parseInt(values[1]);
                int month = Integer.parseInt(values[2]);
                int day = Integer.parseInt(values[3]);
                int hour = Integer.parseInt(values[4]);
                int minute = Integer.parseInt(values[5]);
                Calendar orario = new GregorianCalendar(year, month, day, hour, minute, 0);
                orariInizio.add(orario);
                //siccome va in ordine di giornata la prima che non è già passata è la giornata corrente
            }
        } catch (Exception e) {
            Log.e("DATI", "Error loading asset files --> " + e.getMessage(), e);
        }

        ultimaGiornata = orariInizio.size();

        for (int i = 0; i < orariInizio.size(); i++) {//sono in ordine
            Calendar g = orariInizio.get(i);
            g.add(Calendar.DATE, 2);//si suppone che due giorni dopo siano finite le partite
            if (currentCal.before(g)) {
                giornataCorrente = i + 1;
                orarioInizio = orariInizio.get(i);
                break;
            }
        }

        //dati dinamici
        user = FirebaseAuth.getInstance().getCurrentUser();
        userDataReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        legheReference = FirebaseDatabase.getInstance().getReference("leghe");
        roster = new ArrayList<>(16);
        loadCurrentRoster();


//        FirebaseDatabase.getInstance().getReference("giornataCorrente").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                Integer value = snapshot.getValue(Integer.class);
//                if (value != null) {
//                    giornataCorrente = value;
//                }
//                Log.i("MIO", "GiornataCorrente: " + giornataCorrente);
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        });

        it.units.fantabasket.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.addOnBackStackChangedListener(() -> Log.i("MIO-BS","Numero di fragment: "+fragmentManager.getBackStackEntryCount()));

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

}