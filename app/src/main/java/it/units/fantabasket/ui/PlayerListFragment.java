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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentPlayerListBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static it.units.fantabasket.MainActivity.roster;
import static it.units.fantabasket.MainActivity.userDataReference;

public class PlayerListFragment extends Fragment {

    public static List<String> newRoster;
    private final int rosterSize = 16;
    private int money;
    private int numberOfPlayersSelected;
    private FragmentPlayerListBinding binding;

    public static void setCompletePlayerList(FragmentActivity fragmentActivity, List<Player> playerList) {
        for (Team team : Team.values()) {
            try {
                InputStreamReader is = new InputStreamReader(
                        fragmentActivity.getAssets()
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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPlayerListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        newRoster = new ArrayList<>(roster);

        int moneySize = 500;
        money = moneySize;
        numberOfPlayersSelected = 0;
        binding.moneyCount.setText(getString(R.string.fantamilioni) + ": " + moneySize);
        binding.roster.setText(getString(R.string.roster) + ": " + newRoster.size() + "/" + rosterSize);

        binding.saveRosterButton.setOnClickListener(view -> {
                    roster = new ArrayList<>(newRoster);
                    userDataReference.child("players").setValue(roster);
                    NavHostFragment.findNavController(PlayerListFragment.this)
                            .navigate(R.id.action_PlayerListFragment_to_DashboardFragment);
                }
        );

        List<Player> playerList = new ArrayList<>();

        setCompletePlayerList(getActivity(), playerList);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            List<Integer> listOfValues = new ArrayList<>();
//            listOfValues.add(0);
//            listOfValues.add(0);
//            listOfValues.add(0);
//            listOfValues.add(0);
//            listOfValues.add(0);
//            listOfValues.add(0);
//            listOfValues.add(0);
//            DatabaseReference playerStatisticsRef = FirebaseDatabase.getInstance().getReference("playersStatistics");
//
//            playerList.forEach(player -> {
//                        HashMap<String, Object> playerStatistics = new HashMap<>();
//                        playerStatistics.put("id", player.getId());
//                        playerStatistics.put("points", listOfValues);
//                        playerStatistics.put("fouls", listOfValues);
//                        playerStatistics.put("rebounds", listOfValues);
//                        playerStatistics.put("recoverBalls", listOfValues);
//                        playerStatistics.put("lostBalls", listOfValues);
//                        playerStatistics.put("voteL", listOfValues);
//                        playerStatisticsRef.child(player.getId()).setValue(playerStatistics);
//                    }
//            );
//
//            int size = playerList.size();
//            Log.e("MIO", "dim "+size);
//            int sizeD = (int) playerList.stream().distinct().count();
//            if (size != sizeD) {
//                Log.e("MIO", "NON SONO IDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//            }else {
//                Log.e("MIO", "Tutti cognomi diversi");
//            }
//
//            Log.i("MIO","MANNION: "+playerList.stream().filter(player -> !Objects.equals(player.getId(), "MANNION")).count());
//            Log.i("MIO","BANKS: "+playerList.stream().filter(player -> !Objects.equals(player.getId(), "BANKS")).count());
//        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            playerList.forEach(
                    player -> {
                        PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(getContext(), player);

                        GradientDrawable border = (GradientDrawable) playerLayout.getPlayerLayout().getBackground();
                        if (newRoster.contains(player.getId())) {
                            border.setColor(Color.GREEN);
                        }

                        playerLayout.setOnClickListener(view -> {

                            ColorStateList color = border.getColor();
                            if (color.getDefaultColor() == Color.LTGRAY) {
                                if (numberOfPlayersSelected < rosterSize && money > 0) {
                                    money--;//TODO: sarà da mettere sia la condizione di money sufficienti che la giusta operazione
                                    numberOfPlayersSelected++;
                                    newRoster.add(player.getId());
                                    border.setColor(Color.GREEN);
                                }
                            } else {
                                money++;
                                numberOfPlayersSelected--;
                                newRoster.remove(player.getId());
                                border.setColor(Color.LTGRAY);
                            }
                            binding.moneyCount.setText(getString(R.string.fantamilioni) + ": " + money);
                            binding.roster.setText(getString(R.string.roster) + ": " + numberOfPlayersSelected + "/" + rosterSize);

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

}