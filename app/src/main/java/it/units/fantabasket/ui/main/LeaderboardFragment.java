package it.units.fantabasket.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLeaderboardBinding;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.layouts.LeaderboardElementLayout;
import it.units.fantabasket.utils.AssetDecoderUtil;
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.*;
import static it.units.fantabasket.utils.DecoderUtil.*;

@SuppressWarnings({"ConstantConditions", "unchecked"})
public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        Context context = getContext();

        if (legaSelezionata != null) {
            showClassifica(context);

            if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                showCalendario(context);
            } else {
                binding.calendarShortcut.setVisibility(View.GONE);
            }

        } else {
            binding.leaderboard.addView(getBaseTextView(context, R.string.not_league_selected));
        }

        if (isUserTheAdminOfLeague) {
            binding.updateLeaderboardButton.setVisibility(View.VISIBLE);
            setUpdateLeaderboardButton();
        }
        return binding.getRoot();
    }

    private void setUpdateLeaderboardButton() {
        boolean isEnable = false;
        if (leagueOn.get().getLastRoundCalculated() < AssetDecoderUtil.currentRound - 1) {
            isEnable = true;
            binding.updateLeaderboardButton.setOnClickListener(view ->
                    calcolaGiornateDaUnCertoRound(leagueOn.get().getLastRoundCalculated() + 1));
        } else if (leagueOn.get().getLastRoundCalculated() == AssetDecoderUtil.calendarListOfRoundStart.size()) {
            isEnable = true;
            binding.updateLeaderboardButton.setText(getString(R.string.ricalcola_tutte_le_giornate));
            binding.updateLeaderboardButton.setOnClickListener(view -> calcolaGiornateDaUnCertoRound(0));
        }
        binding.updateLeaderboardButton.setEnabled(isEnable);
    }

    private void showClassifica(Context context) {
        List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();

        if (classifica != null) {
            for (int i = 1; i < classifica.size(); i++) {
                HashMap<String, Object> element = classifica.get(i);
                int totalPointsScored = (int) element.get(TOTAL_POINTS_SCORED);
                LeaderboardElementLayout elementLayout = getLeaderboardElementLayout(context, i, element, totalPointsScored);
                binding.leaderboard.addView(elementLayout.getLeaderboardElementLayout());
            }
        } else {
            binding.leaderboard.addView(getBaseTextView(context, R.string.not_started_yet));
        }
    }

    @NotNull
    private LeaderboardElementLayout getLeaderboardElementLayout(
            Context context, int i, HashMap<String, Object> element, int totalPointsScored) {
        LeaderboardElementLayout elementLayout;
        if (leagueOn.get().getTipologia() == LegaType.FORMULA1) {
            elementLayout = new LeaderboardElementLayout(context,
                    i, (String) element.get(TEAM_NAME), totalPointsScored);
        } else {
            int totalPointsAllowed = (int) element.get(TOTAL_POINTS_ALLOWED);
            int pointsOfVictories = (int) element.get(POINTS_OF_VICTORIES);
            elementLayout = new LeaderboardElementLayout(context,
                    i, (String) element.get(TEAM_NAME), totalPointsScored,
                    totalPointsAllowed, pointsOfVictories);
        }
        return elementLayout;
    }

    private void showCalendario(Context context) {
        HashMap<String, List<Game>> calendario = leagueOn.get().getCalendario();

        if (calendario != null) {
            binding.calendarioContainer.setVisibility(View.VISIBLE);
            addToCalendarioViewAllRounds(context, calendario);
        }
    }


    private void addToCalendarioViewAllRounds(Context context, HashMap<String, List<Game>> calendario) {
        for (String key : calendario.keySet()) {
            TextView titleGiornata = new TextView(context);
            titleGiornata.setBackgroundColor(getResources().getColor(R.color.esteco, context.getTheme()));
            titleGiornata.setTextColor(Color.WHITE);
            titleGiornata.setText(key);
            binding.calendarioContainer.addView(titleGiornata);

            for (Game game : calendario.get(key)) {
                String gameString = game.homeUserId + " " + game.homePoints + " - " + game.awayPoints + " " + game.awayUserId;
                TextView gameTextView = new TextView(context);
                gameTextView.setText(gameString);
                gameTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                binding.calendarioContainer.addView(gameTextView);
            }
        }
    }

    private void calcolaGiornateDaUnCertoRound(int firstRoundToCalculate) {
        for (int roundToCalculate = firstRoundToCalculate; roundToCalculate < AssetDecoderUtil.currentRound; roundToCalculate++) {

            List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();

            if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                List<Game> gameListOfRoundToCalculate = leagueOn.get().getCalendario().get(GIORNATA_ + roundToCalculate);
                calcoloGiornataCalendario(roundToCalculate, gameListOfRoundToCalculate);
                legheReference.child(legaSelezionata).child(CALENDARIO).child(GIORNATA_ + roundToCalculate).setValue(gameListOfRoundToCalculate);

                updateClassificaLegaCalendario(classifica, gameListOfRoundToCalculate);

            } else {
                updateClassificaLegaFormula1(classifica, roundToCalculate);
            }

            List<HashMap<String, Object>> orderClassifica = reorderClassificaFromLegaType(classifica, leagueOn.get().getTipologia());
            legheReference.child(legaSelezionata).child(CLASSIFICA).setValue(orderClassifica);
            legheReference.child(legaSelezionata).child(LAST_ROUND_CALCULATED).setValue(roundToCalculate);
        }
    }

    private TextView getBaseTextView(Context context, int resIdString) {
        TextView textView = new TextView(context);
        textView.setBackgroundColor(Color.LTGRAY);
        textView.setText(resIdString);
        return textView;
    }

    private void updateClassificaLegaFormula1(List<HashMap<String, Object>> classifica, int roundToCalculate) {
        for (HashMap<String, Object> elementoClassifica : classifica) {
            String elId = (String) elementoClassifica.get("userId");
            int pointsScoredThisRound = calcolaPuntiGiornataFromUserId(roundToCalculate, elId);

            Object objTotalPointScored = elementoClassifica.get(TOTAL_POINTS_SCORED);
            int totalPointsScored = (objTotalPointScored != null) ? (int) objTotalPointScored : 0;

            elementoClassifica.put(TOTAL_POINTS_SCORED, totalPointsScored + pointsScoredThisRound);
        }
    }

    @NotNull
    private List<HashMap<String, Object>> reorderClassificaFromLegaType(List<HashMap<String, Object>> classifica, LegaType legaType) {
        List<HashMap<String, Object>> classificaUpdate = new ArrayList<>(classifica.size());

        for (int i = 0; i < classifica.size(); i++) {
            HashMap<String, Object> max = classifica.get(0);
            for (HashMap<String, Object> hashMap : classifica) {
                if (!classificaUpdate.contains(hashMap)) {
                    if (legaType == LegaType.CALENDARIO) {
                        if ((int) hashMap.get(POINTS_OF_VICTORIES) > (int) max.get(POINTS_OF_VICTORIES) ||
                                ((int) hashMap.get(POINTS_OF_VICTORIES) == (int) max.get(POINTS_OF_VICTORIES) &&
                                        (int) hashMap.get(TOTAL_POINTS_SCORED) == (int) max.get(TOTAL_POINTS_SCORED))) {
                            max = hashMap;
                        }
                    } else {
                        if ((int) hashMap.get(TOTAL_POINTS_SCORED) > (int) max.get(TOTAL_POINTS_SCORED)) {
                            max = hashMap;
                        }
                    }

                }
            }
            classificaUpdate.add(max);
            classifica.remove(max);
        }
        return classificaUpdate;
    }

    private void updateClassificaLegaCalendario(List<HashMap<String, Object>> classifica, List<Game> gameList) {
        for (HashMap<String, Object> elementoClassifica : classifica) {
            String elId = (String) elementoClassifica.get("userId");
            int totalPointsScored = (int) elementoClassifica.get(TOTAL_POINTS_SCORED);
            int totalPointsAllowed = (int) elementoClassifica.get(TOTAL_POINTS_ALLOWED);
            int pointsOfVictories = (int) elementoClassifica.get(POINTS_OF_VICTORIES);
            for (Game game : gameList) {
                if (elId.equals(game.homeUserId)) {
                    elementoClassifica.put(TOTAL_POINTS_SCORED, totalPointsScored + game.homePoints);
                    elementoClassifica.put(TOTAL_POINTS_ALLOWED, totalPointsAllowed + game.awayPoints);
                    if (game.homePoints > game.awayPoints) {
                        elementoClassifica.put(POINTS_OF_VICTORIES, pointsOfVictories + 2);
                    }
                    break;
                } else if (elId.equals(game.awayUserId)) {
                    elementoClassifica.put(TOTAL_POINTS_SCORED, totalPointsScored + game.awayPoints);
                    elementoClassifica.put(TOTAL_POINTS_ALLOWED, totalPointsAllowed + game.homePoints);
                    if (game.awayPoints > game.homePoints) {
                        elementoClassifica.put(POINTS_OF_VICTORIES, pointsOfVictories + 2);
                    }
                    break;
                }
            }
        }
    }

    private void calcoloGiornataCalendario(int roundToCalculate, List<Game> gameList) {
        for (Game game : gameList) {

            int puntiHome = calcolaPuntiGiornataFromUserId(roundToCalculate, game.homeUserId);
            int puntiAway = calcolaPuntiGiornataFromUserId(roundToCalculate, game.awayUserId);

            //nel basket non si pareggia--> a parità vince il giocatore in casa
            if (puntiHome == puntiAway) puntiHome++;

            game.setHomePoints(puntiHome);
            game.setAwayPoints(puntiAway);
        }
    }

    private int calcolaPuntiGiornataFromUserId(int giornata, String userId) {
        HashMap<FieldPositions, String> formazione = membersLeagueOn.get(userId).formazioniPerGiornata.get(giornata - 1);
        int pointsScored = 0;

        if (formazione == null) {
            for (int oldRound = giornata - 1; oldRound > 0; oldRound--) {
                formazione = membersLeagueOn.get(userId).formazioniPerGiornata.get(oldRound - 1);
                if (formazione != null) break;
            }
        }

        if (formazione != null) {
            for (FieldPositions key : formazione.keySet()) {
                pointsScored = pointsScored + (int) (Math.round(
                        getFactorPositionOnField(key) *
                                getPointsFromPlayerIdAndGiornata(formazione.get(key), giornata)));
            }
        } else {
            return 0;
        }

        return pointsScored;
    }

    private int getPointsFromPlayerIdAndGiornata(String playerId, int giornata) {
        final int[] vote = {0};
        FirebaseDatabase.getInstance().getReference("playersStatistics").child(playerId).addListenerForSingleValueEvent(
                (MyValueEventListener) snapshot -> {
                    HashMap<String, Object> playerStatistic = (HashMap<String, Object>) snapshot.getValue();
                    int points = ((List<Integer>) playerStatistic.get("points")).get(giornata);
                    int fouls = ((List<Integer>) playerStatistic.get("fouls")).get(giornata);
                    int rebounds = ((List<Integer>) playerStatistic.get("rebounds")).get(giornata);
                    int recoverBalls = ((List<Integer>) playerStatistic.get("recoverBalls")).get(giornata);
                    int lostBalls = ((List<Integer>) playerStatistic.get("lostBalls")).get(giornata);

                    vote[0] = points + rebounds + recoverBalls - fouls - lostBalls;
                }
        );
        return vote[0];
    }

    private double getFactorPositionOnField(FieldPositions fieldPosition) {
        if (FieldPositions.onFieldPositions.contains(fieldPosition)) {
            return 1;
        } else if (FieldPositions.primaPanchina.contains(fieldPosition)) {
            return 3.0 / 4.0;
        } else if (FieldPositions.secondaPanchina.contains(fieldPosition)) {
            return 2.0 / 4.0;
        } else if (FieldPositions.terzaPanchina.contains(fieldPosition)) {
            return 1.0 / 4.0;
        }
        return 0;
    }
}