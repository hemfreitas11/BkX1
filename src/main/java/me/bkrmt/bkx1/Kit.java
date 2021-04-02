package me.bkrmt.bkx1;

import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Kit {
    private final String name;
    private final ItemStack displayItem;
    private final List<ItemStack> items;
    private double price;
    private final Configuration config;

    public Kit(Configuration config) {
        this.config = config;
        this.name = config.getString("name");
        this.price = config.getDouble("price");

        this.displayItem = config.getItemStack("display-item");
        ItemMeta tempMeta = displayItem.getItemMeta();
        tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        displayItem.setItemMeta(tempMeta);

        items = new ArrayList<>();

        ConfigurationSection section = config.getConfigurationSection("items");
        for (String key : section.getKeys(false)) {
            items.add(config.getItemStack("items." + key));
        }
    }

    public static void giveKit(Player player, List<ItemStack> items, boolean equipArmor) {
        items.remove(null);
        ItemStack[] itemsArray = new ItemStack[items.size()];
        for (int c = 0; c < itemsArray.length; c++) {
            itemsArray[c] = items.get(c);
        }
        giveKit(player, itemsArray, equipArmor);
    }

    public static void giveKit(Player player, ItemStack[] items, boolean equipArmor) {
        for (ItemStack item : items) {
            if (item != null) {
                PlayerInventory inventory = player.getInventory();

                if (equipArmor) {
                    String material = item.getType().toString().toLowerCase();
                    if (material.contains("helmet")) {
                        if (inventory.getHelmet() == null) {
                            inventory.setHelmet(item);
                        } else {
                            addItem(player, item);
                        }
                    } else if (material.contains("chestplate")) {
                        if (inventory.getChestplate() == null) {
                            inventory.setChestplate(item);
                        } else {
                            addItem(player, item);
                        }
                    } else if (material.contains("leggings")) {
                        if (inventory.getLeggings() == null) {
                            inventory.setLeggings(item);
                        } else {
                            addItem(player, item);
                        }
                    } else if (material.contains("boots")) {
                        if (inventory.getBoots() == null) {
                            inventory.setBoots(item);
                        } else {
                            addItem(player, item);
                        }
                    } else {
                        addItem(player, item);
                    }
                } else {
                    addItem(player, item);
                }
            }
        }
    }

    public static void clearPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
    }

    public static boolean hasAvaliableSlot(Player player){
        Inventory inv = player.getInventory();
        for (ItemStack item: inv.getContents()) {
            if(item == null) {
                return true;
            }
        }
        return false;
    }

    private static void addItem(Player player, ItemStack item) {
        Inventory inventory = player.getInventory();
        if (!hasAvaliableSlot(player)) {
            if (inventory.getHolder() instanceof HumanEntity) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        } else {
            inventory.addItem(item);
        }
    }

    public static boolean ownsKit(Player player, String kitName) {
        kitName = ChatColor.stripColor(kitName);
        Configuration config = BkX1.plugin.getConfig("player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        if (config.get(uuid+".kits") == null) return false;
        List<String> ownedKits = config.getStringList(uuid+".kits");
        return ownedKits.contains(kitName);
    }

    public static void addOwner(Player player, String kitName) {
        kitName = ChatColor.stripColor(kitName);
        Configuration config = BkX1.plugin.getConfig("player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        List<String> ownedKits = config.get(uuid+".kits") == null ? new ArrayList<>() : config.getStringList(uuid+".kits");
        ownedKits.add(kitName);
        config.set(uuid + ".kits", ownedKits);
        config.save(false);
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getName() {
        return name;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public double getPrice() {
        return price;
    }

    public Configuration getConfig() {
        return config;
    }
}
