package me.bkrmt.bkduel;

import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkduel.api.events.NewTopPlayerEvent;
import me.bkrmt.bkduel.api.events.StatsUpdateEvent;
import me.bkrmt.bkduel.npc.UpdateReason;
import me.bkrmt.bkduel.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ConstantListener implements Listener {

    private BkDuel bkDuel;

    public ConstantListener() {
        this.bkDuel = BkDuel.getInstance();
    }

    @EventHandler
    public void onStatUpdate(StatsUpdateEvent event) {
        if (bkDuel.getHookManager().hasHologramHook() && bkDuel.getConfigManager().getConfig().getBoolean("top-1-npc.enabled")) {
            if (event.getOldStatsList().size() > 0 && event.getNewStatsList().size() > 0) {
                PlayerStats oldTopStats = event.getOldStatsList().get(0);
                PlayerStats newTopStats = event.getNewStatsList().get(0);
                if (oldTopStats.getDuels() != newTopStats.getDuels() && bkDuel.getNpcManager() != null)
                    bkDuel.getNpcManager().setTopNpc(newTopStats, UpdateReason.STATS);
            }
        }
    }

    @EventHandler
    public void onNewTop(NewTopPlayerEvent event) {
        if (bkDuel.getHookManager().hasHologramHook() && bkDuel.getConfigManager().getConfig().getBoolean("top-1-npc.enabled") && bkDuel.getNpcManager() != null)
            bkDuel.getNpcManager().setTopNpc(event.getNewPlayer(), UpdateReason.NPC_AND_STATS);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Configuration config = bkDuel.getConfigManager().getConfig("player-data", "player-inventories.yml");
        Player player = event.getPlayer();

        if (config.get(player.getUniqueId().toString()) != null) {
            if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                Duel.returnItems(bkDuel, player, true);
            } else {
                player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                config.set(player.getUniqueId().toString(), null);
                config.saveToFile();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Configuration config = bkDuel.getConfigManager().getConfig("player-data", "player-inventories.yml");
        Player player = event.getPlayer();

        if (config.get(player.getUniqueId().toString()) != null) {
            if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                Duel.returnItems(bkDuel, player, true);
            } else {
                player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                config.set(player.getUniqueId().toString(), null);
                config.saveToFile();
            }
        }
    }
}
