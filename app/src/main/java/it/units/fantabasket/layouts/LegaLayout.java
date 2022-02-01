package it.units.fantabasket.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import it.units.fantabasket.entities.Lega;

import static it.units.fantabasket.layouts.ExpandCollapseLayout.setExpandCollapseLayout;

public class LegaLayout {
    final ViewGroup.LayoutParams linearLayoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final LinearLayout legaHeaderLayout;
    private final Button actionButton;

    @SuppressLint("SetTextI18n")
    public LegaLayout(Context context, Lega lega) {
        legaHeaderLayout = new LinearLayout(context);
        legaHeaderLayout.setOrientation(LinearLayout.VERTICAL);
        legaHeaderLayout.setLayoutParams(linearLayoutParams);
        int paddingLati = 20;
        legaHeaderLayout.setPadding(paddingLati, 0, paddingLati, 0);
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.LTGRAY);
        border.setStroke(1, Color.BLACK);
        legaHeaderLayout.setBackground(border);

        TextView name = new TextView(context);
        name.setText(lega.getName());
        name.setTextSize(20);
        legaHeaderLayout.addView(name);

        LinearLayout legaParamsLayout = new LinearLayout(context);
        legaParamsLayout.setOrientation(LinearLayout.VERTICAL);
        legaParamsLayout.setLayoutParams(linearLayoutParams);
        legaParamsLayout.setPadding(paddingLati * 2, 0, paddingLati * 2, paddingLati);

        legaParamsLayout.addView(getLeagueParamLayout(context, "Location", lega.getLocation()));
        legaParamsLayout.addView(getLeagueParamLayout(context, "Type", lega.getTipologia().name()));
        String numPartecipanti = lega.getPartecipanti().size() + "/" + lega.getNumPartecipanti();
        legaParamsLayout.addView(getLeagueParamLayout(context, "Partecipanti", numPartecipanti));
        legaParamsLayout.addView(getLeagueParamLayout(context, "Started", String.valueOf(lega.isStarted())));

        actionButton = new Button(context);
        final String colorBlueOpaqueString = "#133A53";
        actionButton.setBackgroundColor(Color.parseColor(colorBlueOpaqueString));
        actionButton.setTextColor(Color.WHITE);
        legaParamsLayout.addView(actionButton);

        legaHeaderLayout.addView(legaParamsLayout);
        setExpandCollapseLayout(legaHeaderLayout, legaParamsLayout);
    }

    private View getLeagueParamLayout(Context context, String nameOfParam, String param) {
        LinearLayout paramLayout = new LinearLayout(context);
        paramLayout.setOrientation(LinearLayout.HORIZONTAL);
        paramLayout.setLayoutParams(linearLayoutParams);
        paramLayout.setWeightSum(2);

        TextView nameOfParamTextView = new TextView(context);
        nameOfParamTextView.setText(nameOfParam);

        TextView paramTextView = new TextView(context);
        paramTextView.setText(param);
        paramTextView.setMaxLines(1);

        LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        paramLayout.addView(nameOfParamTextView, textViewParam);
        paramLayout.addView(paramTextView, textViewParam);
        return paramLayout;
    }

    public LinearLayout getLegaHeaderLayout() {
        return legaHeaderLayout;
    }

    public Button getActionButton() {
        return actionButton;
    }
}
