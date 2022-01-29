package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLegaCreationBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static it.units.fantabasket.MainActivity.firebaseUser;
import static it.units.fantabasket.MainActivity.userDataReference;

public class LegaCreationFragment extends Fragment {

    private FragmentLegaCreationBinding binding;
    private int numPartecipanti = 8;
    private double latitude;
    private double longitude;
    private double lastLocationLatitude = 100;//[0,90] -->100 is impossible
    private double lastLocationLongitude = 200;//[-180,180] -->200 is impossible

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
                numPartecipanti = numPartecipanti + ((binding.calendarioRadioButton.isChecked()) ? 2 : 1);
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.decrementNumPartecipantiButton.setOnClickListener(view -> {
            if (numPartecipanti > 3) {
                numPartecipanti = numPartecipanti - ((binding.calendarioRadioButton.isChecked()) ? 2 : 1);
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.typeLegaRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            numPartecipanti = 8;
            binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
        });

        binding.getYourLocationButton.setOnClickListener(view -> {
            final Task<Location> locationTask = Utils.getLastLocation(getContext(), getActivity());
            if (locationTask == null) {
                binding.yourLocationTextView.setError("No location");
                return;
            }

            locationTask.addOnSuccessListener(location -> {
                if (location != null) {

                    lastLocationLatitude = location.getLatitude();
                    lastLocationLongitude = location.getLongitude();

                    Log.i("MIO",
                            "Latitude: " + location.getLatitude() +
                                    " - Longitude: " + location.getLongitude());
                    Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = gcd.getFromLocation(lastLocationLatitude, lastLocationLongitude, 1);
                    } catch (IOException ignored) {
                    }
                    if (addresses != null && addresses.size() > 0) {
                        final Address address = addresses.get(0);
                        binding.yourLocationTextView.setText(address.getLocality() + " (" + address.getCountryName() + ")");
                    } else {
                        binding.yourLocationTextView.setError("No address");
                    }
                } else {
                    binding.yourLocationTextView.setError("No location");
                }
            });
        });


        binding.legaName.addTextChangedListener((TextWatcherAfterChange) editable ->
                binding.creaLegaButton.setEnabled(!editable.toString().equals("")));


        binding.creaLegaButton.setOnClickListener(view -> {
            String legaName = binding.legaName.getText().toString();

            String location;
            if (binding.mainLocationRadioButton.isChecked()) {
                location = binding.locationSpinner.getSelectedItem().toString();
                setLatitudeAndLongitudeFromSpinnerLocationString(location);
            } else if (lastLocationLatitude != 100 && lastLocationLongitude != 200) {
                location = binding.yourLocationTextView.getText().toString();
                latitude = lastLocationLatitude;
                longitude = lastLocationLongitude;
            } else {
                Utils.showToast(getContext(), "No location found", "error");
                return;
            }

            LegaType legaType = binding.formula1RadioButton.isChecked() ? LegaType.FORMULA1 : LegaType.CALENDARIO;
            int numPartecipanti = Integer.parseInt(binding.numPartecipantiTextView.getText().toString());

            Lega newLega = new Lega(legaName, location, latitude, longitude, firebaseUser.getUid(),
                    numPartecipanti, legaType);

            FirebaseDatabase.getInstance().getReference("leghe").child(legaName).setValue(newLega);
            userDataReference.child("legaSelezionata").setValue(legaName);
//            NavHostFragment.findNavController(LegaCreationFragment.this).popBackStack();
            NavHostFragment.findNavController(LegaCreationFragment.this)
                    .navigate(R.id.action_LegaCreationFragment_to_HomeFragment);
        });

        return binding.getRoot();
    }

    private void setLatitudeAndLongitudeFromSpinnerLocationString(String location) {
        if (getString(R.string.roma).equals(location)) {
            latitude = 41.9027008;
            longitude = 12.4962352;
        } else if (getString(R.string.trieste).equals(location)) {
            latitude = 45.6495264;
            longitude = 13.7768182;
        } else if (getString(R.string.bologna).equals(location)) {
            latitude = 44.5005101;
            longitude = 11.3047836;
        } else if (getString(R.string.napoli).equals(location)) {
            latitude = 40.901975;
            longitude = 14.332644;
        } else if (getString(R.string.milano).equals(location)) {
            latitude = 45.458626;
            longitude = 9.181873;
        } else if (getString(R.string.londra).equals(location)) {
            latitude = 51.5072178;
            longitude = -0.1275862;
        } else if (getString(R.string.new_york).equals(location)) {
            latitude = 40.7127753;
            longitude = -74.0059728;
        } else if (getString(R.string.parigi).equals(location)) {
            latitude = 48.856614;
            longitude = 2.3522219;
        }
    }
}