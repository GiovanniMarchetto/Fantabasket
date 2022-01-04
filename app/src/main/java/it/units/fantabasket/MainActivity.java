package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
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

        List<String> players = new ArrayList<>();
        players.add("ciccio");
        players.add("caio");
        players.add("caio");
        players.add("caio");
        players.add("sempronio");
        players.add("sempronio");
        players.add("sempronio");
        players.add("sempronio");

        int nCol = 3;
        int nRow = players.size() / 3 + 1;

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setLayoutParams(new GridLayout.LayoutParams());
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setColumnCount(nCol);
        gridLayout.setRowCount(nRow);
        Log.i("MIO","prima del for");
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                int index = i*3+j;
                if (i*3+j>=players.size()){
                    break;
                }
                Log.i("MIO","dentro al for "+index);
                LinearLayout player = new LinearLayout(this);
                player.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                player.setOrientation(LinearLayout.VERTICAL);
                player.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                Button button = new Button(this);
                int value = i % 2 == 0 ? R.drawable.shirt : R.drawable.shirt2;
                button.setText(index+"");//numero giocatore
                button.setBackground(getDrawable(value));

                TextView name = new TextView(this);
                name.setText(players.get(index));

                player.addView(button);
                player.addView(name);

                GridLayout.LayoutParams param = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, 1f)
                );
//                param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//                param.width = 0;
//            param.rightMargin = 5;
//            param.topMargin = 5;
//                param.setGravity(Gravity.NO_GRAVITY);
//                param.rowSpec = GridLayout.spec(i);
//                param.columnSpec = GridLayout.spec(j);
                player.setLayoutParams(param);

                gridLayout.addView(player);
                Log.i("MIO","fine");
            }
        }
        Log.i("MIO","fuori al for");
        bottomSheetDialog.setContentView(gridLayout);
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