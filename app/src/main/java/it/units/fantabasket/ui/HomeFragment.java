package it.units.fantabasket.ui;

import android.annotation.SuppressLint;
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
import it.units.fantabasket.enums.FieldPositions;
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
            @SuppressLint("SetTextI18n")
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
                        HashMap<String, List<HashMap<String, Object>>> calendario =
                                (HashMap<String, List<HashMap<String, Object>>>) legaParams.get("calendario");
                        List<HashMap<String, Object>> partiteDellaGiornata = calendario.get("giornata_" + giornataCorrente);
                        for (HashMap<String, Object> partita : partiteDellaGiornata) {

                            String homeUserId = (String) partita.get("homeUserId");
                            String awayUserId = (String) partita.get("awayUserId");

                            if (homeUserId.equals(user.getUid()) || awayUserId.equals(user.getUid())) {

                                Game userGame = new Game(homeUserId, awayUserId);

                                FirebaseDatabase.getInstance().getReference("users").child(userGame.homeUserId).addValueEventListener(
                                        getPlayerListener(binding.logoHome, binding.teamHome));

                                FirebaseDatabase.getInstance().getReference("users").child(userGame.awayUserId).addValueEventListener(
                                        getPlayerListener(binding.logoAway, binding.teamAway));
                                break;
                            }
                        }

                        if (isUserTheAdminOfLeague && giornataCorrente > 1) {
                            binding.calcoloGiornataButton.setOnClickListener(view -> {
                                int giornataPrecedente = giornataCorrente - 1;
                                List<Game> partiteDellaGiornataPrecedente = getGamesFromHashmap(calendario.get("giornata_" + giornataPrecedente));
                                calcoloGiornata(giornataPrecedente, partiteDellaGiornataPrecedente);
                                legheReference.child(legaSelezionata).child("calendario")
                                        .child("giornata_" + giornataPrecedente).setValue(partiteDellaGiornataPrecedente);
                            });
                            binding.calcoloGiornataButton.setVisibility(View.VISIBLE);
                            binding.calcoloGiornataButton.setEnabled(true);
                        }

                    } else {
                        //TODO: add classifica
                        if (isUserTheAdminOfLeague && giornataCorrente > 1) {
                            binding.calcoloGiornataButton.setOnClickListener(view -> {
                                int giornataPrecedente = giornataCorrente - 1;
                                List<HashMap<String, Object>> classifica = (List<HashMap<String, Object>>) legaParams.get("classifica");
                                List<HashMap<String, Object>> classificaUpdate = new ArrayList<>(classifica.size());
                                for (HashMap<String, Object> hashMap : classifica) {
                                    int pointsOfUser = getPointsFromPlayerIdAndGiornata((String) hashMap.get("userId"), giornataPrecedente)
                                            + (int) hashMap.get("points");
                                    hashMap.put("points", pointsOfUser);
                                }
                                //no stream no party...// also sort not supported
                                for (int i = 0; i < classifica.size(); i++) {
                                    HashMap<String, Object> max = classifica.get(0);
                                    for (HashMap<String, Object> hashMap : classifica) {
                                        if (!classificaUpdate.contains(hashMap) &&
                                                (int) hashMap.get("points") > (int) max.get("points")) {
                                            max = hashMap;
                                        }
                                    }
                                    classificaUpdate.add(max);
                                    classifica.remove(max);
                                }
                                legheReference.child(legaSelezionata).child("classifica").setValue(classificaUpdate);

                            });
                            binding.calcoloGiornataButton.setVisibility(View.VISIBLE);
                            binding.calcoloGiornataButton.setEnabled(true);
                        }

                        List<HashMap<String, Object>> classifica = (List<HashMap<String, Object>>) legaParams.get("classifica");
                        int punteggioAttuale = 0;
                        int posizione = 0;
                        //TODO: refactoring
                        for (HashMap<String, Object> hashMap : classifica) {
                            if (hashMap.get("userId").equals(user.getUid())) {
                                posizione = classifica.indexOf(hashMap) + 1;
                                punteggioAttuale = (int) hashMap.get("points");
                            }
                        }

                        binding.posizioneInClassifica.setText(posizione + "º");
                        binding.totalePunti.setText(punteggioAttuale + " punti");

                        //TODO: trasformare i partecipanti in un hashmap con userId:punti-->cioè nella classifica stessa
                        //  e per il calendario sarebbero i punti delle partite non il punteggio
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

    private List<Game> getGamesFromHashmap(List<HashMap<String, Object>> hashMapsList) {
        List<Game> gameList = new ArrayList<>(hashMapsList.size());
        for (HashMap<String, Object> hashMap : hashMapsList) {
            gameList.add(
                    new Game((String) hashMap.get("homeUserId"), (String) hashMap.get("awayUserId"),
                            (Integer) hashMap.get("homePoints"), (Integer) hashMap.get("awayPoints")));
        }
        return gameList;
    }

    private void calcoloGiornata(int giornata, List<Game> gameList) {
        for (Game game : gameList) {

            int puntiHome = calcolaPuntiFromUserIdAndGiornata(game.homeUserId, giornata);
            int puntiAway = calcolaPuntiFromUserIdAndGiornata(game.awayUserId, giornata);

            //nel basket non si pareggia--> a parità vince il giocatore in casa
            if (puntiHome == puntiAway) puntiHome++;

            game.setHomePoints(puntiHome);
            game.setAwayPoints(puntiAway);
        }
    }

    private int calcolaPuntiFromUserIdAndGiornata(String userId, int giornata) {
        final int[] points = {0};
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("formazionePerGiornata")
                .child(String.valueOf(giornata)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @SuppressWarnings({"unchecked", "ConstantConditions"})
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                HashMap<String, String> formazioneHome = (HashMap<String, String>) snapshot.getValue();
                                for (String key : formazioneHome.keySet()) {
                                    points[0] = points[0] + (int) (Math.round(
                                            getFactorPositionOnField(FieldPositions.valueOf(key)) *
                                                    getPointsFromPlayerIdAndGiornata(formazioneHome.get(key), giornata)));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        }
                );
        return points[0];
    }

    private int getPointsFromPlayerIdAndGiornata(String playerId, int giornata) {
        final int[] vote = {0};
        FirebaseDatabase.getInstance().getReference("playersStatistics").child(playerId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @SuppressWarnings({"unchecked", "ConstantConditions"})
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        HashMap<String, Object> playerStatistic = (HashMap<String, Object>) snapshot.getValue();
                        int points = ((List<Integer>) playerStatistic.get("points")).get(giornata);
                        int fouls = ((List<Integer>) playerStatistic.get("fouls")).get(giornata);
                        int rebounds = ((List<Integer>) playerStatistic.get("rebounds")).get(giornata);
                        int recoverBalls = ((List<Integer>) playerStatistic.get("recoverBalls")).get(giornata);
                        int lostBalls = ((List<Integer>) playerStatistic.get("lostBalls")).get(giornata);

                        vote[0] = points + rebounds + recoverBalls - fouls - lostBalls;
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                }
        );
        return vote[0];
    }

    private double getFactorPositionOnField(FieldPositions fieldPositions) {
        switch (fieldPositions) {
            case PLAYMAKER:
            case GUARDIA_DX:
            case GUARDIA_SX:
            case CENTRO:
            case ALA:
                return 1;
            case PANCHINA_1:
            case PANCHINA_2:
            case PANCHINA_3:
                return 3.0 / 4.0;
            case PANCHINA_4:
            case PANCHINA_5:
                return 2.0 / 4.0;
            case PANCHINA_6:
            case PANCHINA_7:
                return 1.0 / 4.0;
            default:
                return 0;
        }
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