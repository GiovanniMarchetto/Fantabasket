package it.units.fantabasket.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

@SuppressLint("ViewConstructor")
public class PlayerLayoutHorizontal extends LinearLayout{

    private final LinearLayout playerLayout;
    private final Button playerButton;

    public PlayerLayoutHorizontal(Context context, Player player) {
        super(context);

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(LinearLayout.HORIZONTAL);
        playerLayout.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.LTGRAY);
        border.setStroke(1, Color.BLACK);

        playerLayout.setBackground(border);
        playerLayout.setPadding(30,15,15,15);

        playerButton = new Button(context);
        playerButton.setText(player.number);
        playerButton.setTextColor(player.colorNumber);
        playerButton.setBackground(context.getDrawable(player.shirt));

        int pixels = getPixelsOfShirts(context);
        //linear params: width and height
        LinearLayout.LayoutParams linearParamsButton = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        linearParamsButton.width = pixels;
        linearParamsButton.height = pixels;
        playerButton.setLayoutParams(linearParamsButton);

        TextView name = new TextView(context);
        name.setText(player.name);

        LinearLayout.LayoutParams emptyParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LinearLayout subLayout = new LinearLayout(context);
        subLayout.setLayoutParams(emptyParam);
        subLayout.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);

        TextView role = new TextView(context);
        role.setText(player.role.name());
        subLayout.addView(role);

        playerLayout.addView(playerButton);
        playerLayout.addView(name);
        playerLayout.addView(subLayout);
    }

    private int getPixelsOfShirts(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int dp = 58;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
        playerButton.setOnClickListener(l);
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }
}
