package it.units.fantabasket.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import it.units.fantabasket.entities.Lega;

import static it.units.fantabasket.layouts.ExpandCollapseLayout.setExpandCollapseLayout;
import static it.units.fantabasket.utils.Utils.LAYOUT_PARAMS;

public class LegaLayout {
    private final LinearLayout legaHeaderLayout;
    private final Button actionButton;

    public LegaLayout(Context context, Lega lega) {
        legaHeaderLayout = new LinearLayout(context);
        legaHeaderLayout.setOrientation(LinearLayout.VERTICAL);
        legaHeaderLayout.setLayoutParams(LAYOUT_PARAMS);
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
        legaParamsLayout.setLayoutParams(LAYOUT_PARAMS);
        legaParamsLayout.setPadding(paddingLati * 2, 0, paddingLati * 2, paddingLati);

        addLegaParamsAtView(context, lega, legaParamsLayout);

        actionButton = new Button(context);
        final String colorBlueOpaqueString = "#133A53";
        actionButton.setBackgroundColor(Color.parseColor(colorBlueOpaqueString));
        actionButton.setTextColor(Color.WHITE);
        legaParamsLayout.addView(actionButton);

        legaHeaderLayout.addView(legaParamsLayout);
        setExpandCollapseLayout(legaHeaderLayout, legaParamsLayout);
    }

    public static void addLegaParamsAtView(Context context, Lega lega, LinearLayout viewContainer) {
        viewContainer.addView(getLeagueParamLayout(context, "Location", lega.getLocation()));
        viewContainer.addView(getLeagueParamLayout(context, "Type", lega.getTipologia().name()));

        String numPartecipanti = lega.getPartecipanti().size() + "/" + lega.getNumPartecipanti();
        viewContainer.addView(getLeagueParamLayout(context, "Partecipanti", numPartecipanti));
        viewContainer.addView(getLeagueParamLayout(context, "Started", String.valueOf(lega.isStarted())));

        String giornataInizio = lega.getGiornataInizio() != 0 ? String.valueOf(lega.getGiornataInizio()) : "not set";
        viewContainer.addView(getLeagueParamLayout(context, "Giornata d'inizio", giornataInizio));
    }

    public static View getLeagueParamLayout(Context context, String nameOfParam, String param) {
        LinearLayout paramLayout = new LinearLayout(context);
        paramLayout.setOrientation(LinearLayout.HORIZONTAL);
        paramLayout.setLayoutParams(LAYOUT_PARAMS);
        paramLayout.setWeightSum(2);

        TextView nameOfParamTextView = new TextView(context);
        nameOfParamTextView.setText(nameOfParam);

        TextView paramTextView = new TextView(context);
        paramTextView.setText(param);
        paramTextView.setMaxLines(1);

        LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
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
