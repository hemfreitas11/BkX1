package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.message.InternalMessages;
import me.bkrmt.teleport.Teleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;

public class Duel implements Listener {
    private final BkPlugin plugin;

    private Player fighter1;
    private Player fighter2;

    private Listener duelListener;

    private final boolean[] playersReady;

    private Location fighter1Return;
    private Location fighter2Return;

    private BigDecimal fighter1Bet;
    private BigDecimal fighter2Bet;

    private Arena arena;
    private Kit kit;

    private ArrayList<Page> kitPages;

    private BukkitTask checkReady;
    private BukkitTask checkStart;

    private final ArrayList<DuelOptions> options;

    public Duel(BkPlugin plugin) {
        this(plugin, false);
    }

    public Duel(BkPlugin plugin, boolean editMode) {
        this.plugin = plugin;
        this.options = new ArrayList<>();
        this.kitPages = new ArrayList<>();
        if (editMode) {
            options.add(DuelOptions.EDIT_MODE);
            options.add(DuelOptions.OWN_ITEMS);
        }
        fighter1 = null;
        fighter2 = null;
        fighter1Return = null;
        fighter2Return = null;
        checkReady = null;
        checkStart = null;
        arena = null;
        kit = null;
        playersReady = new boolean[2];
    }

    public void startDuel() {
        if (fighter1 != null && fighter2 != null && arena != null) {

            fighter1.closeInventory();
            fighter2.closeInventory();

            fighter1.sendMessage("Starting duel...");
            fighter2.sendMessage("Starting duel...");

            //Teleport player 1
            this.fighter1Return = fighter1.getLocation();
            new Teleport(plugin, fighter1, false)
                    .setRunnable((player, location, isCanceled) -> {
                        if (!isCanceled) {
                            //Ask if player is ready
                            playersReady[0] = true;
                        }
                    })
                    .setLocation(getArena().getConfig().getString("name"), getArena().getLocation1())
                    .setDuration(0)
                    .setIsCancellable(false)
                    .startTeleport();

            //Teleport player 2
            this.fighter2Return = fighter2.getLocation();
            new Teleport(plugin, fighter2, false)
                    .setRunnable((player, location, isCanceled) -> {
                        if (!isCanceled) {
                            //Ask if player is ready
                            playersReady[1] = true;
                        }
                    })
                    .setLocation(getArena().getConfig().getString("name"), getArena().getLocation2())
                    .setDuration(0)
                    .setIsCancellable(false)
                    .startTeleport();

            //Cancel if not ready after 10 seconds
            checkStart = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!(playersReady[0] && playersReady[1])) {
                        checkReady.cancel();
                        fighter1.sendMessage("Duel canceled.");
                        fighter2.sendMessage("Duel canceled.");
                        cancel();
                    }
                }
            }.runTaskLater(plugin, 200);

            //Check if both are in the arena
            checkReady = new BukkitRunnable() {
                @Override
                public void run() {
                    if (playersReady[0] && playersReady[1]) {
                        checkStart.cancel();
                        setKits();
                        startListener();
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, 10);

        } else {
            plugin.getServer().getLogger().log(Level.SEVERE, "The duel was not correctly initialized.");
        }
    }

    public BkPlugin getPlugin() {
        return plugin;
    }

    private void startListener() {
        this.duelListener = new Listener() {
            @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerDeath(PlayerDeathEvent event) {
                Player player = event.getEntity();
                if (isDuelPlayer(player)) {
                    if (!getOptions().contains(DuelOptions.DROP_ITEMS)) {
                        event.setKeepInventory(true);
                    }
                    if (!getOptions().contains(DuelOptions.DROP_EXP)) {
                        event.setKeepLevel(true);
                    }
                    checkDuelEnd(player);
                }
            }

            @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onPlayerLeave(PlayerQuitEvent event) {
                Player player = event.getPlayer();
                if (isDuelPlayer(player)) {
                    checkDuelEnd(player);
                }
            }
        };
        plugin.getServer().getPluginManager().registerEvents(duelListener, plugin);
    }

    private boolean isDuelPlayer(Player player) {
        return player.getUniqueId().equals(fighter1.getUniqueId()) || player.getUniqueId().equals(fighter2.getUniqueId());
    }

    public final ArrayList<DuelOptions> getOptions() {
        return options;
    }

    private void checkDuelEnd(Player player) {
        if (playersReady[0] && playersReady[1]) {
            if (player.getUniqueId().equals(fighter1.getUniqueId())) {
                endDuel(fighter2);
            } else if (player.getUniqueId().equals(fighter2.getUniqueId())) {
                endDuel(fighter1);
            }
        }
    }

    private void endDuel(Player player) {

        //Broadcast Winner
        for (Player tempPlayer : Bukkit.getOnlinePlayers()) {
            plugin.sendTitle(tempPlayer, 10, 20, 10, "§6" + player.getName() + " won!", "asd as a qwe  asf");
        }

        //Return player items
        if (kit != null) {
            returnItems(plugin, player);
        }

        //Teleport player back
        returnLocation(player);
    }

    private void returnLocation(Player player) {
        if (player.getUniqueId().equals(fighter1.getUniqueId())) {
            fighter2.teleport(fighter2Return);
        } else {
            fighter1.teleport(fighter1Return);
        }
        waitForPickUp(player);
    }

    private void waitForPickUp(Player player) {
        int time = getOptions().contains(DuelOptions.OWN_ITEMS) ? plugin.getConfig().getInt("time-to-pick-items") : 3;
        if (getOptions().contains(DuelOptions.OWN_ITEMS)) player.sendMessage("You have " + time + " seconds to pickup the items");
        final int[] timesRun = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.sendActionBar(player, "Leaving arena in " + String.valueOf(time-timesRun[0]));
                timesRun[0]++;
                if (timesRun[0] > time) {
                    plugin.sendActionBar(player, "");
                    if (player.getUniqueId().equals(fighter1.getUniqueId())) {
                        fighter1.teleport(fighter1Return);
                    } else {
                        fighter2.teleport(fighter2Return);
                    }
                    finish();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void finish() {
        //Give player reward

        HandlerList.unregisterAll(duelListener);

        //Remove from HashTable
        BkX1.getOngoingDuels().remove(fighter1.getUniqueId());
        BkX1.getOngoingDuels().remove(fighter2.getUniqueId());
    }

    public static void returnItems(BkPlugin plugin, Player player) {
        Configuration config = plugin.getConfig("player-inventories.yml");
        ItemStack[] invContents = null;
        if (plugin.getNmsVer().number < 9) {
            //Join armor contents and inventory
            String stringInventory = config.getString(player.getUniqueId().toString() + ".inventory");
            String stringArmor = config.getString(player.getUniqueId().toString() + ".armor");

            ItemStack[] tempInv = null;
            ItemStack[] tempArmor = null;
            try {
                tempInv = Utils.itemStackArrayFromBase64(stringInventory);
                tempArmor = Utils.itemStackArrayFromBase64(stringArmor);
            } catch (IOException e) {
                tempInv = new ItemStack[]{new ItemStack(Material.DIRT)};
                tempArmor = new ItemStack[]{new ItemStack(Material.DIRT)};

                //Send error message to Player

                e.printStackTrace();
            }

            invContents = Utils.concatenate(tempInv, tempArmor);

        } else {
            String stringContents = config.getString(player.getUniqueId().toString() + ".inventory");
            try {
                invContents = Utils.itemStackArrayFromBase64(stringContents);
            } catch (IOException e) {
                invContents = new ItemStack[]{new ItemStack(Material.DIRT)};
                e.printStackTrace();
            }
        }

        Kit.clearPlayer(player);
        Kit.giveKit(player, invContents, true);

        config.set(player.getUniqueId().toString(), null);
        config.save(false);
    }

    public void checkAuthorization() {
        String start = Utils.translateColor(InternalMessages.VALIDATOR_START.getMessage(plugin).replace("{0}", BkX1.prefix));
        String noResponse = Utils.translateColor(InternalMessages.VALIDATOR_NO_RESPONSE.getMessage(plugin).replace("{0}", "&7[&4&lBkX1&7]&c").replace("{1}", "&b&l"));
        String success = Utils.translateColor(InternalMessages.VALIDATOR_SUCCESS.getMessage(plugin).replace("{0}", BkX1.prefix));
        String error = Utils.translateColor(InternalMessages.VALIDATOR_ERROR.getMessage(plugin).replace("{0}", "&7[&4&lBkX1&7]&c").replace("{1}", "&b&l"));
        plugin.setRunning(false);
        BukkitTask validationTimeout = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isRunning()) {
                    sendError(noResponse);
                }
            }
        }.runTaskLater(plugin, 15*20);
        plugin.sendConsoleMessage(start);
        try {
            URL url = new URL("https://git-ds-bot.herokuapp.com/ver");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("plugin", plugin.getName().toLowerCase());
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
                } else {
                    sendError(error);
                    validationTimeout.cancel();
                }
            } else {
                sendError(noResponse);
                validationTimeout.cancel();
            }
        } catch(Exception ignored) {
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

    public ArrayList<Page> getKitPages() {
        return kitPages;
    }

    private void setKits() {
        if (kit != null) {

            PlayerInventory inv1 = fighter1.getInventory();
            PlayerInventory inv2 = fighter2.getInventory();

            String[] player1Inventory = Utils.playerInventoryToBase64(inv1);
            String[] player2Inventory = Utils.playerInventoryToBase64(inv2);

            Kit.clearPlayer(fighter1);
            Kit.clearPlayer(fighter2);

            Configuration invStorage = plugin.getConfig("player-inventories.yml");

            invStorage.set(fighter1.getUniqueId().toString() + ".inventory", player1Inventory[0]);
            invStorage.set(fighter1.getUniqueId().toString() + ".armor", player1Inventory[1]);
            invStorage.set(fighter2.getUniqueId().toString() + ".inventory", player2Inventory[0]);
            invStorage.set(fighter2.getUniqueId().toString() + ".armor", player2Inventory[1]);
            invStorage.save(false);

            Kit.clearPlayer(fighter1);
            Kit.clearPlayer(fighter2);

            Kit.giveKit(fighter1, kit.getItems(), true);
            Kit.giveKit(fighter2, kit.getItems(), true);
        }
    }

    public Duel setFighter1(Player fighter1) {
        this.fighter1 = fighter1;
        return this;
    }

    public Duel setFighter2(Player fighter2) {
        this.fighter2 = fighter2;
        return this;
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
