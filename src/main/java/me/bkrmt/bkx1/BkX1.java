package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.message.InternalMessages;
import me.bkrmt.bkx1.commands.CmdX1;
import me.bkrmt.bkx1.events.StatsUpdateEvent;
import me.bkrmt.bkx1.stats.StatsManager;
import me.bkrmt.opengui.OpenGUI;
import me.bkrmt.teleport.TeleportCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;

public final class BkX1 extends BkPlugin {
    public static BkX1 plugin;
    private static Hashtable<UUID, Duel> ongoingDuels;
    public static String prefix;
    public static Economy econ;
    private static StatsManager statsManager;

    @Override
    public void onEnable() {
        prefix = "&7[&6&lBkX1&7]&f";
        try {
            plugin = this;
            OpenGUI.INSTANCE.register(plugin);
            start(true);
            File playerData = getFile("player-data", "");
            if (!playerData.exists()) playerData.mkdir();

            getConfig("player-data", "player-purchases.yml");
            getConfig("player-data", "player-stats.yml");

            if (!setupEconomy()) {
                plugin.sendConsoleMessage(Utils.translateColor(InternalMessages.NO_ECONOMY.getMessage(plugin).replace("{0}", prefix)));
                getServer().getPluginManager().disablePlugin(this);
            } else {
                Duel duel = new Duel(plugin);
                duel.getArena();
                duel.checkAuthorization();
                duel.getKitPages();
                duel.getOptions();
                getCommandMapper().addCommand(new CommandModule(new CmdX1(this, "x1", ""), null)).registerAll();
                try {
                    TeleportCore.playersInCooldown.get("Core-Started");
                } catch (NullPointerException ignored) {
                    new TeleportCore(this);
                    TeleportCore.playersInCooldown.put("Core-Started", true);
                }
                ongoingDuels = new Hashtable<>();
                Listener constantListener = new Listener() {
                    @EventHandler
                    public void onStatUpdate(StatsUpdateEvent event) {
                        StatsManager.printStats(event.getNewStatsList());
                    }

                    @EventHandler
                    public void onRespawn(PlayerRespawnEvent event) {
                        Configuration config = getConfig("player-data", "player-inventories.yml");
                        Player player = event.getPlayer();

                        if (config.get(player.getUniqueId().toString()) != null) {
                            Duel.returnItems(plugin, player);
                        }
                    }

                    @EventHandler
                    public void onJoin(PlayerJoinEvent event) {
                        Configuration config = getConfig("player-data", "player-inventories.yml");
                        Player player = event.getPlayer();

                        if (config.get(player.getUniqueId().toString()) != null) {
                            Duel.returnItems(plugin, player);
                        }
                    }
                };
                getServer().getPluginManager().registerEvents(constantListener, plugin);
                statsManager = new StatsManager(this);
                if (isRunning()) {
                    sendStartMessage(prefix);
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
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
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Hashtable<UUID, Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public static StatsManager getStatsManager() {
        return statsManager;
    }
}
