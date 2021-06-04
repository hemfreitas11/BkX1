package me.bkrmt.bkduel;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.menus.ChooseKitsMenu;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Page;
import me.bkrmt.opengui.Rows;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Kit extends Purchasable {
    private final List<ItemStack> items;

    public Kit(BkPlugin plugin, String kitName) {
        super(plugin, kitName, "kits");

        items = new ArrayList<>();

        loadItems();
    }

    private void loadItems() {
        ConfigurationSection section = getConfig().getConfigurationSection("items");
        if (section != null) {
            if (section.getKeys(false).size() > 0) {
                for (String key : section.getKeys(false)) {
                    items.add(getConfig().getItemStack("items." + key));
                }
            }
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

    public static boolean hasAvaliableSlot(Player player) {
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null) {
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

    public void showEditMenu(Duel duel) {
        Player player = duel.getFighter1();

        Page menu = new Page(getPlugin(), BkDuel.getAnimatorManager(), new GUI(AnimatorManager.cleanText(ChatColor.stripColor(getName())), Rows.FIVE), 1);

        ItemBuilder name = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getSign(), true,
                getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-name.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-name.description"))));
        ItemBuilder desc = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getWritableBook(), true,
                getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-description.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-description.description"))));
        ItemBuilder item = new ItemBuilder(Utils.createItem(Material.ITEM_FRAME, true,
                getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-display-item.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.kit-edit.edit-display-item.description"))));
        ItemBuilder price = new ItemBuilder(Utils.createItem(Material.EMERALD, true,
                getPlugin().getLangFile().get("gui-buttons.kit-edit.set-price.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.kit-edit.set-price.description"))));
        ItemBuilder backButton = new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                getPlugin().getLangFile().get("gui-buttons.back-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.back-button.description"))));
        ItemBuilder setItems = new ItemBuilder(Utils.createItem(Material.CHEST, true,
                getPlugin().getLangFile().get("gui-buttons.kit-edit.set-from-inventory.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.kit-edit.set-from-inventory.description"))));
        ItemBuilder deleteButton = new ItemBuilder(Utils.createItem(Material.TNT, true,
                getPlugin().getLangFile().get("gui-buttons.delete-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.delete-button.description"))));

        menu.setItemOnXY(6, 3, setItems, "kit-edit-set-items", event -> {
            for (ItemStack invItem : duel.getFighter1().getInventory().getContents()) {
                String keyName = getConfig().getConfigurationSection("items") == null ? "item1" : "item" + (getConfig().getConfigurationSection("items").getKeys(false).size() + 1);
                getConfig().setItemStack("items." + keyName, invItem);
            }
            if (getPlugin().getNmsVer().number < 9) {
                for (ItemStack armor : duel.getFighter1().getInventory().getArmorContents()) {
                    String keyName = getConfig().getConfigurationSection("items") == null ? "item1" : "item" + (getConfig().getConfigurationSection("items").getKeys(false).size() + 1);
                    getConfig().setItemStack("items." + keyName, armor);
                }
            }
            getConfig().save(false);
            duel.getFighter1().sendMessage(getPlugin().getLangFile().get("info.kit-items-set"));
            showEditMenu(duel);
        });

        menu.setItemOnXY(5, 5, backButton, "kit-edit-back-button", event -> {
            ChooseKitsMenu.showGUI(duel);
        });

        menu.setItemOnXY(2, 2, name, "kit-edit-name-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                if (!setName(input)) {
                    event.getWhoClicked().sendMessage(getPlugin().getLangFile().get("error.no-letters"));
                    return;
                }
                ChooseKitsMenu.showGUI(duel);
            },
                    input -> {
                        if (input.equalsIgnoreCase(getPlugin().getConfig().getString("cancel-input"))) showEditMenu(duel);
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get("info.input.kit-name"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfig().getString("cancel-input")))
                    .sendInput();
        });

        menu.setItemOnXY(4, 3, desc, "kit-edit-description-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                List<String> lore = new ArrayList<>();

                if (input.contains("#")) {
                    String[] parts = input.split("#");
                    for (String part : parts) {
                        if (part != null) {
                            lore.add(Utils.translateColor(part));
                        }
                    }
                } else {
                    lore.add(Utils.translateColor(input));
                }
                setDescription(lore);
                ChooseKitsMenu.showGUI(duel);
            },
                    input -> {
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get("info.input.new-description"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.new-description-subtitle"))
                    .sendInput();
        });

        menu.setItemOnXY(4, 2, item, "kit-edit-item-button", event -> {

            Page page = new Page(getPlugin(), BkDuel.getAnimatorManager(), new GUI(ChatColor.stripColor(getName()), Rows.THREE), 1);
            page.getGuiSettings().setCanDrag(false);
            page.getGuiSettings().setCanEnterItems(true);
            page.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                    setDisplayItem(event.getCursor().getType());
                    player.sendMessage(getPlugin().getLangFile().get("info.item-set"));
                }
            });

            page.setItemOnXY(5, 3, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                    getPlugin().getLangFile().get("gui-buttons.kit-edit.enter-item.name"),
                    Arrays.asList(getPlugin().getLangFile().get("gui-buttons.kit-edit.enter-item.description"), " ", getPlugin().getLangFile().get("gui-buttons.back-button.description")))), "kit-edit-item-input-button",
                    event1 -> {
                        showEditMenu(duel);
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(6, 2, price, "kit-edit-price-button", event -> {
            event.getWhoClicked().closeInventory();
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                double newPrice = 0;
                try {
                    newPrice = Double.parseDouble(input);
                } catch (NumberFormatException ignored) {
                    event.getWhoClicked().sendMessage(getPlugin().getLangFile().get("error.not-a-number").replace("{value}", input));
                    return;
                }
                setPrice(newPrice);
                event.getWhoClicked().sendMessage(getPlugin().getLangFile().get("info.price-set"));
                showEditMenu(duel);
            },
                    input -> {
                    })
                    .setCancellable(false)
                    .setTitle(getPlugin().getLangFile().get("info.input.price"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfig().getString("cancel-input")))
                    .sendInput();
        });

        menu.setItemOnXY(8, 2, deleteButton, "kit-edit-delete-button", event -> {
            ItemBuilder greenPane = new ItemBuilder(getPlugin().getHandler().getItemManager().getGreenPane()).setName(getPlugin().getLangFile().get("info.confirm"));
            ItemBuilder redPane = new ItemBuilder(getPlugin().getHandler().getItemManager().getRedPane()).setName(getPlugin().getLangFile().get("info.cancel"));

            String kitDeletedMessage = getPlugin().getLangFile().get("info.kit-deleted");
            Page page = new Page(getPlugin(), BkDuel.getAnimatorManager(), new GUI(ChatColor.stripColor(getName()), Rows.SIX), 1);
            page.getGuiSettings().setCanDrag(false);
            page.getGuiSettings().setCanEnterItems(true);
            page.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                setDisplayItem(event.getCursor().getType());
                player.sendMessage(getPlugin().getLangFile().get("info.item-set"));
            });
            for (int i = 2; i < 5; i++) {
                for (int c = 2; c < 5; c++) {
                    page.setItemOnXY(c, i, greenPane, "kit-edit-green-confirmation-button-" + c + "-" + i, event1 -> {
                        if (getConfig().getFile().delete()) {
                            event.getWhoClicked().closeInventory();
                            event.getWhoClicked().sendMessage(kitDeletedMessage);
                        }
                    });
                }
            }
            for (int i = 2; i < 5; i++) {
                for (int c = 6; c < 9; c++) {
                    page.setItemOnXY(c, i, redPane, "kit-edit-red-confirmation-button-" + c + "-" + i, event1 -> {
                        showEditMenu(duel);
                    });
                }
            }

            ItemBuilder confirmBuilder = new ItemBuilder(getPlugin().getHandler().getItemManager().getWritableBook())
                    .setName(getPlugin().getLangFile().get("gui-buttons.delete-confirm.name"))
                    .setLore(getPlugin().getLangFile().get("gui-buttons.delete-confirm.description"))
                    .hideTags();

            page.setItemOnXY(5, 6, confirmBuilder, "kit-edit-comfirm-info-button",
                    event1 -> {
                    });

            page.openGui(player);
        });

        menu.openGui(player);
    }
}
