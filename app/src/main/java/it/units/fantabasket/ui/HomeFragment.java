package it.units.fantabasket.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentHomeBinding;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.*;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String legaSelezionata;
    private ValueEventListener legaSelezionataListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userDataReference.child("teamName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                String teamName = dataSnapshot.getValue(String.class);
                binding.teamName.setText(teamName);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

        userDataReference.child("teamLogo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                String teamLogo = dataSnapshot.getValue(String.class);
                byte[] decodedString = Base64.decode(teamLogo, Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.teamLogo.setImageBitmap(decodedByte);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

        userDataReference.child("legaSelezionata").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                if (legaSelezionata != null && legaSelezionataListener != null) {
                    legheReference.child(legaSelezionata).removeEventListener(legaSelezionataListener);
                }

                legaSelezionata = dataSnapshot.getValue(String.class);
                if (binding == null) {//TODO: capire come fare a "riciclare" i fragment
                    Log.i("MIO", getId() + "---binding null");
                    binding = FragmentHomeBinding.inflate(inflater, container, false);
                } else {
                    Log.i("MIO", getId() + "---binding find");
                }
                if (legaSelezionata != null) {
                    binding.legaName.setText(legaSelezionata);

                    setLegaSelezionataValueEventListener();
                    legheReference.child(legaSelezionata).addValueEventListener(legaSelezionataListener);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.w("ERROR", "legaSelezionata:onCancelled", databaseError.toException());
            }
        });


        ActivityResultLauncher<Intent> teamLogoLoaderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                Utils.getActivityResultCallbackForChangeTeamLogo(requireContext().getContentResolver(), binding.teamLogo));

        //BUTTONS
        binding.modifyButton.setOnClickListener(view -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
            //save file in db
            userDataReference.child("teamLogo").setValue(Utils.teamLogoBase64);
        });

        binding.changeLegaButton.setOnClickListener(view ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_LegheFragment)
        );

        return root;
    }

    private void setLegaSelezionataValueEventListener() {
        legaSelezionataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                @SuppressWarnings("unchecked") HashMap<String, Object> legaParams = (HashMap<String, Object>) snapshot.getValue();
                assert legaParams != null;
                Lega lega = LegheFragment.getLegaFromHashMapParams(legaParams);
                binding.legaNameOption.setText(lega.getName());
                String start = lega.isStarted() ? "IN CORSO" : "IN ATTESA";
                binding.legaStartOption.setText(start);

                boolean isUserTheAdminOfLeague = lega.getAdmin().equals(user.getUid());
                boolean isNotStarted = !lega.isStarted();
                binding.startLeagueButton.setVisibility((isNotStarted && isUserTheAdminOfLeague) ? View.VISIBLE : View.GONE);

                boolean enableStart = isUserTheAdminOfLeague && isNotStarted &&
                        ((lega.getTipologia() == LegaType.CALENDARIO && lega.getPartecipanti().size() == lega.getNumPartecipanti())
                                || (lega.getTipologia() == LegaType.FORMULA1 && lega.getPartecipanti().size() > 1));
                binding.startLeagueButton.setEnabled(enableStart);

                if (enableStart) {
                    binding.startLeagueButton.setOnClickListener(view -> {
                        legheReference.child(legaSelezionata).child("started").setValue(true);
                        if (lega.getTipologia() == LegaType.CALENDARIO) {
                            HashMap<String, List<Pair<String, String>>> campionato = getCampionato(lega.getPartecipanti());
                            legheReference.child(legaSelezionata).child("calendario").setValue(campionato);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w("ERROR", "legaSelezionataIn leghe:onCancelled", error.toException());
            }
        };
    }

    private HashMap<String, List<Pair<String, String>>> getCampionato(List<String> players) {
        int allGiornate = 14;//TODO: totale squadre
        int coppieTotaliPerGiornata = (players.size() - players.size() % 2) / 2;// TODO: rendi possibile che con il calendario si possa fare solo in pari

        List<Pair<String, String>> allCouples = new ArrayList<>();

        for (int i = 0; i < players.size() - 1; i++) {
            for (int j = i + 1; j < players.size(); j++) {
                allCouples.add(new Pair<>(players.get(i), players.get(j)));
            }
        }

        HashMap<String, List<Pair<String, String>>> campionato = new HashMap<>();

        //andata
        for (int i = 1; i <= allGiornate / 2; i++) {
            int j = i + allGiornate / 2;
            List<Pair<String, String>> partiteAndata = new ArrayList<>(coppieTotaliPerGiornata);
            List<Pair<String, String>> partiteRitorno = new ArrayList<>(coppieTotaliPerGiornata);
            List<String> giocatoriPresentiNelTurno = new ArrayList<>();

            for (Pair<String, String> couple : allCouples) {
                if (!giocatoriPresentiNelTurno.contains(couple.first) && !giocatoriPresentiNelTurno.contains(couple.second)) {
                    giocatoriPresentiNelTurno.add(couple.first);
                    giocatoriPresentiNelTurno.add(couple.second);
                    partiteAndata.add(couple);
                    Pair<String, String> coupleReturn = new Pair<>(couple.second, couple.first);
                    partiteRitorno.add(coupleReturn);
                }
            }

            allCouples.removeAll(partiteAndata);

            if (Math.random() < 0.5) {
                campionato.put("giornata_" + i, partiteAndata);
                campionato.put("giornata_" + j, partiteRitorno);
            } else {
                campionato.put("giornata_" + i, partiteRitorno);
                campionato.put("giornata_" + j, partiteAndata);
            }
        }

//        for (String key:campionato.keySet()) {
//            Log.i("CAMPIONATO", key);
//            for (Pair<String, String> partita:campionato.get(key)) {
//                Log.i("CAMPIONATO", "               " + partita.first + " - " + partita.second);
//            }
//        }

        return campionato;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}