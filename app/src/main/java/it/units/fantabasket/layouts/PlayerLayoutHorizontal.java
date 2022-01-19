package it.units.fantabasket.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import it.units.fantabasket.entities.Player;
import org.jetbrains.annotations.NotNull;

@SuppressLint("ViewConstructor")
public class PlayerLayoutHorizontal extends LinearLayout {

    private final LinearLayout playerLayout;

    public PlayerLayoutHorizontal(Context context, Player player) {
        super(context);

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(LinearLayout.HORIZONTAL);
        playerLayout.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.LTGRAY);
        border.setStroke(1, Color.BLACK);

        playerLayout.setBackground(border);
        playerLayout.setPadding(30, 15, 15, 15);

        Button playerButton = getPlayerButton(context, player);

        TextView name = new TextView(context);
        name.setText(player.getId());

        LinearLayout subLayout = getRightLinearLayout(context, player);

        playerLayout.addView(playerButton);
        playerLayout.addView(name);
        playerLayout.addView(subLayout);
    }

    @NotNull
    public static Button getPlayerButton(Context context, Player player) {
        final Button playerButton = new Button(context);
        playerButton.setText(player.getNumber());
        playerButton.setTextColor(player.getNumberColor());
        playerButton.setBackground(context.getDrawable(player.getShirt()));

        int pixels = getPixelsOfShirts(context);
        //linear params: width and height
        LayoutParams linearParamsButton = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        linearParamsButton.width = pixels;
        linearParamsButton.height = pixels;
        playerButton.setLayoutParams(linearParamsButton);
        return playerButton;
    }

    private static int getPixelsOfShirts(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int dp = 58;
        return (int) (dp * scale + 0.5f);
    }

    @SuppressLint("SetTextI18n")
    @NotNull
    private LinearLayout getRightLinearLayout(Context context, Player player) {
        LayoutParams subParam = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LinearLayout subLayout = new LinearLayout(context);
        subLayout.setLayoutParams(subParam);
        subLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
//        subLayout.setOnClickListener(view -> playerLayout.cli);

        TextView role = new TextView(context);
        String roleText = player.getRole_1().name();
        if (player.getRole_2() != null) {
            roleText = roleText + "/" + player.getRole_2().name();
        }
        role.setText(roleText);
        subLayout.addView(role);
        return subLayout;
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
        playerLayout.setOnClickListener(l);
//        playerButton.setOnClickListener(l);
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }
}