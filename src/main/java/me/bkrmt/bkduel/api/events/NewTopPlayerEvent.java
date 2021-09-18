package me.bkrmt.bkduel.api.events;

import me.bkrmt.bkduel.stats.PlayerStats;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NewTopPlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final PlayerStats oldPlayer;
    private final PlayerStats newPlayer;

    public NewTopPlayerEvent(PlayerStats oldPlayer, PlayerStats newPlayer) {
        this.oldPlayer = oldPlayer;
        this.newPlayer = newPlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerStats getOldPlayer() {
        return oldPlayer;
    }

    public PlayerStats getNewPlayer() {
        return newPlayer;
    }
}
