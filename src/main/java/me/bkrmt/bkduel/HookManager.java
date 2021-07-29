package me.bkrmt.bkduel;

import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.factions.event.EventFactionsPvpDisallowed;
import com.massivecraft.factions.event.PowerLossEvent;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkduel.npc.NPCManager;
import me.bkrmt.bkduel.placeholder.PAPIExpansion;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.events.AddKillEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class HookManager {
    private final BkDuel bkDuel;

    public HookManager(BkDuel bkDuel) {
        this.bkDuel = bkDuel;
    }

    public boolean hasChatHook() {
        Plugin legendchat = bkDuel.getServer().getPluginManager().getPlugin("Legendchat");
        Plugin nchat = bkDuel.getServer().getPluginManager().getPlugin("nChat");
        Plugin herochat = bkDuel.getServer().getPluginManager().getPlugin("Herochat");
        Plugin herochatPro = bkDuel.getServer().getPluginManager().getPlugin("HerochatPro");
        return (legendchat != null && legendchat.isEnabled()) ||
                (nchat != null && nchat.isEnabled()) ||
                (herochat != null && herochat.isEnabled()) ||
                (herochatPro != null && herochatPro.isEnabled());
    }

    public boolean hasPlaceHolderHook() {
        Plugin papi = bkDuel.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        return (papi != null && papi.isEnabled());
    }

    public Plugin getMcMMOHook() {
        Plugin mcmmo = bkDuel.getServer().getPluginManager().getPlugin("mcMMO");
        return (mcmmo != null && mcmmo.isEnabled()) ? mcmmo : null;
    }

    public Plugin getMyPetHook() {
        Plugin myPet = bkDuel.getServer().getPluginManager().getPlugin("MyPet");
        return (myPet != null && myPet.isEnabled()) ? myPet : null;
    }

    public Plugin getSimpleClansHook() {
        Plugin clans = bkDuel.getServer().getPluginManager().getPlugin("SimpleClans");
        return (clans != null && clans.isEnabled()) ? clans : null;
    }

    public boolean hasHologramHook() {
        Plugin holo = bkDuel.getServer().getPluginManager().getPlugin("Holograms");
        Plugin holoapi = bkDuel.getServer().getPluginManager().getPlugin("HolographicDisplays");
        return (holo != null && holo.isEnabled()) ||
                (holoapi != null && holoapi.isEnabled());
    }

    public Plugin getFactionHook() {
        Plugin factions = bkDuel.getServer().getPluginManager().getPlugin("Factions");
        return (factions != null && factions.isEnabled()) ? factions : null;
    }

    public String getFactionsName(Plugin plugin) {
        if (plugin != null) {
            try {
                Class.forName("com.massivecraft.factions.event.EventFactionsPvpDisallowed");
                return "FactionsLegacy";
            } catch (Exception ignored) {
                return "FactionsUUID";
            }
        }
        return null;
    }

    public Plugin getCombatHook() {
        Plugin combatLog = bkDuel.getServer().getPluginManager().getPlugin("CombatLogX");
        Plugin pvpManager = bkDuel.getServer().getPluginManager().getPlugin("PvPManager");
        Plugin combatTag = bkDuel.getServer().getPluginManager().getPlugin("CombatTagPlus");
        return (combatLog != null && combatLog.isEnabled()) ? combatLog :
                (pvpManager != null && pvpManager.isEnabled()) ? pvpManager :
                (combatTag != null && combatTag.isEnabled()) ? combatTag : null;
    }

    public void setupHooks() {
        Plugin papi = bkDuel.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.PLACEHOLDER_FOUND.getMessage(bkDuel)).replace("{0}", "PlaceholderAPI"));
            new PAPIExpansion(bkDuel).register();
        }

        Plugin bkshop = bkDuel.getServer().getPluginManager().getPlugin("BkShop");
        Plugin bkteleport = bkDuel.getServer().getPluginManager().getPlugin("BkTeleport");
        if (bkshop != null && bkshop.isEnabled() || bkteleport != null && bkteleport.isEnabled()) {
            bkDuel.sendConsoleMessage(Utils.translateColor(InternalMessages.OWN_PLUGINS_FOUND.getMessage(bkDuel)).replace("{0}", "PlaceholderAPI"));
        }

        Plugin combatPlugin = bkDuel.getHookManager().getCombatHook();
        if (combatPlugin != null) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.COMBATLOGGER_FOUND.getMessage(bkDuel)).replace("{0}", combatPlugin.getName()));
        }

        if (bkDuel.getHookManager().getMyPetHook() != null) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.MYPET_FOUND.getMessage(bkDuel)));
        }

        if (bkDuel.getHookManager().getSimpleClansHook() != null) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.SIMPLECLANS_FOUND.getMessage(bkDuel)));
            if (bkDuel.getConfigManager().getConfig().getBoolean("simpleclans.disable-death-incremet")) {
                Listener simpleClansListener = new Listener() {
                    @EventHandler
                    public void onDeathAdd(AddKillEvent event) {
                        ClanPlayer attacker = event.getAttacker();
                        ClanPlayer victim = event.getVictim();
                        if (bkDuel.getOngoingDuels().containsKey(attacker.getUniqueId()) || bkDuel.getOngoingDuels().containsKey(victim.getUniqueId())) {
                            event.setCancelled(true);
                        }
                    }
                };
                bkDuel.getServer().getPluginManager().registerEvents(simpleClansListener, bkDuel);
            }
        }

        if (bkDuel.getHookManager().getMcMMOHook() != null) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.MCMMO_FOUND.getMessage(bkDuel)));
            if (bkDuel.getConfigManager().getConfig().getBoolean("mcmmo.disable-abilities")) {
                Listener mcmmoListener = new Listener() {
                    @EventHandler
                    public void onAbility(McMMOPlayerAbilityActivateEvent event) {
                        Player player = event.getPlayer();
                        if (bkDuel.getOngoingDuels().containsKey(player.getUniqueId())) {
                            event.setCancelled(true);
                            player.sendMessage(bkDuel.getLangFile().get("error.no-ability-in-duel"));
                        }
                    }
                };
                bkDuel.getServer().getPluginManager().registerEvents(mcmmoListener, bkDuel);
            }
        }

        Plugin factions = bkDuel.getHookManager().getFactionHook();
        if (factions != null) {
            String factionsName = bkDuel.getHookManager().getFactionsName(factions);
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.FACTIONS_FOUND.getMessage(bkDuel).replace("{0}", factionsName)));
            Listener factionsListener = null;
            boolean allyDamage = bkDuel.getConfigManager().getConfig().getBoolean("factions.enable-ally-damage");
            boolean disablePowerLoss = bkDuel.getConfigManager().getConfig().getBoolean("factions.disable-power-loss");
            if (factionsName.equalsIgnoreCase("FactionsLegacy")) {
                factionsListener = new Listener() {
                    @EventHandler
                    public void onPowerLoss(EventFactionsPowerChange event) {
                        if (disablePowerLoss && bkDuel.getOngoingDuels().containsKey(event.getMPlayer().getUuid())) {
                            Player player = event.getMPlayer().getPlayer();
                            event.setCancelled(true);
                            player.sendMessage(bkDuel.getLangFile().get("info.power-loss-negated"));
                        }
                    }

                    @EventHandler
                    public void onPowerLoss(EventFactionsPvpDisallowed event) {
                        Player attacker = event.getAttacker();
                        Player defender = event.getDefender();
                        if (!allyDamage) return;
                        if ((attacker != null && bkDuel.getOngoingDuels().containsKey(attacker.getUniqueId())) &&
                                bkDuel.getOngoingDuels().containsKey(defender.getUniqueId())) event.setCancelled(true);
                    }
                };
            } else if (factionsName.equalsIgnoreCase("FactionsUUID")) {
                factionsListener = new Listener() {
                    @EventHandler
                    public void onPowerLoss(PowerLossEvent event) {
                        if (disablePowerLoss && bkDuel.getOngoingDuels().containsKey(event.getfPlayer().getPlayer().getUniqueId())) {
                            Player player = event.getfPlayer().getPlayer();
                            event.setCancelled(true);
                            player.sendMessage(bkDuel.getLangFile().get("info.power-loss-negated"));
                        }

                    }
                };
            }
            if (factionsListener == null) {
                bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.IMCOMPATIBLE_FACTIONS.getMessage()));
            } else {
                bkDuel.getServer().getPluginManager().registerEvents(factionsListener, bkDuel);
            }
        }
        
        Plugin citizens = bkDuel.getServer().getPluginManager().getPlugin("Citizens");
        if (citizens != null && citizens.isEnabled()) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.CITIZENS_FOUND.getMessage(bkDuel)));
            if (bkDuel.getHookManager().hasHologramHook()) {
                bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.HOLOGRAM_FOUND.getMessage(bkDuel)));
                bkDuel.setNpcManager(new NPCManager(bkDuel));
            } else {
                bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_HOLOGRAM.getMessage(bkDuel)));
            }
        } else {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_CITIZENS.getMessage(bkDuel)));
        }

        Listener chatListener = null;
        String chatHook = bkDuel.getChatHook();
        if (!bkDuel.getHookManager().hasChatHook()) {
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NO_CHAT.getMessage(bkDuel)));
        } else {
            switch (chatHook) {
                case "LegendChat":
                case "nChat":
                    chatListener = new LegendListener(bkDuel);
                    break;
                case "HeroChat":
                case "HeroChatPro":
                    break;
            }
        }

        if (chatListener != null) {
            bkDuel.getServer().getPluginManager().registerEvents(chatListener, bkDuel);
            bkDuel.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.CHAT_FOUND.getMessage(bkDuel)));
        }
    }

}
