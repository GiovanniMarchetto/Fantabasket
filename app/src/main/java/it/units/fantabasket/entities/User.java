package it.units.fantabasket.entities;

import it.units.fantabasket.enums.FieldPositions;

import java.util.HashMap;
import java.util.List;

public class User {
    public String id;
    public String nickname;
    public String teamName;
    public String teamLogo;
    public String legaSelezionata;
    public List<String> roster;
    public List<HashMap<FieldPositions, String>> formazioniPerGiornata;

    public User(String id, String nickname, String teamName, String teamLogo) {
        this.id = id;
        this.nickname = nickname;
        this.teamName = teamName;
        this.teamLogo = teamLogo;
        this.legaSelezionata = null;
        this.roster = null;
        this.formazioniPerGiornata = null;
    }

    public User(String id, String nickname, String teamName, String teamLogo,
                String legaSelezionata, List<String> roster,
                List<HashMap<FieldPositions, String>> formazioniPerGiornata) {
        this.id = id;
        this.nickname = nickname;
        this.teamName = teamName;
        this.teamLogo = teamLogo;
        this.legaSelezionata = legaSelezionata;
        this.roster = roster;
        this.formazioniPerGiornata = formazioniPerGiornata;
    }
}
