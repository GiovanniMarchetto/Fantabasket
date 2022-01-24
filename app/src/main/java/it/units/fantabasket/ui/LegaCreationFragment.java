package it.units.fantabasket.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLegaCreationBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static it.units.fantabasket.MainActivity.user;
import static it.units.fantabasket.MainActivity.userDataReference;

public class LegaCreationFragment extends Fragment {

    private FragmentLegaCreationBinding binding;
    private int numPartecipanti = 8;
    private double latitude;
    private double longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLegaCreationBinding.inflate(inflater, container, false);

        binding.incrementNumPartecipantiButton.setOnClickListener(view -> {
            if (numPartecipanti < 20) {
                numPartecipanti++;
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.decrementNumPartecipantiButton.setOnClickListener(view -> {
            if (numPartecipanti > 2) {
                numPartecipanti--;
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.getLocationButton.setOnClickListener(view -> {
            Activity activity = getActivity();
            Context context = getContext();

            if (activity == null || context == null) {
                Log.e("MIO", "Something null: \n---> Activity-" + activity + "\n---> Context-" + context);
                return;
            }

            FusedLocationProviderClient fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(activity);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PackageManager.PERMISSION_GRANTED
                );
                return;
            }

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity,
                    location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.i("MIO",
                                    "Latitude: " + location.getLatitude() +
                                            " - Longitude: " + location.getLongitude());
                            Geocoder gcd = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = gcd.getFromLocation(latitude, longitude, 1);
                            } catch (IOException ignored) {
                            }
                            if (addresses != null && addresses.size() > 0) {
                                final Address address = addresses.get(0);
                                binding.location.setText(address.getLocality() + " (" + address.getCountryName() + ")");
                            } else {
                                binding.location.setError("No address");
                            }
                        } else {
                            binding.location.setError("No location");
                        }
                    });
        });

        TextWatcher enableCreationButton = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.creaLegaButton.setEnabled(
                        !binding.legaName.getText().toString().equals("") &&
                                !binding.location.getText().toString().equals("")
                );
            }
        };

        binding.legaName.addTextChangedListener(enableCreationButton);
        binding.location.addTextChangedListener(enableCreationButton);
        binding.creaLegaButton.setOnClickListener(view -> {
            String legaName = binding.legaName.getText().toString();
            String location = binding.location.getText().toString();
            int numPartecipanti = Integer.parseInt(binding.numPartecipantiTextView.getText().toString());
            LegaType legaType = binding.formula1.isChecked() ? LegaType.FORMULA1 : LegaType.CALENDARIO;
            Lega newLega = new Lega(legaName, location, latitude, longitude, user.getUid(),
                    numPartecipanti, legaType);
            FirebaseDatabase.getInstance().getReference("leghe").child(legaName).setValue(newLega);
            userDataReference.child("legaSelezionata").setValue(legaName);
//            NavHostFragment.findNavController(LegaCreationFragment.this).popBackStack();
            NavHostFragment.findNavController(LegaCreationFragment.this)
                    .navigate(R.id.action_LegaCreationFragment_to_HomeFragment);
        });

        return binding.getRoot();
    }
}