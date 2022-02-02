package it.units.fantabasket.ui.main;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import it.units.fantabasket.databinding.FragmentHomeBinding;
import it.units.fantabasket.entities.Game;
import it.units.fantabasket.entities.User;
import it.units.fantabasket.enums.LegaType;
import it.units.fantabasket.enums.Team;
import it.units.fantabasket.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.*;

public class HomeFragment extends Fragment {

    public static Fragment homeFragment;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeFragment = this;
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (user != null) {
            binding.teamName.setText(user.teamName);

            Bitmap decodedByte = Utils.getBitmapFromBase64(user.teamLogo);
            binding.teamLogo.setImageBitmap(decodedByte);
        }

        binding.infoAboutLeague.setVisibility((leagueOn != null) ? View.VISIBLE : View.GONE);
        binding.noLeagueSelected.setVisibility((leagueOn == null) ? View.VISIBLE : View.GONE);

        if (leagueOn != null) {
            binding.noLeagueSelected.setVisibility(View.GONE);
            binding.infoAboutLeague.setVisibility(View.VISIBLE);
            binding.legaName.setText(leagueOn.get().getName());
            if (leagueOn.get().isStarted()) {
                showInfoAboutLeagueForThisUser();
            } else {
                Button startLeagueButton = binding.startLeagueButton;
                startLeagueButton.setVisibility(View.VISIBLE);
                if (isUserTheAdminOfLeague && (
                        leagueOn.get().getTipologia() == LegaType.CALENDARIO
                                && leagueOn.get().getPartecipanti().size() == leagueOn.get().getNumPartecipanti())
                        || (leagueOn.get().getTipologia() == LegaType.FORMULA1 && leagueOn.get().getPartecipanti().size() > 1)) {

                    startLeagueButton.setOnClickListener(viewListener -> initializeAndStartLeague());
                    startLeagueButton.setEnabled(true);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("ConstantConditions")
    private void showInfoAboutLeagueForThisUser() {
        if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
            binding.nextGameLayout.setVisibility(View.VISIBLE);

            HashMap<String, List<Game>> calendario = leagueOn.get().getCalendario();

            if (giornataCorrente > 0 && giornataCorrente <= orariInizioPartite.size()) {
                binding.nextGameLayout.setVisibility(View.VISIBLE);
                List<Game> partiteDellaGiornata = calendario.get("giornata_" + giornataCorrente);
                setGameLayout(partiteDellaGiornata,
                        binding.logoHomeNextGame, binding.teamHomeNextGame, binding.pointsHomeNextGame,
                        binding.logoAwayNextGame, binding.teamAwayNextGame, binding.pointsAwayNextGame);
            }
            if (giornataCorrente - 1 > 0 && giornataCorrente - 1 <= orariInizioPartite.size()) {
                binding.lastGameLayout.setVisibility(View.VISIBLE);
                List<Game> partiteDellaGiornata = calendario.get("giornata_" + (giornataCorrente - 1));
                setGameLayout(partiteDellaGiornata,
                        binding.logoHomeLastGame, binding.teamHomeLastGame, binding.pointsHomeLastGame,
                        binding.logoAwayLastGame, binding.teamAwayLastGame, binding.pointsAwayLastGame);
            }
        }

        List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();
        for (int posizione = 0; posizione < classifica.size(); posizione++) {
            if (classifica.get(posizione).get("userId").equals(user.id)) {
                binding.posizioneInClassifica.setText((posizione + 1) + "º");
                String parametro = (leagueOn.get().getTipologia() == LegaType.CALENDARIO)
                        ? "pointsOfVictories" : "totalPointsScored";
                binding.totalePunti.setText(classifica.get(posizione).get(parametro) + " punti");
                break;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setGameLayout(List<Game> partiteDellaGiornata,
                               ImageView logoHome, TextView teamHomeName, TextView pointsHome,
                               ImageView logoAway, TextView teamAwayName, TextView pointsAway) {
        for (Game partita : partiteDellaGiornata) {
            if (partita.homeUserId.equals(user.id) || partita.awayUserId.equals(firebaseUser.getUid())) {
                User userHome = membersLeagueOn.get(partita.homeUserId);
                logoHome.setImageBitmap(Utils.getBitmapFromBase64(userHome.teamLogo));
                teamHomeName.setText(userHome.teamName);
                pointsHome.setText(String.valueOf(partita.homePoints));

                User userAway = membersLeagueOn.get(partita.awayUserId);
                logoAway.setImageBitmap(Utils.getBitmapFromBase64(userAway.teamLogo));
                teamAwayName.setText(userAway.teamName);
                pointsAway.setText(String.valueOf(partita.awayPoints));
                break;
            }
        }
    }

    private void initializeAndStartLeague() {

        Calendar nowCalendarPlusTwoHours = Utils.getCalendarNow();
        nowCalendarPlusTwoHours.add(Calendar.HOUR, 2);
        int giornataInizioLega = nowCalendarPlusTwoHours.before(orarioInizioPrimaPartitaDellaGiornataCorrente) ? giornataCorrente : giornataCorrente + 1;
        legheReference.child(legaSelezionata).child("giornataInizio").setValue(giornataInizioLega);
        legheReference.child(legaSelezionata).child("lastRoundCalculated").setValue(giornataInizioLega - 1);

        List<HashMap<String, Object>> classifica = new ArrayList<>();
        for (User member : membersLeagueOn.values()) {
            HashMap<String, Object> elementOfClassifica = new HashMap<>();
            elementOfClassifica.put("userId", member.id);
            elementOfClassifica.put("teamName", member.teamName);
            elementOfClassifica.put("totalPointsScored", 0);

            if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
                elementOfClassifica.put("totalPointsAllowed", 0);
                elementOfClassifica.put("pointsOfVictories", 0);
            }

            classifica.add(elementOfClassifica);
        }
        legheReference.child(legaSelezionata).child("classifica").setValue(classifica);

        if (leagueOn.get().getTipologia() == LegaType.CALENDARIO) {
            HashMap<String, List<Game>> campionato = createCalendario(leagueOn.get().getPartecipanti());
            legheReference.child(legaSelezionata).child("calendario").setValue(campionato);
        }

        legheReference.child(legaSelezionata).child("started").setValue(true);

        refreshFragment();
    }

    private void refreshFragment() {
        //noinspection ConstantConditions
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    private HashMap<String, List<Game>> createCalendario(List<String> players) {
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

}