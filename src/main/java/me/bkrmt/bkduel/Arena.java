package me.bkrmt.bkduel;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.menus.ChooseArenaMenu;
import me.bkrmt.bkduel.menus.ChooseKitsMenu;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Page;
import me.bkrmt.opengui.Rows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class Arena extends Purchasable {
    private Location location1;
    private Location location2;
    private Location spectators;
    private Kit boundKit;

    public Arena(BkPlugin plugin, String arenaName) {
        super(plugin, arenaName, "arenas");

        if (getConfig().get("locations") == null) {
            location1 = null;
            location2 = null;
            spectators = null;
        } else {
            location1 = getConfig().getLocation("locations.fighter1");
            location2 = getConfig().getLocation("locations.fighter2");
            spectators = getConfig().getLocation("locations.spectators");
        }
        setBoundKit();
    }

    public void setLocation1(Location location1) {
        this.location1 = location1;
        getConfig().setLocation("locations.fighter1", location1);
        getConfig().save(false);
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
        getConfig().setLocation("locations.fighter2", location2);
        getConfig().save(false);
    }

    public void setSpectators(Location spectators) {
        this.spectators = spectators;
        getConfig().setLocation("locations.spectators", spectators);
        getConfig().save(false);
    }

    public void showEditMenu(Duel duel) {
        Player player = duel.getFighter1();

        Page menu = new Page(getPlugin(), BkDuel.getAnimatorManager(), new GUI(ChatColor.stripColor(getName()), Rows.SIX), 1);

        ItemBuilder name = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getSign(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-name.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-name.description"))));
        ItemBuilder desc = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getWritableBook(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-description.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-description.description"))));
        ItemBuilder pos1 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getCyanDye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position1.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position1.description"))));
        ItemBuilder pos2 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getRedDye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position2.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position2.description"))));
        ItemBuilder spec = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getEnderEye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-spectator.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-spectator.description"))));
        ItemBuilder item = new ItemBuilder(Utils.createItem(Material.ITEM_FRAME, true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-display-item.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-display-item.description"))));
        ItemBuilder price = new ItemBuilder(Utils.createItem(Material.EMERALD, true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-price.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-price.description"))));
        ItemBuilder backButton = new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                getPlugin().getLangFile().get("gui-buttons.back-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.back-button.description"))));
        ItemBuilder deleteButton = new ItemBuilder(Utils.createItem(Material.TNT, true,
                getPlugin().getLangFile().get("gui-buttons.delete-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.delete-button.description"))));
        ItemBuilder selectKit = new ItemBuilder(Utils.createItem(Material.IRON_CHESTPLATE, true,
                getPlugin().getLangFile().get("gui-buttons.select-bound-kit.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.select-bound-kit.description"))));

        menu.setItemOnXY(5, 6, backButton, "arena-edit-back-button", event -> {
            ChooseArenaMenu.showGUI(duel);
        });

        menu.setItemOnXY(5, 4, selectKit,"arena-edit-select-kit-button", event -> {
            duel.getOptions().add(DuelOptions.BOUND_KIT_SELECTION);
            ChooseKitsMenu.showGUI(duel, this, 0);
        });

        menu.setItemOnXY(2, 2, name,"arena-edit-name-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                if (!setName(input)) {
                    event.getWhoClicked().sendMessage(getPlugin().getLangFile().get("error.no-letters"));
                    return;
                }
                ChooseArenaMenu.showGUI(duel);
            },
                    input -> {
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get("info.input.arena-name"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfig().getString("cancel-input")))
                    .sendInput();
        });

        menu.setItemOnXY(2, 3, desc,"arena-edit-description-button", event -> {
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
                ChooseArenaMenu.showGUI(duel);
            },
                    input -> {
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get("info.input.new-description"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.new-description-subtitle"))
                    .sendInput();
        });

        menu.setItemOnXY(4, 2, item,"arena-edit-item-button", event -> {

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
                    getPlugin().getLangFile().get("gui-buttons.arena-edit.enter-item.name"),
                    Arrays.asList(getPlugin().getLangFile().get("gui-buttons.arena-edit.enter-item.description"), " ", getPlugin().getLangFile().get("gui-buttons.back-button.description")))),"arena-edit-item-input-button",
                    event1 -> {
                        showEditMenu(duel);
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(8, 3, spec,"arena-edit-spectate-button", event -> {
            setLocation(player, "spectators");
            showEditMenu(duel);
        });
        menu.setItemOnXY(6, 2, price,"arena-edit-price-button", event -> {
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
        menu.setItemOnXY(4, 3, pos1,"arena-edit-position1-button", event -> {
            setLocation(player, "fighter1");
            showEditMenu(duel);
        });
        menu.setItemOnXY(8, 2, deleteButton,"arena-edit-delete-button", event -> {
            ItemBuilder greenPane = new ItemBuilder(getPlugin().getHandler().getItemManager().getGreenPane()).setName(getPlugin().getLangFile().get("info.confirm"));
            ItemBuilder redPane = new ItemBuilder(getPlugin().getHandler().getItemManager().getRedPane()).setName(getPlugin().getLangFile().get("info.cancel"));

            String arenaDeletedMessage = getPlugin().getLangFile().get("info.arena-deleted");
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
                    page.setItemOnXY(c, i, greenPane,"arena-edit-green-confirmation-button-" + c + "-" + i, event1 -> {
                        if (getConfig().getFile().delete()) {
                            event.getWhoClicked().closeInventory();
                            event.getWhoClicked().sendMessage(arenaDeletedMessage);
                        }
                    });
                }
            }
            for (int i = 2; i < 5; i++) {
                for (int c = 6; c < 9; c++) {
                    page.setItemOnXY(c, i, redPane,"arena-edit-red-confirmation-button-" + c + "-" + i, event1 -> {
                        showEditMenu(duel);
                    });
                }
            }

            ItemBuilder confirmBuilder = new ItemBuilder(getPlugin().getHandler().getItemManager().getWritableBook())
                    .setName(getPlugin().getLangFile().get("gui-buttons.delete-confirm.name"))
                    .setLore(getPlugin().getLangFile().get("gui-buttons.delete-confirm.description"))
                    .hideTags();

            page.setItemOnXY(5, 6, confirmBuilder, "arena-edit-comfirm-info-button",
                    event1 -> {
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(6, 3, pos2, "arena-edit-position2-button", event -> {
            setLocation(player, "fighter2");
            showEditMenu(duel);
        });

        menu.openGui(player);
    }

    private void setLocation(Player player, String key) {
        player.closeInventory();
        getConfig().setLocation("locations." + key, player.getLocation());
        getConfig().save(false);
        player.sendMessage(getPlugin().getLangFile().get("info.location-set"));
    }

    public boolean isValidArena() {
        boolean isValidArena = true;

        if (getName() == null) {
            isValidArena = false;
        }

        if (getLocation1() == null) {
            isValidArena = false;
        }

        if (getLocation2() == null) {
            isValidArena = false;
        }

        if (getSpectators() == null) {
            isValidArena = false;
        }

        if (getLocation1() != null && getLocation2() != null) {
            if (!getLocation1().getWorld().getName().equals(getLocation2().getWorld().getName())) isValidArena = false;
            if (Bukkit.getServer().getWorld(getLocation1().getWorld().getName()) == null) isValidArena = false;
        }
        return isValidArena;
    }

    public Kit getBoundKit() {
        return boundKit;
    }

    public void saveBoundKit(int kitId) {
        if (kitId == -1) {
            getConfig().set("kit", null);
        } else {
            getConfig().set("kit", kitId);
        }
        getConfig().save(false);

        setBoundKit();
    }

    public void setBoundKit() {
        if (getConfig().get("kit") != null) {
            File kitsFolder = getPlugin().getFile("", "kits");
            if (kitsFolder.listFiles().length > 0) {
                for (File kitFile : kitsFolder.listFiles()) {
                    try {
                        Kit kit = new Kit(getPlugin(), kitFile.getName().replace(".yml", ""));
                        if (kit.getId() == getConfig().getInt("kit")) boundKit = kit;
                    } catch (Exception ignored) {
                        getPlugin().getServer().getLogger().log(Level.SEVERE, "The kit in the file '" + kitFile.getName() + "' is corrupted and could not be loaded.");
                        return;
                    }
                }
            }
        } else {
            boundKit = null;
        }
    }

    public boolean isInUse() {
        return getConfig().getBoolean("in-use");
    }

    public void setInUse(boolean inUse) {
        getConfig().set("in-use", inUse);
        getConfig().save(false);
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

}
