package it.units.fantabasket.entities;

import com.google.firebase.database.FirebaseDatabase;
import it.units.fantabasket.utils.MyValueEventListener;

import java.util.HashMap;

public class User {
    public String id;
    public String name;
    public String teamName;
    public String teamLogo;

    public User(HashMap<String, String> hashMap) {
        this.id = hashMap.get("id");
        this.name = hashMap.get("name");
        this.teamName = hashMap.get("teamName");
        this.teamLogo = hashMap.get("teamLogo");
    }

    public User(String userId) {
        this.id = userId;
        FirebaseDatabase.getInstance().getReference("users").child(userId).addListenerForSingleValueEvent(
                (MyValueEventListener) snapshot -> {
                    HashMap<String, String> hashMap = (HashMap<String, String>) snapshot.getValue();
                    this.name = hashMap.get("name");
                    this.teamName = hashMap.get("teamName");
                    this.teamLogo = hashMap.get("teamLogo");
                }
        );
    }
}
