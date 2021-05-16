package me.bkrmt.bkduel;

import me.bkrmt.bkcore.BkPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class Kit extends Purchasable {
    private final List<ItemStack> items;

    public Kit(BkPlugin plugin, String kitName) {
        super(plugin, kitName, "kits");

        items = new ArrayList<>();

        ConfigurationSection section = getConfig().getConfigurationSection("items");
        for (String key : section.getKeys(false)) {
            items.add(getConfig().getItemStack("items." + key));
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
        if (BkDuel.PLUGIN.getNmsVer().number > 9) {
            inv.setItemInMainHand(null);
            inv.setItemInOffHand(null);
            inv.setArmorContents(null);
            inv.setExtraContents(null);
        }
    }

    public List<ItemStack> getItems() {
        return items;
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
}
