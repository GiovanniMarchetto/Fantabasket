package it.units.fantabasket.entities;

import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.enums.FieldPositions;
import it.units.fantabasket.utils.MyValueEventListener;

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

    public User(HashMap<String, String> hashMap) {
        this.id = hashMap.get("id");
        this.nickname = hashMap.get("nickname");
        this.teamName = hashMap.get("teamName");
        this.teamLogo = hashMap.get("teamLogo");
    }

    public User(String userId) {
        this.id = userId;
        FirebaseDatabase.getInstance().getReference("users").child(userId).addListenerForSingleValueEvent(
                (MyValueEventListener) snapshot -> {
                    HashMap<String, String> hashMap = (HashMap<String, String>) snapshot.getValue();
                    this.nickname = hashMap.get("nickname");
                    this.teamName = hashMap.get("teamName");
                    this.teamLogo = hashMap.get("teamLogo");
                }
        );
    }
}
