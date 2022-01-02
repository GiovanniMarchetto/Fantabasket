package it.units.fantabasket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import it.units.fantabasket.databinding.ActivityMainBinding;

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
    }

    private void returnToLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}