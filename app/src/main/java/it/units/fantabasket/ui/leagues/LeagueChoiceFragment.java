package it.units.fantabasket.ui.leagues;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLeagueChoiceBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.layouts.LegaLayout;
import it.units.fantabasket.utils.DecoderUtil;
import it.units.fantabasket.utils.MyValueEventListener;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static it.units.fantabasket.ui.MainActivity.firebaseUser;
import static it.units.fantabasket.ui.MainActivity.legheReference;

public class LeagueChoiceFragment extends Fragment {

    public static Map<String, Object> legheEsistenti;

    private FragmentLeagueChoiceBinding binding;
    private Context context;
    private double lastLocationLatitude;
    private double lastLocationLongitude;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLeagueChoiceBinding.inflate(inflater, container, false);
        context = getContext();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.refreshButton.setOnClickListener(view1 ->
                this.getParentFragmentManager().beginTransaction()
                        .replace(this.getId(), new LeagueChoiceFragment()).commit());

        legheReference.addListenerForSingleValueEvent(getLegheValueEventListener());

        binding.createLeagueButton.setOnClickListener(viewListener ->
                NavHostFragment.findNavController(LeagueChoiceFragment.this)
                        .navigate(R.id.action_LeagueChoiceFragment_to_LeagueCreationFragment)
        );
    }

    @SuppressWarnings("unchecked")
    private ValueEventListener getLegheValueEventListener() {
        return (MyValueEventListener) dataSnapshot -> {

            legheEsistenti = (Map<String, Object>) dataSnapshot.getValue();
            AtomicInteger numLeghePartecipate = new AtomicInteger();
            AtomicInteger numLegheDisponibili = new AtomicInteger();

            if (legheEsistenti != null && legheEsistenti.size() > 0) {
                binding.leghePartecipate.removeAllViews();
                binding.legheDisponibili.removeAllViews();

                HashMap<Lega, LegaLayout> legaLinearLayoutHashMap = new HashMap<>();

                for (Map.Entry<String, Object> valueOfMap : legheEsistenti.entrySet()) {
                    String legaName = valueOfMap.getKey();
                    HashMap<String, Object> legaParams = (HashMap<String, Object>) valueOfMap.getValue();
                    Lega lega = DecoderUtil.getLegaFromHashMapOfDB(legaParams);
                    LegaLayout legaLayout = new LegaLayout(context, lega);
                    int idActionDescription;

                    final boolean isUserInThisLeague = lega.getPartecipanti().contains(firebaseUser.getUid());

                    if (isUserInThisLeague) {
                        numLeghePartecipate.getAndIncrement();
                        idActionDescription = R.string.seleziona_lega;
                        binding.leghePartecipate.addView(legaLayout.getLegaHeaderLayout());
                    } else {
                        numLegheDisponibili.getAndIncrement();
                        idActionDescription = R.string.unisciti_lega;
                        binding.legheDisponibili.addView(legaLayout.getLegaHeaderLayout());
                        legaLinearLayoutHashMap.put(lega, legaLayout);
                    }

                    Button actionButton = legaLayout.getActionButton();

                    actionButton.setText(getResources().getString(idActionDescription));
                    if (isUserInThisLeague) {
                        actionButton.setOnClickListener(view -> setLegaSelezionataAndReturnToHome(legaName));
                    } else {
                        actionButton.setOnClickListener(view -> joinTheLeagueIfPossible(view, legaName, lega));
                    }
                }

                final Task<Location> locationTask = Utils.getLastLocation(context, getActivity());
                if (locationTask != null) {
                    locationTask.addOnSuccessListener(location -> {
                        if (location != null) {
                            lastLocationLatitude = location.getLatitude();
                            lastLocationLongitude = location.getLongitude();
                            reorderOpenLeaguesByDistance(legaLinearLayoutHashMap);
                        }
                    });
                }
            }

            binding.nessunaLegaPartecipata.setVisibility((numLeghePartecipate.get() > 0) ? View.GONE : View.VISIBLE);
            binding.nessunaLegaDisponibile.setVisibility((numLegheDisponibili.get() > 0) ? View.GONE : View.VISIBLE);
        };
    }

    private void reorderOpenLeaguesByDistance(HashMap<Lega, LegaLayout> legaLinearLayoutHashMap) {
        HashMap<Integer, List<LinearLayout>> legaLayoutDistancesHashMap = new HashMap<>();
        List<Integer> distances = new ArrayList<>();

        for (Lega lega : legaLinearLayoutHashMap.keySet()) {
            calculateDistances(legaLayoutDistancesHashMap, distances, lega, legaLinearLayoutHashMap.get(lega));
        }

        binding.legheDisponibili.removeAllViews();
        Collections.sort(distances);
        for (Integer distance : distances) {
            List<LinearLayout> thisDistanceLeghe = legaLayoutDistancesHashMap.get(distance);
            assert thisDistanceLeghe != null;
            for (LinearLayout legaLayout : thisDistanceLeghe) {
                binding.legheDisponibili.addView(legaLayout);
            }
        }
    }

    private void calculateDistances(
            HashMap<Integer, List<LinearLayout>> legaLayoutDistancesHashMap,
            List<Integer> distances, Lega lega, LegaLayout legaLayout) {

        int distanceFromUser = getLegaDistance(lega.getLatitude(), lega.getLongitude());
        if (!distances.contains(distanceFromUser)) {
            distances.add(distanceFromUser);
        }

        List<LinearLayout> updateList = legaLayoutDistancesHashMap.get(distanceFromUser);
        if (updateList == null) {
            updateList = new ArrayList<>();
        }
        updateList.add(legaLayout.getLegaHeaderLayout());
        legaLayoutDistancesHashMap.put(distanceFromUser, updateList);
    }

    private Integer getLegaDistance(double latitude, double longitude) {// Haversine formula
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(latitude - lastLocationLatitude);  // deg2rad below
        double dLon = deg2rad(longitude - lastLocationLongitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(deg2rad(lastLocationLatitude)) * Math.cos(deg2rad(latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (R * c);// Distance in km
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

    private void joinTheLeagueIfPossible(View view, String legaName, Lega lega) {
        if (lega.getPartecipanti().size() < lega.getNumPartecipanti()) {
            if (!lega.isStarted()) {
                List<String> newPartecipanti = lega.getPartecipanti();
                newPartecipanti.add(firebaseUser.getUid());
                legheReference.child(legaName).child("partecipanti").setValue(newPartecipanti);
                binding.refreshButton.performClick();
            } else {
                Utils.showSnackbar(view, getString(R.string.started_yet), Utils.ERROR);
            }
        } else {
            Utils.showSnackbar(view, getString(R.string.no_more_space), Utils.ERROR);
        }
    }

    private void setLegaSelezionataAndReturnToHome(String legaName) {
        DialogFragment selectLeagueDialogFragment = LeaguesActivity.SelectLeagueDialogFragment.newInstance(legaName);
        selectLeagueDialogFragment.show(getChildFragmentManager(), "Selected league");
    }
}