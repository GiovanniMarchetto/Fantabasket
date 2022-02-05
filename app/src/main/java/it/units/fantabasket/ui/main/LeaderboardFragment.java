package it.units.fantabasket.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentLeaderboardBinding;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.layouts.ExpandCollapseLayout;
import it.units.fantabasket.layouts.LeaderboardElementHelper;
import it.units.fantabasket.utils.AssetDecoderUtil;
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

        if (legaSelezionata != null && leagueOn != null
                && leagueOn.get() != null && membersLeagueOn.size() == leagueOn.get().getNumPartecipanti()) {
            showClassifica(context);

            ExpandCollapseLayout.setExpandCollapseLayout(binding.calendarShortcut, binding.calendarioContainer);

            binding.calendarShortcut.setVisibility((isLeagueOnCalendarioType) ? View.VISIBLE : View.GONE);

            if (isLeagueOnCalendarioType) {
                loadCalendario(context);
            }
        } else {
            binding.leaderboardContainer.addView(getBaseTextView(context, R.string.not_league_selected));
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
        }
        binding.updateLeaderboardButton.setEnabled(leagueOn.get().isStarted() && isEnable
                && playersStatistics != null);
    }

    private void showClassifica(Context context) {
        List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();


        if (classifica != null) {
            binding.leaderboardContainer.addView(getLeaderboardGrid(classifica));
        } else {
            binding.leaderboardContainer.addView(getBaseTextView(context, R.string.not_started_yet));
        }
    }

    private GridLayout getLeaderboardGrid(List<HashMap<String, Object>> classifica) {
        final Context context = getContext();
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        final int columnCount = isLeagueOnCalendarioType ? 5 : 3;
        gridLayout.setColumnCount(columnCount);
        final int rowCount = classifica.size() + 1;
        gridLayout.setRowCount(rowCount);

        String[] infoLeaderboard = {"   ", "Team name", "scored", "allowed", "win pt"};
        for (int c = 0; c < columnCount; c++) {
            TextView titleColumn = new TextView(context);
            titleColumn.setTypeface(Typeface.DEFAULT_BOLD);
            titleColumn.setText(infoLeaderboard[c]);

            setGridParams(gridLayout, c, 0, titleColumn);
        }

        for (int r = 1; r < rowCount; r++) {
            List<TextView> elementLeaderboard = getLeaderboardElement(context, r, classifica.get(r - 1));

            for (int c = 0; c < columnCount; c++) {
                TextView element = elementLeaderboard.get(c);
                setGridParams(gridLayout, c, r, element);
            }
        }
        return gridLayout;
    }

    private void setGridParams(GridLayout gridLayout, int col, int row, TextView textView) {
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        gridLayout.addView(textView);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = GridLayout.LayoutParams.WRAP_CONTENT;
        param.width = GridLayout.LayoutParams.WRAP_CONTENT;
        param.setMargins(5, 5, 5, 0);
        param.setGravity(Gravity.CENTER);
        param.columnSpec = GridLayout.spec(col, GridLayout.FILL, 1f);
        param.rowSpec = GridLayout.spec(row);
        textView.setLayoutParams(param);
    }

    @NotNull
    private List<TextView> getLeaderboardElement(
            Context context, int position, HashMap<String, Object> element) {

        int totalPointsScored = (int) element.get(TOTAL_POINTS_SCORED);
        final String teamName = (String) element.get(TEAM_NAME);

        LeaderboardElementHelper elementLayout;
        if (isLeagueOnCalendarioType) {
            int totalPointsAllowed = (int) element.get(TOTAL_POINTS_ALLOWED);
            int pointsOfVictories = (int) element.get(POINTS_OF_VICTORIES);

            elementLayout = new LeaderboardElementHelper(context,
                    position, teamName, totalPointsScored,
                    totalPointsAllowed, pointsOfVictories);
        } else {
            elementLayout = new LeaderboardElementHelper(context,
                    position, teamName, totalPointsScored);
        }
        return elementLayout.getLeaderboardElementList();
    }

    private void loadCalendario(Context context) {
        HashMap<String, List<Game>> calendario = leagueOn.get().getCalendario();

        if (calendario != null) {
            addToCalendarioViewAllRounds(context, calendario);
        }
    }


    private void addToCalendarioViewAllRounds(Context context, HashMap<String, List<Game>> calendario) {
        for (int indexRound = 1; indexRound <= calendario.size(); indexRound++) {
            TextView titleGiornata = new TextView(context);
            titleGiornata.setBackgroundColor(getResources().getColor(R.color.deepGrey, context.getTheme()));
            titleGiornata.setTextColor(Color.WHITE);
            titleGiornata.setPadding(15, 0, 0, 0);
            final String roundDescription = context.getString(R.string.round) + " " + indexRound;
            titleGiornata.setText(roundDescription);
            binding.calendarioContainer.addView(titleGiornata);

            for (Game game : calendario.get(GIORNATA_ + indexRound)) {
                final String homeUserId = membersLeagueOn.get(game.homeUserId).teamName;
                final String awayUserId = membersLeagueOn.get(game.awayUserId).teamName;
                String gameString = homeUserId + " " + game.homePoints + " - " + game.awayPoints + " " + awayUserId;
                TextView gameTextView = new TextView(context);
                gameTextView.setText(gameString);
                gameTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                binding.calendarioContainer.addView(gameTextView);
            }
        }
    }

    private void calcolaGiornateDaUnCertoRound(int firstRoundToCalculate) {
        List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();

        for (int roundToCalculate = firstRoundToCalculate; roundToCalculate < AssetDecoderUtil.currentRound; roundToCalculate++) {

            if (isLeagueOnCalendarioType) {
                List<Game> gameListOfRoundToCalculate = leagueOn.get().getCalendario().get(GIORNATA_ + roundToCalculate);
                calcoloGiornataCalendario(roundToCalculate, gameListOfRoundToCalculate);
                legheReference.child(legaSelezionata).child(CALENDARIO).child(GIORNATA_ + roundToCalculate).setValue(gameListOfRoundToCalculate);

                updateClassificaLegaCalendario(classifica, gameListOfRoundToCalculate);

            } else {
                updateClassificaLegaFormula1(classifica, roundToCalculate);
            }
        }

        List<HashMap<String, Object>> orderClassifica =
                reorderClassificaFromLegaType(classifica);

        legheReference.child(legaSelezionata).child(CLASSIFICA).setValue(orderClassifica);
        legheReference.child(legaSelezionata).child(LAST_ROUND_CALCULATED).setValue(AssetDecoderUtil.currentRound - 1);
    }

    private TextView getBaseTextView(Context context, int resIdString) {
        TextView textView = new TextView(context);
        textView.setBackgroundColor(Color.LTGRAY);
        textView.setTextColor(Color.DKGRAY);
        textView.setPadding(10, 0, 0, 0);
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
    private List<HashMap<String, Object>> reorderClassificaFromLegaType(List<HashMap<String, Object>> classifica) {
        final int classificaSize = classifica.size();
        List<HashMap<String, Object>> classificaUpdate = new ArrayList<>(classificaSize);

        for (int i = 0; i < classificaSize; i++) {
            HashMap<String, Object> moreHighMember = classifica.get(0);
            for (HashMap<String, Object> hashMap : classifica) {
                if (!classificaUpdate.contains(hashMap) && isMoreHigherInLeaderboard(moreHighMember, hashMap)) {
                    moreHighMember = hashMap;
                }
            }
            classificaUpdate.add(moreHighMember);
            classifica.remove(moreHighMember);
        }
        return classificaUpdate;
    }

    private boolean isMoreHigherInLeaderboard(HashMap<String, Object> moreHighMember, HashMap<String, Object> hashMap) {
        boolean isMoreHigherInLeaderboard;
        final boolean moreTotalPointsScored = (int) hashMap.get(TOTAL_POINTS_SCORED) > (int) moreHighMember.get(TOTAL_POINTS_SCORED);

        if (isLeagueOnCalendarioType) {
            final boolean moreWinPoints = (int) hashMap.get(POINTS_OF_VICTORIES) > (int) moreHighMember.get(POINTS_OF_VICTORIES);
            final boolean equalWinPoints = (int) hashMap.get(POINTS_OF_VICTORIES) == (int) moreHighMember.get(POINTS_OF_VICTORIES);
            final boolean equalTotalPointsScored = (int) hashMap.get(TOTAL_POINTS_SCORED) == (int) moreHighMember.get(TOTAL_POINTS_SCORED);
            final boolean lessTotalPointsAllowed = (int) hashMap.get(TOTAL_POINTS_ALLOWED) < (int) moreHighMember.get(TOTAL_POINTS_ALLOWED);

            isMoreHigherInLeaderboard = moreWinPoints || (equalWinPoints && moreTotalPointsScored)
                    || (equalWinPoints && equalTotalPointsScored && lessTotalPointsAllowed);
        } else {
            isMoreHigherInLeaderboard = moreTotalPointsScored;
        }
        return isMoreHigherInLeaderboard;
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
        final HashMap<String, HashMap<FieldPositions, String>> formazioniPerGiornata =
                membersLeagueOn.get(userId).formazioniPerGiornata;
        if (formazioniPerGiornata == null) {
            return 0;
        }

        HashMap<FieldPositions, String> formazione = formazioniPerGiornata.get(GIORNATA_ + giornata);
        int pointsScored = 0;

        if (formazione == null) {
            for (int oldRound = giornata - 1; oldRound > 0; oldRound--) {
                formazione = formazioniPerGiornata.get(GIORNATA_ + oldRound);
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
        int posizioneGiornataInLista = giornata - 1;
        HashMap<String, Object> playerStatistic = playersStatistics.get(playerId);

        int points = (int) (long) ((List<Long>) playerStatistic.get("points")).get(posizioneGiornataInLista);
        int fouls = (int) (long) ((List<Long>) playerStatistic.get("fouls")).get(posizioneGiornataInLista);
        int rebounds = (int) (long) ((List<Long>) playerStatistic.get("rebounds")).get(posizioneGiornataInLista);
        int recoverBalls = (int) (long) ((List<Long>) playerStatistic.get("recoverBalls")).get(posizioneGiornataInLista);
        int lostBalls = (int) (long) ((List<Long>) playerStatistic.get("lostBalls")).get(posizioneGiornataInLista);

        return points + rebounds + recoverBalls - fouls - lostBalls;
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