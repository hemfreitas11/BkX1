package me.bkrmt.bkduel;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.command.HelpCmd;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.message.InternalMessages;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.events.NewTopPlayerEvent;
import me.bkrmt.bkduel.events.StatsUpdateEvent;
import me.bkrmt.bkduel.npc.NPCManager;
import me.bkrmt.bkduel.npc.NPCUpdateReason;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.bkduel.stats.StatsManager;
import me.bkrmt.opengui.OpenGUI;
import me.bkrmt.teleport.TeleportCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.*;

public final class BkDuel extends BkPlugin {
    public static String PREFIX;
    public static BkDuel PLUGIN;
    private static String chatHook;
    private static String hologramHook;
    private static Hashtable<UUID, Duel> ongoingDuels;
    private static Economy economy;
    private static StatsManager statsManager;
    private static AnimatorManager animatorManager;

    @Override
    public void onEnable() {
        PREFIX = "&7[&6&lBkDuel&7]&e";
        try {
            PLUGIN = this;
            OpenGUI.INSTANCE.register(PLUGIN);
            start(true);
            File playerData = getFile("player-data", "");
            File arenas = getFile("arenas", "");
            File kits = getFile("kits", "");
            if (!playerData.exists()) playerData.mkdir();
            if (!arenas.exists()) arenas.mkdir();
            if (!kits.exists()) kits.mkdir();
            sendConsoleMessage(Utils.translateColor("&e__________ __    &6_______               .__"));
            sendConsoleMessage(Utils.translateColor("&e\\______   \\  | __&6\\_____ \\  __ __  ____ |  |"));
            sendConsoleMessage(Utils.translateColor("&e |    |  _/  |/ / &6|   |  \\|  |  \\/ __ \\|  |"));
            sendConsoleMessage(Utils.translateColor("&e |    |   \\    <  &6|   '   \\  |  |  ___/|  |__"));
            sendConsoleMessage(Utils.translateColor("&e |______  /__|_ \\&6/______  /____/ \\___  >____/"));
            sendConsoleMessage(Utils.translateColor("&e        \\/     \\/       &6\\/           \\/"));
            sendConsoleMessage(Utils.translateColor(""));
            sendConsoleMessage(Utils.translateColor("     &e© BkPlugins | discord.gg/2MHgyjCuPc"));
            sendConsoleMessage("");

            getConfig("player-data", "player-purchases.yml");
            getConfig("player-data", "player-stats.yml");

            if (!setupEconomy()) {
                PLUGIN.sendConsoleMessage(Utils.translateColor(InternalMessages.NO_ECONOMY.getMessage(PLUGIN).replace("{0}", PREFIX)));
                getServer().getPluginManager().disablePlugin(this);
            } else {
                Listener constantListener = new Listener() {
                    @EventHandler
                    public void onStatUpdate(StatsUpdateEvent event) {
                        if (BkDuel.getHologramHook() != null && getConfig().getBoolean("top-1-npc.enabled")) {
                            if (event.getOldStatsList().size() > 0 && event.getNewStatsList().size() > 0) {
                                PlayerStats oldTopStats = event.getOldStatsList().get(0);
                                PlayerStats newTopStats = event.getNewStatsList().get(0);
                                if (oldTopStats.getDuels() != newTopStats.getDuels()) NPCManager.setTopNpc(newTopStats, NPCUpdateReason.UPDATE_STATS);
                            }
                        }
                    }

                    @EventHandler
                    public void onNewTop(NewTopPlayerEvent event) {
                        if (BkDuel.getHologramHook() != null && getConfig().getBoolean("top-1-npc.enabled"))
                            NPCManager.setTopNpc(event.getNewPlayer(), NPCUpdateReason.UPDATE_NPC);
                    }

                    @EventHandler
                    public void onRespawn(PlayerRespawnEvent event) {
                        Configuration config = getConfig("player-data", "player-inventories.yml");
                        Player player = event.getPlayer();

                        if (config.get(player.getUniqueId().toString()) != null) {
                            if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                                Duel.returnItems(PLUGIN, player, true);
                            } else {
                                player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                                config.set(player.getUniqueId().toString(), null);
                                config.save(false);
                            }
                        }
                    }

                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        Configuration config = getConfig("player-data", "player-inventories.yml");
                        Player player = event.getPlayer();

                        if (config.get(player.getUniqueId().toString()) != null) {
                            if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                                Duel.returnItems(PLUGIN, player, true);
                            } else {
                                player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                                config.set(player.getUniqueId().toString(), null);
                                config.save(false);
                            }
                        }
                    }
                };

                Duel duel = new Duel(PLUGIN);
                duel.getArena();
                duel.checkAuthorization(() -> {
                    // Configure NPC hook
                    animatorManager = new AnimatorManager(this);
                    Plugin citizens = getServer().getPluginManager().getPlugin("Citizens");
                    if (citizens != null && citizens.isEnabled()) {
                        sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.CITIZENS_FOUND.getMessage(this)));
                        hologramHook = configureHologramHook();
                        if (hologramHook != null) {
                            sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.HOLOGRAM_FOUND.getMessage(this)));
                        } else {
                            sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_HOLOGRAM.getMessage(this)));
                        }
                    } else {
                        sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_CITIZENS.getMessage(this)));
                    }

                    // Configure chat hook
                    Listener chatListener = null;
                    chatHook = configureChatHook();
                    if (chatHook == null) {
                        sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_CHAT.getMessage(this)));
                    } else {
                        switch (chatHook) {
                            case "LegendChat":
                            case "nChat":
                                chatListener = new Listener() {
                                    @EventHandler
                                    public void onChat(ChatMessageEvent event) {
                                        PlayerStats playerStat = BkDuel.getStatsManager().getStats().get(0);
                                        if (playerStat != null) {
                                            UUID topUUID = playerStat.getUUID();
                                            if (event.getTags().contains("bkduel_top") && event.getSender().getUniqueId().toString().equalsIgnoreCase(topUUID.toString()))
                                                event.setTagValue("bkduel_top", Utils.translateColor(getConfig().getString("top-1-tag")));
                                        }
                                    }
                                };
                                break;
                            case "HeroChat":
                            case "HeroChatPro":
                                break;
                        }
                    }
                    if (chatListener != null) {
                        getServer().getPluginManager().registerEvents(chatListener, PLUGIN);
                        sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.CHAT_FOUND.getMessage(this)));
                    }

                    // Create commands
                    getCommandMapper().addCommand(new CommandModule(new HelpCmd(PLUGIN, "bkduel", ""), (a, b, c, d) -> Collections.singletonList("")))
                            .addCommand(new CommandModule(new CmdDuel(this, "duel", ""), (sender, command, alias, args) -> {
                                List<String> completions = new ArrayList<>();
                                if (sender.hasPermission("bkshop.setshop")) {
                                }
                                String challenge = PLUGIN.getLangFile().get("commands.duel.subcommands.challenge.command");
                                String top = PLUGIN.getLangFile().get("commands.duel.subcommands.top.command");
                                String stats = PLUGIN.getLangFile().get("commands.duel.subcommands.stats.command");
                                String spectate = PLUGIN.getLangFile().get("commands.duel.subcommands.spectate.command");
                                String accept = PLUGIN.getLangFile().get("commands.duel.subcommands.accept.command");
                                String decline = PLUGIN.getLangFile().get("commands.duel.subcommands.decline.command");
                                String edit = PLUGIN.getLangFile().get("commands.duel.subcommands.edit.command");
                                String edit_arenas = PLUGIN.getLangFile().get("commands.duel.subcommands.edit.subcommands.arenas.command");
                                String edit_kits = PLUGIN.getLangFile().get("commands.duel.subcommands.edit.subcommands.kits.command");
                                String npc = PLUGIN.getLangFile().get("commands.duel.subcommands.npc.command");
                                String enable = PLUGIN.getLangFile().get("commands.duel.subcommands.enable.command");
                                String disable = PLUGIN.getLangFile().get("commands.duel.subcommands.disable.command");
                                String npc_update = PLUGIN.getLangFile().get("commands.duel.subcommands.npc.subcommands.update.command");
                                String npc_location = PLUGIN.getLangFile().get("commands.duel.subcommands.npc.subcommands.location.command");

                                List<String> subCommands = new ArrayList<>(Arrays.asList(challenge, top, stats, spectate, accept, decline, edit, enable, disable, npc));

                                if (args.length == 1) {
                                    String partialCommand = args[0];
                                    StringUtil.copyPartialMatches(partialCommand, subCommands, completions);
                                }else  if (args.length == 2 && (args[0].equalsIgnoreCase(challenge) || args[0].equalsIgnoreCase(stats))) {
                                    List<String> players = new ArrayList<>();
                                    for (Player player : PLUGIN.getHandler().getMethodManager().getOnlinePlayers()) {
                                        if (args[0].equalsIgnoreCase(challenge)) {
                                            if (!player.getName().equalsIgnoreCase(sender.getName()))
                                                players.add(player.getName());
                                        } else {
                                            players.add(player.getName());
                                        }
                                    }
                                    String partialName = args[1];
                                    StringUtil.copyPartialMatches(partialName, players, completions);
                                } else if (args.length == 2 && (args[0].equalsIgnoreCase(npc))) {
                                    String partialSubCommand = args[1];
                                    StringUtil.copyPartialMatches(partialSubCommand, Arrays.asList(npc_location, npc_update), completions);
                                } else if (args.length == 2 && (args[0].equalsIgnoreCase(edit))) {
                                    String partialSubCommand = args[1];
                                    StringUtil.copyPartialMatches(partialSubCommand, Arrays.asList(edit_arenas, edit_kits), completions);
                                }

                                Collections.sort(completions);
                                return completions;

                            })).registerAll();

//                    sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.LOADING_CONFIGS.getMessage(this)));
//                    sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.RESETING_ARENAS.getMessage(this)));
                    clearUsedArenas();
//                    sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.RESTORING_INVENTORIES.getMessage(this)));
                    restoreInventories();
                    if (isEnabled()) {
                        statsManager = new StatsManager(this);
                        ongoingDuels = new Hashtable<>();
                        try {
                            TeleportCore.playersInCooldown.get("Core-Started");
                        } catch (NullPointerException ignored) {
                            new TeleportCore(this);
                            TeleportCore.playersInCooldown.put("Core-Started", true);
                        }
                        getServer().getPluginManager().registerEvents(constantListener, PLUGIN);
                        if (getHologramHook() != null && getStatsManager().getStats().size() > 0)
                            NPCManager.setTopNpc(getStatsManager().getStats().get(0), NPCUpdateReason.UPDATE_NPC);
                    }
                    sendStartMessage(PREFIX);
                });
                duel.getKitPages();
                duel.getOptions();

            }
        } catch (Exception ignored) {
        }
    }

    public static String getChatHook() {
        return chatHook;
    }

    public static String getHologramHook() {
        return hologramHook;
    }

    public static Hashtable<UUID, Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static StatsManager getStatsManager() {
        return statsManager;
    }

    public static AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    private String configureHologramHook() {
        Plugin holoDisplays = getServer().getPluginManager().getPlugin("HolographicDisplays");
        Plugin holograms = getServer().getPluginManager().getPlugin("Holograms");

        if (holoDisplays != null) {
            if (holoDisplays.isEnabled()) {
                return "Holograms";
            }
        }

        if (holograms != null) {
            if (holograms.isEnabled()) {
                return "HolographicDisplays";
            }
        }

        return null;
    }

    private String configureChatHook() {
        Plugin legendchat = getServer().getPluginManager().getPlugin("Legendchat");
        Plugin nchat = getServer().getPluginManager().getPlugin("nChat");
        Plugin herochat = getServer().getPluginManager().getPlugin("Herochat");
        Plugin herochatPro = getServer().getPluginManager().getPlugin("HerochatPro");

        if (legendchat != null) {
            if (legendchat.isEnabled()) {
                return "LegendChat";
            }
        }

        if (nchat != null) {
            if (nchat.isEnabled()) {
                return "nChat";
            }
        }

        if (herochat != null) {
            if (herochat.isEnabled()) {
                return "HeroChat";
            }
        }

        if (herochatPro != null) {
            if (herochatPro.isEnabled()) {
                return "HeroChatPro";
            }
        }

        return null;
    }

    @Override
    public void onDisable() {
        if (isRunning()) {
            if (getHologramHook() != null) NPCManager.removeNpc(NPCUpdateReason.UPDATE_NPC);

            if (ongoingDuels != null && !ongoingDuels.isEmpty()) {
                Collection<Duel> duels = ongoingDuels.values();
                if (!duels.isEmpty()) {
                    for (Duel duel : duels) {
                        duel.endWithoutPlayers();
                        duel.finish(duel.getFighter1().getUniqueId(), duel.getFighter2().getUniqueId());
                    }
                }
            }
            if (getConfig().getBoolean("close-inventory-on-reload")) {
                for (Player player : getHandler().getMethodManager().getOnlinePlayers()) {
                    player.closeInventory();
                }
            }
        }
    }

    private void restoreInventories() {
        Configuration config = getConfig("player-data", "player-inventories.yml");
        for (Player player : getHandler().getMethodManager().getOnlinePlayers()) {

            if (config.get(player.getUniqueId().toString()) != null) {
                if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                    Duel.returnItems(PLUGIN, player, true);
                } else {
                    player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                    config.set(player.getUniqueId().toString(), null);
                    config.save(false);
                }
            }
        }
    }

    private void clearUsedArenas() {
        File[] listFiles = PLUGIN.getFile("", "arenas").listFiles();
        if (listFiles.length > 0) {
            for (File arena : listFiles) {
                Configuration config = getConfig("arenas", arena.getName());
                config.set("in-use", false);
                config.save(false);
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
