package me.bkrmt.bkx1.menus;

import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkx1.BkX1;
import me.bkrmt.bkx1.Page;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerProfileMenu {
    public static void showGui(Player player, String uuid, Configuration config) {
        OfflinePlayer target = BkX1.plugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
        uuid += ".";
        String name = config.getString(uuid + "name");
        int winsInt = config.getInt(uuid + "wins");
        int defeatsInt = config.getInt(uuid + "defeats");
        int duelsInt = config.getInt(uuid + "duels");
        int disconnectsInt = config.getInt(uuid + "disconnects");

        Page page = new Page(BkX1.plugin, new SimpleGUI(new GUI(ChatColor.stripColor("Player Profile"), Rows.FIVE)), 1);

        ItemBuilder head = new ItemBuilder(BkX1.plugin.getHandler().getItemManager().getHead());
        SkullMeta headMeta = (SkullMeta) head.getItem().getItemMeta();
        if (target != null) headMeta.setOwningPlayer(target);
        head.setMeta(headMeta)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.head.name")
                        .replace("{player}", name))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.head.description")
                        .replace("{player}", name))
                .hideTags()
                .update();

        ItemBuilder wins = new ItemBuilder(Material.IRON_SWORD)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.wins.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.wins.description")
                        .replace("{wins}", String.valueOf(winsInt)))
                .hideTags()
                .update();

        ItemBuilder defeats = new ItemBuilder(Material.BONE)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.defeats.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.defeats.description")
                        .replace("{defeats}", String.valueOf(defeatsInt)))
                .hideTags()
                .update();

        ItemBuilder duels = new ItemBuilder(BkX1.plugin.getHandler().getItemManager().getExpBottle())
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.duels.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.duels.description")
                        .replace("{duels}", String.valueOf(duelsInt)))
                .hideTags()
                .update();

        ItemBuilder disconnects = new ItemBuilder(Material.PAPER)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.disconnects.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.disconnects.description")
                        .replace("{disconnects}", String.valueOf(disconnectsInt)))
                .hideTags()
                .update();

        float kdrFloat = (float) winsInt / (float) defeatsInt;
        String formatKdr = kdrFloat < 1 ? ChatColor.RED + String.format("%.1f", kdrFloat) : ChatColor.GREEN + String.format("%.1f", kdrFloat);
        ItemBuilder kdr = new ItemBuilder(Material.NETHER_STAR)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.kdr.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.player-profile.kdr.description")
                        .replace("{kdr}", formatKdr))
                .hideTags()
                .update();

        ItemBuilder close = new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName(BkX1.plugin.getLangFile().get("gui-buttons.close-button.name"))
                .setLore(BkX1.plugin.getLangFile().get("gui-buttons.close-button.description"))
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
