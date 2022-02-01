package it.units.fantabasket.ui.leagues;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.tasks.Task;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLeagueCreationBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static it.units.fantabasket.LeaguesActivity.*;

public class LeagueCreationFragment extends Fragment {

    private FragmentLeagueCreationBinding binding;

    private int numPartecipanti = 8;
    private double latitude;
    private double longitude;
    private double lastLocationLatitude = 100;//[0,90] -->100 is impossible
    private double lastLocationLongitude = 200;//[-180,180] -->200 is impossible

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLeagueCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        manageNumberOfMember();

        binding.getYourLocationButton.setOnClickListener(viewListener -> retriveLastLocationAndSetParameters());

        binding.legaName.addTextChangedListener((TextWatcherAfterChange) editable ->
                binding.creaLegaButton.setEnabled(!editable.toString().equals("")));


        binding.creaLegaButton.setOnClickListener(viewListener -> {
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
                Utils.showSnackbar(binding.leagueCreationFragmentView, "No location found", "error");
                return;
            }

            LegaType legaType = binding.formula1RadioButton.isChecked() ? LegaType.FORMULA1 : LegaType.CALENDARIO;
            int numPartecipanti = Integer.parseInt(binding.numPartecipantiTextView.getText().toString());

            Lega newLega = new Lega(legaName, location, latitude, longitude, firebaseUserLeagues.getUid(),
                    numPartecipanti, legaType);

            legheReferenceInLeagues.child(legaName).setValue(newLega);
            userDataReferenceInLeagues.child("legaSelezionata").setValue(legaName);

            NavHostFragment.findNavController(LeagueCreationFragment.this)
                    .navigate(R.id.action_LeagueCreationFragment_to_LeagueChoiceFragment);
        });
    }

    private void retriveLastLocationAndSetParameters() {
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
                    String cityAndCountry = "(" + address.getCountryCode() + ") " + address.getLocality();
                    binding.yourLocationTextView.setText(cityAndCountry);
                    binding.yourLocationTextView.setError(null);
                } else {
                    binding.yourLocationTextView.setError("No address");
                }
            } else {
                binding.yourLocationTextView.setError("No location");
            }
        });
    }

    private void manageNumberOfMember() {
        binding.incrementNumPartecipantiButton.setOnClickListener(viewListener -> {
            if (numPartecipanti < 20) {
                numPartecipanti = numPartecipanti + ((binding.calendarioRadioButton.isChecked()) ? 2 : 1);
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.decrementNumPartecipantiButton.setOnClickListener(viewListener -> {
            if (numPartecipanti > 3) {
                numPartecipanti = numPartecipanti - ((binding.calendarioRadioButton.isChecked()) ? 2 : 1);
                binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
            }
        });

        binding.typeLegaRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            numPartecipanti = 8;
            binding.numPartecipantiTextView.setText(String.valueOf(numPartecipanti));
        });
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