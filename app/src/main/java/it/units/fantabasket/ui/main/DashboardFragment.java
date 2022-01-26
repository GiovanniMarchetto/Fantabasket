package it.units.fantabasket.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentDashboardBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import it.units.fantabasket.layouts.PlayerOnFieldLayout;
import it.units.fantabasket.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.*;
import static it.units.fantabasket.enums.FieldPositions.*;

@SuppressWarnings("ConstantConditions")
public class DashboardFragment extends Fragment {

    private static List<Player> playerList;
    private static HashMap<FieldPositions, Player> formazione;
    private static FieldPositions selectedRole;
    private final int formazioneSize = 12;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        for (FieldPositions fieldPosition : FieldPositions.values()) {
            PlayerOnFieldLayout playerOnFieldLayout = new PlayerOnFieldLayout(getContext(), new Player());
            playerOnFieldLayout.getPlayerButton().setOnClickListener(view -> {
                        selectedRole = fieldPosition;
                        showBottomSheet(playerOnFieldLayout.getPlayerButton(), playerOnFieldLayout.getPlayerTextView());
                    }
            );
            addToCorrectView(fieldPosition, playerOnFieldLayout.getPlayerLayout());
        }

        setPlayerList();

        binding.changeRosterButton.setOnClickListener(view ->
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.action_DashboardFragment_to_PlayerListFragment)
        );


        binding.salvaFormazioneButton.setOnClickListener(view -> {
            if (Utils.getCalendarNow().before(orarioInizioPrimaPartitaDellaGiornataCorrente)) {
                if (!formazione.containsValue(null)) {
                    HashMap<String, String> formazione = new HashMap<>(formazioneSize);
                    for (FieldPositions key : DashboardFragment.formazione.keySet()) {
                        Log.i("MIO", "chiave : " + key);
                        formazione.put(key.name(), DashboardFragment.formazione.get(key).getId());
                    }
                    userDataReference.child("formazionePerGiornata").child(String.valueOf(giornataCorrente)).setValue(formazione);
                } else {
                    Log.i("MIO", "Formazione non completa");
                }
            } else {
                Log.i("MIO", "Tempo scaduto");
            }
        });

        return root;
    }

    private void addToCorrectView(FieldPositions fieldPosition, LinearLayout playerLayout) {
        if (onFieldPositions.contains(fieldPosition)) {
            switch (fieldPosition) {
                case PLAYMAKER:
                    binding.playmaker.addView(playerLayout);
                    break;
                case CENTRO:
                    binding.centro.addView(playerLayout);
                    break;
                case GUARDIA_SX:
                    binding.guardiaSx.addView(playerLayout);
                    break;
                case GUARDIA_DX:
                    binding.guardiaDx.addView(playerLayout);
                    break;
                case ALA:
                    binding.ala.addView(playerLayout);
            }
        } else if (primaPanchina.contains(fieldPosition)) {
            binding.panchinaFirstSection.addView(playerLayout);
        } else if (secondaPanchina.contains(fieldPosition)) {
            binding.panchinaSecondSection.addView(playerLayout);
        } else {
            binding.panchinaThirdSection.addView(playerLayout);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setPlayerList() {
        formazione = new HashMap<>(formazioneSize);
        for (FieldPositions position : FieldPositions.values()) {
            formazione.put(position, null);
        }

        playerList = new ArrayList<>();

        List<Player> completePlayersList = new ArrayList<>();
        Utils.setCompletePlayerList(getActivity(), completePlayersList);
        //TODO: valutare se rendere la complete list una variabile globale

        for (Player player : completePlayersList) {
            if (roster.contains(player.getId())) {
                playerList.add(player);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showBottomSheet(Button playerButton, TextView playerName) {
        Context context = getContext();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout playersLayout = new LinearLayout(context);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        playersLayout.setLayoutParams(params);
        playersLayout.setOrientation(LinearLayout.VERTICAL);

        for (Player player : playerList) {
            if (!formazione.containsValue(player)) {
                PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(context, player);
                playerLayout.setOnClickListener(view -> {
                    occupyPositionField(playerButton, playerName, player);
                    bottomSheetDialog.dismiss();
                });
                playerLayout.setLayoutParams(params);
                playersLayout.addView(playerLayout.getPlayerLayout());
            }
        }

        //add remove button
        Player emptyPlayer = new Player();
        PlayerLayoutHorizontal emptyPlayerLayout = new PlayerLayoutHorizontal(context, emptyPlayer);
        final LinearLayout subLayout = (LinearLayout) emptyPlayerLayout.getPlayerLayout().getChildAt(2);
        TextView textView = (TextView) subLayout.getChildAt(0);
        textView.setText("Libera posizione");
        emptyPlayerLayout.setOnClickListener(view -> {
            occupyPositionField(playerButton, playerName, emptyPlayer);
            formazione.put(selectedRole, null);
            bottomSheetDialog.dismiss();
        });
        emptyPlayerLayout.setLayoutParams(params);
        playersLayout.addView(emptyPlayerLayout.getPlayerLayout());

        scrollView.addView(playersLayout);
        bottomSheetDialog.setContentView(scrollView);
        bottomSheetDialog.show();
    }

    private void occupyPositionField(Button playerButton, TextView playerName, Player player) {
        playerButton.setText(player.getNumber());
        playerButton.setTextColor(player.getNumberColor());
        playerButton.setBackground(getContext().getDrawable(player.getShirt()));
        playerName.setText(player.getId());

        formazione.put(selectedRole, player);
    }
}