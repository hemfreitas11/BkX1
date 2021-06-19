package me.bkrmt.bkduel;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import me.NoChance.PvPManager.PvPManager;
import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.command.CommandModule;
import me.bkrmt.bkcore.command.HelpCmd;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.message.InternalMessages;
import me.bkrmt.bkcore.message.LangFile;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.commands.CmdDuelCompleter;
import me.bkrmt.bkduel.npc.NPCManager;
import me.bkrmt.bkduel.npc.UpdateReason;
import me.bkrmt.bkduel.placeholder.PlaceholderLangFile;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.bkrmt.bkduel.stats.StatsManager;
import me.bkrmt.bkteleport.TpaUtils;
import me.bkrmt.opengui.OpenGUI;
import me.bkrmt.teleport.TeleportCore;
import net.milkbowl.vault.economy.Economy;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BkDuel extends BkPlugin {
    private static BkDuel instance;
    public String PREFIX;
    private ConcurrentHashMap<UUID, Duel> ongoingDuels;
    private Economy economy;
    private PlaceholderLangFile placeholderLangFile;
    private NPCManager npcManager;
    private StatsManager statsManager;
    private HookManager hookManager;
    private AnimatorManager animatorManager;

    @Override
    public void onEnable() {
        PREFIX = "&7[&6&lBkDuel&7]&e";
        try {
            instance = this;
            npcManager = null;
            OpenGUI.INSTANCE.register(instance);
            start(true);

            try {
                TeleportCore.INSTANCE.getPlayersInCooldown().get("Core-Started");
            } catch (Exception ignored) {
                TeleportCore.INSTANCE.start(this);
            }

            hookManager = new HookManager(this);
            ArrayList<String> langs = new ArrayList<>();
            langs.add("en_US");
            langs.add("pt_BR");
            placeholderLangFile = new PlaceholderLangFile(this, langs);
            File playerData = getFile("player-data", "");
            File arenas = getFile("arenas", "");
            File kits = getFile("kits", "");
            if (!playerData.exists()) playerData.mkdir();
            if (!arenas.exists()) arenas.mkdir();
            if (!kits.exists()) kits.mkdir();
            sendConsoleMessage("§e__________ __    §6_______               .__");
            sendConsoleMessage("§e\\______   \\  | __§6\\_____ \\  __ __  ____ |  |");
            sendConsoleMessage("§e |    |  _/  |/ / §6|   |  \\|  |  \\/ __ \\|  |");
            sendConsoleMessage("§e |    |   \\    <  §6|   '   \\  |  |  ___/|  |__");
            sendConsoleMessage("§e |______  /__|_ \\§6/______  /____/ \\___  >____/");
            sendConsoleMessage("§e        \\/     \\/       §6\\/           \\/");
            sendConsoleMessage("");
            sendConsoleMessage("     §e© BkPlugins | discord.gg/2MHgyjCuPc");
            sendConsoleMessage("");

            getConfig("player-data", "player-purchases.yml");
            getConfig("player-data", "player-stats.yml");

            if (!setupEconomy()) {
                sendConsoleMessage(Utils.translateColor(InternalMessages.NO_ECONOMY.getMessage(instance).replace("{0}", PREFIX)));
                getServer().getPluginManager().disablePlugin(this);
            } else {
                setRunning(true);
                getHookManager().setupHooks();
                animatorManager = new AnimatorManager(this);

                // Create commands
                getCommandMapper()
                        .addCommand(new CommandModule(new HelpCmd(instance, "bkduel", ""), (a, b, c, d) -> Collections.singletonList("")))
                        .addCommand(new CommandModule(new CmdDuel(this, "duel", ""), new CmdDuelCompleter()))
                        .registerAll();

                // Other
                clearUsedArenas();
                restoreInventories();
                statsManager = new StatsManager(this);
                ongoingDuels = new ConcurrentHashMap<>();
                getServer().getPluginManager().registerEvents(new ConstantListener(), instance);
                if (getHookManager().hasHologramHook() && getStatsManager().getStats().size() > 0 && getNpcManager() != null)
                    getNpcManager().setTopNpc(getStatsManager().getStats().get(0), UpdateReason.NPC_AND_STATS);
                sendStartMessage(PREFIX);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public String getPlaceholderValue(OfflinePlayer player, String identifier) {
        String returnValue = null;
        PlayerStats stats = null;
        StatsManager manager = getStatsManager();
        if (manager != null) {
            if (player != null)
                stats = manager.getPlayerStat(player.getUniqueId());

            if (stats != null && identifier != null) {
                if (identifier.equalsIgnoreCase("defeats")) {
                    returnValue = String.valueOf(stats.getDefeats());
                } else if (identifier.equalsIgnoreCase("wins")) {
                    returnValue = String.valueOf(stats.getWins());
                } else if (identifier.equalsIgnoreCase("duels")) {
                    returnValue = String.valueOf(stats.getDuels());
                } else if (identifier.equalsIgnoreCase("disconnects")) {
                    returnValue = String.valueOf(stats.getDisconnects());
                } else if (identifier.equalsIgnoreCase("kdr")) {
                    returnValue = String.valueOf(stats.getKDR());
                }
            }
        }
        return returnValue;
    }

    public boolean isValidPlaceholder(String hologramLine) {
        return hologramLine.contains("%") && getHookManager().hasPlaceHolderHook();
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public static BkDuel getInstance() {
        return instance;
    }

    public ConcurrentHashMap<UUID, Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public Economy getEconomy() {
        return economy;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public AnimatorManager getAnimatorManager() {
        return animatorManager;
    }

    public boolean isInvalidChallenge(Player sender, Player target, double duelCost) {
        double targetMoney = BkDuel.getInstance().getEconomy().getBalance(target);
        if (targetMoney < duelCost) {
            sender.sendMessage(getLangFile().get(target, "error.no-money.others").replace("{player}", target.getName()));
            return true;
        }

        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(sender.getName()) != null) {
            sender.sendMessage(getLangFile().get(target, "error.waiting-teleport.self"));
            return true;
        }

        if (TeleportCore.INSTANCE.getPlayersInCooldown().get(target.getName()) != null) {
            sender.sendMessage(getLangFile().get(target, "error.waiting-teleport.others").replace("{player}", target.getName()));
            return true;
        }

        Plugin bkteleport = getServer().getPluginManager().getPlugin("BkTeleport");
        if (bkteleport != null && bkteleport.isEnabled()) {
            for (String key : TpaUtils.playerExpiredChecker.keySet()) {
                if (key.toLowerCase().contains(target.getName().toLowerCase())) {
                    sender.sendMessage(getLangFile().get(target, "error.waiting-teleport.others").replace("{player}", target.getName()));
                    return true;
                }
                if (key.toLowerCase().contains(sender.getName().toLowerCase())) {
                    sender.sendMessage(getLangFile().get(sender, "error.waiting-teleport.self"));
                    return true;
                }
            }
        }
        if (isInCombat(sender, target)) {
            return true;
        }
        return false;
    }

    private String getHologramHook() {
        Plugin holoDisplays = getServer().getPluginManager().getPlugin("HolographicDisplays");
        Plugin holograms = getServer().getPluginManager().getPlugin("Holograms");

        String returnValue = null;
        if (holoDisplays != null) {
            if (holoDisplays.isEnabled()) {
                returnValue = "Holograms";
            }
        } else if (holograms != null) {
            if (holograms.isEnabled()) {
                returnValue = "HolographicDisplays";
            }
        }

        return returnValue;
    }

    @Override
    public LangFile getLangFile() {
        return placeholderLangFile;
    }

    String getChatHook() {
        Plugin legendchat = getServer().getPluginManager().getPlugin("Legendchat");
        Plugin nchat = getServer().getPluginManager().getPlugin("nChat");
        Plugin herochat = getServer().getPluginManager().getPlugin("Herochat");
        Plugin herochatPro = getServer().getPluginManager().getPlugin("HerochatPro");

        String returnPlugin = null;
        if (legendchat != null) {
            if (legendchat.isEnabled()) {
                returnPlugin = "LegendChat";
            }
        } else if (nchat != null) {
            if (nchat.isEnabled()) {
                returnPlugin = "nChat";
            }
        } else if (herochat != null) {
            if (herochat.isEnabled()) {
                returnPlugin = "HeroChat";
            }
        } else if (herochatPro != null) {
            if (herochatPro.isEnabled()) {
                returnPlugin = "HeroChatPro";
            }
        }
        return returnPlugin;
    }

    @Override
    public void onDisable() {
        if (isRunning()) {
            if (getHookManager().hasHologramHook()) getNpcManager().remove(UpdateReason.NPC_AND_STATS);

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

    public HookManager getHookManager() {
        return hookManager;
    }

    private void restoreInventories() {
        Configuration config = getConfig("player-data", "player-inventories.yml");
        for (Player player : getHandler().getMethodManager().getOnlinePlayers()) {

            if (config.get(player.getUniqueId().toString()) != null) {
                if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
                    Duel.returnItems(instance, player, true);
                } else {
                    player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));
                    config.set(player.getUniqueId().toString(), null);
                    config.save(false);
                }
            }
        }
    }

    public boolean isInCombat(Player sender, Player target) {
        Plugin combatPlugin = getHookManager().getCombatHook();
        if (combatPlugin != null) {
            boolean self = target == null;
            String errorMessage = getLangFile().get(self ? sender : target, "error.in-combat." + (self ? "self" : "others")).replace("{player}", self ? "" : target.getName());
            switch (combatPlugin.getName()) {
                case "CombatLogX":
                    ICombatLogX combatLog = (ICombatLogX) combatPlugin;
                    if (combatLog.getCombatManager().isInCombat(self ? sender : target)) {
                        sender.sendMessage(errorMessage);
                        return true;
                    }
                    break;
                case "PvPManager":
                    PvPManager pvpmanager = (PvPManager) combatPlugin;
                    if (pvpmanager.getPlayerHandler().get(self ? sender : target).isInCombat()) {
                        sender.sendMessage(errorMessage);
                        return true;
                    }
                    break;
                case "CombatTagPlus":
                    CombatTagPlus combatTag = (CombatTagPlus) combatPlugin;
                    if (combatTag.getTagManager().isTagged((self ? sender : target).getUniqueId())) {
                        sender.sendMessage(errorMessage);
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public void setNpcManager(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    private void clearUsedArenas() {
        File[] listFiles = getFile("", "arenas").listFiles();
        if (listFiles != null && listFiles.length > 0) {
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