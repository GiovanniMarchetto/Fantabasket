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
import it.units.fantabasket.enums.Role;
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

    private static List<Player> rosterOfPlayers;
    private static HashMap<FieldPositions, Player> formazione;
    private static HashMap<FieldPositions, PlayerOnFieldLayout> playerOnFieldLayoutHashMap;
    private static FieldPositions selectedRole;
    private final int formazioneSize = 12;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        setPlayerButtons();

        setRoster();

        binding.changeRosterButton.setOnClickListener(view ->
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.action_DashboardFragment_to_PlayerListFragment)
        );


        binding.salvaFormazioneButton.setOnClickListener(view -> {
            if (Utils.getCalendarNow().before(orarioInizioPrimaPartitaDellaGiornataCorrente)) {
                if (!formazione.containsValue(null)) {
                    HashMap<String, String> formazioneDBFormat = new HashMap<>(formazioneSize);
                    for (FieldPositions key : formazione.keySet()) {
                        formazioneDBFormat.put(key.name(), formazione.get(key).getId());
                    }
                    userDataReference.child("formazioniPerGiornata").child(String.valueOf(giornataCorrente)).setValue(formazioneDBFormat);
                    Utils.showToast(getContext(), "Salvata!", "good");
                } else {
                    Utils.showToast(getContext(), "Formazione non completa", "error");
                    Log.i("MIO", "Formazione non completa");
                }
            } else {
                Utils.showToast(getContext(), "Tempo scaduto", "error");
                Log.i("MIO", "Tempo scaduto");
            }
        });

        return binding.getRoot();
    }

    private void setPlayerButtons() {
        playerOnFieldLayoutHashMap = new HashMap<>(values().length);
        for (FieldPositions fieldPosition : FieldPositions.values()) {
            PlayerOnFieldLayout playerOnFieldLayout = new PlayerOnFieldLayout(getContext(), new Player());
            playerOnFieldLayout.getPlayerButton().setOnClickListener(view -> {
                        selectedRole = fieldPosition;
                        showBottomSheet(playerOnFieldLayout);
                    }
            );
            addToCorrectView(fieldPosition, playerOnFieldLayout.getPlayerLayout());
            playerOnFieldLayoutHashMap.put(fieldPosition, playerOnFieldLayout);
        }
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

    private void setRoster() {
        formazione = new HashMap<>(formazioneSize);
        rosterOfPlayers = new ArrayList<>();

        List<Player> completePlayersList = new ArrayList<>();
        Utils.setCompletePlayerList(getActivity(), completePlayersList);

        if (user.formazioniPerGiornata != null && user.formazioniPerGiornata.size() > giornataCorrente && user.formazioniPerGiornata.get(giornataCorrente) != null) {
            HashMap<FieldPositions, String> formazioneSalvata = user.formazioniPerGiornata.get(giornataCorrente);
            for (FieldPositions position : formazioneSalvata.keySet()) {
                for (Player player : completePlayersList) {
                    if (formazioneSalvata.get(position).equals(player.getId())) {
                        formazione.put(position, player);
                        occupyPositionField(playerOnFieldLayoutHashMap.get(position), player);
                    }
                }
            }
        } else {
            for (FieldPositions position : FieldPositions.values()) {
                formazione.put(position, null);
            }
        }

        //TODO: valutare se rendere la complete list una variabile globale

        for (Player player : completePlayersList) {
            if (roster.contains(player.getId())) {
                rosterOfPlayers.add(player);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void showBottomSheet(PlayerOnFieldLayout playerOnFieldLayout) {
        Context context = getContext();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout playersLayout = new LinearLayout(context);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        playersLayout.setLayoutParams(params);
        playersLayout.setOrientation(LinearLayout.VERTICAL);

        for (Player player : rosterOfPlayers) {
            if (!formazione.containsValue(player) && isPlayerAdaptForThisPosition(player)) {
                PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(context, player);
                playerLayout.setOnClickListener(view -> {
                    occupyPositionField(playerOnFieldLayout, player);
                    formazione.put(selectedRole, player);
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
            occupyPositionField(playerOnFieldLayout, emptyPlayer);
            formazione.put(selectedRole, null);
            bottomSheetDialog.dismiss();
        });
        emptyPlayerLayout.setLayoutParams(params);
        playersLayout.addView(emptyPlayerLayout.getPlayerLayout());

        scrollView.addView(playersLayout);
        bottomSheetDialog.setContentView(scrollView);
        bottomSheetDialog.show();
    }

    private boolean isPlayerAdaptForThisPosition(Player player) {
        if (onFieldPositions.contains(selectedRole)) {
            switch (selectedRole) {
                case PLAYMAKER:
                    return player.getRole_1() == Role.PLAYMAKER || player.getRole_2() == Role.PLAYMAKER;
                case GUARDIA_DX:
                case GUARDIA_SX:
                    return player.getRole_1() == Role.GUARDIA || player.getRole_2() == Role.GUARDIA;
                case ALA:
                    return player.getRole_1() == Role.ALA || player.getRole_2() == Role.ALA;
                case CENTRO:
                    return player.getRole_1() == Role.CENTRO || player.getRole_2() == Role.CENTRO;
            }
        }
        return true;
    }

    private void occupyPositionField(PlayerOnFieldLayout playerOnFieldLayout, Player player) {
        Button playerButton = playerOnFieldLayout.getPlayerButton();
        TextView playerName = playerOnFieldLayout.getPlayerTextView();
        playerButton.setText(player.getNumber());
        playerButton.setTextColor(player.getNumberColor());
        playerButton.setBackground(getContext().getDrawable(player.getShirt()));
        playerName.setText(player.getId());
    }
}