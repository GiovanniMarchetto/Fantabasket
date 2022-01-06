package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String user = "Pinco pallino";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TAG", "create main activity");

        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            String currentUser = intent.getStringExtra(user);
            binding.text.setText(currentUser);
        }

        binding.exitButton.setOnClickListener(view -> returnToLogin());

        binding.playmaker.setOnClickListener(view ->
                binding.playmaker.setImageResource(R.drawable.shirt2)
        );

        binding.centro.setOnClickListener(view ->
                showBottomSheet()
        );
    }

    private void showBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        List<String> playerList = new ArrayList<>();
        playerList.add("ciccio");
        playerList.add("caio");
        playerList.add("caio");
        playerList.add("caio");
        playerList.add("sempronio");
        playerList.add("sempronio");
        playerList.add("sempronio");
        playerList.add("sempronio");

        int nCol = 3;
        int nRow = playerList.size() / 3 + 1;

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin=50;
        rowParams.leftMargin=25;
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearParams.gravity = LinearLayout.TEXT_ALIGNMENT_CENTER;

        TableLayout playersLayout = new TableLayout(this);
        playersLayout.setStretchAllColumns(true);
//        playersLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        playersLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < nRow; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableParams);// TableLayout is the parent view

            for (int j = 0; j < nCol; j++) {

                int index = i*3+j;
                if (i*3+j>=playerList.size()){
                    break;
                }

                LinearLayout player = new LinearLayout(this);
                player.setLayoutParams(rowParams);
                player.setOrientation(LinearLayout.VERTICAL);

                Button button = new Button(this);
                int value = i % 2 == 0 ? R.drawable.shirt : R.drawable.shirt2;
                button.setText(index+"");//numero giocatore
                button.setBackground(getDrawable(value));
                final float scale = this.getResources().getDisplayMetrics().density;
                int dp = 58;
                int pixels = (int) (58 * scale + 0.5f);
                linearParams.width=pixels;
                linearParams.height=pixels;
                button.setLayoutParams(linearParams);

                TextView name = new TextView(this);
                name.setText(playerList.get(index));
//                name.setLayoutParams(linearParams);

                player.addView(button);
                player.addView(name);

                tableRow.addView(player);
                Log.i("MIO","fine");
            }
            playersLayout.addView(tableRow);
        }
















        Log.i("MIO","fuori al for");
        bottomSheetDialog.setContentView(playersLayout);
        bottomSheetDialog.show();
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}