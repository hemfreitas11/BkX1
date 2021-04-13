package me.bkrmt.bkx1.events;

import me.bkrmt.bkx1.stats.PlayerStats;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;

public class StatsUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final ArrayList<PlayerStats> oldStatsList;
    private final ArrayList<PlayerStats> newStatsList;


    public StatsUpdateEvent(ArrayList<PlayerStats> oldStatsList, ArrayList<PlayerStats> newStatsList) {
        this.oldStatsList = oldStatsList;
        this.newStatsList = newStatsList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ArrayList<PlayerStats> getOldStatsList() {
        return oldStatsList;
    }

    public ArrayList<PlayerStats> getNewStatsList() {
        return newStatsList;
    }
}
