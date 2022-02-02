package it.units.fantabasket;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import it.units.fantabasket.databinding.ActivityNoConnectionBinding;
import it.units.fantabasket.utils.NetworkChangeReceiver;

public class NoConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNoConnectionBinding binding = ActivityNoConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button2.setOnClickListener(view -> {
            if (NetworkChangeReceiver.isNetworkAvailable(getApplicationContext())) {
                onBackPressed();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (NetworkChangeReceiver.isNetworkAvailable(getApplicationContext())) {
            super.onBackPressed();
            finish();
        }
    }
}