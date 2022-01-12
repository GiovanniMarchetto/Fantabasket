package it.units.fantabasket.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentDashboardBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.PlayerLayoutHorizontal;
import it.units.fantabasket.entities.PlayerOnFieldLayout;
import it.units.fantabasket.enums.FieldPositions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.roster;

@SuppressWarnings("ConstantConditions")
public class DashboardFragment extends Fragment {

    private static List<Player> playerList;
    private static HashMap<FieldPositions, Player> selectedPlayerList;
    private static FieldPositions selectedRole;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        int panchinaSize = 5;
        for (int i = 1; i <= panchinaSize; i++) {
            PlayerOnFieldLayout playerOnFieldLayout = new PlayerOnFieldLayout(getContext(), new Player());
            int finalI = i;
            playerOnFieldLayout.getPlayerButton().setOnClickListener(view -> {
                        selectedRole = FieldPositions.valueOf("PANCHINA_" + finalI);
                        showBottomSheet(playerOnFieldLayout.getPlayerButton(), playerOnFieldLayout.getPlayerTextView());
                    }
            );
            binding.panchina.addView(playerOnFieldLayout.getPlayerLayout());
        }

        setPlayerList();

        binding.playmakerButton.setOnClickListener(view -> {
            selectedRole = FieldPositions.PLAYMAKER;
            showBottomSheet(binding.playmakerButton, binding.playmakerText);
        });
        binding.guardiaDxButton.setOnClickListener(view -> {
            selectedRole = FieldPositions.GUARDIA_DX;
            showBottomSheet(binding.guardiaDxButton, binding.guardiaDxText);
        });
        binding.guardiaSxButton.setOnClickListener(view -> {
            selectedRole = FieldPositions.GUARDIA_SX;
            showBottomSheet(binding.guardiaSxButton, binding.guardiaSxText);
        });
        binding.alaButton.setOnClickListener(view -> {
            selectedRole = FieldPositions.ALA;
            showBottomSheet(binding.alaButton, binding.alaText);
        });
        binding.centroButton.setOnClickListener(view -> {
            selectedRole = FieldPositions.CENTRO;
            showBottomSheet(binding.centroButton, binding.centroText);
        });

        binding.modifyButton.setOnClickListener(view ->
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.action_DashboardFragment_to_PlayerListFragment)
        );

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setPlayerList() {
        selectedPlayerList = new HashMap<>();
        for (FieldPositions position : FieldPositions.values()) {
            selectedPlayerList.put(position, null);
        }

        playerList = new ArrayList<>();

        List<Player> completePlayersList = new ArrayList<>();
        PlayerListFragment.setCompletePlayerList(getActivity(), completePlayersList);
        //TODO: valutare se rendere la complete list una variabile globale

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            completePlayersList.stream().filter(player -> roster.contains(player.getId())).forEach(
                    player -> playerList.add(player)
            );
        }
    }

    private void showBottomSheet(Button playerButton, TextView playerName) {
        Context context = getContext();
        if (context == null) {
            Log.e("URCA", "Contesto nullo");
        }
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        LinearLayout playersLayout = new LinearLayout(context);
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        playersLayout.setLayoutParams(params);
        playersLayout.setOrientation(LinearLayout.VERTICAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playerList.stream()
                    .filter(player -> !selectedPlayerList.containsValue(player))
                    .forEach(
                            player -> {
                                PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(context, player);
                                playerLayout.setOnClickListener(view -> {
                                    occupyPositionField(playerButton, playerName, player);
                                    bottomSheetDialog.dismiss();
                                });
                                playerLayout.setLayoutParams(params);
                                playersLayout.addView(playerLayout.getPlayerLayout());
                            }
                    );
        }

        bottomSheetDialog.setContentView(playersLayout);
        bottomSheetDialog.show();
    }

    private void occupyPositionField(Button playerButton, TextView playerName, Player player) {
        playerButton.setText(player.getNumber());
        playerButton.setTextColor(player.getNumberColor());
        playerButton.setBackground(getContext().getDrawable(player.getShirt()));
        playerName.setText(player.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedPlayerList.replace(selectedRole, player);
        }
    }
}