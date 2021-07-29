package me.bkrmt.bkduel;

import de.Keyle.MyPet.MyPetPlugin;
import de.Keyle.MyPet.api.entity.MyPet;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.enums.DuelStatus;
import me.bkrmt.bkduel.enums.EndCause;
import me.bkrmt.teleport.Teleport;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Duel implements Listener {
    private final BkDuel plugin;

    private Player fighter1;
    private Player fighter2;
    private Player winner;
    private Player loser;

    private Listener duelListener;

    private DuelStatus status;

    private final boolean[] playersReady;
    private final boolean[] playersInArena;

    private Location fighter1Return;
    private Location fighter2Return;

    private GameMode fighter1Gamemode;
    private GameMode fighter2Gamemode;

    private boolean fighter1HasPet;
    private boolean fighter2HasPet;

    private boolean fighter1HasFF;
    private boolean fighter2HasFF;

    private boolean fighter1IsFlying;
    private boolean fighter2IsFlying;

    private DuelRequest request;

    private BigDecimal fighter1Bet;
    private BigDecimal fighter2Bet;

    private Arena arena;
    private Kit kit;

//    private final ArrayList<Page> kitPages;

    private EndCause endDuelCause;

    private BukkitTask startTimer;

    private final ArrayList<DuelOptions> options;

    public Duel() {
        this(false);
    }

    public Duel(boolean editMode) {
        this.plugin = BkDuel.getInstance();
        this.options = new ArrayList<>();
//        this.kitPages = new ArrayList<>();
        if (editMode) {
            options.add(DuelOptions.EDIT_MODE);
            options.add(DuelOptions.OWN_ITEMS);
        }
        fighter1 = null;
        fighter2 = null;
        winner = null;
        loser = null;
        fighter1Return = null;
        fighter2Return = null;
        startTimer = null;
        arena = null;
        kit = null;
        playersReady = new boolean[2];
        playersInArena = new boolean[2];
    }

    public void startDuel() {
        if (fighter1 != null && fighter2 != null && arena != null) {

            if (arena.getLocation1() == null || arena.getLocation2() == null || arena.getSpectators() == null || Bukkit.getServer().getWorld(arena.getLocation1().getWorld().getName()) == null) {
                fighter1.sendMessage(plugin.getLangFile().get(fighter1, "error.invalid-arena"));
                fighter2.sendMessage(plugin.getLangFile().get(fighter2, "error.invalid-arena"));
                endWithoutPlayers();
            }

            fighter1IsFlying = fighter1.isFlying();
            fighter1Gamemode = fighter1.getGameMode();
            fighter1.setGameMode(GameMode.SURVIVAL);
            fighter2IsFlying = fighter2.isFlying();
            fighter2Gamemode = fighter2.getGameMode();
            fighter2.setGameMode(GameMode.SURVIVAL);

            fighter1.closeInventory();
            fighter2.closeInventory();

            fighter1.sendMessage(getPlugin().getLangFile().get(fighter1, "info.starting-duel"));
            fighter2.sendMessage(getPlugin().getLangFile().get(fighter2, "info.starting-duel"));

            arena.setInUse(true);

            setStatus(DuelStatus.TELEPORTING_PLAYERS);

            //Teleport player 1
            this.fighter1Return = fighter1.getLocation();

            boolean disablePets = plugin.getConfigManager().getConfig().getBoolean("mypets.disable-in-duels");
            if (disablePets) {
                Plugin petPlugin = plugin.getHookManager().getMyPetHook();
                if (petPlugin != null) {
                    MyPetPlugin myPet = (MyPetPlugin) petPlugin;
                    MyPet pet1 = myPet.getMyPetManager().getMyPet(fighter1);
                    MyPet pet2 = myPet.getMyPetManager().getMyPet(fighter2);

                    if (despawnPet(pet1)) fighter1HasPet = true;
                    if (despawnPet(pet2)) fighter2HasPet = true;
                }
            }

            boolean enableAllyDamage = plugin.getConfigManager().getConfig().getBoolean("simpleclans.enable-ally-damage");
            if (enableAllyDamage) {
                Plugin clansPlugin = plugin.getHookManager().getSimpleClansHook();
                if (clansPlugin != null) {
                    SimpleClans simpleClans = (SimpleClans) clansPlugin;
                    ClanPlayer clanPlayer1 = simpleClans.getClanManager().getClanPlayer(fighter1.getUniqueId());
                    ClanPlayer clanPlayer2 = simpleClans.getClanManager().getClanPlayer(fighter2.getUniqueId());
                    if (setFriendlyFire(clanPlayer1, true)) fighter1HasFF = true;
                    if (setFriendlyFire(clanPlayer2, true)) fighter2HasFF = true;
                }
            }


            new Teleport(plugin, fighter1, false)
                    .setRunnable((player, location, isCanceled) -> {
                        if (!isCanceled) {
                            playersInArena[0] = true;
                            setKit(fighter1, fighter1Return);
                        }
                    })
                    .setLocation(AnimatorManager.cleanText(getArena().getName()), getArena().getLocation1())
                    .setDuration(0)
                    .setIsCancellable(false)
                    .startTeleport();

            //Teleport player 2
            this.fighter2Return = fighter2.getLocation();
            new Teleport(plugin, fighter2, false)
                    .setRunnable((player, location, isCanceled) -> {
                        if (!isCanceled) {
                            playersInArena[1] = true;
                            setKit(fighter2, fighter2Return);
                        }
                    })
                    .setLocation(AnimatorManager.cleanText(getArena().getName()), getArena().getLocation2())
                    .setDuration(0)
                    .setIsCancellable(false)
                    .startTeleport();

            int tempInt = plugin.getConfigManager().getConfig().getInt("start-countdown");
            int timerCount = tempInt > 0 ? tempInt : 1;

            startListener();

            startTimer = new BukkitRunnable() {
                int indexCount = 0;
                boolean firstRun = true;

                @Override
                public void run() {
                    if (playersInArena[0] && playersInArena[1]) {
                        if (firstRun) {
                            firstRun = false;
                            setStatus(DuelStatus.AWAITING_READY);
                            broadcastStart();
                        }
                        int remaining = timerCount - indexCount++;
                        if (remaining > 0) {
                            plugin.sendTitle(getFighter1(), 0, 21, 0, plugin.getLangFile().get(fighter1, "info.seconds-remaining").replace("{seconds}", String.valueOf(remaining)), "");
                            plugin.sendTitle(getFighter2(), 0, 21, 0, plugin.getLangFile().get(fighter2, "info.seconds-remaining").replace("{seconds}", String.valueOf(remaining)), "");

                        } else {
                            plugin.sendTitle(getFighter1(), 0, 15, 0, plugin.getLangFile().get(fighter1, "info.go"), "");
                            plugin.sendTitle(getFighter2(), 0, 15, 0, plugin.getLangFile().get(fighter2, "info.go"), "");

                            playersReady[0] = true;
                            playersReady[1] = true;
                            startTimer.cancel();
                        }
                    } else {
                        if (!playersInArena[0]) {
                            plugin.sendTitle(getFighter2(), 0, 21, 0, plugin.getLangFile().get(fighter2, "info.waiting-other"), "");
                        }
                        if (!playersInArena[1]) {
                            plugin.sendTitle(getFighter1(), 0, 21, 0, plugin.getLangFile().get(fighter1, "info.waiting-other"), "");
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 20);

        } else {
            plugin.getServer().getLogger().log(Level.SEVERE, "The duel was not correctly initialized.");
        }
    }

    private boolean despawnPet(MyPet pet) {
        if (pet != null) {
            if (pet.getStatus().equals(MyPet.PetState.Here)) {
                pet.setStatus(MyPet.PetState.Despawned);
                return true;
            }
        }
        return false;
    }

    private boolean spawnPet(MyPet pet) {
        if (pet != null) {
            if (pet.getStatus().equals(MyPet.PetState.Despawned)) {
                pet.setStatus(MyPet.PetState.Here);
                pet.setWantsToRespawn(true);
                return true;
            }
        }
        return false;
    }

    private boolean setFriendlyFire(ClanPlayer clanPlayer, boolean value) {
        if (clanPlayer != null) {
            if (clanPlayer.getClan() != null) {
                if (!value || !clanPlayer.isFriendlyFire()) {
                    clanPlayer.setFriendlyFire(value);
                    return true;
                }
            }
        }
        return false;
    }

    public static Duel findDuel(Arena arena) {
        Duel spectatedDuel = null;
        Collection<Duel> duels = BkDuel.getInstance().getOngoingDuels().values();
        if (!duels.isEmpty()) {
            for (Duel ongoingDuel : duels) {
                if (arena.getId() == ongoingDuel.getArena().getId()) {
                    spectatedDuel = ongoingDuel;
                    break;
                }
            }
        }
        return spectatedDuel;
    }

    public static Duel findDuel(String name) {
        Duel spectatedDuel = null;
        Collection<Duel> duels = BkDuel.getInstance().getOngoingDuels().values();
        if (!duels.isEmpty()) {
            for (Duel ongoingDuel : duels) {
                if (name.equalsIgnoreCase(ongoingDuel.getFighter1().getName()) || name.equalsIgnoreCase(ongoingDuel.getFighter2().getName())) {
                    spectatedDuel = ongoingDuel;
                    break;
                }
            }
        }
        return spectatedDuel;
    }

    private void broadcastStart() {
        List<String> broadcastMessage = new ArrayList<>();

        for (String line : getPlugin().getLangFile().getConfig().getStringList("info.broadcast-to-all-start")) {
            broadcastMessage.add(Utils.translateColor(line)
                    .replace("{fighter1}", getFighter1().getName())
                    .replace("{fighter2}", getFighter2().getName()));
        }
        for (Player player : getPlugin().getHandler().getMethodManager().getOnlinePlayers()) {
            for (String line : broadcastMessage) {
                if (line.contains("{spectate-button}")) {
                    String[] lineParts = line.split("\\{spectate-button}");
                    ComponentBuilder lineBuilder = new ComponentBuilder("")
                            .append(lineParts[0])
                            .append(getPlugin().getLangFile().get(player, "info.spectate-button.button"))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/" + getPlugin().getLangFile().get(player, "commands.duel.command") + " " + getPlugin().getLangFile().get(player, "commands.duel.subcommands.spectate.command") + " " + getFighter1().getName()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(getPlugin().getLangFile().get(player, "info.spectate-button.hover")).create()));
                    if (lineParts.length == 2) lineBuilder.append(lineParts[1]).reset();
                    player.spigot().sendMessage(lineBuilder.create());
                } else {
                    player.sendMessage(line);
                }
            }
        }
    }

    public BkDuel getPlugin() {
        return plugin;
    }

    private void startListener() {
        this.duelListener = new Listener() {
            @EventHandler
            public void onKick(PlayerKickEvent event) {
                Player player = event.getPlayer();
                if (isDuelPlayer(player)) {
                    if (kit != null) {
                        Kit.clearPlayer(player);
                    }
                    endWithoutPlayers();
                    finish(fighter1.getUniqueId(), fighter2.getUniqueId());
                }
            }

            @EventHandler
            public void onReload(PluginDisableEvent event) {
                if (event.getPlugin().getName().equalsIgnoreCase(plugin.getName())) {
                    endWithoutPlayers();
                    finish(fighter1.getUniqueId(), fighter2.getUniqueId());
                }
            }


            @EventHandler
            public void onMove(PlayerMoveEvent event) {
                Player player = event.getPlayer();
                if (player.getUniqueId().equals(getFighter1().getUniqueId()) || player.getUniqueId().equals(getFighter2().getUniqueId())) {
                    if (!playersReady[0] || !playersReady[1]) {
                        event.setCancelled(true);
                    }
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerDeath(PlayerDeathEvent event) {
                Player player = event.getEntity();
                if (isDuelPlayer(player)) {
                    if (!getOptions().contains(DuelOptions.DROP_ITEMS)) {
                        event.setKeepInventory(true);
                    }
                    if (!getOptions().contains(DuelOptions.DROP_EXP)) {
                        event.setKeepLevel(true);
                    }
                    if (kit != null) {
                        Kit.clearPlayer(player);
                    }
                    endDuelCause = EndCause.BATTLE_END;
                    checkDuelEnd(player);
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerLeave(PlayerQuitEvent event) {
                Player player = event.getPlayer();
                endDuelCause = EndCause.DISCONNECT;
                if (isDuelPlayer(player)) {
                    if (kit != null) {
                        Kit.clearPlayer(player);
                    }
                    checkDuelEnd(player);
                }
            }
        };
        plugin.getServer().getPluginManager().registerEvents(duelListener, plugin);
    }

    public void startRequest() {
        double playerMoney = BkDuel.getInstance().getEconomy().getBalance(getFighter1());
        double duelCost = plugin.getConfigManager().getConfig().getDouble("duel-cost");
        if (playerMoney >= duelCost) {
            EconomyResponse r = BkDuel.getInstance().getEconomy().withdrawPlayer(getFighter1(), duelCost);
            getFighter1().closeInventory();
            getFighter1().sendMessage(plugin.getLangFile().get(fighter1, "info.request-sent"));
            setRequest(new DuelRequest(this).sendMessage());
        } else {
            getFighter1().sendMessage(plugin.getLangFile().get(fighter1, "error.no-money.self"));
        }
    }

    private boolean isDuelPlayer(Player player) {
        return player.getUniqueId().equals(fighter1.getUniqueId()) || player.getUniqueId().equals(fighter2.getUniqueId());
    }

    public final ArrayList<DuelOptions> getOptions() {
        return options;
    }

    private void checkDuelEnd(Player player) {
        if (getFighter1() != null && getFighter2() != null) {
            if (player.getUniqueId().equals(fighter1.getUniqueId())) {
                winner = fighter2;
                loser = fighter1;
                endDuel();
            } else if (player.getUniqueId().equals(fighter2.getUniqueId())) {
                winner = fighter1;
                loser = fighter2;
                endDuel();
            }
        } else {
            endWithoutPlayers();
        }
    }

    public void endWithoutPlayers() {
        if (startTimer != null) startTimer.cancel();
        if (arena != null) arena.setInUse(false);
        if (duelListener != null) HandlerList.unregisterAll(duelListener);
        if (getFighter1() != null && !getStatus().equals(DuelStatus.AWAITING_REPLY)) fighter1.teleport(fighter1Return);
        if (getFighter2() != null && !getStatus().equals(DuelStatus.AWAITING_REPLY)) fighter2.teleport(fighter2Return);
    }

    private void endDuel() {
        startTimer.cancel();

        // Broadcast Winner

        // Ending Title

        if (!endDuelCause.equals(EndCause.PLUGIN_RELOAD)) {
            plugin.sendTitle(winner, 10, 50, 10, plugin.getLangFile().get(winner, "info.duel-end").replace("{player}", winner.getName()), "");
            plugin.sendTitle(loser, 10, 50, 10, plugin.getLangFile().get(loser, "info.duel-end").replace("{player}", winner.getName()), "");
        }

        //Return player items
        if (kit != null) {
            setStatus(DuelStatus.RETURNING_ITEMS);
            returnItems(plugin, winner, false);
        }

        if (fighter1.isOnline()) {
            fighter1.setFlying(fighter1IsFlying);
            fighter1.setGameMode(fighter1Gamemode);
        }
        if (fighter2.isOnline()) {
            fighter2.setFlying(fighter2IsFlying);
            fighter2.setGameMode(fighter2Gamemode);
        }

        Plugin petPlugin = plugin.getHookManager().getMyPetHook();
        if (petPlugin != null) {
            MyPetPlugin myPet = (MyPetPlugin) petPlugin;
            MyPet pet1 = myPet.getMyPetManager().getMyPet(fighter1);
            MyPet pet2 = myPet.getMyPetManager().getMyPet(fighter2);
            if (fighter1HasPet) spawnPet(pet1);
            if (fighter2HasPet) spawnPet(pet2);
        }

        Plugin clansPlugin = plugin.getHookManager().getSimpleClansHook();
        if (clansPlugin != null) {
            SimpleClans simpleClans = (SimpleClans) clansPlugin;
            ClanPlayer clanPlayer1 = simpleClans.getClanManager().getClanPlayer(fighter1.getUniqueId());
            ClanPlayer clanPlayer2 = simpleClans.getClanManager().getClanPlayer(fighter2.getUniqueId());
            if (fighter1HasFF) setFriendlyFire(clanPlayer1, false);
            if (fighter2HasFF) setFriendlyFire(clanPlayer2, false);
        }

        arena.setInUse(false);

        updateStatistics();

        //Teleport player back
        returnLocation(winner);
    }

    private void returnLocation(Player player) {
        setStatus(DuelStatus.RETURNING_TO_LOCATION);
        if (player.getUniqueId().equals(fighter1.getUniqueId())) {
            fighter2.teleport(fighter2Return);
        } else {
            fighter1.teleport(fighter1Return);
        }
        waitForPickUp(player);
    }

    private void waitForPickUp(Player player) {
        setStatus(DuelStatus.AWAITING_WINNER_PICKUP);
        int time = getOptions().contains(DuelOptions.OWN_ITEMS) ? plugin.getConfigManager().getConfig().getInt("time-to-pick-items") : 3;
        if (getOptions().contains(DuelOptions.OWN_ITEMS))
            player.sendMessage(getPlugin().getLangFile().get(player, "info.time-to-pickup").replace("{seconds}", String.valueOf(time)));
        final int[] timesRun = {0};
        String actionMessage = plugin.getLangFile().get(player, "info.action-bar-leaving");
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.sendActionBar(player, actionMessage.replace("{seconds}", String.valueOf(time - timesRun[0])));
                timesRun[0]++;
                if (timesRun[0] > time) {
                    plugin.sendActionBar(player, "");
                    if (player.getUniqueId().equals(fighter1.getUniqueId())) {
                        fighter1.teleport(fighter1Return);
                    } else {
                        fighter2.teleport(fighter2Return);
                    }
                    finish(fighter1.getUniqueId(), fighter2.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void finish(UUID uuid1, UUID uuid2) {
        //Give player reward

        HandlerList.unregisterAll(duelListener);

        //Remove from HashTable
        BkDuel.getInstance().getOngoingDuels().remove(uuid1);
        BkDuel.getInstance().getOngoingDuels().remove(uuid2);
    }

    public static void returnItems(BkDuel plugin, Player player, boolean returnLocation) {
        Configuration config = plugin.getConfigManager().getConfig("player-data", "player-inventories.yml");
        ItemStack[] invContents = new ItemStack[]{new ItemStack(Material.DIRT)};

        if (config.get(player.getUniqueId().toString() + ".inventory") != null && config.get(player.getUniqueId().toString() + ".armor") != null) {
            if (plugin.getNmsVer().number < 9) {
                String stringInventory = config.getString(player.getUniqueId().toString() + ".inventory");
                String stringArmor = config.getString(player.getUniqueId().toString() + ".armor");

                ItemStack[] tempInv = new ItemStack[]{new ItemStack(Material.DIRT)};
                ItemStack[] tempArmor = new ItemStack[]{new ItemStack(Material.DIRT)};
                try {
                    tempInv = Utils.itemStackArrayFromBase64(stringInventory);
                    tempArmor = Utils.itemStackArrayFromBase64(stringArmor);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                invContents = Utils.concatenate(tempInv, tempArmor);

            } else {
                String stringContents = config.getString(player.getUniqueId().toString() + ".inventory");
                try {
                    invContents = Utils.itemStackArrayFromBase64(stringContents);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Kit.clearPlayer(player);
        Kit.giveKit(player, invContents, true);

        if (returnLocation) player.teleport(config.getLocation(player.getUniqueId().toString() + ".return-location"));

        config.set(player.getUniqueId().toString(), null);
        config.saveToFile();
    }

    private void updateStatistics() {
        setStatus(DuelStatus.UPDATING_STATS);
        if (!endDuelCause.equals(EndCause.PLUGIN_RELOAD)) {
            Configuration statsConfig = plugin.getConfigManager().getConfig("player-data", "player-stats.yml");

            if (statsConfig.get(winner.getUniqueId().toString() + ".name") == null)
                statsConfig.set(winner.getUniqueId().toString() + ".name", winner.getName());

            if (statsConfig.get(loser.getUniqueId().toString() + ".name") == null)
                statsConfig.set(loser.getUniqueId().toString() + ".name", loser.getName());

            statsConfig.set(winner.getUniqueId().toString() + ".duels", statsConfig.getInt(winner.getUniqueId().toString() + ".duels") + 1);
            statsConfig.set(loser.getUniqueId().toString() + ".duels", statsConfig.getInt(loser.getUniqueId().toString() + ".duels") + 1);

            statsConfig.set(winner.getUniqueId().toString() + ".wins", statsConfig.getInt(winner.getUniqueId().toString() + ".wins") + 1);
            statsConfig.set(loser.getUniqueId().toString() + ".defeats", statsConfig.getInt(loser.getUniqueId().toString() + ".defeats") + 1);

            if (endDuelCause.equals(EndCause.DISCONNECT))
                statsConfig.set(loser.getUniqueId().toString() + ".disconnects", statsConfig.getInt(loser.getUniqueId().toString() + ".disconnects") + 1);

            statsConfig.saveToFile();
            BkDuel.getInstance().getStatsManager().updateStats();
        }
    }

    /*public void checkAuthorization(AuthorizationRunnable runnable) {
        String start = Utils.translateColor(InternalMessages.VALIDATOR_START.getMessage(plugin).replace("{0}", BkDuel.PREFIX));
        String noResponse = Utils.translateColor(InternalMessages.VALIDATOR_NO_RESPONSE.getMessage(plugin).replace("{0}", "&7[&4&lBkDuel&7]&c").replace("{1}", "&b&l"));
        String success = Utils.translateColor(InternalMessages.VALIDATOR_SUCCESS.getMessage(plugin).replace("{0}", BkDuel.PREFIX));
        String error = Utils.translateColor(InternalMessages.VALIDATOR_ERROR.getMessage(plugin).replace("{0}", "&7[&4&lBkDuel&7]&c").replace("{1}", "&b&l"));
        plugin.setRunning(false);
        BukkitTask validationTimeout = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isRunning()) {
                    sendError(noResponse);
                }
            }
        }.runTaskLater(plugin, 15 * 20);
        plugin.sendConsoleMessage(start);
        try {
            URL url = new URL("https://git-ds-bot.herokuapp.com/k1j2-39il-kjdc-ao03-90hf-a872");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String pluginVersion = plugin.getDescription().getVersion();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("plugin", plugin.getName().toLowerCase());
            connection.setRequestProperty("version", pluginVersion);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                String stringResponse = response.toString();
                if (stringResponse.contains("true")) {
                    plugin.setRunning(true);
                    validationTimeout.cancel();
                    plugin.sendConsoleMessage(success);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable::run);
                    if (stringResponse.contains("_")) {
                        String newVersion = stringResponse.split("_")[1].split("-")[0];
                        plugin.sendConsoleMessage(Utils.translateColor(me.bkrmt.bkduel.InternalMessages.NEW_VERSION.getMessage().replace("{0}", pluginVersion).replace("{1}", newVersion)));
                    }
                } else {
                    plugin.getPluginLoader().disablePlugin(plugin);
                    sendError(error.replace("{2}", stringResponse.split("-")[1].replace("\"}", "")));
                    validationTimeout.cancel();
                }
            } else {
                plugin.getPluginLoader().disablePlugin(plugin);
                sendError(noResponse);
                validationTimeout.cancel();
            }
        } catch (Exception ignored) {
            plugin.getPluginLoader().disablePlugin(plugin);
            sendError(noResponse);
            validationTimeout.cancel();
        }
    }

    private void sendError(String error) {
        Bukkit.getConsoleSender().sendMessage(" ");
        Bukkit.getConsoleSender().sendMessage(error);
        Bukkit.getConsoleSender().sendMessage(" ");
    }

    @FunctionalInterface
    public interface AuthorizationRunnable {
        void run();
    }*/

//    public ArrayList<Page> getKitPages() {
//        return kitPages;
//    }

    private void setKit(Player player, Location returnLoc) {
        Configuration invStorage = plugin.getConfigManager().getConfig("player-data", "player-inventories.yml");

        if (kit != null) {
            PlayerInventory inv = player.getInventory();

            String[] player1Inventory = Utils.playerInventoryToBase64(inv);

            Kit.clearPlayer(player);

            invStorage.set(player.getUniqueId().toString() + ".inventory", player1Inventory[0]);
            invStorage.set(player.getUniqueId().toString() + ".armor", player1Inventory[1]);

            Kit.clearPlayer(player);

            Kit.giveKit(player, kit.getItems(), true);
        }

        invStorage.setLocation(player.getUniqueId().toString() + ".return-location", returnLoc);
        invStorage.saveToFile();
    }

    public Duel setFighter1(Player fighter1) {
        this.fighter1 = fighter1;
        return this;
    }

    public Duel setFighter2(Player fighter2) {
        this.fighter2 = fighter2;
        return this;
    }

    public void setRequest(DuelRequest request) {
        this.request = request;
    }

    public DuelRequest getRequest() {
        return request;
    }

    public void setStatus(DuelStatus status) {
        this.status = status;
    }

    public DuelStatus getStatus() {
        return status;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public Player getFighter1() {
        return fighter1;
    }

    public Player getFighter2() {
        return fighter2;
    }

    public Arena getArena() {
        return arena;
    }

    public Kit getKit() {
        return kit;
    }
}
