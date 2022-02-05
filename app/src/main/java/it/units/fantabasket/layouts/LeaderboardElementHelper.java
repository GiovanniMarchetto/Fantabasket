package it.units.fantabasket.layouts;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardElementHelper {

    private final List<TextView> leaderboardElementList;

    public LeaderboardElementHelper(Context context, int position, String teamName, int totalPointsScored) {
        this.leaderboardElementList = new ArrayList<>();

        TextView positionTextView = new TextView(context);
        final String pos = position + "º";
        positionTextView.setText(pos);
        positionTextView.setTypeface(Typeface.DEFAULT_BOLD);

        TextView teamNameTextView = new TextView(context);
        teamNameTextView.setText(teamName);

        TextView totalPointsTextView = new TextView(context);
        totalPointsTextView.setText(String.valueOf(totalPointsScored));

        leaderboardElementList.add(0, positionTextView);
        leaderboardElementList.add(1, teamNameTextView);
        leaderboardElementList.add(2, totalPointsTextView);
    }

    public LeaderboardElementHelper(Context context, int position, String teamName, int totalPointsScored,
                                    int totalPointsAllowed, int pointsOfVictories) {
        this(context, position, teamName, totalPointsScored);

        TextView totalPointsAllowedTextView = new TextView(context);
        totalPointsAllowedTextView.setText(String.valueOf(totalPointsAllowed));

        TextView pointsOfVictoriesTextView = new TextView(context);
        pointsOfVictoriesTextView.setText(String.valueOf(pointsOfVictories));

        leaderboardElementList.add(3, totalPointsAllowedTextView);
        leaderboardElementList.add(4, pointsOfVictoriesTextView);
    }

    public List<TextView> getLeaderboardElementList() {
        return leaderboardElementList;
    }
}
