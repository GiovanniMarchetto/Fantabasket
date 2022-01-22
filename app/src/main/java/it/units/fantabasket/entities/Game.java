package it.units.fantabasket.entities;

import org.jetbrains.annotations.NotNull;

public class Game {
    public final String homeUserId;
    public final String awayUserId;
    public int homePoints;
    public int awayPoints;

    public Game(String homeId, String awayId) {
        this.homeUserId = homeId;
        this.awayUserId = awayId;
        this.homePoints = 0;
        this.awayPoints = 0;
    }

    public String getHomeUserId() {
        return homeUserId;
    }

    public String getAwayUserId() {
        return awayUserId;
    }

    public int getHomePoints() {
        return homePoints;
    }

    public void setHomePoints(int homePoints) {
        this.homePoints = homePoints;
    }

    public int getAwayPoints() {
        return awayPoints;
    }

    public void setAwayPoints(int awayPoints) {
        this.awayPoints = awayPoints;
    }

    public boolean equals(Game g) {
        return homeUserId.equals(g.homeUserId) && awayUserId.equals(g.awayUserId);
    }

    @NotNull
    public String toString() {
        return "Game: " + homeUserId + " - " + awayUserId;
    }
}