package it.units.fantabasket.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentHomeBinding;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.enums.Team;
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
                Utils.getActivityResultCallbackForChangeTeamLogoAndSaveIfSpecified(requireContext().getContentResolver(), binding.teamLogo, true));

        //BUTTONS
        binding.changeLogoButton.setOnClickListener(view -> {
            Intent teamLogoIntent = new Intent();
            teamLogoIntent.setType("image/*");
            teamLogoIntent.setAction(Intent.ACTION_GET_CONTENT);
            teamLogoLoaderLauncher.launch(teamLogoIntent);
        });

        binding.changeLegaButton.setOnClickListener(view ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_LegheFragment)
        );

        return root;
    }

    private void setLegaSelezionataValueEventListener() {
        legaSelezionataListener = new ValueEventListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                @SuppressWarnings("unchecked") HashMap<String, Object> legaParams = (HashMap<String, Object>) snapshot.getValue();
                assert legaParams != null;
                Lega lega = LegheFragment.getLegaFromHashMapParams(legaParams);
                binding.legaNameOption.setText(lega.getName());
                String start = lega.isStarted() ? "IN CORSO" : "IN ATTESA";
                binding.legaStartOption.setText(start);

                boolean isUserTheAdminOfLeague = lega.getAdmin().equals(user.getUid());
                if (lega.isStarted()) {
                    if (lega.getTipologia() == LegaType.CALENDARIO) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, List<HashMap<String, String>>> calendario =
                                (HashMap<String, List<HashMap<String, String>>>) legaParams.get("calendario");
                        List<HashMap<String, String>> partiteDellaGiornata = calendario.get("giornata_" + giornataCorrente);
                        for (HashMap<String, String> partita : partiteDellaGiornata) {

                            String homeUserId = partita.get("homeUserId");
                            String awayUserId = partita.get("awayUserId");

                            if (homeUserId.equals(user.getUid()) || awayUserId.equals(user.getUid())) {

                                Game userGame = new Game(homeUserId, awayUserId);

                                FirebaseDatabase.getInstance().getReference("users").child(userGame.homeUserId).addValueEventListener(
                                        getPlayerListener(binding.logoHome, binding.teamHome));

                                FirebaseDatabase.getInstance().getReference("users").child(userGame.awayUserId).addValueEventListener(
                                        getPlayerListener(binding.logoAway, binding.teamAway));
                                break;
                            }
                        }


                    } else {
//TODO: scegli cosa mostrare nel caso di modalità formula1
                        Log.i("MIO", "modalità formula 1");
                    }

                    binding.nextGameLayout.setVisibility((lega.getTipologia() == LegaType.CALENDARIO) ? View.VISIBLE : View.GONE);
//                    binding.myTeamLayout.setVisibility((lega.getTipologia() == LegaType.FORMULA1) ?View.VISIBLE  : View.GONE);
                } else {
                    binding.startLeagueButton.setVisibility(isUserTheAdminOfLeague ? View.VISIBLE : View.GONE);

                    boolean enableStart = isUserTheAdminOfLeague &&
                            (
                                    (lega.getTipologia() == LegaType.CALENDARIO && lega.getPartecipanti().size() == lega.getNumPartecipanti())
                                            || (lega.getTipologia() == LegaType.FORMULA1 && lega.getPartecipanti().size() > 1)
                            );
                    binding.startLeagueButton.setEnabled(enableStart);

                    if (enableStart) {
                        binding.startLeagueButton.setOnClickListener(view -> {
                            legheReference.child(legaSelezionata).child("started").setValue(true);
                            if (lega.getTipologia() == LegaType.CALENDARIO) {
                                HashMap<String, List<Game>> campionato = getCampionato(lega.getPartecipanti());
                                legheReference.child(legaSelezionata).child("calendario").setValue(campionato);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.w("ERROR", "legaSelezionataIn leghe:onCancelled", error.toException());
            }
        };
    }

    @NotNull
    private ValueEventListener getPlayerListener(ImageView imageView, TextView textView) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                @SuppressWarnings("unchecked") HashMap<String, Object> user = (HashMap<String, Object>) snapshot.getValue();
                assert user != null;
                byte[] decodedString = Base64.decode((String) user.get("teamLogo"), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
                textView.setText((String) user.get("teamName"));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };
    }

    private HashMap<String, List<Game>> getCampionato(List<String> players) {
        int allGiornate = (Team.values().length - 1) * 2;
        int coppieTotaliPerGiornata = (players.size() - players.size() % 2) / 2;// TODO: rendi possibile che con il calendario si possa fare solo in pari

        List<Game> allCouples = new ArrayList<>();

        for (int i = 0; i < players.size() - 1; i++) {
            for (int j = i + 1; j < players.size(); j++) {
                allCouples.add(new Game(players.get(i), players.get(j)));
            }
        }

        HashMap<String, List<Game>> campionato = new HashMap<>();

        for (int i = 1; i <= allGiornate / 2; i++) {
            int j = i + allGiornate / 2;
            List<Game> partiteAndata = new ArrayList<>(coppieTotaliPerGiornata);
            List<Game> partiteRitorno = new ArrayList<>(coppieTotaliPerGiornata);
            List<String> giocatoriPresentiNelTurno = new ArrayList<>();

            for (Game couple : allCouples) {
                if (!giocatoriPresentiNelTurno.contains(couple.homeUserId) && !giocatoriPresentiNelTurno.contains(couple.awayUserId)) {
                    giocatoriPresentiNelTurno.add(couple.homeUserId);
                    giocatoriPresentiNelTurno.add(couple.awayUserId);
                    partiteAndata.add(couple);
                    Game coupleReturn = new Game(couple.awayUserId, couple.homeUserId);
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
//            for (Game partita:campionato.get(key)) {
//                Log.i("CAMPIONATO", "               " + partita.homeUserId + " - " + partita.awayUserId);
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