package it.units.fantabasket.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentLeaderboardBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.layouts.LeaderboardElementLayout;
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static it.units.fantabasket.MainActivity.*;
import static it.units.fantabasket.ui.main.HomeFragment.*;

public class LeaderboardFragment extends Fragment {

    private final int sevenDaysInMillisecond = 518400000;
    private FragmentLeaderboardBinding binding;
    private boolean betterUpdateLeaderboard = false;

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        Context context = getContext();

        //TODO: se la lega non è a calendario bisogna nascondere la sezione calendario
        if (legaSelezionata != null) {
            legheReference.child(legaSelezionata).child("classifica").addValueEventListener(
                    (MyValueEventListener) snapshot -> {
                        List<HashMap<String, Object>> classifica = (List<HashMap<String, Object>>) snapshot.getValue();
//TODO: il primo elemento della lista è l'ultimo aggiornamento, se è passato troppo allora si mostra il tasto per ricalcolare
                        if (classifica != null) {
                            long lastUpdate = (long) classifica.get(0).get("lastUpdate");
                            Calendar orarioFineUltimaGiornataCalendar = orariInizioPartite.get(giornataCorrente - 2);
                            orarioFineUltimaGiornataCalendar.add(Calendar.DATE, 2);
                            long orarioFineUltimaGiornata = orarioFineUltimaGiornataCalendar.getTime().getTime();
                            if (lastUpdate < orarioFineUltimaGiornata && isUserTheAdminOfLeague) {
                                binding.updateLeaderboardButton.setVisibility(View.VISIBLE);
                                binding.updateLeaderboardButton.setEnabled(true);
                            }

                            for (int i = 1; i < classifica.size(); i++) {
                                HashMap<String, Object> element = classifica.get(i);
                                int totalPointsScored = (int) element.get("totalPointsScored");
                                LeaderboardElementLayout elementLayout;
                                if (legaSelezionataType == LegaType.FORMULA1) {
                                    elementLayout = new LeaderboardElementLayout(context,
                                            i, (String) element.get("teamName"), totalPointsScored);
                                } else {
                                    int totalPointsAllowed = (int) element.get("totalPointsAllowed");
                                    int pointsOfVictories = (int) element.get("pointsOfVictories");
                                    elementLayout = new LeaderboardElementLayout(context,
                                            i, (String) element.get("teamName"), totalPointsScored,
                                            totalPointsAllowed, pointsOfVictories);
                                }
                                binding.leaderboard.addView(elementLayout.getLeaderboardElementLayout());
                            }
                        } else {
                            TextView textView = new TextView(context);
                            textView.setBackgroundColor(Color.LTGRAY);
                            textView.setText("Non è ancora iniziata la competizione");
                            binding.leaderboard.addView(textView);
                        }
                    }
            );
        } else {
            TextView textView = new TextView(context);
            textView.setBackgroundColor(Color.LTGRAY);
            textView.setText("Non è selezionata alcuna lega");
            binding.leaderboard.addView(textView);
        }

        binding.updateLeaderboardButton.setOnClickListener(view -> {
                    AtomicReference<Lega> lega = null;
                    legheReference.child(legaSelezionata).child("giornataInizio").addListenerForSingleValueEvent(
                            (MyValueEventListener) snapshot -> {
                                int lastRoundCalculated = snapshot.getValue(Integer.class);

                                if (legaSelezionataType == LegaType.CALENDARIO) {
//TODO:prendere l'ultima giornata calcolata e da quello poi devo calcolare tutte le nuove giornate
                                } else {
//TODO: devo calcolare solo i punti e aggiungerli
                                }
                            }
                    );

                }
        );

        return binding.getRoot();
    }
}