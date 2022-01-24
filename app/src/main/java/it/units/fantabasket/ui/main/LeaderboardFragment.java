package it.units.fantabasket.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentLeaderboardBinding;
import it.units.fantabasket.layouts.LeaderboardElementLayout;
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.legheReference;
import static it.units.fantabasket.ui.main.HomeFragment.legaSelezionata;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);

        //TODO: se la lega non è a calendario bisogna nascondere la sezione calendario
        if (legaSelezionata != null) {
            legheReference.child(legaSelezionata).child("classifica").addValueEventListener(
                    (MyValueEventListener) snapshot -> {
                        List<HashMap<String, Object>> classifica = (List<HashMap<String, Object>>) snapshot.getValue();
                        //TODO: differenziare se è lega con calendario o no
                        Context context = getContext();
                        for (int i = 0; i < classifica.size(); i++) {
                            HashMap<String, Object> element = classifica.get(i);
                            int position = i + 1;
                            int points = (int) element.get("points");
                            LeaderboardElementLayout elementLayout = new LeaderboardElementLayout(context,
                                    position, (String) element.get("teamName"), points);
                            binding.leaderboard.addView(elementLayout.getLeaderboardElementLayout());
                        }
                    }
            );
        }

        return binding.getRoot();
    }
}