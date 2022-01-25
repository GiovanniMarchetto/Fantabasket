package it.units.fantabasket.entities;

import androidx.annotation.Nullable;
import it.units.fantabasket.enums.LegaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lega {
    private final String name;
    private final String location;
    private final double latitude;
    private final double longitude;
    private final boolean started;
    private final int giornataInizio;
    private final int lastRoundCalculated;
    private final String admin;
    private final List<String> partecipanti;
    private final int numPartecipanti;
    private final LegaType tipologia;
    @Nullable
    private final List<HashMap<String, Object>> classifica;
    @Nullable
    private final HashMap<String, List<Game>> calendario;

    public Lega(String name, String location, double latitude, double longitude,
                String admin, int numPartecipanti, LegaType tipologia) {
        //new league
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.started = false;
        this.classifica = null;
        this.giornataInizio = 0;
        this.lastRoundCalculated = 0;
        this.calendario = null;
        this.admin = admin;
        this.numPartecipanti = numPartecipanti;
        this.tipologia = tipologia;
        this.partecipanti = new ArrayList<>(numPartecipanti);
        this.partecipanti.add(admin);
    }

    public Lega(String name, String location, double latitude, double longitude,
                boolean started, int giornataInizio, int lastRoundCalculated,
                String admin, List<String> partecipanti, int numPartecipanti,
                LegaType tipologia,
                @Nullable List<HashMap<String, Object>> classifica,
                @Nullable HashMap<String, List<Game>> calendario) {
        this.name = name;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.started = started;
        this.giornataInizio = giornataInizio;
        this.lastRoundCalculated = lastRoundCalculated;
        this.admin = admin;
        this.partecipanti = partecipanti;
        this.numPartecipanti = numPartecipanti;
        this.tipologia = tipologia;
        this.classifica = classifica;
        this.calendario = calendario;

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

    public LegaType getTipologia() {
        return tipologia;
    }

    public int getLastRoundCalculated() {
        return lastRoundCalculated;
    }

    @Nullable
    public HashMap<String, List<Game>> getCalendario() {
        return calendario;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getGiornataInizio() {
        return giornataInizio;
    }

    @Nullable
    public List<HashMap<String, Object>> getClassifica() {
        return classifica;
    }
}
