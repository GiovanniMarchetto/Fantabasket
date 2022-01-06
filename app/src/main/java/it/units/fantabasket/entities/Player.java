package it.units.fantabasket.entities;

import it.units.fantabasket.R;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

public class Player {
//    String id;
    String name;
    String number;
    Role role;
    Team team;

    public Player(String name, int number, Role role, Team team) {
        this.name = name;
        this.number = number+"";
        this.role = role;
        this.team = team;
    }

    public int getShirt() {
        int shirt;
        switch (team) {
            case ATHLETISMO:
                shirt= R.drawable.shirt_athletismo;
                break;
            case GORIZIANA:
                shirt=R.drawable.shirt_goriziana;
                break;
            case OLIMPIA:
                shirt=R.drawable.shirt_olimpia;
                break;
            case ROMANS:
                shirt=R.drawable.shirt2;
                break;
            default:
                shirt=R.drawable.shirt;
        }
        return shirt;
    }
}
