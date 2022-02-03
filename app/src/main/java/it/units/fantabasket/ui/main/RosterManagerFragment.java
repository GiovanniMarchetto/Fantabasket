package it.units.fantabasket.ui.main;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentRosterManagerBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static it.units.fantabasket.ui.MainActivity.*;

public class RosterManagerFragment extends Fragment {

    public static List<String> newRoster;
    private static HashMap<String, LinearLayout> mapOfLayoutByPlayerId;
    private static List<LinearLayout> listOfLayoutOrderedByTeam;
    private static List<LinearLayout> listOfLayoutOrderedByCost;
    private final int rosterSize = 16;
    @SuppressWarnings("FieldCanBeLocal")
    private final int moneySize = 200;
    private boolean orderByTeam = true;
    private int money;
    private int numberOfPlayersSelected;
    private FragmentRosterManagerBinding binding;

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRosterManagerBinding.inflate(inflater, container, false);
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
                        Utils.showSnackbar(view, "È iniziata la giornata non puoi cambiare il roster", "error");
                    } else if (numberOfPlayersSelected < 12) {
                        Utils.showSnackbar(view, "Minimo 12 giocatori", "warning");
                    } else {
                        roster = new ArrayList<>(newRoster);
                        userDataReference.child("roster").setValue(roster);
                        NavHostFragment.findNavController(RosterManagerFragment.this)
                                .navigate(R.id.action_RosterManagerFragment_to_LineupFragment);
                    }
                }
        );


        scriptPerStatisticheGiocatori(playerList);

        HashMap<String, Integer> mapOfCostByPlayerId = new HashMap<>();
        int maxCostOfAPlayer = 0;

        mapOfLayoutByPlayerId = new HashMap<>();
        listOfLayoutOrderedByTeam = new ArrayList<>();
        listOfLayoutOrderedByCost = new ArrayList<>();

        for (Player player : playerList) {
            PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(getContext(), player);

            TextView costView = new TextView(getContext());
            String costOfPlayer = player.getCost() + " fm";
            costView.setText(costOfPlayer);
            costView.setPadding(15, 0, 15, 0);
            playerLayout.getRightLinearLayout().addView(costView, 1);

            int colorTake = Color.parseColor("#66BB6A");
            int colorFree = getContext().getColor(R.color.listPlayerBackGround);//Color.parseColor(getResources().getString(R.color.white));

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

            LinearLayout finalLayout = playerLayout.getPlayerLayout();

            mapOfLayoutByPlayerId.put(player.getId(), finalLayout);
            mapOfCostByPlayerId.put(player.getId(), player.getCost());
            listOfLayoutOrderedByTeam.add(finalLayout);

            if (player.getCost() > maxCostOfAPlayer) maxCostOfAPlayer = player.getCost();

            binding.principalLinearLayout.addView(finalLayout);
        }

        for (int cost = maxCostOfAPlayer; cost > 0; cost--) {
            List<String> idToRemove = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mapOfCostByPlayerId.entrySet()) {
                if (entry.getValue() == cost) {
                    listOfLayoutOrderedByCost.add(mapOfLayoutByPlayerId.get(entry.getKey()));
                    idToRemove.add(entry.getKey());
                }
            }
            for (String id : idToRemove) {
                mapOfCostByPlayerId.remove(id);
            }
        }

        binding.searchPlayer.addTextChangedListener((TextWatcherAfterChange) searchEditable -> {
            String searchString = searchEditable.toString().toUpperCase();

            for (String id : mapOfLayoutByPlayerId.keySet()) {
                int visibility = (searchString.equals("") || id.contains(searchString)) ? View.VISIBLE : View.GONE;
                //noinspection ConstantConditions
                mapOfLayoutByPlayerId.get(id).setVisibility(visibility);
            }
        });

        binding.orderButton.setOnClickListener(view -> {
            if (orderByTeam) {
                binding.principalLinearLayout.removeAllViews();
                for (LinearLayout linearLayout : listOfLayoutOrderedByCost) {
                    binding.principalLinearLayout.addView(linearLayout);
                }
                Utils.showSnackbar(view, "order by cost");
                orderByTeam = false;
            } else {
                binding.principalLinearLayout.removeAllViews();
                for (LinearLayout linearLayout : listOfLayoutOrderedByTeam) {
                    binding.principalLinearLayout.addView(linearLayout);
                }
                Utils.showSnackbar(view, "order by team");
                orderByTeam = true;
            }
        });
        return root;
    }

    private void scriptPerStatisticheGiocatori(List<Player> playerList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Random random = new Random();


            DatabaseReference playerStatisticsRef = FirebaseDatabase.getInstance().getReference("playersStatistics");

            playerList.forEach(player -> {
                        HashMap<String, Object> playerStatistics = new HashMap<>();

                        int costo = player.getCost();
                        int fattoreCosto = 0;
                        for (int i = 0; i < 50; i = i + 5) {
                            if (costo > i) {
                                fattoreCosto++;
                            }
                        }

                        int maxReb = 0;
                        int maxRecover = 0;
                        int maxLost = 0;
                        switch (player.getRole_1()) {
                            case PLAYMAKER:
                                maxReb = fattoreCosto;
                                maxRecover = 5;
                                maxLost = 5;
                                break;
                            case GUARDIA:
                                maxReb = 3 + fattoreCosto;
                                maxRecover = 5 + fattoreCosto / 2;
                                maxLost = 5 - fattoreCosto / 2;
                                break;
                            case ALA:
                                maxReb = 4 + fattoreCosto;
                                maxRecover = 2 + fattoreCosto / 2;
                                maxLost = 3;
                                break;
                            case CENTRO:
                                maxReb = 5 + fattoreCosto;
                                maxRecover = fattoreCosto;
                                maxLost = 4;
                                break;
                        }


                        List<Integer> listOfPoints = new ArrayList<>();
                        List<Integer> listOfFouls = new ArrayList<>();
                        List<Integer> listOfRebounds = new ArrayList<>();
                        List<Integer> listOfRecoverBalls = new ArrayList<>();
                        List<Integer> listOfLostBalls = new ArrayList<>();
                        for (int i = 0; i < giornataCorrente; i++) {
                            listOfPoints.add(random.nextInt(costo) + random.nextInt(fattoreCosto));
                            listOfFouls.add(random.nextInt(6));
                            listOfRebounds.add(random.nextInt(maxReb));
                            listOfRecoverBalls.add(random.nextInt(maxRecover));
                            listOfLostBalls.add(random.nextInt(maxLost));
                        }


                        playerStatistics.put("id", player.getId());
                        playerStatistics.put("points", listOfPoints);
                        playerStatistics.put("fouls", listOfFouls);
                        playerStatistics.put("rebounds", listOfRebounds);
                        playerStatistics.put("recoverBalls", listOfRecoverBalls);
                        playerStatistics.put("lostBalls", listOfLostBalls);
                        playerStatisticsRef.child(player.getId()).setValue(playerStatistics);
                    }
            );

            int size = playerList.size();
            Log.e("MIO", "dim " + size);
            int sizeD = (int) playerList.stream().distinct().count();
            if (size != sizeD) {
                Log.e("MIO", "NON SONO IDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            } else {
                Log.e("MIO", "Tutti cognomi diversi");
            }

            Log.i("MIO", "MANNION: " + playerList.stream().filter(player -> !Objects.equals(player.getId(), "MANNION")).count());
            Log.i("MIO", "BANKS: " + playerList.stream().filter(player -> !Objects.equals(player.getId(), "BANKS")).count());
        }
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