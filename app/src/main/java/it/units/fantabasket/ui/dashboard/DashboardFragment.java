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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class DashboardFragment extends Fragment {

    private static List<Player> playerList;
    private static HashMap<Role, Player> selectedPlayerList;
    private static Role selectedRole;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setPlayerList();

        binding.playmakerButton.setOnClickListener(view -> {
            selectedRole = Role.PLAYMAKER;
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
        selectedPlayerList.put(Role.PLAYMAKER, null);
        selectedPlayerList.put(Role.GUARDIA_DX, null);
        selectedPlayerList.put(Role.GUARDIA_SX, null);
        selectedPlayerList.put(Role.ALA, null);
        selectedPlayerList.put(Role.CENTRO, null);

        playerList = new ArrayList<>();

        for (Team team : Team.values()) {
            try {
                InputStreamReader is = new InputStreamReader(
                        getActivity().getAssets()
                                .open("giocatori/" + team.name().toLowerCase() + ".csv"));
                BufferedReader reader = new BufferedReader(is);
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    Role role_1;
                    Role role_2 = null;
                    if (values[3].contains("/")) {
                        String[] roles = values[3].split("/");
                        role_1 = Role.valueOf(roles[0].toUpperCase());
                        role_2 = Role.valueOf(roles[1].toUpperCase());
                    } else {
                        role_1 = Role.valueOf(values[3].toUpperCase());
                    }


                    playerList.add(new Player(
                            values[0], values[1], values[2],
                            role_1, role_2,
                            values[4], values[5], values[6],
                            values[7], team
                    ));
                }
            } catch (IOException e) {
                Log.e("DATI", "Error loading asset files", e);
            } catch (Exception ee) {
                Log.e("DATI", ee.getMessage());
                ee.printStackTrace();
            }
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
        playerName.setText(player.getSurname());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedPlayerList.replace(selectedRole, player);
        }
    }
}