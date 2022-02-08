package it.units.fantabasket.ui.main;

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
import it.units.fantabasket.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static it.units.fantabasket.ui.MainActivity.*;
import static it.units.fantabasket.utils.AssetDecoderUtil.*;
import static it.units.fantabasket.utils.DecoderUtil.*;

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
            binding.posizioneInClassifica.setText("");
            binding.totalePunti.setText("");
            binding.teamName.setText(user.teamName);

            Bitmap decodedByte = Utils.getBitmapFromBase64(user.teamLogo);
            binding.teamLogo.setImageBitmap(decodedByte);
        }

        if (leagueOn != null) {
            binding.legaName.setText(leagueOn.get().getName());
            Button startLeagueButton = binding.startLeagueButton;

            if (leagueOn.get().isStarted()) {
                startLeagueButton.setVisibility(View.GONE);
                showInfoAboutLeagueForThisUser();
            } else {
                startLeagueButton.setVisibility(View.VISIBLE);
                if (isUserTheAdminOfLeague && (
                        (isLeagueOnCalendarioType && leagueOn.get().getPartecipanti().size() == leagueOn.get().getNumPartecipanti())
                                || (leagueOn.get().getTipologia() == LegaType.FORMULA1 && leagueOn.get().getPartecipanti().size() > 1))) {

                    startLeagueButton.setOnClickListener(viewListener -> initializeAndStartLeague());
                    startLeagueButton.setEnabled(true);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void showInfoAboutLeagueForThisUser() {
        if (isLeagueOnCalendarioType) {
            binding.nextGameLayout.setVisibility(View.VISIBLE);

            HashMap<String, List<Game>> calendario = leagueOn.get().getCalendario();

            final int lastRoundOfTheSeason = numberOfGamesInTheSeason;

            if (currentRound > 0 && currentRound <= lastRoundOfTheSeason) {
                binding.nextGameLayout.setVisibility(View.VISIBLE);
                List<Game> partiteDellaGiornata = calendario.get(GIORNATA_ + currentRound);
                setGameLayout(partiteDellaGiornata,
                        binding.logoHomeNextGame, binding.teamHomeNextGame, binding.pointsHomeNextGame,
                        binding.logoAwayNextGame, binding.teamAwayNextGame, binding.pointsAwayNextGame);
            } else {
                binding.nextGameLayout.setVisibility(View.GONE);
            }

            final int previousRound = currentRound - 1;

            if (previousRound > 0 && previousRound <= lastRoundOfTheSeason
                    && previousRound >= leagueOn.get().getGiornataInizio()) {
                binding.lastGameLayout.setVisibility(View.VISIBLE);
                List<Game> partiteDellaGiornata = calendario.get(GIORNATA_ + previousRound);
                setGameLayout(partiteDellaGiornata,
                        binding.logoHomeLastGame, binding.teamHomeLastGame, binding.pointsHomeLastGame,
                        binding.logoAwayLastGame, binding.teamAwayLastGame, binding.pointsAwayLastGame);
            } else {
                binding.lastGameLayout.setVisibility(View.GONE);
            }
        }

        List<HashMap<String, Object>> classifica = leagueOn.get().getClassifica();
        for (int posizione = 0; posizione < classifica.size(); posizione++) {
            if (classifica.get(posizione).get("userId").equals(user.id)) {

                String posizioneInClassifica = (posizione + 1) + "º";
                binding.posizioneInClassifica.setText(posizioneInClassifica);

                String parametro = (isLeagueOnCalendarioType)
                        ? POINTS_OF_VICTORIES : TOTAL_POINTS_SCORED;
                binding.totalePunti.setText(String.valueOf(classifica.get(posizione).get(parametro)));
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
        int giornataInizioLega = nowCalendarPlusTwoHours.before(calendarOfCurrentRoundStart)
                ? currentRound : currentRound + 1;

        legheReference.child(legaSelezionata).child(GIORNATA_INIZIO).setValue(giornataInizioLega);
        legheReference.child(legaSelezionata).child(LAST_ROUND_CALCULATED).setValue(giornataInizioLega - 1);

        List<HashMap<String, Object>> classifica = new ArrayList<>();
        for (User member : membersLeagueOn.values()) {
            HashMap<String, Object> elementOfClassifica = new HashMap<>();
            elementOfClassifica.put("userId", member.id);
            elementOfClassifica.put(TEAM_NAME, member.teamName);
            elementOfClassifica.put(TOTAL_POINTS_SCORED, 0);

            if (isLeagueOnCalendarioType) {
                elementOfClassifica.put(TOTAL_POINTS_ALLOWED, 0);
                elementOfClassifica.put(POINTS_OF_VICTORIES, 0);
            }

            classifica.add(elementOfClassifica);
        }
        legheReference.child(legaSelezionata).child(CLASSIFICA).setValue(classifica);

        if (isLeagueOnCalendarioType) {
            HashMap<String, List<Game>> campionato = createCalendario(leagueOn.get().getPartecipanti(), giornataInizioLega);
            legheReference.child(legaSelezionata).child(CALENDARIO).setValue(campionato);
        }

        legheReference.child(legaSelezionata).child("started").setValue(true);
        legheReference.child(legaSelezionata).child("numPartecipanti").setValue(leagueOn.get().getPartecipanti().size());

        onStart();
    }

    private HashMap<String, List<Game>> createCalendario(List<String> members, int primaGiornata) {
        final int ultimaGiornata = numberOfGamesInTheSeason;
        int coppieTotaliPerGiornata = (members.size() - members.size() % 2) / 2;

        HashMap<String, List<Game>> campionato = new HashMap<>();
        int ultimaGiornataCreata = 0;
        while (ultimaGiornata > ultimaGiornataCreata) {
            if (ultimaGiornataCreata != 0) {
                primaGiornata = ultimaGiornataCreata + 1;
            }

            List<Game> allCouples = new ArrayList<>();

            for (int i = 0; i < members.size() - 1; i++) {
                for (int j = i + 1; j < members.size(); j++) {
                    allCouples.add(new Game(members.get(i), members.get(j)));
                }
            }

            for (int i = 1; i < members.size(); i++) {
                int j = i + members.size() - 1;
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

                final boolean homeOrAwayFactor = Math.random() < 0.5;
                int nuovaGiornataAndata = i + primaGiornata - 1;
                int nuovaGiornataRitorno = j + primaGiornata - 1;
                if (nuovaGiornataAndata <= ultimaGiornata) {
                    campionato.put(GIORNATA_ + nuovaGiornataAndata, (homeOrAwayFactor) ? partiteAndata : partiteRitorno);
                    ultimaGiornataCreata = Math.max(ultimaGiornataCreata, nuovaGiornataAndata);
                    if (nuovaGiornataRitorno <= ultimaGiornata) {
                        campionato.put(GIORNATA_ + nuovaGiornataRitorno, (homeOrAwayFactor) ? partiteRitorno : partiteAndata);
                        ultimaGiornataCreata = Math.max(ultimaGiornataCreata, nuovaGiornataRitorno);
                    }
                }
            }
        }

        return campionato;
    }

}