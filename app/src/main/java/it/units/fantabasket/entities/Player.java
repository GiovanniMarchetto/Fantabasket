package it.units.fantabasket.entities;

import android.graphics.Color;
import it.units.fantabasket.R;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

public class Player {
    //    String id;
    String name;
    String number;
    Role role;
    Team team;
    int shirt;
    int colorNumber;

    public Player(String name, int number, Role role, Team team) {
        this.name = name;
        this.number = number + "";
        this.role = role;
        this.team = team;
        setShirtAndColorNumber();
    }

    private void setShirtAndColorNumber() {
        switch (team) {
            case ATHLETISMO:
                shirt = R.drawable.shirt_athletismo;
                colorNumber = Color.BLACK;
                break;
            case GORIZIANA:
                shirt = R.drawable.shirt_goriziana;
                colorNumber = Color.WHITE;
                break;
            case OLIMPIA:
                shirt = R.drawable.shirt_olimpia;
                colorNumber = Color.WHITE;
                break;
            case ROMANS:
                shirt = R.drawable.shirt2;
                colorNumber = Color.WHITE;
                break;
            default:
                shirt = R.drawable.shirt;
                colorNumber = Color.BLACK;
        }
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public int getShirt() {
        return shirt;
    }
}
