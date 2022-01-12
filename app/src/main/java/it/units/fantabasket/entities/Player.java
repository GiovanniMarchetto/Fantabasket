package it.units.fantabasket.entities;

import android.graphics.Color;
import it.units.fantabasket.R;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String id;
    private final String name;
    private final String surname;
    private final String dateBirth;
    private final String height;
    private final String weight;
    private final String nationality;
    private final String number;
    private final Role role_1;
    private final Role role_2;
    private final Team team;
    private int shirt;
    private int numberColor;


    public Player() {
        this.id = "";
        this.name = "";
        this.surname = "";
        this.dateBirth = "";
        this.height = "";
        this.weight = "";
        this.nationality = "";
        this.number = "";
        this.role_1 = Role.SCONOSCIUTO;
        this.role_2 = Role.SCONOSCIUTO;
        this.team = null;
        this.shirt = R.drawable.shirt;
        this.numberColor = Color.TRANSPARENT;
    }

    public Player(String number, String name, String surname,
                  Role role_1, Role role_2,
                  String dateBirth, String height, String weight,
                  String nationality, Team team) {
        this.name = name;
        this.surname = surname;
        this.dateBirth = dateBirth;
        this.weight = weight;
        this.height = height;
        this.nationality = nationality;
        this.number = number;
        this.role_1 = role_1;
        this.role_2 = role_2;
        this.team = team;
        setShirt();
        setColorNumber();

        this.id = surname;
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void setShirt() {
        shirt = getResId(team.name().toLowerCase(), R.drawable.class);
    }

    private void setColorNumber() {
        List<Team> lightShirts = new ArrayList<>();
        lightShirts.add(Team.TORTONA);
        lightShirts.add(Team.TRIESTE);

        if (lightShirts.contains(team)) {
            numberColor = Color.BLACK;
        } else {
            numberColor = Color.WHITE;
        }
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public int getShirt() {
        return shirt;
    }

    public int getNumberColor() {
        return numberColor;
    }

    public Role getRole_1() {
        return role_1;
    }

    public Role getRole_2() {
        return role_2;
    }
}
