package it.units.fantabasket.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLegaCreationBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import org.jetbrains.annotations.NotNull;

import static it.units.fantabasket.MainActivity.user;
import static it.units.fantabasket.MainActivity.userDataReference;

public class LegaCreationFragment extends Fragment {

    private FragmentLegaCreationBinding binding;
    private int numPartecipanti = 8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
            Lega newLega = new Lega(legaName, location, user.getUid(),
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