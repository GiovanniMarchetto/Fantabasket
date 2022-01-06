package it.units.fantabasket.entities;

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

}
