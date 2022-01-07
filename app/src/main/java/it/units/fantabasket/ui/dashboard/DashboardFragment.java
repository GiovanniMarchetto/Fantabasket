package it.units.fantabasket.ui.dashboard;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import it.units.fantabasket.databinding.FragmentDashboardBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.PlayerLayoutHorizontal;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private static List<Player> playerList;
    private static HashMap<Role, Player> selectedPlayerList;
    private static Role selectedRole;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setPlayerList();

        binding.playmakerButton.setOnClickListener(view -> {
            selectedRole = Role.PLAY;
            showBottomSheet(binding.playmakerButton, binding.playmakerText);
        });
        binding.guardiaDxButton.setOnClickListener(view -> {
            selectedRole = Role.GUARDIA_DX;
            showBottomSheet(binding.guardiaDxButton, binding.guardiaDxText);
        });
        binding.guardiaSxButton.setOnClickListener(view -> {
            selectedRole = Role.GUARDIA_SX;
            showBottomSheet(binding.guardiaSxButton, binding.guardiaSxText);
        });
        binding.alaButton.setOnClickListener(view -> {
            selectedRole = Role.ALA;
            showBottomSheet(binding.alaButton, binding.alaText);
        });
        binding.centroButton.setOnClickListener(view -> {
            selectedRole = Role.CENTRO;
            showBottomSheet(binding.centroButton, binding.centroText);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setPlayerList() {
        selectedPlayerList = new HashMap<>();
        selectedPlayerList.put(Role.PLAY, null);
        selectedPlayerList.put(Role.GUARDIA_DX, null);
        selectedPlayerList.put(Role.GUARDIA_SX, null);
        selectedPlayerList.put(Role.ALA, null);
        selectedPlayerList.put(Role.CENTRO, null);

        playerList = new ArrayList<>();

        playerList.add(new Player("Marchetto", 14, Role.GUARDIA, Team.ATHLETISMO));
        playerList.add(new Player("Junior", 4, Role.PLAY, Team.ATHLETISMO));
        playerList.add(new Player("Mago", 19, Role.PLAY, Team.ATHLETISMO));

        playerList.add(new Player("Rosso", 6, Role.GUARDIA, Team.GORIZIANA));
        playerList.add(new Player("Minu", 4, Role.CENTRO, Team.GORIZIANA));

        playerList.add(new Player("Zeus", 1, Role.ALA, Team.OLIMPIA));
        playerList.add(new Player("Ade", 99, Role.GUARDIA, Team.OLIMPIA));
        playerList.add(new Player("Poseidone", 8, Role.GUARDIA, Team.OLIMPIA));

        playerList.add(new Player("Vecchiet", 19, Role.CENTRO, Team.ROMANS));
    }

    private void showBottomSheet(Button playerButton, TextView playerName) {
        Context context = getContext();
        if (context==null){
            Log.e("URCA","Contesto nullo");
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
        playerButton.setBackground(getContext().getDrawable(player.getShirt()));
        playerName.setText(player.getName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedPlayerList.replace(selectedRole, player);
        }
    }
}