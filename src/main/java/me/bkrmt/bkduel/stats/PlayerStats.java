package me.bkrmt.bkduel.stats;

import java.util.UUID;

public class PlayerStats {

    private final String playerName;
    private final UUID uuid;
    private final int wins;
    private final int defeats;
    private final int duels;
    private final int disconnects;

    public PlayerStats(String playerName, UUID uuid, int wins, int defeats, int duels, int disconnects) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.wins = wins;
        this.defeats = defeats;
        this.duels = duels;
        this.disconnects = disconnects;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getWins() {
        return wins;
    }

    public int getDefeats() {
        return defeats;
    }

    public int getDuels() {
        return duels;
    }

    public int getDisconnects() {
        return disconnects;
    }

    public float getKDR() {
        return (float) getWins() / (float) getDefeats();
    }
}
