package me.bkrmt.bkduel.npc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.textanimator.TextAnimator;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.InternalMessages;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.stats.PlayerStats;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;

import static me.bkrmt.bkduel.BkDuel.PLUGIN;

public class NPCManager {
    static private NPC topNpc;
    static private Hologram topHologram;
    static private BukkitTask neabyChecker = null;
    static private Listener interactListener = null;
    static HashMap<Integer, TextAnimator> animators = null;

    public static void setTopNpc(final PlayerStats newTop, NPCUpdateReason reason) {
        boolean npcEnabled = PLUGIN.getConfig().getBoolean("top-1-npc.enabled");
        if (npcEnabled && BkDuel.getHologramHook() != null) {
            removeNpc(reason);
            boolean swordAnim = PLUGIN.getConfig().getBoolean("top-1-npc.hologram.sword-animation");
            List<String> hologramLines = null;
            try {
                hologramLines = PLUGIN.getConfig().getStringList("top-1-npc.hologram.lines");
            } catch (Exception ignored) {
                PLUGIN.sendConsoleMessage(Utils.translateColor(InternalMessages.HOLOGRAM_LINES_ERROR.getMessage()));
                return;
            }

            boolean lookAtPlayers = !reason.equals(NPCUpdateReason.UPDATE_NPC) ? false : PLUGIN.getConfig().getBoolean("top-1-npc.npc.look-at-players.enabled");
            int lookDistance = !reason.equals(NPCUpdateReason.UPDATE_NPC) || !lookAtPlayers ? 0 : PLUGIN.getConfig().getInt("top-1-npc.npc.look-at-players.distance-to-look");
            boolean lookAround = !reason.equals(NPCUpdateReason.UPDATE_NPC) ? false : PLUGIN.getConfig().getBoolean(("top-1-npc.npc.random-look-around.enabled"));
            float leftRange = !reason.equals(NPCUpdateReason.UPDATE_NPC) || !lookAround ? 0f : (float) PLUGIN.getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.left-range");
            float rightRange = !reason.equals(NPCUpdateReason.UPDATE_NPC) || !lookAround ? 0f : (float) PLUGIN.getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.right-range");
            float upRange = !reason.equals(NPCUpdateReason.UPDATE_NPC) || !lookAround ? 0f : (float) PLUGIN.getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.up-range");
            float downRange = !reason.equals(NPCUpdateReason.UPDATE_NPC) || !lookAround ? 0f : (float) PLUGIN.getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.down-range");

            Location location = null;
            try {
                location = PLUGIN.getConfig().getLocation("top-1-npc.npc.location");
                ;
            } catch (Exception ignored) {
                PLUGIN.sendConsoleMessage(Utils.translateColor(InternalMessages.NPC_LOCATION_ERROR.getMessage()));
                return;
            }

            Chunk chunk = location.getChunk();
            boolean wasLoaded = chunk.isLoaded();
            if (!wasLoaded) chunk.load();

            if (reason.equals(NPCUpdateReason.UPDATE_NPC)) {
                NPCRegistry registry = CitizensAPI.getNPCRegistry();
                topNpc = registry.createNPC(EntityType.PLAYER, "");
                topNpc.spawn(location);
                topNpc.data().setPersistent("nameplate-visible", false);
                if (lookAtPlayers) topNpc.getOrAddTrait(LookClose.class).lookClose(true);
                if (lookAtPlayers) topNpc.getOrAddTrait(LookClose.class).setRange(lookDistance);
                topNpc.getOrAddTrait(LookClose.class).setRandomLook(true);
                topNpc.getOrAddTrait(LookClose.class).setRandomLookPitchRange(location.getPitch() - downRange, location.getPitch() + upRange);
                topNpc.getOrAddTrait(LookClose.class).setRandomLookYawRange(location.getYaw() - leftRange, location.getYaw() + rightRange);
                topNpc.getOrAddTrait(LookClose.class).setRealisticLooking(true);
                topNpc.getOrAddTrait(SkinTrait.class).setSkinName(newTop.getPlayerName(), true);

                if (interactListener != null) HandlerList.unregisterAll(interactListener);
                interactListener = new Listener() {
                    @EventHandler
                    public void onInteractNpc(NPCLeftClickEvent event) {
                        if (event.getNPC().getId() == topNpc.getId()) {
                            event.getClicker().performCommand(PLUGIN.getLangFile().get("commands.duel.command") + " " + CmdDuel.getSubCommands().get("stats") + " " + newTop.getPlayerName());
                        }
                    }
                };
                PLUGIN.getConfig().set("top-1-npc.npc.id", getTopNpc().getId());
                Bukkit.getServer().getPluginManager().registerEvents(interactListener, PLUGIN);
            }

            location.add(0.0D, 2.12 + (hologramLines.size() > 0 ? hologramLines.size() * 0.23 : 0) + (swordAnim ? 0.60 : 0), 0.0D);

//        final String prefix = PermissionsEx.getUser(p).getGroups()[0].getPrefix().replace("&", "§");
            topHologram = HologramsAPI.createHologram(PLUGIN, location);
            HashMap<Integer, TextLine> lines = new HashMap<>();
            animators = new HashMap<>();
            for (int c = 0; c < hologramLines.size(); c++) {
                int finalC = c;
                if (hologramLines.get(finalC).equalsIgnoreCase(" ")) continue;

                String hologramLine = Utils.translateColor(hologramLines.get(finalC)
                        .replace("{wins}", String.valueOf(newTop.getWins()))
                        .replace("{defeats}", String.valueOf(newTop.getDefeats()))
                        .replace("{disconnects}", String.valueOf(newTop.getDisconnects()))
                        .replace("{duels}", String.valueOf(newTop.getDuels()))
                        .replace("{player}", String.valueOf(newTop.getPlayerName()))
                        .replace("{kdr}", String.format("%.2f", newTop.getKDR()).replace(",", ".")));

                lines.put(finalC, topHologram.appendTextLine(hologramLine));

                TextAnimator animator = BkDuel.getAnimatorManager().getTextAnimator("top-npc-hologram-line-" + finalC, hologramLine);
                if (animator != null) {
                    boolean isOptionText = BkDuel.getAnimatorManager().isOptionText(hologramLine);
                    animator.setReceiver(animationFrame -> {
                        if (isOptionText) lines.get(finalC).setText(hologramLine.replaceAll("\\{([^}]*)}", animationFrame));
                        else lines.get(finalC).setText(animationFrame);
                    });
                    animator.animate();
                    animators.put(finalC, animator);
                }
            }
            if (swordAnim) topHologram.insertItemLine(hologramLines.size(), new ItemStack(Material.DIAMOND_SWORD));
            if (animators.values().size() > 0) {
                neabyChecker = new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean hasLineOfSight = false;
                        for (Player player : PLUGIN.getHandler().getMethodManager().getOnlinePlayers()) {
                            if (player.hasLineOfSight(getTopNpc().getEntity()) && player.getLocation().distance(getTopNpc().getEntity().getLocation()) < 35) {
                                    hasLineOfSight = true;
                                break;
                            }
                        }

                        for (TextAnimator animator : animators.values()) {
                            if (hasLineOfSight) {
                                if (animator.getAnimatorRunnable() == null || animator.getAnimatorRunnable().isCancelled())
                                    animator.animate();
                            } else {
                                if (animator.getAnimatorRunnable() != null) animator.pause();
                            }
                        }

                    }
                }.runTaskTimerAsynchronously(PLUGIN, 0, 20);
            }
            if (!wasLoaded) chunk.unload();
        }
    }

    public static NPC getTopNpc() {
        return topNpc;
    }

    public static Hologram getTopHologram() {
        return topHologram;
    }

    public static BukkitTask getNeabyChecker() {
        return neabyChecker;
    }

    public static Listener getInteractListener() {
        return interactListener;
    }

    public static HashMap<Integer, TextAnimator> getAnimators() {
        return animators;
    }

    public static void removeNpc(NPCUpdateReason reason) {
        if (reason.equals(NPCUpdateReason.UPDATE_NPC)) {
            if (getTopNpc() != null) {
                getTopNpc().getOrAddTrait(LookClose.class).lookClose(false);
                getTopNpc().destroy();
            }
            NPC configNpc = CitizensAPI.getNPCRegistry().getById(PLUGIN.getConfig().getInt("top-1-npc.npc.id"));
            if (configNpc != null) {
                configNpc.getOrAddTrait(LookClose.class).lookClose(false);
                configNpc.destroy();
            }
        }
        if (neabyChecker != null) neabyChecker.cancel();
        if (animators != null) {
            for (TextAnimator animator : animators.values()) {
                if (animator != null) animator.destroy();
            }
        }
        if (topHologram != null) getTopHologram().delete();
    }

}
