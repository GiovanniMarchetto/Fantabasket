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
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.*;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;

    @Override
    @SuppressWarnings({"ConstantConditions"})
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        Context context = getContext();

        if (legaSelezionata != null) {
            List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();

            if (classifica != null) {
                long lastUpdate = (long) classifica.get(0).get("lastUpdate");
                Calendar orarioFineUltimaGiornataCalendar = orariInizioPartite.get(giornataCorrente - 2);
                orarioFineUltimaGiornataCalendar.add(Calendar.DATE, 2);
                long orarioFineUltimaGiornata = orarioFineUltimaGiornataCalendar.getTime().getTime();
                if (lastUpdate < orarioFineUltimaGiornata && isUserTheAdminOfLeague) {
                    binding.updateLeaderboardButton.setVisibility(View.VISIBLE);
                    binding.updateLeaderboardButton.setEnabled(true);
                }

                for (int i = 1; i < classifica.size(); i++) {
                    HashMap<String, Object> element = classifica.get(i);
                    int totalPointsScored = (int) element.get("totalPointsScored");
                    LeaderboardElementLayout elementLayout;
                    if (leagueOn.get().getTipologia() == LegaType.FORMULA1) {
                        elementLayout = new LeaderboardElementLayout(context,
                                i, (String) element.get("teamName"), totalPointsScored);
                    } else {
                        int totalPointsAllowed = (int) element.get("totalPointsAllowed");
                        int pointsOfVictories = (int) element.get("pointsOfVictories");
                        elementLayout = new LeaderboardElementLayout(context,
                                i, (String) element.get("teamName"), totalPointsScored,
                                totalPointsAllowed, pointsOfVictories);
                    }
                    binding.leaderboard.addView(elementLayout.getLeaderboardElementLayout());
                }
            } else {
                binding.leaderboard.addView(getBaseTextView(context, R.string.not_started_yet));
            }

            if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                HashMap<String, List<Game>> calendario = leagueOn.get().getCalendario();

                if (calendario != null) {
                    binding.calendarioContainer.setVisibility(View.VISIBLE);

                    addToCalendarioViewAllRounds(context, calendario);
                }
            } else {
                binding.calendarShortcut.setVisibility(View.GONE);
            }
        } else {
            binding.leaderboard.addView(getBaseTextView(context, R.string.not_league_selected));
        }

        if (isUserTheAdminOfLeague && leagueOn.get().getLastRoundCalculated() < giornataCorrente - 1) {
            binding.updateLeaderboardButton.setOnClickListener(view -> {
                        for (int roundToCalculate = leagueOn.get().getLastRoundCalculated() + 1; roundToCalculate < giornataCorrente; roundToCalculate++) {

                            List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();

                            if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                                List<Game> gameListOfRoundToCalculate = leagueOn.get().getCalendario().get("giornata_" + roundToCalculate);
                                calcoloGiornataCalendario(roundToCalculate, gameListOfRoundToCalculate);
                                legheReference.child(legaSelezionata).child("calendario").child("giornata_" + roundToCalculate).setValue(gameListOfRoundToCalculate);

                                updateClassificaLegaCalendario(classifica, gameListOfRoundToCalculate);

                            } else {
                                updateClassificaLegaFormula1(classifica, roundToCalculate);
                            }

                            List<HashMap<String, Object>> orderClassifica = reorderClassificaFromLegaType(classifica, leagueOn.get().getTipologia());
                            legheReference.child(legaSelezionata).child("classifica").setValue(orderClassifica);
                            legheReference.child(legaSelezionata).child("lastRoundCalculated").setValue(roundToCalculate);
                        }
                    }
            );
        }
        return binding.getRoot();
    }

    @SuppressWarnings("ConstantConditions")
    private void addToCalendarioViewAllRounds(Context context, HashMap<String, List<Game>> calendario) {
        for (String key : calendario.keySet()) {
            TextView titleGiornata = new TextView(context);
            titleGiornata.setBackgroundColor(getResources().getColor(R.color.esteco, context.getTheme()));
            titleGiornata.setTextColor(Color.WHITE);
            titleGiornata.setText(key);
            binding.calendarioContainer.addView(titleGiornata);

            for (Game game : calendario.get(key)) {
                String gameString = game.homeUserId + " " + game.homePoints + " - " + game.awayPoints + " " + game.awayUserId;//TODO:da migliorare
                TextView gameTextView = new TextView(context);
                gameTextView.setText(gameString);
                binding.calendarioContainer.addView(gameTextView);
            }
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
            int totalPointsScored = (int) elementoClassifica.get("totalPointsScored");
            elementoClassifica.put("totalPointsScored", totalPointsScored + pointsScoredThisRound);
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
                        if ((int) hashMap.get("pointsOfVictories") > (int) max.get("pointsOfVictories") ||
                                ((int) hashMap.get("pointsOfVictories") == (int) max.get("pointsOfVictories") &&
                                        (int) hashMap.get("totalPointsScored") == (int) max.get("totalPointsScored"))) {
                            max = hashMap;
                        }
                    } else {
                        if ((int) hashMap.get("totalPointsScored") > (int) max.get("totalPointsScored")) {
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
            int totalPointsScored = (int) elementoClassifica.get("totalPointsScored");
            int totalPointsAllowed = (int) elementoClassifica.get("totalPointsAllowed");
            int pointsOfVictories = (int) elementoClassifica.get("pointsOfVictories");
            for (Game game : gameList) {
                if (elId.equals(game.homeUserId)) {
                    elementoClassifica.put("totalPointsScored", totalPointsScored + game.homePoints);
                    elementoClassifica.put("totalPointsAllowed", totalPointsAllowed + game.awayPoints);
                    if (game.homePoints > game.awayPoints) {
                        elementoClassifica.put("pointsOfVictories", pointsOfVictories + 2);
                    }
                    break;
                } else if (elId.equals(game.awayUserId)) {
                    elementoClassifica.put("totalPointsScored", totalPointsScored + game.awayPoints);
                    elementoClassifica.put("totalPointsAllowed", totalPointsAllowed + game.homePoints);
                    if (game.awayPoints > game.homePoints) {
                        elementoClassifica.put("pointsOfVictories", pointsOfVictories + 2);
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

    @SuppressWarnings("ConstantConditions")
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

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private int getPointsFromPlayerIdAndGiornata(String playerId, int giornata) {
        //TODO: forse da mettere un unico listener globale per le statistiche
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