package it.units.fantabasket.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.firebase.database.ValueEventListener;
import it.units.fantabasket.R;
import it.units.fantabasket.databinding.FragmentHomeBinding;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.enums.Team;
import it.units.fantabasket.utils.MyValueEventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.MainActivity.leagueOn;
import static it.units.fantabasket.MainActivity.user;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (leagueOn == null) {
            binding.noLeagueSelected.setVisibility(View.VISIBLE);
            binding.infoAboutLeague.setVisibility(View.GONE);
        } else {
            binding.teamName.setText(user.teamName);

            byte[] decodedString = Base64.decode(user.teamLogo, Base64.NO_WRAP);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            binding.teamLogo.setImageBitmap(decodedByte);

            binding.legaName.setText(leagueOn.get().getName());

            binding.legaNameOption.setText(leagueOn.get().getName());
            String start = leagueOn.get().isStarted() ? "IN CORSO" : "IN ATTESA";
            binding.legaStartOption.setText(start);

            //BUTTONS
            binding.changeLegaButton.setOnClickListener(view ->
                    NavHostFragment.findNavController(HomeFragment.this)
                            .navigate(R.id.action_HomeFragment_to_LegheFragment)
            );
        }

        return root;
    }

    /*
          private void setLegaSelezionataValueEventListener() {
              legaSelezionataListener = (MyValueEventListener) snapshot -> {
      //            HashMap<String, Object> legaParams = (HashMap<String, Object>) snapshot.getValue();
      //            assert legaParams != null;
      //            Lega lega = Utils.getLegaFromHashMapOfDB(legaParams);


      //            isUserTheAdminOfLeague = lega.getAdmin().equals(firebaseUser.getUid());
                  if (lega.isStarted()) {
                      if (lega.getTipologia() == LegaType.CALENDARIO) {
                          HashMap<String, List<HashMap<String, Object>>> calendario =
                                  (HashMap<String, List<HashMap<String, Object>>>) legaParams.get("calendario");
                          List<HashMap<String, Object>> partiteDellaGiornata = calendario.get("giornata_" + giornataCorrente);
                          for (HashMap<String, Object> partita : partiteDellaGiornata) {

                              String homeUserId = (String) partita.get("homeUserId");
                              String awayUserId = (String) partita.get("awayUserId");

                              if (homeUserId.equals(firebaseUser.getUid()) || awayUserId.equals(firebaseUser.getUid())) {

                                  Game userGame = new Game(homeUserId, awayUserId);

                                  FirebaseDatabase.getInstance().getReference("users").child(userGame.homeUserId).addValueEventListener(
                                          getPlayerListener(binding.logoHome, binding.teamHome));

                                  FirebaseDatabase.getInstance().getReference("users").child(userGame.awayUserId).addValueEventListener(
                                          getPlayerListener(binding.logoAway, binding.teamAway));
                                  break;
                              }
                          }

                      } else {

                          List<HashMap<String, Object>> classifica = (List<HashMap<String, Object>>) legaParams.get("classifica");
                          int punteggioAttuale = 0;
                          int posizione = 0;
                          //TODO: refactoring
                          assert classifica != null;
                          for (HashMap<String, Object> hashMap : classifica) {
                              if (Objects.equals(hashMap.get("userId"), firebaseUser.getUid())) {
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
      //                binding.myTeamLayout.setVisibility((lega.getTipologia() == LegaType.FORMULA1) ? View.VISIBLE : View.GONE);
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

                              Calendar nowCalendarPlusTwoHours = Utils.getCalendarNow();
                              nowCalendarPlusTwoHours.add(Calendar.HOUR, 2);
                              int giornataInizioLega = nowCalendarPlusTwoHours.before(orarioInizioPrimaPartitaDellaGiornataCorrente) ? giornataCorrente : giornataCorrente + 1;
                              legheReference.child(legaSelezionata).child("giornataInizio").setValue(giornataInizioLega);
                              legheReference.child(legaSelezionata).child("lastRoundCalculated").setValue(giornataInizioLega - 1);

                              List<HashMap<String, Object>> classifica = new ArrayList<>();
                              for (String partecipante : lega.getPartecipanti()) {
                                  User partecipanteData = new User(partecipante);

                                  HashMap<String, Object> elementOfClassifica = new HashMap<>();
                                  elementOfClassifica.put("userId", partecipante);
                                  elementOfClassifica.put("teamName", partecipanteData.teamName);
                                  elementOfClassifica.put("totalPointsScored", 0);

                                  if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                                      elementOfClassifica.put("totalPointsAllowed", 0);
                                      elementOfClassifica.put("pointsOfVictories", 0);
                                  }

                                  classifica.add(elementOfClassifica);
                              }
                              legheReference.child(legaSelezionata).child("classifica").setValue(classifica);
                              if (lega.getTipologia() == LegaType.CALENDARIO) {
                                  HashMap<String, List<Game>> campionato = getCampionato(lega.getPartecipanti());
                                  legheReference.child(legaSelezionata).child("calendario").setValue(campionato);
                              }
                          });
                      }
                  }
              };
          }
      */
    @NotNull
    @SuppressWarnings("unchecked")
    private ValueEventListener getPlayerListener(ImageView imageView, TextView textView) {
        return (MyValueEventListener) snapshot -> {
            HashMap<String, Object> user = (HashMap<String, Object>) snapshot.getValue();
            assert user != null;
            byte[] decodedString = Base64.decode((String) user.get("teamLogo"), Base64.NO_WRAP);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
            textView.setText((String) user.get("teamName"));
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
//            Log.i("MIO", key);
//            for (Game partita:campionato.get(key)) {
//                Log.i("MIO", "               " + partita.homeUserId + " - " + partita.awayUserId);
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