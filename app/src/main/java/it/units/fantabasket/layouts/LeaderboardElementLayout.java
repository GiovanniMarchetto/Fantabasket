package it.units.fantabasket.layouts;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import static it.units.fantabasket.utils.Utils.LAYOUT_PARAMS;

public class LeaderboardElementLayout {

    private final LinearLayout leaderboardElementLayout;
    private final int paddingSmall = 5;

    public LeaderboardElementLayout(Context context, int position, String teamName, int totalPointsScored) {
        this.leaderboardElementLayout = new LinearLayout(context);
        leaderboardElementLayout.setOrientation(LinearLayout.HORIZONTAL);
        leaderboardElementLayout.setLayoutParams(LAYOUT_PARAMS);

        TextView positionTextView = new TextView(context);
        positionTextView.setText(String.valueOf(position));
        positionTextView.setPadding(paddingSmall, paddingSmall, paddingSmall, paddingSmall);

        TextView teamNameTextView = new TextView(context);
        teamNameTextView.setText(teamName);
        teamNameTextView.setPadding(paddingSmall, paddingSmall, paddingSmall, paddingSmall);

        TextView totalPointsTextView = new TextView(context);
        totalPointsTextView.setText(String.valueOf(totalPointsScored));
        totalPointsTextView.setPadding(paddingSmall, paddingSmall, paddingSmall, paddingSmall);

        leaderboardElementLayout.addView(positionTextView);
        leaderboardElementLayout.addView(teamNameTextView);
        leaderboardElementLayout.addView(totalPointsTextView);
    }

    public LeaderboardElementLayout(Context context, int position, String teamName, int totalPointsScored,
                                    int totalPointsAllowed, int pointsOfVictories) {
        this(context, position, teamName, totalPointsScored);

        TextView totalPointsAllowedTextView = new TextView(context);
        totalPointsAllowedTextView.setText(String.valueOf(totalPointsAllowed));
        totalPointsAllowedTextView.setPadding(paddingSmall, paddingSmall, paddingSmall, paddingSmall);

        TextView pointsOfVictoriesTextView = new TextView(context);
        pointsOfVictoriesTextView.setText(String.valueOf(pointsOfVictories));
        pointsOfVictoriesTextView.setPadding(paddingSmall, paddingSmall, paddingSmall, paddingSmall);

        leaderboardElementLayout.addView(totalPointsAllowedTextView);
        leaderboardElementLayout.addView(pointsOfVictoriesTextView);
    }

    public LinearLayout getLeaderboardElementLayout() {
        return leaderboardElementLayout;
    }
}
