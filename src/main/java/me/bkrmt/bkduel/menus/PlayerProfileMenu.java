package me.bkrmt.bkduel.menus;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Page;
import me.bkrmt.opengui.Rows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerProfileMenu {
    public static void showGui(BkPlugin plugin, Player player, PlayerStats stat) {
        OfflinePlayer target = BkDuel.PLUGIN.getServer().getOfflinePlayer(stat.getUUID());
        String name = stat.getPlayerName();
        int winsInt = stat.getWins();
        int defeatsInt = stat.getDefeats();
        int duelsInt = stat.getDuels();
        int disconnectsInt = stat.getDisconnects();

        Page page = new Page(BkDuel.PLUGIN, new GUI(plugin.getLangFile().get("info.player-profile-title"), Rows.FIVE), 1);

        ItemBuilder head = new ItemBuilder(BkDuel.PLUGIN.getHandler().getItemManager().getHead());
        SkullMeta headMeta = (SkullMeta) head.getItem().getItemMeta();
        if (target != null) headMeta.setOwningPlayer(target);
        head.setMeta(headMeta)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.head.name")
                        .replace("{player}", name))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.head.description")
                        .replace("{player}", name))
                .hideTags()
                .update();

        ItemBuilder wins = new ItemBuilder(Material.IRON_SWORD)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.wins.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.wins.description")
                        .replace("{wins}", String.valueOf(winsInt)))
                .hideTags()
                .update();

        ItemBuilder defeats = new ItemBuilder(Material.BONE)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.defeats.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.defeats.description")
                        .replace("{defeats}", String.valueOf(defeatsInt)))
                .hideTags()
                .update();

        ItemBuilder duels = new ItemBuilder(BkDuel.PLUGIN.getHandler().getItemManager().getExpBottle())
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.duels.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.duels.description")
                        .replace("{duels}", String.valueOf(duelsInt)))
                .hideTags()
                .update();

        ItemBuilder disconnects = new ItemBuilder(Material.PAPER)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.disconnects.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.disconnects.description")
                        .replace("{disconnects}", String.valueOf(disconnectsInt)))
                .hideTags()
                .update();
        float kdrFloat = (winsInt > 0 && defeatsInt > 0) ? (float) winsInt / (float) defeatsInt : 0;

        String formatKdr = kdrFloat < 1 ? ChatColor.RED + String.format("%.1f", kdrFloat) : ChatColor.GREEN + String.format("%.1f", kdrFloat);
        ItemBuilder kdr = new ItemBuilder(Material.NETHER_STAR)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.kdr.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.player-profile.kdr.description")
                        .replace("{kdr}", formatKdr))
                .hideTags()
                .update();

        ItemBuilder close = new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName(BkDuel.PLUGIN.getLangFile().get("gui-buttons.close-button.name"))
                .setLore(BkDuel.PLUGIN.getLangFile().get("gui-buttons.close-button.description"))
                .hideTags()
                .update();

        page.setItemOnXY(5, 2, head, null);
        page.setItemOnXY(3, 3, duels, null);
        page.setItemOnXY(4, 3, wins, null);
        page.setItemOnXY(5, 3, defeats, null);
        page.setItemOnXY(6, 3, kdr, null);
        page.setItemOnXY(7, 3, disconnects, null);
        page.setItemOnXY(5, 5, close, event -> {
            player.closeInventory();
        });
        page.getGui().openInventory(player);
    }
}
