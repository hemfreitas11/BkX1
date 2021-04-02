package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkx1.menus.ChooseArenaMenu;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Arena {
    private String name;
    private ItemStack displayItem;
    private Location location1;
    private Location location2;
    private Location spectators;
    private double price;
    private BkPlugin plugin;
    private int id;
    private final Configuration config;
    private boolean isInUse;

    public Arena(BkPlugin plugin, String arenaName) {
        this.plugin = plugin;
        this.config = plugin.getConfig("arenas", arenaName);
        this.isInUse = getConfig().getBoolean("in-use");
        this.price = config.getDouble("price");
        if (config.getInt("id") > 1000 ) {
            this.id = config.getInt("id");
        } else {
            int newId = generateArenaID();
            this.id = newId;
            config.set("id", newId);
            config.save(false);
        }

        if (config.get("name") == null) setName(arenaName);
        else name = config.getString("name");

        if (config.get("display-item") == null) {
            setDisplayItem(new ItemStack(Material.DIRT));
        } else {
            setDisplayItem(config.getItemStack("display-item"));
        }

        if (config.get("locations") == null) {
            location1 = null;
            location2 = null;
            spectators = null;
        }
        else {
            location1 = config.getLocation("locations.fighter1");
            location2 = config.getLocation("locations.fighter2");
            spectators = config.getLocation("locations.spectators");
        }
    }

    private int generateArenaID() {
        int returnId = Utils.getRandomInRange(1001, 9999);
        File[] listFiles = plugin.getFile("", "arenas").listFiles();
        if (listFiles.length > 0) {
            int[] arenaIds = new int[listFiles.length];
            for (int c = 0; c < arenaIds.length; c++) {
                arenaIds[c] = plugin.getConfig("arenas", listFiles[c].getName()).getInt("id");
            }

            while (Arrays.asList(arenaIds).contains(returnId)) {
                returnId = Utils.getRandomInRange(1001, 9999);
            }
        }
        return returnId;
    }

    public void setLocation1(Location location1) {
        this.location1 = location1;
        config.setLocation("locations.fighter1", location1);
        config.save(false);
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
        config.setLocation("locations.fighter2", location2);
        config.save(false);
    }

    public void setSpectators(Location spectators) {
        this.spectators = spectators;
        config.setLocation("locations.spectators", spectators);
        config.save(false);
    }

    public void showEditMenu(Duel duel) {
        Player player = duel.getFighter1();
        SimpleGUI gui = new SimpleGUI(new GUI(ChatColor.stripColor(getName()), Rows.FIVE));

        Page menu = new Page(plugin, gui, 1);
        List<String> test = new ArrayList<>();
        test.add("aksjdlkasjdl jasdj ");
        test.add("asdkqiworsd");
        test.add("slçakj dd");

        ItemBuilder name = new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getSign(), true, "Teste", test));
        ItemBuilder pos1 = new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getCyanDye(), true, "Teste", test));
        ItemBuilder pos2 = new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getRedDye(), true, "Teste", test));
        ItemBuilder spec = new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getEnderEye(), true, "Teste", test));
        ItemBuilder item = new ItemBuilder(Utils.createItem(Material.ITEM_FRAME, true, "Teste", test));
        ItemBuilder backButton = new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, Utils.translateColor("Go back"), test));

        menu.setItemOnXY(5, 5, backButton, event -> {
            ChooseArenaMenu.showGUI(duel);
        });

        menu.setItemOnXY(3, 2, name, event -> {
            new PlayerInput(plugin, duel.getFighter1(), input -> {
                            setName(input);
                            ChooseArenaMenu.showGUI(duel);
                        },
                        input -> {
                        })
                        .setCancellable(false)
                        .setSubTitle("asdasd")
                        .setTitle("Digite o nome da arena")
                        .sendInput();
        });
        menu.setItemOnXY(7, 2, item, event -> {
            SimpleGUI tempGui = new SimpleGUI(new GUI(ChatColor.stripColor(getName()), Rows.THREE));
            tempGui.getGuiSettings().setCanDrag(false);
            tempGui.getGuiSettings().setCanEnterItems(true);
            AtomicInteger cout = new AtomicInteger();
            tempGui.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                player.sendMessage("Item set.");
                setDisplayItem(event.getCursor());
            });

            Page page = new Page(plugin, tempGui, 1);

            List<String> lore = new ArrayList<>();
            lore.add(Utils.translateColor("&aasd lkaskdj lsajk "));
            lore.add(Utils.translateColor("&4asd saldka"));
            lore.add(Utils.translateColor("&basd saldka"));

            page.setItemOnXY(5, 3, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, Utils.translateColor("&6Enter Item"), lore)), event1 -> {
                showEditMenu(duel);
            });

            page.openGui(player);
        });
        menu.setItemOnXY(5, 2, spec, event -> {
            setLocation(player, "spectators");
        });
        menu.setItemOnXY(4, 3, pos1, event -> {
            setLocation(player, "fighter1");
        });
        menu.setItemOnXY(6, 3, pos2, event -> {
            setLocation(player, "fighter2");
        });

        menu.openGui(player);
    }

    public int getId() {
        return id;
    }

    public static boolean ownsArena(Player player, String arenaName) {
        arenaName = ChatColor.stripColor(arenaName);
        Configuration config = BkX1.plugin.getConfig("player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        if (config.get(uuid+".arenas") == null) return false;
        List<String> ownedKits = config.getStringList(uuid+".arenas");
        return ownedKits.contains(arenaName);
    }

    public static void addOwner(Player player, String arenaName) {
        arenaName = ChatColor.stripColor(arenaName);
        Configuration config = BkX1.plugin.getConfig("player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        List<String> ownedKits = config.get(uuid+".arenas") == null ? new ArrayList<>() : config.getStringList(uuid+".arenas");
        ownedKits.add(arenaName);
        config.set(uuid + ".arenas", ownedKits);
        config.save(false);
    }

    private void setLocation(Player player, String key) {
        player.closeInventory();
        getConfig().setLocation("locations." + key, player.getLocation());
        getConfig().save(false);
        player.sendMessage("Location set.");
    }

    public void setName(String name) {
        this.name = name;
        config.set("name", name);
        config.save(false);
    }

    public boolean isInUse() {
        return isInUse;
    }

    public void setInUse(boolean inUse) {
        this.isInUse = inUse;
        config.set("in-use", inUse);
        config.save(false);
    }

    public Location getLocation1() {
        return location1;
    }

    public Location getLocation2() {
        return location2;
    }

    public Location getSpectators() {
        return spectators;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getName() {
        return name;
    }

    public void setDisplayItem(ItemStack item) {
        displayItem = Utils.createItem(item, true, Utils.translateColor(config.getString("name")), config.getStringList("display-item.lore"));

        config.setItemStack("display-item", displayItem);
        config.set("display-item.name", null);
        config.save(false);
    }

    public double getPrice() {
        return price;
    }

    public Configuration getConfig() {
        return config;
    }
}
