package it.units.fantabasket;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.databinding.ActivityMainBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.PlayerLayout;
import it.units.fantabasket.entities.PlayerLayoutHorizontal;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public static String user = "Pinco pallino";
    private static List<Player> playerList;
    private static HashMap<Role, Player> selectedPlayerList;
    private static Role selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "create main activity");

        setPlayerList();

        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            String currentUser = intent.getStringExtra(user);
            binding.text.setText(currentUser);
        }

        binding.exitButton.setOnClickListener(view -> returnToLogin());

        binding.playmakerButton.setOnClickListener(view -> {
            selectedRole = Role.PLAY;
            showBottomSheet(binding.playmakerButton, binding.playmakerText);
        });
        binding.guardiaDxButton.setOnClickListener(view -> {
            selectedRole = Role.GUARDIA_DX;
            showBottomSheet(binding.guardiaDxButton, binding.guardiaDxText);
        });
        binding.guardiaSxButton.setOnClickListener(view -> {
            selectedRole = Role.GUARDIA_SX;
            showBottomSheet(binding.guardiaSxButton, binding.guardiaSxText);
        });
        binding.alaButton.setOnClickListener(view -> {
            selectedRole = Role.ALA;
            showBottomSheet(binding.alaButton, binding.alaText);
        });
        binding.centroButton.setOnClickListener(view -> {
            selectedRole = Role.CENTRO;
            showBottomSheet(binding.centroButton, binding.centroText);
        });
    }

    private void setPlayerList() {
        selectedPlayerList = new HashMap<>();
        selectedPlayerList.put(Role.PLAY, null);
        selectedPlayerList.put(Role.GUARDIA_DX, null);
        selectedPlayerList.put(Role.GUARDIA_SX, null);
        selectedPlayerList.put(Role.ALA, null);
        selectedPlayerList.put(Role.CENTRO, null);

        playerList = new ArrayList<>();

        playerList.add(new Player("Marchetto", 14, Role.GUARDIA, Team.ATHLETISMO));
        playerList.add(new Player("Junior", 4, Role.PLAY, Team.ATHLETISMO));
        playerList.add(new Player("Mago", 19, Role.PLAY, Team.ATHLETISMO));

        playerList.add(new Player("Rosso", 6, Role.GUARDIA, Team.GORIZIANA));
        playerList.add(new Player("Minu", 4, Role.CENTRO, Team.GORIZIANA));

        playerList.add(new Player("Zeus", 1, Role.ALA, Team.OLIMPIA));
        playerList.add(new Player("Ade", 99, Role.GUARDIA, Team.OLIMPIA));
        playerList.add(new Player("Poseidone", 8, Role.GUARDIA, Team.OLIMPIA));

        playerList.add(new Player("Vecchiet", 19, Role.CENTRO, Team.ROMANS));
    }

    private void showBottomSheet(Button playerButton, TextView playerName) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        LinearLayout playersLayout = new LinearLayout(this);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        playersLayout.setLayoutParams(params);
        playersLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playerList.stream()
                    .filter(player -> !selectedPlayerList.containsValue(player))
                    .forEach(
                            player -> {
                                PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(this, player);
                                playerLayout.setOnClickListener(view -> {
                                    occupyPositionField(playerButton, playerName, player);
                                    bottomSheetDialog.dismiss();
                                });
                                playerLayout.setLayoutParams(params);
                                playersLayout.addView(playerLayout.getPlayerLayout());
                            }
                    );
        }

        bottomSheetDialog.setContentView(playersLayout);
        bottomSheetDialog.show();
    }

    private void occupyPositionField(Button playerButton, TextView playerName, Player player) {
        playerButton.setText(player.getNumber());
        playerButton.setBackground(this.getDrawable(player.getShirt()));
        playerName.setText(player.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedPlayerList.replace(selectedRole, player);
        }
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}