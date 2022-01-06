package it.units.fantabasket;

public class Player {
    String id;
    String name;
    String number;
    String role;
    Team team;

    public Player(String id, String name, String number, String role, Team team) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.role = role;
        this.team = team;
    }

}
