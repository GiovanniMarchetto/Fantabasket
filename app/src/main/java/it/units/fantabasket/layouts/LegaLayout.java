package it.units.fantabasket.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.utils.MyValueEventListener;

import static it.units.fantabasket.layouts.ExpandCollapseLayout.setExpandCollapseLayout;

public class LegaLayout {
    final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final LinearLayout legaHeaderLayout;
    private final LinearLayout legaParamsLayout;
    private final TextView admin;
    private final Button actionButton;

    @SuppressLint("SetTextI18n")
    public LegaLayout(Context context, Lega lega) {
        legaHeaderLayout = new LinearLayout(context);
        legaHeaderLayout.setOrientation(LinearLayout.VERTICAL);
        legaHeaderLayout.setLayoutParams(params);
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

        legaParamsLayout = new LinearLayout(context);
        legaParamsLayout.setOrientation(LinearLayout.VERTICAL);
        legaParamsLayout.setLayoutParams(params);
        legaParamsLayout.setPadding(paddingLati * 2, 0, paddingLati * 2, paddingLati);


        TextView location = new TextView(context);
        location.setText("Location: " + lega.getLocation());
        legaParamsLayout.addView(location);

        admin = new TextView(context);
        admin.setText("Admin: ");
        getAdminName(lega.getAdmin());
        legaParamsLayout.addView(admin);

        TextView tipologia = new TextView(context);
        tipologia.setText("Tipologia: " + lega.getTipologia().name());
        legaParamsLayout.addView(tipologia);

        TextView partecipanti = new TextView(context);
        partecipanti.setText("Partecipanti: " + lega.getPartecipanti().size() + "/" + lega.getNumPartecipanti());
        legaParamsLayout.addView(partecipanti);

        TextView started = new TextView(context);
        started.setText("Iniziata: " + lega.isStarted());
        legaParamsLayout.addView(started);

        actionButton = new Button(context);
        final String colorBlueOpaqueString = "#133A53";
        actionButton.setBackgroundColor(Color.parseColor(colorBlueOpaqueString));
        actionButton.setTextColor(Color.WHITE);
        legaParamsLayout.addView(actionButton);

        legaHeaderLayout.addView(legaParamsLayout);
        setExpandCollapseLayout(legaHeaderLayout, legaParamsLayout);
    }

    @SuppressLint("SetTextI18n")//TODO: remove this suppress lint
    private void getAdminName(String userId) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("nickname")
                .addListenerForSingleValueEvent((MyValueEventListener) dataSnapshot -> {
                    String name = dataSnapshot.getValue(String.class);
                    admin.setText("Admin: " + name);
                });
    }

    public LinearLayout getLegaHeaderLayout() {
        return legaHeaderLayout;
    }

    public Button getActionButton() {
        return actionButton;
    }
}
