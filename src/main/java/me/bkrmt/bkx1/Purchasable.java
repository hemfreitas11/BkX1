package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.config.Configuration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Purchasable {
    private String purchasableName;
    private final int id;
    private final double price;
    private final Configuration config;
    private ItemStack displayItem;
    private final String keyName;
    private final BkPlugin plugin;

    public Purchasable(BkPlugin plugin, String name, String keyName) {
        this.plugin = plugin;
        this.keyName = keyName;
        this.config = plugin.getConfig(keyName, name + ".yml");

        if (config.get("name") == null) setName(name);
        else this.purchasableName = config.getString("name");

        this.price = config.getDouble("price");

        if (config.getInt("id") > 1000 ) {
            this.id = config.getInt("id");
        } else {
            int newId = generateID();
            this.id = newId;
            config.set("id", newId);
            config.save(false);
        }

        if (config.get("display-item") == null) {
            setDisplayItem(new ItemStack(Material.DIRT));
        } else {
            setDisplayItem(config.getItemStack("display-item"));
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

    public BkPlugin getPlugin() {
        return plugin;
    }

    public void setDisplayItem(ItemStack item) {
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

    public boolean isOwner(Player player) {
        Configuration config = getPlugin().getConfig("player-data", "player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        if (config.get(uuid+"."+getKeyName()) == null) return false;
        List<String> ownedKits = config.getStringList(uuid+"."+getKeyName());
        return ownedKits.contains(String.valueOf(getId()));
    }

    public void addOwner(Player player) {
        Configuration config = getPlugin().getConfig("player-data", "player-purchases.yml");
        String uuid = String.valueOf(player.getUniqueId());
        List<String> ownedPurchasables = config.get(uuid+"."+getKeyName()) == null ? new ArrayList<>() : config.getStringList(uuid+"."+getKeyName());
        ownedPurchasables.add(String.valueOf(getId()));
        config.set(uuid + "." + getKeyName(), ownedPurchasables);
        config.save(false);
    }

    public void setName(String purchasableName) {
        String newName = Utils.cleanString(purchasableName.toLowerCase()
                                            .replaceAll("\\P{L}+", ""));
        File purchasableFile = getConfig().getFile();
        purchasableFile.renameTo(plugin.getFile(getKeyName(), newName + ".yml"));
        config.loadFile(purchasableFile);
        this.purchasableName = purchasableName;
        config.set("name", purchasableName);
        config.save(false);
    }

}
