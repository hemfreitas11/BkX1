package me.bkrmt.bkduel.npc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkcore.textanimator.TextAnimator;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.InternalMessages;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.stats.PlayerStats;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
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

public class NPCManager {
    private NPC topNpc;
    private Hologram topHologram;
    private BukkitTask neabyChecker;
    private Listener interactListener;
    private HashMap<Integer, TextAnimator> animators;
    private HashMap<Integer, BukkitTask> updaters;
    private BkDuel bkDuel;

    public NPCManager(BkDuel bkDuel) {
        this.bkDuel = bkDuel;
        this.topNpc = null;
        this.topHologram = null;
        this.updaters = null;
        this.neabyChecker = null;
        this.interactListener = null;
        this.animators = null;
    }

    public void setTopNpc(final PlayerStats newTop, UpdateReason reason) {
        boolean npcEnabled = bkDuel.getConfigManager().getConfig().getBoolean("top-1-npc.enabled");
        if (npcEnabled && bkDuel.getHookManager().hasHologramHook()) {
            remove(reason);
            boolean swordAnim = bkDuel.getConfigManager().getConfig().getBoolean("top-1-npc.hologram.sword-animation");
            List<String> hologramLines = null;
            try {
                hologramLines = bkDuel.getConfigManager().getConfig().getStringList("top-1-npc.hologram.lines");
            } catch (Exception ignored) {
                bkDuel.sendConsoleMessage(Utils.translateColor(InternalMessages.HOLOGRAM_LINES_ERROR.getMessage()));
                return;
            }

            int placeholderUpdate = bkDuel.getConfigManager().getConfig().getInt("top-1-npc.hologram.placeholder-update");
            boolean lookAtPlayers = !reason.equals(UpdateReason.NPC_AND_STATS) ? false : bkDuel.getConfigManager().getConfig().getBoolean("top-1-npc.npc.look-at-players.enabled");
            int lookDistance = !reason.equals(UpdateReason.NPC_AND_STATS) || !lookAtPlayers ? 0 : bkDuel.getConfigManager().getConfig().getInt("top-1-npc.npc.look-at-players.distance-to-look");
            boolean lookAround = !reason.equals(UpdateReason.NPC_AND_STATS) ? false : bkDuel.getConfigManager().getConfig().getBoolean(("top-1-npc.npc.random-look-around.enabled"));
            float leftRange = !reason.equals(UpdateReason.NPC_AND_STATS) || !lookAround ? 0f : (float) bkDuel.getConfigManager().getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.left-range");
            float rightRange = !reason.equals(UpdateReason.NPC_AND_STATS) || !lookAround ? 0f : (float) bkDuel.getConfigManager().getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.right-range");
            float upRange = !reason.equals(UpdateReason.NPC_AND_STATS) || !lookAround ? 0f : (float) bkDuel.getConfigManager().getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.up-range");
            float downRange = !reason.equals(UpdateReason.NPC_AND_STATS) || !lookAround ? 0f : (float) bkDuel.getConfigManager().getConfig().getDouble("top-1-npc.npc.random-look-around.radom-look-range.down-range");

            Location location = null;
            try {
                location = bkDuel.getConfigManager().getConfig().getLocation("top-1-npc.npc.location");
                ;
            } catch (Exception ignored) {
                bkDuel.sendConsoleMessage(Utils.translateColor(InternalMessages.NPC_LOCATION_ERROR.getMessage()));
                return;
            }

            Chunk chunk = location.getChunk();
            boolean wasLoaded = chunk.isLoaded();
            if (!wasLoaded) chunk.load();

            if (reason.equals(UpdateReason.NPC_AND_STATS)) {
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
                            event.getClicker().performCommand(bkDuel.getLangFile().get(Bukkit.getOfflinePlayer(newTop.getUUID()), "commands.duel.command") + " " + CmdDuel.getSubCommands().get("stats") + " " + newTop.getPlayerName());
                        }
                    }
                };
                bkDuel.getConfigManager().getConfig().set("top-1-npc.npc.id", getTopNpc().getId());
                Bukkit.getServer().getPluginManager().registerEvents(interactListener, bkDuel);
            }

            location.add(0.0D, 2.12 + (hologramLines.size() > 0 ? hologramLines.size() * 0.23 : 0) + (swordAnim ? 0.60 : 0), 0.0D);

            topHologram = HologramsAPI.createHologram(bkDuel, location);
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

                TextAnimator animator = bkDuel.getAnimatorManager().getTextAnimator("bkduel-top-npc-hologram-line-" + finalC, hologramLine);
                if (animator != null) {
                    setLineAnimator(newTop, lines, finalC, hologramLine, animator);
                }

                if (bkDuel.isValidPlaceholder(hologramLine)) {
                    if (updaters == null) updaters = new HashMap<>();
                    try {
                        if (updaters.get(finalC) != null) updaters.get(finalC).cancel();
                    } catch (Exception ignored) {}
                    if (placeholderUpdate > 2) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                updateLine(newTop, hologramLine, lines, finalC);
                            }
                        }.runTaskLater(BkDuel.getInstance(), 20 * 2);
                    }
                    updaters.put(finalC, new BukkitRunnable() {
                        @Override
                        public void run() {
                            updateLine(newTop, hologramLine, lines, finalC);
                        }
                    }.runTaskTimerAsynchronously(BkDuel.getInstance(), 0, 20 * placeholderUpdate));
                }
            }

            if (swordAnim) topHologram.insertItemLine(hologramLines.size(), new ItemStack(Material.DIAMOND_SWORD));
            if (animators.values().size() > 0) {
                neabyChecker = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!bkDuel.getHookManager().hasPlaceHolderHook()) cancel();
                        boolean hasLineOfSight = false;
                        for (Player player : bkDuel.getHandler().getMethodManager().getOnlinePlayers()) {
                            if (player.hasLineOfSight(getTopNpc().getEntity()) && player.getLocation().distance(getTopNpc().getEntity().getLocation()) < 35) {
                                hasLineOfSight = true;
                                break;
                            }
                        }

                        for (TextAnimator animator : animators.values()) {
                            if (animator != null) {
                                if (hasLineOfSight) {
                                    if (animator.getAnimatorRunnable() == null || animator.getAnimatorRunnable().isCancelled())
                                        animator.animate();
                                } else {
                                    if (animator.getAnimatorRunnable() != null) animator.pause();
                                }
                            }
                        }

                    }
                }.runTaskTimerAsynchronously(bkDuel, 0, 20);
            }
            if (!wasLoaded) chunk.unload();
        }
    }

    private void updateLine(PlayerStats newTop, String hologramLine, HashMap<Integer, TextLine> lines, int finalC) {
        String updatedLine = bkDuel.isValidPlaceholder(hologramLine) ? PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(newTop.getUUID()), hologramLine) : hologramLine;
        String test1 = AnimatorManager.cleanText(ChatColor.stripColor(updatedLine)).trim();
        String test2 = AnimatorManager.cleanText(ChatColor.stripColor(lines.get(finalC).getText())).trim();
        if (!test1.equalsIgnoreCase(test2)) {
            try {
                TextAnimator tempAnimator = animators.get(finalC);
                if (tempAnimator != null) {
                    bkDuel.getAnimatorManager().destroy(tempAnimator);
                    TextAnimator newAnimator = bkDuel.getAnimatorManager().getTextAnimator("top-npc-hologram-line-" + finalC, updatedLine);
                    setLineAnimator(newTop, lines, finalC, updatedLine, newAnimator);
                } else {
                    lines.get(finalC).setText(updatedLine);
                }
            } catch (Exception ignored) {
                lines.get(finalC).setText(updatedLine);
            }
        }
    }

    private void setLineAnimator(PlayerStats newTop, HashMap<Integer, TextLine> lines, int finalC, String hologramLine, TextAnimator animator) {
        boolean isOptionText = BkDuel.getInstance().getAnimatorManager().isOptionText(hologramLine);
        animator.setReceiver(animationFrame -> {
            try {
                if (isOptionText){
                    if (bkDuel.isValidPlaceholder(hologramLine)) {
                        lines.get(finalC).setText(PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(newTop.getUUID()), hologramLine).replaceAll("\\{([^}]*)}", animationFrame));
                    } else {
                        lines.get(finalC).setText(hologramLine.replaceAll("\\{([^}]*)}", animationFrame));
                    }
                }
                else lines.get(finalC).setText(animationFrame);
            } catch (Exception e) {
                e.printStackTrace();
                lines.get(finalC).setText("§cThis line had an error, check console!");
            }
        });
        animators.put(finalC, animator);
        animators.get(finalC).animate();
    }

    public NPC getTopNpc() {
        return topNpc;
    }

    public Hologram getTopHologram() {
        return topHologram;
    }

    public BukkitTask getNeabyChecker() {
        return neabyChecker;
    }

    public Listener getInteractListener() {
        return interactListener;
    }

    public HashMap<Integer, TextAnimator> getAnimators() {
        return animators;
    }

    public void remove(UpdateReason reason) {
        if (reason.equals(UpdateReason.NPC_AND_STATS)) {
            if (getTopNpc() != null) {
                getTopNpc().getOrAddTrait(LookClose.class).lookClose(false);
                getTopNpc().destroy();
            }
            NPC configNpc = CitizensAPI.getNPCRegistry().getById(BkDuel.getInstance().getConfigManager().getConfig().getInt("top-1-npc.npc.id"));
            if (configNpc != null) {
                configNpc.getOrAddTrait(LookClose.class).lookClose(false);
                configNpc.destroy();
            }
        }
        if (neabyChecker != null) neabyChecker.cancel();
        if (animators != null) {
            for (TextAnimator animator : animators.values()) {
                if (animator != null) bkDuel.getAnimatorManager().destroy(animator);
            }
         }
        if (updaters != null) {
            for (BukkitTask updater : updaters.values()) {
                updater.cancel();
            }
        }
        if (topHologram != null) getTopHologram().delete();
    }

}
