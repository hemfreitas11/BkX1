package me.bkrmt.bkduel.commands;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.Executor;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.Duel;
import me.bkrmt.bkduel.api.events.DuelAcceptEvent;
import me.bkrmt.bkduel.api.events.DuelDeclineEvent;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.enums.DuelStatus;
import me.bkrmt.bkduel.menus.ChooseArenaMenu;
import me.bkrmt.bkduel.menus.ChooseKitsMenu;
import me.bkrmt.bkduel.menus.PlayerProfileMenu;
import me.bkrmt.bkduel.npc.UpdateReason;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.teleport.Teleport;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Hashtable;

public class CmdDuel extends Executor {
    private static Hashtable<String, String> subcommands;

    public CmdDuel(BkDuel plugin, String langKey, String permission) {
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

            if (BkDuel.getInstance().isInCombat(player, null)) return true;

            if (args.length == 0) {
                sendUsage(commandSender);
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase(subcommands.get("enable"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.toggle")) return true;
                    toggleCommand(player, false, "info.duel-enabled");
                } else if (args[0].equalsIgnoreCase(subcommands.get("disable"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.toggle")) return true;
                    toggleCommand(player, true, "info.duel-disabled");
                } else if (args[0].equalsIgnoreCase(subcommands.get("edit"))) {
                    if (notHasPermission(player, "bkduel.admin", "bkduel.edit")) return true;
                    wrongCommandUsage(player, "commands.duel.subcommands.edit.usage");
                } else if (args[0].equalsIgnoreCase(subcommands.get("top"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.top")) return true;
                    topCommand(player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("spectate"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.spectate")) return true;
                    spectateCommand((Player) commandSender, player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("stats"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.stats")) return true;
                    wrongCommandUsage(player, "commands.duel.subcommands.stats.usage");
                } else if (args[0].equalsIgnoreCase(subcommands.get("accept"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.reply")) return true;
                    acceptCommand(player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("decline"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.reply")) return true;
                    declineCommand(player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("challenge"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.challenge")) return true;
                    wrongCommandUsage(player, "commands.duel.subcommands.challenge.usage");
                } else if (args[0].equalsIgnoreCase(subcommands.get("npc"))) {
                    if (notHasPermission(player, "bkduel.admin", "bkduel.npc")) return true;
                    wrongCommandUsage(player, "commands.duel.subcommands.npc.usage");
                } else {
                    sendUsage(commandSender);
                    return true;
                }
            } else if (args.length == 2) {
                Player target = Utils.getPlayer(args[1]);
                if (args[0].equalsIgnoreCase(subcommands.get("stats"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.stats")) return true;
                    if (statsCommand(args, player, target)) return true;
                } else if (args[0].equalsIgnoreCase(subcommands.get("npc"))) {
                    if (notHasPermission(player, "bkduel.admin", "bkduel.npc")) return true;
                    npcCommand(commandSender, args, player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("edit"))) {
                    if (notHasPermission(player, "bkduel.admin", "bkduel.edit")) return true;
                    editCommand((Player) commandSender, args, player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("challenge"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.challenge")) return true;
                    challengeCommand(args, player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("top"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.top")) return true;
                    topCommandPage(args, player);
                } else if (args[0].equalsIgnoreCase(subcommands.get("spectate"))) {
                    if (notHasPermission(player, "bkduel.player", "bkduel.spectate")) return true;
                    spectatePlayerCommand(args, player);
                }
            }
        }
        return true;
    }

    private boolean notHasPermission(Player player, String s, String s2) {
        if (!player.hasPermission(s) && !player.hasPermission(s2)) {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-permission"));
            return true;
        }
        return false;
    }

    private void spectatePlayerCommand(String[] args, Player player) {
        if (!BkDuel.getInstance().getOngoingDuels().containsKey(player.getUniqueId())) {
            if (BkDuel.getInstance().getOngoingDuels().keySet().size() > 0) {
                Duel spectate = Duel.findDuel(args[1]);
                if (spectate != null) {
                    new Teleport(getPlugin(), player, true)
                            .setLocation(AnimatorManager.cleanText(spectate.getArena().getName()), spectate.getArena().getSpectators())
                            .setDuration(getPlugin().getConfigManager().getConfig().getInt("teleport-countdown.spectator-countdown"))
                            .setIsCancellable(true)
                            .startTeleport();
                } else {
                    player.sendMessage(getPlugin().getLangFile().get(player, "error.not-in-duel").replace("{player}", args[1]));
                }
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.no-duels"));
            }
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.cant-spectate"));
        }
    }

    private void topCommandPage(String[] args, Player player) {
        try {
            int pageSize = BkDuel.getInstance().getStatsManager().getRankPages().size();
            if (pageSize > 0) {
                int page = Integer.parseInt(args[1]) - 1;
                if (page < pageSize) {
                    BkDuel.getInstance().getStatsManager().sendRankPage(player, page);
                } else {
                    player.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-page").replace("{page}", args[1]));
                }
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.no-ranks"));
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-page").replace("{page}", args[1]));
        }
    }

    private void challengeCommand(String[] args, Player player) {
        if (getPlugin().getFile("kits", "").listFiles().length > 0) {
            if (getPlugin().getFile("arenas", "").listFiles().length > 0) {
                if (!BkDuel.getInstance().getOngoingDuels().containsKey(player.getUniqueId())) {
                    double playerMoney = BkDuel.getInstance().getEconomy().getBalance(player);
                    double duelCost = getPlugin().getConfigManager().getConfig().getDouble("duel-cost");

                    if (playerMoney >= duelCost) {
                        Player targetPlayer = Utils.getPlayer(args[1]);
                        if (targetPlayer != null) {
                            if (BkDuel.getInstance().isInvalidChallenge(player, targetPlayer, duelCost)) return;

                            Configuration config = getPlugin().getConfigManager().getConfig("player-data", "player-stats.yml");
                            if (!config.getBoolean(targetPlayer.getUniqueId().toString() + ".duel-disabled")) {
                                if (!targetPlayer.isDead()) {
                                    if (!targetPlayer.getUniqueId().equals(player.getUniqueId())) {
                                        if (!BkDuel.getInstance().getOngoingDuels().containsKey(targetPlayer.getUniqueId())) {
                                            Duel duel = new Duel();
                                            duel.setFighter1(player);
                                            duel.setFighter2(targetPlayer);
                                            ChooseArenaMenu.showGUI(duel, null);
                                        } else {
                                            Duel duel = BkDuel.getInstance().getOngoingDuels().get(targetPlayer.getUniqueId());
                                            if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                                                player.sendMessage(getPlugin().getLangFile().get(player, "error.waiting-reply.others"));
                                            } else {
                                                player.sendMessage(getPlugin().getLangFile().get(player, "error.already-in-duel.others"));
                                            }
                                        }
                                    } else {
                                        player.sendMessage(getPlugin().getLangFile().get(player, "error.cant-duel-self"));
                                    }
                                } else {
                                    player.sendMessage(getPlugin().getLangFile().get(player, "error.dead-player").replace("{player}", args[1]));
                                }
                            } else {
                                player.sendMessage(getPlugin().getLangFile().get(player, "error.duel-disabled").replace("{player}", args[1]));
                            }
                        } else {
                            player.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-player").replace("{player}", args[1]));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get(player, "error.no-money.self"));
                    }
                } else {
                    Duel duel = BkDuel.getInstance().getOngoingDuels().get(player.getUniqueId());
                    if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                        player.sendMessage(getPlugin().getLangFile().get(player, "error.waiting-reply.self"));
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get(player, "error.already-in-duel.self"));
                    }
                }
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.no-arenas"));
            }
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-kits"));
        }
    }

    private void editCommand(Player commandSender, String[] args, Player player) {
        if (args[1].equalsIgnoreCase(subcommands.get("edit.arenas"))) {
            ChooseArenaMenu.showGUI(new Duel(true).setFighter1(commandSender), null);
        } else if (args[1].equalsIgnoreCase(subcommands.get("edit.kits"))) {
            ChooseKitsMenu.showGUI(new Duel(true).setFighter1(commandSender), null);
        } else {
            wrongCommandUsage(player, "commands.duel.subcommands.edit.usage");
        }
    }

    private void npcCommand(CommandSender commandSender, String[] args, Player player) {
        if (args[1].equalsIgnoreCase(subcommands.get("npc.location"))) {
            Configuration config = getPlugin().getConfigManager().getConfig();
            config.setLocation("top-1-npc.npc.location", ((Player) commandSender).getLocation());
            config.saveToFile();
            commandSender.sendMessage(getPlugin().getLangFile().get(player, "info.location-set"));
        } else if (args[1].equalsIgnoreCase(subcommands.get("npc.update"))) {
            if (BkDuel.getInstance().getHookManager().hasHologramHook()) {
                if (BkDuel.getInstance().getStatsManager().getStats().size() > 0) {
                    BkDuel.getInstance().getNpcManager().setTopNpc(BkDuel.getInstance().getStatsManager().getStats().get(0), UpdateReason.NPC_AND_STATS);
                    commandSender.sendMessage(getPlugin().getLangFile().get(player, "info.npc-updated"));
                } else {
                    commandSender.sendMessage(getPlugin().getLangFile().get(player, "error.no-ranks"));
                }
            } else {
                commandSender.sendMessage(getPlugin().getLangFile().get(player, "error.no-npc"));
            }
        } else {
            wrongCommandUsage(player, "commands.duel.subcommands.npc.usage");
        }
    }

    private boolean statsCommand(String[] args, Player player, Player target) {
        PlayerStats stat = BkDuel.getInstance().getStatsManager().getPlayerStat(args[1]);
        if (stat == null) {
            if (target != null) {
                stat = BkDuel.getInstance().getStatsManager().createStats(target);
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.invalid-player").replace("{player}", args[1]));
                return true;
            }
        }
        PlayerProfileMenu.showGui((BkDuel) getPlugin(), player, stat);
        return false;
    }

    private void declineCommand(Player player) {
        if (BkDuel.getInstance().getOngoingDuels().containsKey(player.getUniqueId())) {
            Duel duel = BkDuel.getInstance().getOngoingDuels().get(player.getUniqueId());
            if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                    duel.getRequest().getExpireRunnable().cancel();

                    Player fighter1 = duel.getFighter1();
                    Player fighter2 = duel.getFighter2();
                    fighter1.sendMessage(getPlugin().getLangFile().get(player, "info.duel-declined.self").replace("{player}", fighter2.getName()));
                    fighter2.sendMessage(getPlugin().getLangFile().get(player, "info.duel-declined.others").replace("{player}", fighter1.getName()));

                    Bukkit.getPluginManager().callEvent(new DuelDeclineEvent(fighter1, fighter2));

                    //Broadcast to all
                    broadcastToAll();

                    BkDuel.getInstance().getOngoingDuels().remove(fighter1.getUniqueId());
                    BkDuel.getInstance().getOngoingDuels().remove(fighter2.getUniqueId());
                    duel = null;
                } else {
                    player.sendMessage(getPlugin().getLangFile().get(player, "error.duel-already-started"));
                }
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.no-challenge"));
            }
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-challenge"));
        }
    }

    private void acceptCommand(Player player) {
        if (BkDuel.getInstance().getOngoingDuels().containsKey(player.getUniqueId())) {
            Duel duel = BkDuel.getInstance().getOngoingDuels().get(player.getUniqueId());
            if (player.getUniqueId().equals(duel.getFighter2().getUniqueId())) {
                if (duel.getStatus().equals(DuelStatus.AWAITING_REPLY)) {
                    double playerMoney = BkDuel.getInstance().getEconomy().getBalance(duel.getFighter1());
                    double duelCost = getPlugin().getConfigManager().getConfig().getDouble("duel-cost");

                    Player otherPlayer = duel.getFighter1();

                    if (playerMoney >= duelCost) {
                        EconomyResponse r = BkDuel.getInstance().getEconomy().withdrawPlayer(player, duelCost);
                        EconomyResponse r2 = BkDuel.getInstance().getEconomy().withdrawPlayer(otherPlayer, duelCost);
                        if (r.transactionSuccess() && r2.transactionSuccess()) {
                            String paid = getPlugin().getLangFile().get(player, "info.cost-paid").replace("{amount}", String.valueOf(duelCost)).replace("{balance}", String.valueOf(r2.balance));
                            player.sendMessage(paid);
                            otherPlayer.sendMessage(paid);
                            duel.getRequest().getExpireRunnable().cancel();

                            duel.startDuel();
                            Bukkit.getPluginManager().callEvent(new DuelAcceptEvent(duel.getFighter1(), duel.getFighter2()));
                        } else {
                            String errorMessage = r.transactionSuccess() ? r2.errorMessage : r.errorMessage;
                            player.sendMessage(String.format("An error occured: %s", errorMessage));
                        }
                    } else {
                        player.sendMessage(getPlugin().getLangFile().get(player, "error.no-money.self"));
                    }
                } else {
                    player.sendMessage(getPlugin().getLangFile().get(player, "error.duel-already-started"));
                }
            } else {
                player.sendMessage(getPlugin().getLangFile().get(player, "error.no-challenge"));
            }
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-challenge"));
        }
    }

    private void spectateCommand(Player commandSender, Player player) {
        if (BkDuel.getInstance().getOngoingDuels().keySet().size() > 0) {
            Duel spectate = new Duel().setFighter1(commandSender);
            spectate.getOptions().add(DuelOptions.SPECTATOR_MODE);
            ChooseArenaMenu.showGUI(spectate, null);
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-duels"));
        }
    }

    private void topCommand(Player player) {
        int pageSize = BkDuel.getInstance().getStatsManager().getRankPages().size();
        if (pageSize > 0) {
            BkDuel.getInstance().getStatsManager().sendRankPage(player, 0);
        } else {
            player.sendMessage(getPlugin().getLangFile().get(player, "error.no-ranks"));
        }
    }

    private void wrongCommandUsage(Player player, String s) {
        player.sendMessage(getPlugin().getLangFile().get(player, "commands.usage-format")
                .replace("{usage}", getPlugin().getLangFile().get(player, s)));
    }

    private void toggleCommand(Player player, boolean b, String s) {
        Configuration config = getPlugin().getConfigManager().getConfig("player-data", "player-stats.yml");
        config.set(player.getUniqueId().toString() + ".duel-disabled", b);
        config.saveToFile();
        player.sendMessage(getPlugin().getLangFile().get(player, s));
    }

    public static Hashtable<String, String> getSubCommands() {
        return subcommands;
    }

    private void broadcastToAll() {

    }
}