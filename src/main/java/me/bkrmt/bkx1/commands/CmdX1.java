package me.bkrmt.bkx1.commands;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkx1.BkX1;
import me.bkrmt.bkx1.Duel;
import me.bkrmt.bkx1.DuelOptions;
import me.bkrmt.bkx1.menus.ChooseArenaMenu;
import me.bkrmt.bkx1.menus.ChooseKitsMenu;
import me.bkrmt.bkx1.menus.PlayerProfileMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdX1 extends Executor {

    public CmdX1(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (getPlugin().isRunning()) {
            if (blockConsole(commandSender, "&7[&4&lBkX1&7]" + ChatColor.RED)) return true;
            Player player = (Player) commandSender;
            if (args.length == 0) {
                ChooseArenaMenu.showGUI(new Duel(getPlugin(), true).setFighter1((Player) commandSender));
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("top")) {
                    BkX1.getStatsManager().sendRankPage(player, 0);
                } else if (args[0].equalsIgnoreCase("spectate")) {
                    if (BkX1.getOngoingDuels().keySet().size() > 0) {
                        Duel spectate = new Duel(getPlugin()).setFighter1((Player) commandSender);
                        spectate.getOptions().add(DuelOptions.SPECTATOR_MODE);
                        ChooseArenaMenu.showGUI(spectate);
                    } else {
                        // no duels
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
                            fighter1.sendMessage(getPlugin().getLangFile().get("info.duel-declined.self").replace("{player}", fighter2.getName()));
                            fighter2.sendMessage(getPlugin().getLangFile().get("info.duel-declined.others").replace("{player}", fighter1.getName()));

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
                }
                if (args[0].equalsIgnoreCase("test")) {
                    //test 1
                } else if (args[0].equalsIgnoreCase("test2")) {
                    //test 2
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
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("stats")) {
                    Configuration config = getPlugin().getConfig("player-data", "player-stats.yml");

                    String uuid = findUUID(args[1], config);
                    if (uuid != null) {
                        if (config.get(uuid) != null) {
                            PlayerProfileMenu.showGui(player, uuid, config);
                        } else {
                            // no stats
                        }
                    } else {
                        // invalid player
                    }
                } else if (args[0].equalsIgnoreCase("top")) {
                    try {
                        int page = Integer.parseInt(args[1]) - 1;
                        if (page < BkX1.getStatsManager().getRankPages().size()) {
                            BkX1.getStatsManager().sendRankPage(player, page);
                        } else {
                            // invalid page
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                        // invalid page
                    }
                }
            }
        }
        return true;
    }

    private String findUUID(String name, Configuration config) {
        for (String key : config.getKeys(false)) {
            if (config.getString(key + ".name").equalsIgnoreCase(name)) return key;
        }
        return null;
    }

    private void broadcastToAll() {

    }
}