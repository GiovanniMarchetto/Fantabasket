package it.units.fantabasket.layouts;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.utils.MyValueEventListener;

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


        legaParamsLayout.setVisibility(View.GONE);
        legaHeaderLayout.addView(legaParamsLayout);
        legaHeaderLayout.setOnClickListener(view -> {
            if (legaParamsLayout.getVisibility() == View.GONE) {
                expand();
            } else {
                collapse();
            }
        });
    }

    @SuppressLint("SetTextI18n")//TODO: remove this suppress lint
    private void getAdminName(String userId) {
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("name")
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

    private void expand() {
        legaParamsLayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        legaParamsLayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, legaParamsLayout.getMeasuredHeight());
        mAnimator.start();
    }

    private void collapse() {
        int finalHeight = legaParamsLayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                legaParamsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(valueAnimator -> {
            //Update Height
            int value = (Integer) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = legaParamsLayout.getLayoutParams();
            layoutParams.height = value;
            legaParamsLayout.setLayoutParams(layoutParams);
        });
        return animator;
    }
}
