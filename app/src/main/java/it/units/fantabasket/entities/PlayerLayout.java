package it.units.fantabasket.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

@SuppressLint("ViewConstructor")
public class PlayerLayout extends LinearLayout {

    private final LinearLayout playerLayout;
    private final Button playerButton;

    public PlayerLayout(Context context, Player player) {
        super(context);

        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearParams.gravity = LinearLayout.TEXT_ALIGNMENT_CENTER;

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(LinearLayout.VERTICAL);

        playerButton = new Button(context);
        playerButton.setText(player.number);
        playerButton.setBackground(context.getDrawable(player.getShirt()));

        final float scale = context.getResources().getDisplayMetrics().density;
        int dp = 58;
        int pixels = (int) (dp * scale + 0.5f);
        linearParams.width = pixels;
        linearParams.height = pixels;
        playerButton.setLayoutParams(linearParams);

        TextView name = new TextView(context);
        name.setText(player.name);
//                name.setLayoutParams(linearParams);

        playerLayout.addView(playerButton);
        playerLayout.addView(name);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        playerButton.setOnClickListener(l);
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }
}
