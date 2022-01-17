package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.*;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLegheBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.LegaLayout;
import it.units.fantabasket.enums.LegaType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.units.fantabasket.MainActivity.user;
import static it.units.fantabasket.MainActivity.userDataReference;

public class LegheFragment extends Fragment {

    private FragmentLegheBinding binding;
    private DatabaseReference legheReference;
    private List<String> leghePartecipate;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLegheBinding.inflate(inflater, container, false);

        userDataReference.child("leghe").addValueEventListener(getLeghePartecipateValueEventListener());

        legheReference = FirebaseDatabase.getInstance().getReference("leghe");

        legheReference.addValueEventListener(getLegheEsistentiNonPartecipateValueEventListener());

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private ValueEventListener getLeghePartecipateValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                binding.nessunaLegaPartecipata.setVisibility(View.GONE);
                //noinspection unchecked
                leghePartecipate = (List<String>) dataSnapshot.getValue();
                if (leghePartecipate != null && leghePartecipate.size() > 0) {
                    for (String legaName : leghePartecipate) {
                        legheReference.child(legaName).addValueEventListener(
                                new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        @SuppressWarnings("unchecked")
                                        HashMap<String, Object> legaParams = (HashMap<String, Object>) dataSnapshot.getValue();
                                        assert legaParams != null;
                                        Lega lega = getLegaFromNameAndHashMapParams(legaName, legaParams);

                                        Button actionButton = createLegaLayoutAndAddToViewAndReturnActionButton(binding.legheAttive, lega);
                                        actionButton.setText("SELEZIONA");
                                        actionButton.setOnClickListener(view -> {
                                            userDataReference.child("legaSelezionata").setValue(legaName);
                                            NavHostFragment.findNavController(LegheFragment.this)
                                                    .navigate(R.id.action_LegheFragment_to_HomeFragment);
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NotNull DatabaseError databaseError) {
                                        Log.w("ERROR", "leghePartecipateChild:onCancelled", databaseError.toException());
                                    }
                                }
                        );
                    }
                } else {
                    leghePartecipate = new ArrayList<>();
                    binding.nessunaLegaPartecipata.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "leghe:onCancelled", databaseError.toException());
            }
        };
    }

    @SuppressLint("SetTextI18n")
    private ValueEventListener getLegheEsistentiNonPartecipateValueEventListener() {
        ViewGroup parent = binding.legheEsistenti;
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                @SuppressWarnings("unchecked")
                Map<String, Object> legheAcquisite = (Map<String, Object>) dataSnapshot.getValue();
                if (legheAcquisite != null) {
                    for (String legaName : leghePartecipate) {
                        legheAcquisite.remove(legaName);
                    }
                    if (legheAcquisite.size() > 0) {
                        binding.nessunaLegaEsistente.setVisibility(View.GONE);
                        for (Map.Entry<String, Object> entry : legheAcquisite.entrySet()) {
                            String legaName = entry.getKey();
                            //noinspection unchecked
                            HashMap<String, Object> legaParams = (HashMap<String, Object>) entry.getValue();
                            Lega lega = getLegaFromNameAndHashMapParams(legaName, legaParams);

                            Button actionButton = createLegaLayoutAndAddToViewAndReturnActionButton(parent, lega);
                            actionButton.setText("UNISCITI");
                            actionButton.setOnClickListener(view -> {
                                if (lega.getPartecipanti().size() < lega.getNumPartecipanti()) {
                                    if (!lega.isStarted()) {
                                        List<String> newPartecipanti = lega.getPartecipanti();
                                        newPartecipanti.add(user.getUid());
                                        legheReference.child(legaName).child("partecipanti").setValue(newPartecipanti);
                                        leghePartecipate.add(legaName);
                                        userDataReference.child("leghe").setValue(leghePartecipate);

                                    } else {
                                        Log.e("MIO", "È già iniziata");
                                    }
                                } else {
                                    Log.e("MIO", "NON CI SONO POSTI");
                                }
                            });
                        }
                    } else {
                        binding.nessunaLegaEsistente.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "leghe:onCancelled", databaseError.toException());
            }
        };
    }

    private Button createLegaLayoutAndAddToViewAndReturnActionButton(ViewGroup parent, Lega lega) {
        LegaLayout legaLayout = new LegaLayout(getContext(), lega);
        parent.addView(legaLayout.getLegaHeaderLayout());
        return legaLayout.getActionButton();
    }

    private Lega getLegaFromNameAndHashMapParams(String legaName, HashMap<String, Object> legaParams) {
        long numPartecipantiLong = (long) legaParams.get("numPartecipanti");
        String legaType = ((String) Objects.requireNonNull(legaParams.get("tipo"))).toUpperCase();
        return new Lega(legaName,
                (String) legaParams.get("location"),
                (String) legaParams.get("admin"),
                (int) (numPartecipantiLong),
                LegaType.valueOf(legaType)
        );
    }
}