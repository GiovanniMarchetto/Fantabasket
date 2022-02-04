package it.units.fantabasket.utils;

import android.location.Location;
import it.units.fantabasket.entities.Lega;
import it.units.fantabasket.layouts.LegaLayout;

import java.util.HashMap;

public interface UpdateLocationInterface {
    void updateLocation(Location location, HashMap<Lega, LegaLayout> legaLinearLayoutHashMap);
}
