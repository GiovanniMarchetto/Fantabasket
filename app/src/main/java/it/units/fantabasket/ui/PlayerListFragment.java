package it.units.fantabasket.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentPlayerListBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.*;

public class PlayerListFragment extends Fragment {

    public static List<String> newRoster;
    private static HashMap<String, LinearLayout> mapOfLayoutForPlayerId;
    private final int rosterSize = 16;
    @SuppressWarnings("FieldCanBeLocal")
    private final int moneySize = 150;
    private int money;
    private int numberOfPlayersSelected;
    private FragmentPlayerListBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPlayerListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<Player> playerList = new ArrayList<>();
        Utils.setCompletePlayerList(getActivity(), playerList);

        newRoster = new ArrayList<>(roster);
        money = moneySize;
        numberOfPlayersSelected = newRoster.size();
        for (String playerId : roster) {
            money = money - getCostFromPlayerId(playerId, playerList);
        }

        binding.moneyCount.setText(String.valueOf(money));
        String rosterDescription = numberOfPlayersSelected + "/" + rosterSize;
        binding.roster.setText(rosterDescription);

        if (Utils.getCalendarNow().after(orarioInizioPrimaPartitaDellaGiornataCorrente)) {
            binding.saveRosterButton.setEnabled(false);
        }

        binding.saveRosterButton.setOnClickListener(view -> {
            if (Utils.getCalendarNow().after(orarioInizioPrimaPartitaDellaGiornataCorrente)) {
                binding.saveRosterButton.setEnabled(false);//TODO:messaggino
            } else {
                roster = new ArrayList<>(newRoster);
                        userDataReference.child("roster").setValue(roster);
                        NavHostFragment.findNavController(PlayerListFragment.this)
                                .navigate(R.id.action_PlayerListFragment_to_DashboardFragment);
                    }
                }
        );


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

        mapOfLayoutForPlayerId = new HashMap<>();
        for (Player player : playerList) {
            PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(getContext(), player);

            TextView costView = new TextView(getContext());
            String costOfPlayer = player.getCost() + " fm";
            costView.setText(costOfPlayer);
            costView.setPadding(15, 0, 15, 0);
            playerLayout.getRightLinearLayout().addView(costView, 1);

            int colorTake = Color.parseColor("#66BB6A");
            int colorFree = Color.WHITE;//Color.parseColor(getResources().getString(R.color.white));

            GradientDrawable border = (GradientDrawable) playerLayout.getPlayerLayout().getBackground();
            if (newRoster.contains(player.getId())) {
                border.setColor(colorTake);
            }

            playerLayout.setOnClickListener(view -> {

                if (!newRoster.contains(player.getId())) {
                    if (numberOfPlayersSelected < rosterSize && money - player.getCost() > 0) {
                        money = money - player.getCost();
                        numberOfPlayersSelected++;
                        newRoster.add(player.getId());
                        border.setColor(colorTake);
                    }
                } else {
                    money = money + player.getCost();
                    numberOfPlayersSelected--;
                    newRoster.remove(player.getId());
                    border.setColor(colorFree);
                }
                binding.moneyCount.setText(String.valueOf(money));
                String rosterDescriptionUpdate = numberOfPlayersSelected + "/" + rosterSize;
                binding.roster.setText(rosterDescriptionUpdate);

            });
            playerLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mapOfLayoutForPlayerId.put(player.getId(), playerLayout.getPlayerLayout());
            binding.principalLinearLayout.addView(playerLayout.getPlayerLayout());
        }

        binding.searchPlayer.addTextChangedListener((TextWatcherAfterChange) searchEditable -> {
            String searchString = searchEditable.toString().toUpperCase();

            for (String id : mapOfLayoutForPlayerId.keySet()) {
                int visibility = (searchString.equals("") || id.contains(searchString)) ? View.VISIBLE : View.GONE;
                //noinspection ConstantConditions
                mapOfLayoutForPlayerId.get(id).setVisibility(visibility);
            }

        });
        return root;
    }

    private int getCostFromPlayerId(String playerID, List<Player> playerList) {
        for (Player player : playerList) {
            if (player.getId().equals(playerID)) {
                return player.getCost();
            }
        }
        return 0;
    }

}