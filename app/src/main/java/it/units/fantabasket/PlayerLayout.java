package it.units.fantabasket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import it.units.fantabasket.enums.Team;

@SuppressLint("ViewConstructor")
public class PlayerLayout extends LinearLayout {

    private final LinearLayout playerLayout;

    public PlayerLayout(Context context, Player player) {
        super(context);

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearParams.gravity = LinearLayout.TEXT_ALIGNMENT_CENTER;

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(context);
        button.setText(player.number);
        button.setBackground(getShirt(context, player.team));

        final float scale = context.getResources().getDisplayMetrics().density;
        int dp = 58;
        int pixels = (int) (dp * scale + 0.5f);
        linearParams.width = pixels;
        linearParams.height = pixels;
        button.setLayoutParams(linearParams);

        TextView name = new TextView(context);
        name.setText(player.name);
//                name.setLayoutParams(linearParams);

        playerLayout.addView(button);
        playerLayout.addView(name);
    }

    private Drawable getShirt(Context context, Team team) {
        int shirt;
        switch (team) {
            case ATHLETISMO:
                shirt=R.drawable.shirt_athletismo;
                break;
            case GORIZIANA:
                shirt=R.drawable.shirt_goriziana;
                break;
            case OLIMPIA:
                shirt=R.drawable.shirt_olimpia;
                break;
            case ROMANS:
                shirt=R.drawable.shirt2;
                break;
            default:
                shirt=R.drawable.shirt;
        }
        return context.getDrawable(shirt);
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }
}
