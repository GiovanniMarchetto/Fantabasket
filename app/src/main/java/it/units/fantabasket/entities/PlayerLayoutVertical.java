package it.units.fantabasket.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

@SuppressLint("ViewConstructor")
public class PlayerLayoutVertical extends LinearLayout {

    private final LinearLayout playerLayout;
    private final Button playerButton;

    public PlayerLayoutVertical(Context context, Player player) {
        super(context);
        int pixels = getPixelsOfShirts(context);
        playerLayout = new LinearLayout(context);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setGravity(Gravity.CENTER_HORIZONTAL);
        playerLayout.setLayoutParams(params);

        playerLayout.setOrientation(LinearLayout.VERTICAL);

        playerButton = new Button(context);
        playerButton.setText(player.number);
        playerButton.setBackground(context.getDrawable(player.shirt));

        //linear params: width and height
        LinearLayout.LayoutParams linearParamsButton = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        linearParamsButton.gravity = Gravity.CENTER_HORIZONTAL;
        linearParamsButton.width = pixels;
        linearParamsButton.height = pixels;
//        linearParamsButton.leftMargin = pixels;
        playerButton.setLayoutParams(linearParamsButton);
        playerButton.setBackgroundColor(Color.CYAN);//TODO: remove

//        LinearLayout.LayoutParams linearParamsText = new LinearLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        linearParamsText.gravity = Gravity.CENTER_HORIZONTAL;
//        linearParamsText.bottomMargin = pixels/2;
//        linearParamsText.leftMargin = pixels;

        TextView name = new TextView(context);
//        name.setLayoutParams(linearParamsText);
        name.setBackgroundColor(Color.DKGRAY);//TODO: remove
        name.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        name.setText(player.name);

        playerLayout.addView(playerButton);
        playerLayout.addView(name);
    }

    private int getPixelsOfShirts(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int dp = 58;
        return (int) (dp * scale + 0.5f);
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
