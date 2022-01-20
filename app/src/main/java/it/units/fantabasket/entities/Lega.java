package it.units.fantabasket.entities;

import it.units.fantabasket.enums.LegaType;

import java.util.ArrayList;
import java.util.List;

public class Lega {
    private final String name;
    private final String location;
    private final double latitude;
    private final double longitude;
    private final boolean started;
    private final String admin;
    private final List<String> partecipanti;
    private final int numPartecipanti;
    private final LegaType tipologia;

    public Lega(String name, String location, double latitude, double longitude,
                String admin, int numPartecipanti, LegaType tipologia) {
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.started = false;
        this.admin = admin;
        this.numPartecipanti = numPartecipanti;
        this.tipologia = tipologia;
        this.partecipanti = new ArrayList<>(numPartecipanti);
        this.partecipanti.add(admin);
    }

    public Lega(String name, String location, double latitude, double longitude,
                boolean started, String admin, List<String> partecipanti, int numPartecipanti, LegaType tipologia) {
        //copy constructor
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.started = started;
        this.admin = admin;
        this.partecipanti = partecipanti;
        this.numPartecipanti = numPartecipanti;
        this.tipologia = tipologia;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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

    public LegaType getTipologia() {
        return tipologia;
    }
}
