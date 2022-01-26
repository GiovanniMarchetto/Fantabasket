package it.units.fantabasket.enums;

import java.util.Arrays;
import java.util.List;

public enum FieldPositions {
    PLAYMAKER,
    GUARDIA_DX,
    GUARDIA_SX,
    ALA,
    CENTRO,
    PANCHINA_1,
    PANCHINA_2,
    PANCHINA_3,
    PANCHINA_4,
    PANCHINA_5,
    PANCHINA_6,
    PANCHINA_7;

    public static final List<FieldPositions> onFieldPositions = Arrays.asList(PLAYMAKER, GUARDIA_DX,
            GUARDIA_SX, CENTRO, ALA);
    public static final List<FieldPositions> primaPanchina = Arrays.asList(PANCHINA_1, PANCHINA_2,
            PANCHINA_3);
    public static final List<FieldPositions> secondaPanchina = Arrays.asList(PANCHINA_4, PANCHINA_5);
    public static final List<FieldPositions> terzaPanchina = Arrays.asList(PANCHINA_6, PANCHINA_7);
}
