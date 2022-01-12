package it.units.fantabasket.entities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

@SuppressLint("ViewConstructor")
public class PlayerOnFieldLayout extends LinearLayout {

    private final LinearLayout playerLayout;
    private final Button playerButton;
    private final TextView playerTextView;

    public PlayerOnFieldLayout(Context context, Player player) {
        super(context);

        playerLayout = new LinearLayout(context);
        playerLayout.setOrientation(LinearLayout.VERTICAL);
        playerLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        playerButton = PlayerLayoutHorizontal.getPlayerButton(context, player);

        playerTextView = new TextView(context);
        playerTextView.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        playerTextView.setText(player.id);
        playerTextView.setPadding(5, 0, 5, 0);

        playerLayout.addView(playerButton);
        playerLayout.addView(playerTextView);
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        super.setOnClickListener(l);
        playerLayout.setOnClickListener(l);
    }

    public TextView getPlayerTextView() {
        return playerTextView;
    }

    public Button getPlayerButton() {
        return playerButton;
    }

    public LinearLayout getPlayerLayout() {
        return playerLayout;
    }
}
