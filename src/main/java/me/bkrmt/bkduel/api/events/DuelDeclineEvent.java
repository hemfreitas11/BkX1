package me.bkrmt.bkduel.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DuelDeclineEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player challenger;
    private final Player challenged;

    public DuelDeclineEvent(Player challenger, Player challenged) {
        this.challenger = challenger;
        this.challenged = challenged;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getChallenger() {
        return challenger;
    }

    public Player getChallenged() {
        return challenged;
    }
}
