package it.units.fantabasket.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentRosterManagerBinding;
import it.units.fantabasket.entities.Player;
import it.units.fantabasket.enums.Team;
import it.units.fantabasket.layouts.PlayerLayoutHorizontal;
import it.units.fantabasket.utils.AssetDecoderUtil;
import it.units.fantabasket.utils.TextWatcherAfterChange;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.user;
import static it.units.fantabasket.ui.MainActivity.userDataReference;
import static it.units.fantabasket.utils.AssetDecoderUtil.completePlayersList;
import static it.units.fantabasket.utils.AssetDecoderUtil.maxCostOfAPlayer;
import static it.units.fantabasket.utils.Utils.LAYOUT_PARAMS;

@SuppressWarnings("ConstantConditions")
public class RosterManagerFragment extends Fragment {

    @SuppressWarnings("FieldCanBeLocal")
    private final int MONEY_SIZE = 200;
    private final int ROSTER_SIZE = 16;
    private int money;
    private int numberOfPlayersSelected;

    private List<String> newRoster;
    private HashMap<String, LinearLayout> mapOfLayoutByPlayerId;
    private List<LinearLayout> listOfLayoutOrderedByTeam;
    private List<LinearLayout> listOfLayoutOrderedByCost;
    private boolean orderByTeam = true;

    private FragmentRosterManagerBinding binding;
    private Context rosterContext;

    @Override
    public void onStart() {
        super.onStart();
        if (Utils.getCalendarNow().after(AssetDecoderUtil.calendarOfCurrentRoundStart)) {
            binding.saveRosterButton.setEnabled(false);//not permit changing roster during round
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRosterManagerBinding.inflate(inflater, container, false);
        rosterContext = getContext();

        newRoster = new ArrayList<>(user.roster);
        money = MONEY_SIZE;
        numberOfPlayersSelected = newRoster.size();
        for (String playerId : user.roster) {
            money = money - completePlayersList.get(playerId).getCost();
        }
        mapOfLayoutByPlayerId = new HashMap<>();

        for (Player player : completePlayersList.values()) {
            View playerView = getPlayerListLayout(player);
            binding.principalLinearLayout.addView(playerView);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reorderListsOfPlayers();

        binding.moneyCount.setText(String.valueOf(money));
        String rosterDescription = numberOfPlayersSelected + "/" + ROSTER_SIZE;
        binding.roster.setText(rosterDescription);

        binding.searchPlayer.addTextChangedListener((TextWatcherAfterChange) searchEditable -> {
            String searchString = searchEditable.toString().toUpperCase();

            for (String id : mapOfLayoutByPlayerId.keySet()) {
                int visibility = (searchString.equals("") || id.contains(searchString)) ? View.VISIBLE : View.GONE;
                mapOfLayoutByPlayerId.get(id).setVisibility(visibility);
            }
        });

        binding.orderButton.setOnClickListener(viewListener -> {
            binding.principalLinearLayout.removeAllViews();
            if (orderByTeam) {
                for (LinearLayout linearLayout : listOfLayoutOrderedByCost) {
                    binding.principalLinearLayout.addView(linearLayout);
                }
                Utils.showSnackbar(view, "order by cost");
                orderByTeam = false;
            } else {
                for (LinearLayout linearLayout : listOfLayoutOrderedByTeam) {
                    binding.principalLinearLayout.addView(linearLayout);
                }
                Utils.showSnackbar(view, "order by team");
                orderByTeam = true;
            }
        });

        binding.saveRosterButton.setOnClickListener(viewListener -> {
            if (Utils.getCalendarNow().after(AssetDecoderUtil.calendarOfCurrentRoundStart)) {
                Utils.showSnackbar(viewListener, getString(R.string.round_started_yet), "error");
            } else if (numberOfPlayersSelected < 12) {
                Utils.showSnackbar(viewListener, getString(R.string.at_least_12_players), "warning");
            } else {
                user.roster = new ArrayList<>(newRoster);
                userDataReference.child("roster").setValue(user.roster);
                NavHostFragment.findNavController(RosterManagerFragment.this)
                        .navigate(R.id.action_RosterManagerFragment_to_LineupFragment);
            }
        });
    }

    private void reorderListsOfPlayers() {
        HashMap<Integer, List<String>> mapOfCostByPlayerId = new HashMap<>();
        HashMap<Team, List<String>> mapOfTeamByPlayerId = new HashMap<>();

        for (Player player : completePlayersList.values()) {
            List<String> playerIdCostList = mapOfCostByPlayerId.get(player.getCost());
            if (playerIdCostList == null) playerIdCostList = new ArrayList<>();
            playerIdCostList.add(player.getId());
            mapOfCostByPlayerId.put(player.getCost(), playerIdCostList);

            List<String> playerIdTeamList = mapOfTeamByPlayerId.get(player.getTeam());
            if (playerIdTeamList == null) playerIdTeamList = new ArrayList<>();
            playerIdTeamList.add(player.getId());
            mapOfTeamByPlayerId.put(player.getTeam(), playerIdTeamList);
        }

        listOfLayoutOrderedByCost = reorderListByCost(mapOfCostByPlayerId);
        listOfLayoutOrderedByTeam = reorderListByTeam(mapOfTeamByPlayerId);
    }

    private List<LinearLayout> reorderListByTeam(HashMap<Team, List<String>> mapOfTeamByPlayerId) {
        List<LinearLayout> orderedListByTeam = new ArrayList<>();
        for (Team team : Team.values()) {
            List<String> playersOfThisTeam = mapOfTeamByPlayerId.get(team);
            if (playersOfThisTeam != null) {
                for (String playerId : playersOfThisTeam) {
                    orderedListByTeam.add(mapOfLayoutByPlayerId.get(playerId));
                }
            }
        }
        return orderedListByTeam;
    }

    private List<LinearLayout> reorderListByCost(HashMap<Integer, List<String>> mapOfCostByPlayerId) {
        List<LinearLayout> orderedListByCost = new ArrayList<>();

        for (int cost = maxCostOfAPlayer; cost > 0; cost--) {
            List<String> playersOfThisCost = mapOfCostByPlayerId.get(cost);
            if (playersOfThisCost != null) {
                for (String playerId : playersOfThisCost) {
                    orderedListByCost.add(mapOfLayoutByPlayerId.get(playerId));
                }
            }
        }
        return orderedListByCost;
    }

    private View getPlayerListLayout(Player player) {
        PlayerLayoutHorizontal playerLayout = new PlayerLayoutHorizontal(rosterContext, player);

        TextView costView = new TextView(rosterContext);
        String costOfPlayer = player.getCost() + " fm";
        costView.setText(costOfPlayer);
        costView.setPadding(15, 0, 15, 0);
        playerLayout.getRightLinearLayout().addView(costView, 1);

        int colorTake = Color.parseColor("#66BB6A");
        int colorFree = rosterContext.getColor(R.color.listPlayerBackGround);

        GradientDrawable border = (GradientDrawable) playerLayout.getPlayerLayout().getBackground();
        if (newRoster.contains(player.getId())) {
            border.setColor(colorTake);
        }

        playerLayout.setOnClickListener(view -> addOrRemovePlayer(player, colorTake, colorFree, border));
        playerLayout.setLayoutParams(LAYOUT_PARAMS);

        LinearLayout finalLayout = playerLayout.getPlayerLayout();

        mapOfLayoutByPlayerId.put(player.getId(), finalLayout);

        return finalLayout;
    }

    private void addOrRemovePlayer(Player player, int colorTake, int colorFree, GradientDrawable border) {
        if (!newRoster.contains(player.getId())) {
            if (numberOfPlayersSelected < ROSTER_SIZE && money - player.getCost() >= 0) {
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
        String rosterDescriptionUpdate = numberOfPlayersSelected + "/" + ROSTER_SIZE;
        binding.roster.setText(rosterDescriptionUpdate);
    }
}