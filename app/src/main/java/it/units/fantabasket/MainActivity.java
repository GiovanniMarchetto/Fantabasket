package it.units.fantabasket;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.databinding.ActivityMainBinding;
import it.units.fantabasket.utils.MyValueEventListener;
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

    private boolean preferencesChanged = true;
    // called when the user changes the app's preferences
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            (sharedPreferences, key) -> {
                preferencesChanged = true;

                if (key.equals("theme")) {
                    String theme = setTheme(sharedPreferences);
                    Toast.makeText(MainActivity.this, theme, Toast.LENGTH_SHORT).show();
                }

            };

    @NotNull
    private String setTheme(SharedPreferences sharedPreferences) {
        String theme = sharedPreferences.getString("theme", null);

        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        return theme;
    }

    public static Calendar getCalendarNow() {
        final String italyId = "Europe/Rome";
        return Calendar.getInstance(TimeZone.getTimeZone(italyId));
    }

    private void loadCurrentRoster() {
        Log.i("MIO", ".............sto caricando i giocatori");
        userDataReference.child("players").addValueEventListener((MyValueEventListener) dataSnapshot -> {
            @SuppressWarnings("unchecked")
            List<String> currentRoster = (List<String>) dataSnapshot.getValue();
            if (currentRoster != null) {
                roster = currentRoster;
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
            Log.e("MIO", "Error loading asset files --> " + e.getMessage(), e);
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


        it.units.fantabasket.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);


        //preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            setTheme(PreferenceManager.getDefaultSharedPreferences(this));
            preferencesChanged = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.top_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }
}