package it.units.fantabasket.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.ActivityMainBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.utils.AssetDecoderUtil;
import it.units.fantabasket.utils.DecoderUtil;
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static it.units.fantabasket.ui.main.HomeFragment.homeFragment;
import static it.units.fantabasket.utils.DecoderUtil.getUserFromHashMapOfDB;

public class MainActivity extends AppCompatActivity {

    public static FirebaseUser firebaseUser;
    public static User user;
    public static DatabaseReference userDataReference;
    public static DatabaseReference usersReference;
    public static DatabaseReference legheReference;
    public static HashMap<String, HashMap<String, Object>> playersStatistics = null;

    public static String legaSelezionata;
    public static AtomicReference<Lega> leagueOn;//legaSelezionata
    public static boolean isUserTheAdminOfLeague;
    public static boolean isLeagueOnCalendarioType;
    public static HashMap<String, User> membersLeagueOn;

    private MyValueEventListener leagueOnListener;
    private HashMap<String, MyValueEventListener> membersLeagueOnListenerList;

    @Override
    protected void onStart() {
        super.onStart();
        setTheme(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //load round info
        AssetDecoderUtil.loadRoundInfos(getApplicationContext());
        AssetDecoderUtil.loadAllPlayersInfos(getApplicationContext());

        //reference at db
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        usersReference = FirebaseDatabase.getInstance().getReference("users");
        userDataReference = usersReference.child(firebaseUser.getUid());
        legheReference = FirebaseDatabase.getInstance().getReference("leghe");

        //legaSelezionata
        userDataReference.addValueEventListener((MyValueEventListener) snapshotLega -> {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> hashMap = (HashMap<String, Object>) snapshotLega.getValue();
            if (hashMap != null) {
                user = getUserFromHashMapOfDB(hashMap);

                if (!Objects.equals(legaSelezionata, user.legaSelezionata)) {
                    if (leagueOnListener != null) {
                        legheReference.child(leagueOn.get().getName()).removeEventListener(leagueOnListener);
                    }

                    legaSelezionata = user.legaSelezionata;
                    if (legaSelezionata != null) {
                        setLeagueOnListener();
                        legheReference.child(legaSelezionata).addValueEventListener(leagueOnListener);
                    }
                }
            }
        });

        //statisticheGiocatori
        //noinspection unchecked
        FirebaseDatabase.getInstance().getReference("playersStatistics").addListenerForSingleValueEvent(
                (MyValueEventListener) snapshot ->
                        playersStatistics = (HashMap<String, HashMap<String, Object>>) snapshot.getValue()
        );

        //nav nad app-bar
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_lineup, R.id.navigation_roster_manager,
                R.id.navigation_leaderboard, R.id.navigation_profile)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        //preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        (sharedPreferences, key) -> {
                            if (key.equals("theme")) {
                                String theme = setTheme(sharedPreferences);
                                Toast.makeText(this, theme + " theme", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void setLeagueOnListener() {
        leagueOnListener = snapshotLeague -> {

            leagueOn = new AtomicReference<>();
            leagueOn.set(DecoderUtil.getLegaFromHashMapOfDB(snapshotLeague.getValue()));
            isUserTheAdminOfLeague = leagueOn.get().getAdmin().equals(firebaseUser.getUid());
            isLeagueOnCalendarioType = leagueOn.get().getTipologia() == LegaType.CALENDARIO;

            removeOldMemberListener();

            membersLeagueOn = new HashMap<>();
            membersLeagueOnListenerList = new HashMap<>();

            for (String memberId : leagueOn.get().getPartecipanti()) {
                addMemberListener(memberId);
            }
        };
    }

    private void addMemberListener(String memberId) {
        MyValueEventListener memberListener = snapshotMember -> {
            membersLeagueOn.put(memberId, getUserFromHashMapOfDB(snapshotMember.getValue()));
            if (homeFragment != null && membersLeagueOn.size() == leagueOn.get().getNumPartecipanti()) {
                homeFragment.onStart();
            }
        };
        usersReference.child(memberId).addValueEventListener(memberListener);
        membersLeagueOnListenerList.put(memberId, memberListener);
    }

    private void removeOldMemberListener() {
        if (membersLeagueOnListenerList != null) {
            for (String memberId : membersLeagueOnListenerList.keySet()) {
                MyValueEventListener oldMemberListener = membersLeagueOnListenerList.get(memberId);
                if (oldMemberListener != null) {
                    usersReference.child(memberId).removeEventListener(oldMemberListener);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    @NotNull
    private String setTheme(SharedPreferences sharedPreferences) {
        String theme = sharedPreferences.getString("theme", null);

        if ("light".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(theme)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        return theme;
    }
}