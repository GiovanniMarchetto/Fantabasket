package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.databinding.ActivityMainBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.PlayerLayout;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String user = "Pinco pallino";
    private static List<Player> playerList;

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

        binding.playmaker.setOnClickListener(view -> showBottomSheet(binding.playmaker));
        binding.guardiaDx.setOnClickListener(view -> showBottomSheet(binding.guardiaDx));
        binding.guardiaSx.setOnClickListener(view -> showBottomSheet(binding.guardiaSx));
        binding.ala.setOnClickListener(view -> showBottomSheet(binding.ala));
        binding.centro.setOnClickListener(view -> showBottomSheet(binding.centro));
    }

    private void setPlayerList() {
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

    private void showBottomSheet(ImageButton imageButton) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        int nCol = 3;
        int nRow = playerList.size() / 3 + 1;

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = 50;
        rowParams.leftMargin = 25;

        TableLayout playersTableLayout = new TableLayout(this);
        playersTableLayout.setStretchAllColumns(true);

        for (int i = 0; i < nRow; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableParams);// TableLayout is the parent view

            for (int j = 0; j < nCol; j++) {

                int index = i * 3 + j;
                if (i * 3 + j >= playerList.size()) {
                    break;
                }

                PlayerLayout playerLayout = new PlayerLayout(this, playerList.get(index));
                playerLayout.setLayoutParams(rowParams);
                playerLayout.setOnClickListener(view -> {
                    occupyPositionField(imageButton, playerList.get(index));
                    bottomSheetDialog.dismiss();
                });

                tableRow.addView(playerLayout.getPlayerLayout());
                Log.i("MIO", "fine");
            }
            playersTableLayout.addView(tableRow);
        }


        Log.i("MIO", "fuori al for");
        bottomSheetDialog.setContentView(playersTableLayout);
        bottomSheetDialog.show();
    }

    private void occupyPositionField(ImageButton imageButton, Player player) {
        imageButton.setImageResource(player.getShirt());
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}