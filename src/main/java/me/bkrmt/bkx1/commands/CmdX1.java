package me.bkrmt.bkx1.commands;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkx1.BkX1;
import me.bkrmt.bkx1.Duel;
import me.bkrmt.bkx1.menus.ChooseArenaMenu;
import me.bkrmt.bkx1.menus.ChooseKitsMenu;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CmdX1 extends Executor {

    public CmdX1(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (getPlugin().isRunning()) {
            if (blockConsole(commandSender, "&7[&4&lBkX1&7]&f" + ChatColor.RED)) return true;
            Player player = (Player) commandSender;
            if (args.length == 0) {
                ChooseArenaMenu.showGUI(new Duel(getPlugin(), true).setFighter1((Player)commandSender));
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("spectate")) {

                    if (BkX1.getOngoingDuels().keySet().size() > 0) {
                        List<ItemStack> arenaDisplays = new ArrayList<>();
                        SimpleGUI gui = new SimpleGUI(new GUI("asdasdasda", Rows.FOUR));
                        for (UUID uuid : BkX1.getOngoingDuels().keySet()) {
                            Duel duel = BkX1.getOngoingDuels().get(uuid);
                            ItemStack item = duel.getArena().getDisplayItem();
                            ItemMeta meta = item.getItemMeta();
                            List<String> lore = new ArrayList<>();
                            lore.add(" ");
                            lore.add("§aFighter 1: §2" + duel.getFighter1().getName());
                            lore.add("§aFighter 2: §2" + duel.getFighter2().getName());
                            lore.add(" ");
                            lore.add("§2Click to spectate");
                            meta.setLore(lore);
                            item.setItemMeta(meta);

                            arenaDisplays.add(item);

                            gui.addItem(new ItemBuilder(item));
                        }
                        gui.openInventory(player);
                    }

                } else if (args[0].equalsIgnoreCase("accept")) {
                    if (BkX1.getOngoingDuels().containsKey(player.getUniqueId())) {
                        Duel duel = BkX1.getOngoingDuels().get(player.getUniqueId());
                        if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                            duel.startDuel();
                        } else {
                            // not target player
                        }
                    } else {
                        // not in duel - no request
                    }
                } else if (args[0].equalsIgnoreCase("decline")) {
                    if (BkX1.getOngoingDuels().containsKey(player.getUniqueId())) {
                        Duel duel = BkX1.getOngoingDuels().get(player.getUniqueId());
                        if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                            Player fighter1 = duel.getFighter1();
                            Player fighter2 = duel.getFighter2();
                            fighter1.sendMessage("Duel declined.");
                            fighter2.sendMessage("Duel declined.");

                            //Broadcast to all
                            broadcastToAll();

                            BkX1.getOngoingDuels().remove(fighter1.getUniqueId());
                            BkX1.getOngoingDuels().remove(fighter2.getUniqueId());
                            duel = null;
                        } else {
                            // not target player
                        }
                    } else {
                        // not in duel - no request
                    }
                } else {
                    Player targetPlayer = Utils.getPlayer(args[0]);
                    if (targetPlayer != null) {
                        Duel duel = new Duel(getPlugin());
                        duel.setFighter1(player);
                        duel.setFighter2(targetPlayer);
                        ChooseKitsMenu.showGUI(duel);
                    } else {
                        // player not online
                    }
                }
            }
        }
        return true;
    }

    private void broadcastToAll() {

    }
}