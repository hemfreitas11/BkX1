package me.bkrmt.bkduel;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public abstract class Purchasable {
    private String purchasableName;
    private final int id;
    private final double price;
    private Configuration config;
    private ItemStack displayItem;
    private final String keyName;
    private final BkDuel plugin;

    public Purchasable(BkDuel plugin, String name, String keyName) {
        this.plugin = plugin;
        this.keyName = keyName;
        this.config = plugin.getConfig(keyName, name + ".yml");

        if (config.get("name") == null) setName(name);
        else this.purchasableName = config.getString("name");

        this.price = config.getDouble("price");

        if (config.getInt("id") > 1000) {
            this.id = config.getInt("id");
        } else {
            int newId = generateID();
            this.id = newId;
            config.set("id", newId);
            config.save(false);
        }

        if (config.get("display-item") == null) {
            setDisplayItem(Material.DIRT);
        } else {
            setDisplayItem(config.getItemStack("display-item").getType());
        }

    }

    public String getName() {
        return purchasableName;
    }

    public int getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public Configuration getConfig() {
        return config;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getKeyName() {
        return keyName;
    }

    public BkDuel getPlugin() {
        return plugin;
    }

    public void setDisplayItem(Material item) {
        displayItem = Utils.createItem(item, true, Utils.translateColor(config.getString("name")), config.getStringList("display-item.lore"));

        String displayName = getConfig().get("name") == null ? "Unnamed" : Utils.translateColor(getConfig().getString("name"));
        ItemMeta tempMeta = displayItem.getItemMeta();
        tempMeta.setDisplayName(displayName);
        tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        displayItem.setItemMeta(tempMeta);

        config.setItemStack("display-item", displayItem);
        config.set("display-item.name", null);
        config.save(false);
    }

    private int generateID() {
        int returnId = Utils.getRandomInRange(1001, 9999);
        File purchasableFolder = plugin.getFile("", keyName);
        if (purchasableFolder.exists()) purchasableFolder.mkdir();
        File[] listFiles = purchasableFolder.listFiles();
        if (listFiles.length > 0) {
            List<Integer> purchasableIDs = new ArrayList<>();
            for (File listFile : listFiles) {
                purchasableIDs.add(plugin.getConfig(keyName, listFile.getName()).getInt("id"));
            }

            while (purchasableIDs.contains(returnId)) {
                returnId = Utils.getRandomInRange(1001, 9999);
            }
        }
        return returnId;
    }

    public void setPrice(double price) {
        getConfig().set("price", price);
        getConfig().save(false);
    }

    public boolean isOwner(Player player) {
        Configuration config = getPlugin().getConfig("player-data", "player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        if (config.get(uuid + "." + getKeyName()) == null) return false;
        List<String> ownedKits = config.getStringList(uuid + "." + getKeyName());
        return ownedKits.contains(String.valueOf(getId()));
    }

    public void addOwner(Player player) {
        Configuration config = getPlugin().getConfig("player-data", "player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        List<String> ownedPurchasables = config.get(uuid + "." + getKeyName()) == null ? new ArrayList<>() : config.getStringList(uuid + "." + getKeyName());
        ownedPurchasables.add(String.valueOf(getId()));
        config.set(uuid + "." + getKeyName(), ownedPurchasables);
        config.save(false);
    }

    public boolean setName(String purchasableName) {
        boolean returnValue = false;
        String newName = Utils.cleanString(purchasableName.toLowerCase()
                .replaceAll("\\P{L}+", ""));

        if (!newName.isEmpty()) {
            try {
                Files.copy(config.getFile().toPath(), plugin.getFile(getKeyName(), newName + ".yml").toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            Configuration newConfig = plugin.getConfig(getKeyName(), newName + ".yml");
            config.getFile().delete();
            config = newConfig;
            this.purchasableName = purchasableName;
            config.set("name", purchasableName);
            config.save(false);
            returnValue = true;
        }
        return returnValue;
    }

    public void setDescription(List<String> lore) {
        ItemMeta meta = displayItem.getItemMeta();
        meta.setLore(lore);
        ItemStack newDisplay = displayItem;
        newDisplay.setItemMeta(meta);
        displayItem = newDisplay;
        config.set("display-item.lore", lore);
        config.save(false);
    }

}
