package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentPlayerListBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.entities.PlayerLayoutHorizontal;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PlayerListFragment extends Fragment {

    private int money;
    private int numberOfPlayersSelected;


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentPlayerListBinding binding = FragmentPlayerListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        money = 500;
        numberOfPlayersSelected = 0;

        List<Player> playerList = new ArrayList<>();

        setPlayerList(playerList);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playerList.forEach(
                    player -> {
                        PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(getContext(), player);
                        playerLayout.setOnClickListener(view -> {

                            GradientDrawable border = (GradientDrawable) playerLayout.getPlayerLayout().getBackground();
                            ColorStateList color = border.getColor();
                            if (color.getDefaultColor() == Color.LTGRAY) {
                                if (numberOfPlayersSelected < 16 && money > 0) {
                                    money--;//TODO: sarà da mettere sia la condizione di money sufficienti che la giusta operazione
                                    numberOfPlayersSelected++;
                                    border.setColor(Color.GREEN);
                                }
                            } else {
                                money++;
                                numberOfPlayersSelected--;
                                border.setColor(Color.LTGRAY);
                            }
                            binding.moneyCount.setText("Fantamilioni restanti: " + money);

                        });
                        playerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        binding.principalLinearLayout.addView(playerLayout.getPlayerLayout());
                    }
            );
        }

        return root;
//        return inflater.inflate(R.layout.fragment_player_list, container, false);
    }

    private void setPlayerList(List<Player> playerList) {
        for (Team team : Team.values()) {
            try {
                @SuppressWarnings("ConstantConditions")
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

}