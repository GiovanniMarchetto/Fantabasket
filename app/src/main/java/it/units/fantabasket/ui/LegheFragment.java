package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.*;
import it.units.fantabasket.databinding.FragmentLegheBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.entities.LegaLayout;
import it.units.fantabasket.enums.LegaType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static it.units.fantabasket.MainActivity.userDataReference;

public class LegheFragment extends Fragment {

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentLegheBinding binding = FragmentLegheBinding.inflate(inflater, container, false);

        final ValueEventListener valueEventListener = getValueEventListener(binding.legheAttive, "SELEZIONA");

        userDataReference.child("leghe").addValueEventListener(valueEventListener);

        DatabaseReference legheReference = FirebaseDatabase.getInstance().getReference("leghe");

        legheReference.addValueEventListener(getValueEventListener(binding.legheEsistenti, "UNISCITI"));

        return binding.getRoot();
    }

    @NotNull
    private ValueEventListener getValueEventListener(ViewGroup parent, String action) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                @SuppressWarnings("unchecked")
                Map<String, Object> legheAcquisite = (Map<String, Object>) dataSnapshot.getValue();
                if (legheAcquisite != null && legheAcquisite.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        legheAcquisite.forEach((id, o) -> {
                            //noinspection unchecked
                            createLegaLayoutAndAddToView(parent, id, (HashMap<String, Object>) o, action);
                        });
                    } else {
                        for (Map.Entry<String, Object> entry : legheAcquisite.entrySet()) {
                            //noinspection unchecked
                            createLegaLayoutAndAddToView(parent, entry.getKey(), (HashMap<String, Object>) entry.getValue(), action);
                        }
                    }
                } else {
                    createEmptyLegaLayoutAndAddToView(parent);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "leghe:onCancelled", databaseError.toException());
            }
        };
    }

    @SuppressLint("SetTextI18n")
    private void createEmptyLegaLayoutAndAddToView(ViewGroup parent) {
        TextView emptyLega = new TextView(getContext());
        emptyLega.setText("Nessuna lega");
        emptyLega.setId(View.generateViewId());
        emptyLega.setBackgroundColor(Color.RED);
        emptyLega.setTextColor(Color.WHITE);
        emptyLega.setGravity(Gravity.CENTER);
        parent.addView(emptyLega);
    }

    private void createLegaLayoutAndAddToView(ViewGroup parent, String id, HashMap<String, Object> o, String actionOfButton) {
        long numPartecipantiLong = (long) o.get("numPartecipanti");
        String legaType = ((String) Objects.requireNonNull(o.get("tipo"))).toUpperCase();
        Lega lega = new Lega(id,
                (String) o.get("location"),
                (String) o.get("admin"),
                (int) (numPartecipantiLong),
                LegaType.valueOf(legaType)
        );
        LegaLayout legaLayout = new LegaLayout(getContext(), lega, actionOfButton);
        parent.addView(legaLayout.getLegaHeaderLayout());

    }
}