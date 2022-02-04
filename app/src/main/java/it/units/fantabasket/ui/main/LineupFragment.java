package it.units.fantabasket.ui.main;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLineupBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import it.units.fantabasket.layouts.PlayerOnFieldLayout;
import it.units.fantabasket.utils.AssetDecoderUtil;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.user;
import static it.units.fantabasket.ui.MainActivity.userDataReference;
import static it.units.fantabasket.utils.AssetDecoderUtil.completePlayersList;
import static it.units.fantabasket.utils.Utils.*;

@SuppressWarnings("ConstantConditions")
public class LineupFragment extends Fragment {

    private static List<Player> rosterOfPlayers;
    private static HashMap<FieldPositions, Player> lineup;
    private static HashMap<FieldPositions, PlayerOnFieldLayout> playerOnFieldLayoutHashMap;
    private static FieldPositions selectedRole;
    private final int lineupSize = 12;
    private FragmentLineupBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLineupBinding.inflate(inflater, container, false);

        setTimeRestForChangeLineup();

        createLineupUI();

        setRosterOfPlayers();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.changeRosterButton.setOnClickListener(viewListener ->
                NavHostFragment.findNavController(LineupFragment.this)
                        .navigate(R.id.action_LineupFragment_to_RosterManagerFragment)
        );


        binding.salvaFormazioneButton.setOnClickListener(viewListener -> {
            if (Utils.getCalendarNow().before(AssetDecoderUtil.calendarOfCurrentRoundStart)) {
                if (!lineup.containsValue(null)) {
                    HashMap<String, String> formazioneDBFormat = new HashMap<>(lineupSize);
                    for (FieldPositions key : lineup.keySet()) {
                        formazioneDBFormat.put(key.name(), lineup.get(key).getId());
                    }
                    userDataReference.child("formazioniPerGiornata").child(String.valueOf(AssetDecoderUtil.currentRound)).setValue(formazioneDBFormat);
                    Utils.showSnackbar(view, "Salvata!", GOOD);
                } else {
                    Utils.showSnackbar(view, "Formazione non completa", ERROR);
                    Log.i(MIO_TAG, "Formazione non completa");
                }
            } else {
                Utils.showSnackbar(view, "Tempo scaduto", ERROR);
                Log.i(MIO_TAG, "Tempo scaduto");
            }
        });

        binding.resetButton.setOnClickListener(viewListener -> {
            for (FieldPositions position : FieldPositions.values()) {
                lineup.put(position, null);
                occupyPositionField(playerOnFieldLayoutHashMap.get(position), new Player());
            }
        });
    }

    private void setTimeRestForChangeLineup() {
        String timeRestString = "time is up!";
        long endTime = AssetDecoderUtil.calendarOfCurrentRoundStart.getTimeInMillis();
        long currentTime = Utils.getCalendarNow().getTimeInMillis();
        int restTimeInSeconds = (int) Math.floor((endTime - currentTime) / 1000.0);

        if (restTimeInSeconds > 0) {
            double secondsInAMMinute = 60;
            double secondsInAHour = 60 * secondsInAMMinute;
            double secondsInADay = 24 * secondsInAHour;

            int days = (int) Math.floor(restTimeInSeconds / secondsInADay);
            restTimeInSeconds = (int) (restTimeInSeconds - days * secondsInADay);

            int hours = (int) Math.floor(restTimeInSeconds / secondsInAHour);
            restTimeInSeconds = (int) (restTimeInSeconds - hours * secondsInAHour);

            int minutes = (int) Math.floor(restTimeInSeconds / secondsInAMMinute);
            restTimeInSeconds = (int) (restTimeInSeconds - minutes * secondsInAMMinute);

            timeRestString = days + "d " + hours + "h " + minutes + "m " + restTimeInSeconds + "s";
        }

        binding.timeRest.setText(timeRestString);
    }

    private void createLineupUI() {
        playerOnFieldLayoutHashMap = new HashMap<>();
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
        if (FieldPositions.onFieldPositions.contains(fieldPosition)) {
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
        } else if (FieldPositions.primaPanchina.contains(fieldPosition)) {
            binding.panchinaFirstSection.addView(playerLayout);
        } else if (FieldPositions.secondaPanchina.contains(fieldPosition)) {
            binding.panchinaSecondSection.addView(playerLayout);
        } else {
            binding.panchinaThirdSection.addView(playerLayout);
        }
    }

    private void setRosterOfPlayers() {
        lineup = new HashMap<>(lineupSize);
        rosterOfPlayers = new ArrayList<>();

        if (user.formazioniPerGiornata != null &&
                user.formazioniPerGiornata.size() > AssetDecoderUtil.currentRound &&
                user.formazioniPerGiornata.get(AssetDecoderUtil.currentRound) != null) {

            HashMap<FieldPositions, String> formazioneSalvata = user.formazioniPerGiornata.get(AssetDecoderUtil.currentRound);

            for (FieldPositions position : formazioneSalvata.keySet()) {
                String playerId = formazioneSalvata.get(position);
                Player player = completePlayersList.get(playerId);

                lineup.put(position, player);
                occupyPositionField(playerOnFieldLayoutHashMap.get(position), player);
            }
        } else {
            for (FieldPositions position : FieldPositions.values()) {
                lineup.put(position, null);
            }
        }
        if (user.roster != null) {
            for (String playerId : user.roster) {
                rosterOfPlayers.add(completePlayersList.get(playerId));
            }
        }
    }

    private void showBottomSheet(PlayerOnFieldLayout playerOnFieldLayout) {
        Context context = getContext();
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout playersLayout = new LinearLayout(context);
        playersLayout.setLayoutParams(LAYOUT_PARAMS);
        playersLayout.setOrientation(LinearLayout.VERTICAL);

        for (Player player : rosterOfPlayers) {
            if (!lineup.containsValue(player) && isPlayerAdaptForThisPosition(player)) {
                playersLayout.addView(
                        getPlayerView(playerOnFieldLayout, context, bottomSheetDialog, player));
            }
        }

        playersLayout.addView(getFreePositionView(context, playerOnFieldLayout, bottomSheetDialog));

        scrollView.addView(playersLayout);
        bottomSheetDialog.setContentView(scrollView);
        bottomSheetDialog.show();
    }

    @NotNull
    private View getPlayerView(
            PlayerOnFieldLayout playerOnFieldLayout, Context context,
            BottomSheetDialog bottomSheetDialog, Player player) {

        PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(context, player);
        playerLayout.setOnClickListener(view -> {
            occupyPositionField(playerOnFieldLayout, player);
            lineup.put(selectedRole, player);
            bottomSheetDialog.dismiss();
        });
        playerLayout.setLayoutParams(LAYOUT_PARAMS);
        return playerLayout.getPlayerLayout();
    }

    private View getFreePositionView(Context context, PlayerOnFieldLayout playerOnFieldLayout,
                                     BottomSheetDialog bottomSheetDialog) {
        Player emptyPlayer = new Player();
        PlayerLayoutHorizontal emptyPlayerLayout = new PlayerLayoutHorizontal(context, emptyPlayer);

        final LinearLayout subLayout = emptyPlayerLayout.getRightLinearLayout();
        TextView textView = (TextView) subLayout.getChildAt(0);
        textView.setText(R.string.libera_posizione);

        emptyPlayerLayout.setOnClickListener(view -> {
            occupyPositionField(playerOnFieldLayout, emptyPlayer);
            lineup.put(selectedRole, null);
            bottomSheetDialog.dismiss();
        });
        emptyPlayerLayout.setLayoutParams(LAYOUT_PARAMS);
        return emptyPlayerLayout.getPlayerLayout();
    }

    private boolean isPlayerAdaptForThisPosition(Player player) {
        if (FieldPositions.onFieldPositions.contains(selectedRole)) {
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