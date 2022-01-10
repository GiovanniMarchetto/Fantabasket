package it.units.fantabasket.entities;

import android.graphics.Color;
import it.units.fantabasket.R;
import it.units.fantabasket.enums.Role;
import it.units.fantabasket.enums.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Player {
    //    String id;
    String name;
    String surname;
    String dateBirth;
    String height;
    String weight;
    String nationality;
    String number;
    Role role_1;
    Role role_2;
    Team team;
    int shirt;
    int numberColor;

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

    public String getSurname() {
        return surname;
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
}
