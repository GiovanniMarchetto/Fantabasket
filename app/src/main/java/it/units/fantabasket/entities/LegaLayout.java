package it.units.fantabasket.entities;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LegaLayout {
    final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private final LinearLayout legaHeaderLayout;
    private final LinearLayout legaLayout;

    @SuppressLint("SetTextI18n")
    public LegaLayout(Context context, Lega lega) {
        legaHeaderLayout = new LinearLayout(context);
        legaHeaderLayout.setOrientation(LinearLayout.HORIZONTAL);
        legaHeaderLayout.setLayoutParams(params);

        TextView name = new TextView(context);
        name.setText(lega.getName());
        legaHeaderLayout.addView(name);

        legaLayout = new LinearLayout(context);
        legaLayout.setOrientation(LinearLayout.VERTICAL);
        legaLayout.setLayoutParams(params);
        legaLayout.setPadding(5, 0, 5, 0);


        TextView location = new TextView(context);
        location.setText("Location: " + lega.getLocation());
        legaLayout.addView(location);

        TextView admin = new TextView(context);
        admin.setText("Admin: " + lega.getAdmin());
        legaLayout.addView(admin);

        TextView tipologia = new TextView(context);
        tipologia.setText("Tipologia: " + lega.getLegaType().name());
        legaLayout.addView(tipologia);

        TextView partecipanti = new TextView(context);
        partecipanti.setText("Partecipanti: " + lega.getPartecipanti().size() + "/" + lega.getNumPartecipanti());
        legaLayout.addView(partecipanti);

        TextView started = new TextView(context);
        started.setText("Iniziata: " + lega.isStarted());
        legaLayout.addView(started);


        legaLayout.setVisibility(View.GONE);
        legaHeaderLayout.setOnClickListener(view -> {
            if (legaLayout.getVisibility() == View.GONE) {
                expand();
            } else {
                collapse();
            }
        });
    }

    public LinearLayout getLegaHeaderLayout() {
        return legaHeaderLayout;
    }

    public LinearLayout getLegaLayout() {
        return legaLayout;
    }

    private void expand() {
        legaLayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        legaLayout.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, legaLayout.getMeasuredHeight());
        mAnimator.start();
    }

    private void collapse() {
        int finalHeight = legaLayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                legaLayout.setVisibility(View.GONE);
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
            ViewGroup.LayoutParams layoutParams = legaLayout.getLayoutParams();
            layoutParams.height = value;
            legaLayout.setLayoutParams(layoutParams);
        });
        return animator;
    }
}
