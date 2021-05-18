package me.bkrmt.bkduel.commands;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.Duel;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.enums.DuelStatus;
import me.bkrmt.bkduel.menus.ChooseArenaMenu;
import me.bkrmt.bkduel.menus.ChooseKitsMenu;
import me.bkrmt.bkduel.menus.PlayerProfileMenu;
import me.bkrmt.bkduel.npc.NPCManager;
import me.bkrmt.bkduel.npc.NPCUpdateReason;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.bkduel.stats.StatsManager;
import me.bkrmt.teleport.Teleport;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Hashtable;

public class CmdDuel extends Executor {
    private static Hashtable<String, String> subcommands;

    public CmdDuel(BkPlugin plugin, String langKey, String permission) {
        super(plugin, langKey, permission);
        subcommands = new Hashtable<>();
        subcommands.put("challenge", plugin.getLangFile().get("commands.duel.subcommands.challenge.command"));
        subcommands.put("accept", plugin.getLangFile().get("commands.duel.subcommands.accept.command"));
        subcommands.put("decline", plugin.getLangFile().get("commands.duel.subcommands.decline.command"));
        subcommands.put("top", plugin.getLangFile().get("commands.duel.subcommands.top.command"));
        subcommands.put("spectate", plugin.getLangFile().get("commands.duel.subcommands.spectate.command"));
        subcommands.put("stats", plugin.getLangFile().get("commands.duel.subcommands.stats.command"));
        subcommands.put("edit", plugin.getLangFile().get("commands.duel.subcommands.edit.command"));
        subcommands.put("edit.arenas", plugin.getLangFile().get("commands.duel.subcommands.edit.subcommands.arenas.command"));
        subcommands.put("edit.kits", plugin.getLangFile().get("commands.duel.subcommands.edit.subcommands.kits.command"));
        subcommands.put("npc", plugin.getLangFile().get("commands.duel.subcommands.npc.command"));
        subcommands.put("npc.location", plugin.getLangFile().get("commands.duel.subcommands.npc.subcommands.location.command"));
        subcommands.put("npc.update", plugin.getLangFile().get("commands.duel.subcommands.npc.subcommands.update.command"));
        subcommands.put("enable", plugin.getLangFile().get("commands.duel.subcommands.enable.command"));
        subcommands.put("disable", plugin.getLangFile().get("commands.duel.subcommands.disable.command"));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (getPlugin().isRunning()) {
            if (blockConsole(commandSender, "&7[&4&lBkDuel&7]" + ChatColor.RED)) return true;
            Player player = (Player) commandSender;
            if (args.length == 0) {
                sendUsage(commandSender);
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("test")) {
//                    NPCManager.setTopNpc(new PlayerStats(commandSender.getName(), ((Player) commandSender).getUniqueId(), 4, 4, 4, 4));
                } else if (args[0].equalsIgnoreCase(subcommands.get("enable"))) {
                    Configuration config = getPlugin().getConfig("player-data", "player-stats.yml");
                    config.set(player.getUniqueId().toString() + ".duel-disabled", false);
                    config.save(false);
                    player.sendMessage(getPlugin().getLangFile().get("info.duel-enabled"));
                } else if (args[0].equalsIgnoreCase(subcommands.get("disable"))) {
                    Configuration config = getPlugin().getConfig("player-data", "player-stats.yml");
                    config.set(player.getUniqueId().toString() + ".duel-disabled", true);
                    config.save(false);
                    player.sendMessage(getPlugin().getLangFile().get("info.duel-disabled"));
                } else if (args[0].equalsIgnoreCase(subcommands.get("edit"))) {
                    player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                            .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.edit.usage")));
                } else if (args[0].equalsIgnoreCase(subcommands.get("top"))) {
                    int pageSize = BkDuel.getStatsManager().getRankPages().size();
                    if (pageSize > 0) {
                        BkDuel.getStatsManager().sendRankPage(player, 0);
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("error.no-ranks"));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("spectate"))) {
                    if (BkDuel.getOngoingDuels().keySet().size() > 0) {
                        Duel spectate = new Duel(getPlugin()).setFighter1((Player) commandSender);
                        spectate.getOptions().add(DuelOptions.SPECTATOR_MODE);
                        ChooseArenaMenu.showGUI(spectate);
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("error.no-duels"));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("stats"))) {
                    player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                            .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.stats.usage")));
                } else if (args[0].equalsIgnoreCase(subcommands.get("accept"))) {
                    if (BkDuel.getOngoingDuels().containsKey(player.getUniqueId())) {
                        Duel duel = BkDuel.getOngoingDuels().get(player.getUniqueId());
                        if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                            if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                                double playerMoney = BkDuel.getEconomy().getBalance(duel.getFighter1());
                                double duelCost = getPlugin().getConfig().getDouble("duel-cost");

                                Player otherPlayer = duel.getFighter1();

                                if (playerMoney >= duelCost) {
                                    EconomyResponse r = BkDuel.getEconomy().withdrawPlayer(player, duelCost);
                                    EconomyResponse r2 = BkDuel.getEconomy().withdrawPlayer(otherPlayer, duelCost);
                                    if (r.transactionSuccess() && r2.transactionSuccess()) {
                                        String paid = getPlugin().getLangFile().get("info.cost-paid").replace("{amount}", String.valueOf(duelCost)).replace("{balance}", String.valueOf(r2.balance));
                                        player.sendMessage(paid);
                                        otherPlayer.sendMessage(paid);
                                        duel.getRequest().getExpireRunnable().cancel();
                                        duel.startDuel();
                                    } else {
                                        String errorMessage = r.transactionSuccess() ? r2.errorMessage : r.errorMessage;
                                        player.sendMessage(String.format("An error occured: %s", errorMessage));
                                    }
                                } else {
                                    player.sendMessage(getPlugin().getLangFile().get("error.no-money"));
                                }
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get("error.duel-already-started"));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.no-challenge"));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("error.no-challenge"));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("decline"))) {
                    if (BkDuel.getOngoingDuels().containsKey(player.getUniqueId())) {
                        Duel duel = BkDuel.getOngoingDuels().get(player.getUniqueId());
                        if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                            if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                                duel.getRequest().getExpireRunnable().cancel();

                                Player fighter1 = duel.getFighter1();
                                Player fighter2 = duel.getFighter2();
                                fighter1.sendMessage(getPlugin().getLangFile().get("info.duel-declined.self").replace("{player}", fighter2.getName()));
                                fighter2.sendMessage(getPlugin().getLangFile().get("info.duel-declined.others").replace("{player}", fighter1.getName()));

                                //Broadcast to all
                                broadcastToAll();

                                BkDuel.getOngoingDuels().remove(fighter1.getUniqueId());
                                BkDuel.getOngoingDuels().remove(fighter2.getUniqueId());
                                duel = null;
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get("error.duel-already-started"));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.no-challenge"));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("error.no-challenge"));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("challenge"))) {
                    player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                            .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.challenge.usage")));
                } else if (args[0].equalsIgnoreCase(subcommands.get("npc"))) {
                    player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                            .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.npc.usage")));
                } else {
                    sendUsage(commandSender);
                    return true;
                }
            } else if (args.length == 2) {
                Player target = Utils.getPlayer(args[1]);
                if (args[0].equalsIgnoreCase(subcommands.get("stats"))) {
                    PlayerStats stat = StatsManager.getPlayerStat(args[1]);
                    if (stat == null) {
                        if (target != null) {
                            stat = StatsManager.createStats(target);
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.invalid-player").replace("{player}", args[1]));
                            return true;
                        }
                    }
                    PlayerProfileMenu.showGui(getPlugin(), player, stat);
                } else if (args[0].equalsIgnoreCase(subcommands.get("npc"))) {
                    if (args[1].equalsIgnoreCase(subcommands.get("npc.location"))) {
                        Configuration config = getPlugin().getConfig();
                        config.setLocation("top-1-npc.npc.location", ((Player) commandSender).getLocation());
                        config.save(false);
                        commandSender.sendMessage(getPlugin().getLangFile().get("info.location-set"));
                    } else if (args[1].equalsIgnoreCase(subcommands.get("npc.update"))) {
                        if (BkDuel.getHologramHook() != null) {
                            if (BkDuel.getStatsManager().getStats().size() > 0) {
                                NPCManager.setTopNpc(BkDuel.getStatsManager().getStats().get(0), NPCUpdateReason.UPDATE_NPC);
                                commandSender.sendMessage(getPlugin().getLangFile().get("info.npc-updated"));
                            } else {
                                commandSender.sendMessage(getPlugin().getLangFile().get("error.no-ranks"));
                            }
                        } else {
                            commandSender.sendMessage(getPlugin().getLangFile().get("error.no-npc"));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                                .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.npc.usage")));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("edit"))) {
                    if (args[1].equalsIgnoreCase(subcommands.get("edit.arenas"))) {
                        ChooseArenaMenu.showGUI(new Duel(getPlugin(), true).setFighter1((Player) commandSender));
                    } else if (args[1].equalsIgnoreCase(subcommands.get("edit.kits"))) {
                        ChooseKitsMenu.showGUI(new Duel(getPlugin(), true).setFighter1((Player) commandSender));
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("commands.usage-format")
                                .replace("{usage}", getPlugin().getLangFile().get("commands.duel.subcommands.edit.usage")));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("challenge"))) {
                    if (!BkDuel.getOngoingDuels().containsKey(player.getUniqueId())) {
                        double playerMoney = BkDuel.getEconomy().getBalance(player);
                        double duelCost = getPlugin().getConfig().getDouble("duel-cost");

                        if (playerMoney >= duelCost) {
                            Player targetPlayer = Utils.getPlayer(args[1]);
                            if (targetPlayer != null) {
                                Configuration config = getPlugin().getConfig("player-data", "player-stats.yml");
                                if (!config.getBoolean(targetPlayer.getUniqueId().toString() + ".duel-disabled")) {
                                    if (!targetPlayer.isDead()) {
                                        if (targetPlayer.getUniqueId().equals(player.getUniqueId())) {
                                            if (!BkDuel.getOngoingDuels().containsKey(targetPlayer.getUniqueId())) {
                                                Duel duel = new Duel(getPlugin());
                                                duel.setFighter1(player);
                                                duel.setFighter2(targetPlayer);
                                                ChooseArenaMenu.showGUI(duel);
                                            } else {
                                                Duel duel = BkDuel.getOngoingDuels().get(targetPlayer.getUniqueId());
                                                if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                                                    player.sendMessage(getPlugin().getLangFile().get("error.waiting-reply.others"));
                                                } else {
                                                    player.sendMessage(getPlugin().getLangFile().get("error.already-in-duel.others"));
                                                }
                                            }
                                        } else {
                                            player.sendMessage(getPlugin().getLangFile().get("error.cant-duel-self"));
                                        }
                                    } else {
                                        player.sendMessage(getPlugin().getLangFile().get("error.dead-player").replace("{player}", args[1]));
                                    }
                                } else {
                                    player.sendMessage(getPlugin().getLangFile().get("error.duel-disabled").replace("{player}", args[1]));
                                }
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get("error.invalid-player").replace("{player}", args[1]));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.no-money"));
                        }
                    } else {
                        Duel duel = BkDuel.getOngoingDuels().get(player.getUniqueId());
                        if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                            player.sendMessage(getPlugin().getLangFile().get("error.waiting-reply.self"));
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.already-in-duel.self"));
                        }
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("top"))) {
                    try {
                        int pageSize = BkDuel.getStatsManager().getRankPages().size();
                        if (pageSize > 0) {
                            int page = Integer.parseInt(args[1]) - 1;
                            if (page < pageSize) {
                                BkDuel.getStatsManager().sendRankPage(player, page);
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get("error.invalid-page").replace("{page}", args[1]));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.no-ranks"));
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                        player.sendMessage(getPlugin().getLangFile().get("error.invalid-page").replace("{page}", args[1]));
                    }
                } else if (args[0].equalsIgnoreCase(subcommands.get("spectate"))) {
                    if (!BkDuel.getOngoingDuels().containsKey(player.getUniqueId())) {
                        if (BkDuel.getOngoingDuels().keySet().size() > 0) {
                            Duel spectate = Duel.findDuel(args[1]);
                            if (spectate != null) {
                                new Teleport(getPlugin(), player, false)
                                        .setLocation(spectate.getArena().getName(), spectate.getArena().getSpectators())
                                        .setDuration(3)
                                        .setIsCancellable(true)
                                        .startTeleport();
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get("error.not-in-duel").replace("{player}", args[1]));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get("error.no-duels"));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get("error.cant-spectate"));
                    }
                }
            }
        }
        return true;
    }

    public static Hashtable<String, String> getSubCommands() {
        return subcommands;
    }

    private void broadcastToAll() {

    }
}