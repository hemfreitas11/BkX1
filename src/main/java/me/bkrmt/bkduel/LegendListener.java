package me.bkrmt.bkduel;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.bkduel.stats.StatsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class LegendListener implements Listener {
    private BkDuel bkDuel;

    public LegendListener(BkDuel bkDuel) {
        this.bkDuel = bkDuel;
    }

    @EventHandler
    public void onChat(ChatMessageEvent event) {
        StatsManager manager = bkDuel.getStatsManager();
        if (manager != null) {
            PlayerStats playerStat = manager.getStats().get(0);
            if (playerStat != null) {
                UUID topUUID = playerStat.getUUID();
                if (event.getTags().contains("bkduel_top") && event.getSender().getUniqueId().toString().equalsIgnoreCase(topUUID.toString()))
                    event.setTagValue("bkduel_top", Utils.translateColor(bkDuel.getConfigManager().getConfig().getString("top-1-tag")));
            }
        }
    }

}
