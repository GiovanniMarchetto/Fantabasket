package it.units.fantabasket.layouts;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LeaderboardElementLayout {

    private final LinearLayout leaderboardElementLayout;

    public LeaderboardElementLayout(Context context, int position, String teamName, int totalPointsScored) {
        this.leaderboardElementLayout = new LinearLayout(context);
        leaderboardElementLayout.setOrientation(LinearLayout.HORIZONTAL);
        leaderboardElementLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView positionTextView = new TextView(context);
        positionTextView.setText(String.valueOf(position));

        TextView teamNameTextView = new TextView(context);
        teamNameTextView.setText(teamName);

        TextView totalPointsTextView = new TextView(context);
        totalPointsTextView.setText(String.valueOf(totalPointsScored));

        leaderboardElementLayout.addView(positionTextView);
        leaderboardElementLayout.addView(teamNameTextView);
        leaderboardElementLayout.addView(totalPointsTextView);
    }

    public LeaderboardElementLayout(Context context, int position, String teamName, int totalPointsScored,
                                    int totalPointsAllowed, int points) {
        this(context, position, teamName, totalPointsScored);

        TextView totalPointsAllowedTextView = new TextView(context);
        totalPointsAllowedTextView.setText(String.valueOf(totalPointsAllowed));

        TextView pointsTextView = new TextView(context);
        pointsTextView.setText(String.valueOf(points));

        leaderboardElementLayout.addView(totalPointsAllowedTextView);
        leaderboardElementLayout.addView(pointsTextView);
    }

    public LinearLayout getLeaderboardElementLayout() {
        return leaderboardElementLayout;
    }
}
