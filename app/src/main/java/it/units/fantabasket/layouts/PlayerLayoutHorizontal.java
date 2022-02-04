package it.units.fantabasket.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import it.units.fantabasket.R;
import it.units.fantabasket.entities.Player;
import org.jetbrains.annotations.NotNull;

import static it.units.fantabasket.utils.Utils.LAYOUT_PARAMS;

@SuppressLint("ViewConstructor")
public class PlayerLayoutHorizontal extends LinearLayout {

    private final LinearLayout playerLayout;
    private final LinearLayout headerLayout;
    private final LinearLayout rightLinearLayout;
    private ImageButton moreInfoButton;

    public PlayerLayoutHorizontal(Context context, Player player) {
        super(context);

        headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable border = new GradientDrawable();
        border.setColor(context.getColor(R.color.listPlayerBackGround));
        border.setStroke(1, Color.BLACK);

        Button playerButton = getPlayerShirtWithNumberButton(context, player);

        TextView name = new TextView(context);
        name.setText(player.getId());

        rightLinearLayout = createAndGetRightLinearLayout(context, player);

        headerLayout.addView(playerButton);
        headerLayout.addView(name);
        headerLayout.addView(rightLinearLayout);

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(VERTICAL);
        playerLayout.setBackground(border);
        playerLayout.setPadding(30, 15, 15, 15);

        playerLayout.addView(headerLayout);

        LinearLayout infoPlayerLayout = getInfoLayout(context, player);
        playerLayout.addView(infoPlayerLayout);
        ExpandCollapseLayout.setExpandCollapseLayout(moreInfoButton, infoPlayerLayout);
    }

    @NotNull
    public static Button getPlayerShirtWithNumberButton(Context context, Player player) {
        final Button playerButton = new Button(context);
        playerButton.setText(player.getNumber());
        playerButton.setTextColor(player.getNumberColor());
        playerButton.setBackground(context.getDrawable(player.getShirt()));

        int pixels = getPixels(context, 58);
        //linear params: width and height
        LayoutParams linearParamsButton = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        linearParamsButton.width = pixels;
        linearParamsButton.height = pixels;
        playerButton.setLayoutParams(linearParamsButton);
        return playerButton;
    }

    private LinearLayout getInfoLayout(Context context, Player player) {
        LinearLayout infoLayout = new LinearLayout(context);
        infoLayout.setOrientation(VERTICAL);
        infoLayout.setLayoutParams(LAYOUT_PARAMS);

        TextView playerCompleteName = new TextView(context);
        String completeName = player.getName() + " " + player.getSurname();
        playerCompleteName.setText(completeName);

        TextView playerDateBirthAndNationality = new TextView(context);
        String dateBirthAndNationality = player.getDateBirth() + " - " + player.getNationality();
        playerDateBirthAndNationality.setText(dateBirthAndNationality);

        TextView playerMeasures = new TextView(context);
        String measures = player.getHeight() + " cm x " + player.getWeight() + " kg";
        playerMeasures.setText(measures);

        TextView playerTeam = new TextView(context);
        String team = String.valueOf(player.getTeam());
        playerTeam.setText(team);

        TextView playerCost = new TextView(context);
        String cost = "Cost: " + player.getCost() + " fm";
        playerCost.setText(cost);

        infoLayout.addView(playerCompleteName);
        infoLayout.addView(playerDateBirthAndNationality);
        infoLayout.addView(playerMeasures);
        infoLayout.addView(playerTeam);
        infoLayout.addView(playerCost);
        return infoLayout;
    }

    public static int getPixels(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    @NotNull
    private LinearLayout createAndGetRightLinearLayout(Context context, Player player) {
        LayoutParams subParam = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        LinearLayout subLayout = new LinearLayout(context);
        subLayout.setLayoutParams(subParam);
        subLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
//        subLayout.setOnClickListener(view -> playerLayout.cli);

        TextView role = new TextView(context);
        role.setPadding(15, 15, 15, 15);
        String roleText = player.getRole_1().name().toLowerCase();
        if (player.getRole_2() != null) {
            roleText = roleText + "/" + player.getRole_2().name().toLowerCase();
        }
        role.setText(roleText);
        subLayout.addView(role);

        moreInfoButton = new ImageButton(context);
        moreInfoButton.setImageResource(R.drawable.ic_info_24);
        subLayout.addView(moreInfoButton);

        return subLayout;
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
        headerLayout.setOnClickListener(l);
//        playerButton.setOnClickListener(l);
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }

    public LinearLayout getRightLinearLayout() {
        return rightLinearLayout;
    }
}
