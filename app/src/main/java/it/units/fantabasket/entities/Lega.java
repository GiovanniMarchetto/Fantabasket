package it.units.fantabasket.entities;

import it.units.fantabasket.enums.LegaType;

import java.util.ArrayList;
import java.util.List;

public class Lega {
    private final String name;
    private final String location;
    private final boolean started;
    private final String admin;
    private final List<String> partecipanti;
    private final int numPartecipanti;
    private final LegaType legaType;

    public Lega(String name, String location, String admin, int numPartecipanti, LegaType legaType) {
        this.name = name;
        this.location = location;
        this.started = false;
        this.admin = admin;
        this.numPartecipanti = numPartecipanti;
        this.legaType = legaType;
        this.partecipanti = new ArrayList<>(numPartecipanti);
        this.partecipanti.add(admin);
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public boolean isStarted() {
        return started;
    }

    public String getAdmin() {
        return admin;
    }

    public List<String> getPartecipanti() {
        return partecipanti;
    }

    public int getNumPartecipanti() {
        return numPartecipanti;
    }

    public LegaType getLegaType() {
        return legaType;
    }
}
