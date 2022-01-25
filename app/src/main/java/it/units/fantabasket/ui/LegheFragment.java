package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.ValueEventListener;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLegheBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.layouts.LegaLayout;
import it.units.fantabasket.utils.MyValueEventListener;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.units.fantabasket.MainActivity.*;

public class LegheFragment extends Fragment {

    private FragmentLegheBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLegheBinding.inflate(inflater, container, false);

        legheReference.addValueEventListener(getLegheValueEventListener());

        binding.creaLegaButton.setOnClickListener(view ->
                NavHostFragment.findNavController(LegheFragment.this)
                        .navigate(R.id.action_LegheFragment_to_LegaCreationFragment)
        );

        return binding.getRoot();
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SetTextI18n")
    private ValueEventListener getLegheValueEventListener() {
        return (MyValueEventListener) dataSnapshot -> {

            Map<String, Object> legheEsistenti = (Map<String, Object>) dataSnapshot.getValue();
            int numLeghePartecipate = 0;
            int numLegheDisponibili = 0;

            if (legheEsistenti != null && legheEsistenti.size() > 0) {
                for (Map.Entry<String, Object> valueOfMap : legheEsistenti.entrySet()) {
                    String legaName = valueOfMap.getKey();
                    HashMap<String, Object> legaParams = (HashMap<String, Object>) valueOfMap.getValue();
                    Lega lega = Utils.getLegaFromHashMapOfDB(legaParams);
                    Log.i("MIO", legaName + " --> " + lega.getPartecipanti().toString());

                    if (lega.getPartecipanti().contains(firebaseUser.getUid())) {
                        numLeghePartecipate++;
                        Button actionButton = createLegaLayoutAndAddToViewAndReturnActionButton(binding.leghePartecipate, lega);
                        actionButton.setText("SELEZIONA");
                        actionButton.setOnClickListener(view -> setLegaSelezionataAndReturnToHome(legaName));
                    } else {
                        //TODO: ordinale in base alla vicinanza
                        numLegheDisponibili++;
                        Button actionButton = createLegaLayoutAndAddToViewAndReturnActionButton(binding.legheDisponibili, lega);
                        actionButton.setText("UNISCITI");
                        actionButton.setOnClickListener(view -> {
                            if (lega.getPartecipanti().size() < lega.getNumPartecipanti()) {
                                if (!lega.isStarted()) {
                                    List<String> newPartecipanti = lega.getPartecipanti();
                                    newPartecipanti.add(firebaseUser.getUid());
                                    legheReference.child(legaName).child("partecipanti").setValue(newPartecipanti);
                                    setLegaSelezionataAndReturnToHome(legaName);
                                } else {
                                    Log.e("MIO", "È già iniziata");
                                }
                            } else {
                                Log.e("MIO", "NON CI SONO POSTI");
                            }
                        });
                    }

                }
            }

            binding.nessunaLegaPartecipata.setVisibility((numLeghePartecipate > 0) ? View.GONE : View.VISIBLE);
            binding.leghePartecipate.setBackgroundColor((numLeghePartecipate > 0) ? Color.WHITE : Color.RED);
            binding.nessunaLegaDisponibile.setVisibility((numLegheDisponibili > 0) ? View.GONE : View.VISIBLE);
            binding.legheDisponibili.setBackgroundColor((numLegheDisponibili > 0) ? Color.WHITE : Color.RED);
        };
    }

    private Button createLegaLayoutAndAddToViewAndReturnActionButton(ViewGroup parent, Lega lega) {
        LegaLayout legaLayout = new LegaLayout(getContext(), lega);
        parent.addView(legaLayout.getLegaHeaderLayout());
        return legaLayout.getActionButton();
    }

    private void setLegaSelezionataAndReturnToHome(String legaName) {
        userDataReference.child("legaSelezionata").setValue(legaName);
//        NavHostFragment.findNavController(LegheFragment.this).popBackStack();//generate an error (quando elimina diventa contesto nullo e impazzisce...)
        NavHostFragment.findNavController(LegheFragment.this)
                .navigate(R.id.action_LegheFragment_to_HomeFragment);
    }
}