package me.bkrmt.bkduel;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.bkgui.gui.GUI;
import me.bkrmt.bkcore.bkgui.gui.Rows;
import me.bkrmt.bkcore.bkgui.item.ItemBuilder;
import me.bkrmt.bkcore.bkgui.page.Page;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkcore.xlibs.XMaterial;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.menus.ChooseArenaMenu;
import me.bkrmt.bkduel.menus.ChooseKitsMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    public Arena(BkDuel plugin, String arenaName) {
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
        getConfig().saveToFile();
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
        getConfig().setLocation("locations.fighter2", location2);
        getConfig().saveToFile();
    }

    public void setSpectators(Location spectators) {
        this.spectators = spectators;
        getConfig().setLocation("locations.spectators", spectators);
        getConfig().saveToFile();
    }

    public void showEditMenu(Duel duel) {
        Player player = duel.getFighter1();

        Page menu = new Page(getPlugin(), BkDuel.getInstance().getAnimatorManager(), new GUI(ChatColor.stripColor(AnimatorManager.cleanText(getName())), Rows.SIX), 1);

        ItemBuilder name = new ItemBuilder(Utils.createItem(XMaterial.OAK_SIGN.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-name.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-name.description"))));
        ItemBuilder desc = new ItemBuilder(Utils.createItem(XMaterial.WRITABLE_BOOK.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-description.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-description.description"))));
        ItemBuilder pos1 = new ItemBuilder(Utils.createItem(XMaterial.CYAN_DYE.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-position1.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-position1.description"))));
        ItemBuilder pos2 = new ItemBuilder(Utils.createItem(XMaterial.RED_DYE.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-position2.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-position2.description"))));
        ItemBuilder spec = new ItemBuilder(Utils.createItem(XMaterial.ENDER_EYE.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-spectator.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-spectator.description"))));
        ItemBuilder item = new ItemBuilder(Utils.createItem(XMaterial.ITEM_FRAME.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-display-item.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.edit-display-item.description"))));
        ItemBuilder price = new ItemBuilder(Utils.createItem(XMaterial.EMERALD.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-price.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.set-price.description"))));
        ItemBuilder backButton = new ItemBuilder(Utils.createItem(XMaterial.REDSTONE_BLOCK.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.back-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.back-button.description"))));
        ItemBuilder deleteButton = new ItemBuilder(Utils.createItem(XMaterial.TNT.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.delete-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.delete-button.description"))));
        ItemBuilder selectKit = new ItemBuilder(Utils.createItem(XMaterial.IRON_CHESTPLATE.parseItem(), true,
                getPlugin().getLangFile().get(player, "gui-buttons.select-bound-kit.name"),
                Collections.singletonList(getPlugin().getLangFile().get(player, "gui-buttons.select-bound-kit.description"))));

        menu.setItemOnXY(5, 6, backButton, "arena-edit-back-button", event -> {
            ChooseArenaMenu.showGUI(new Duel(true).setFighter1(duel.getFighter1()), null);
        });

        menu.setItemOnXY(5, 4, selectKit,"arena-edit-select-kit-button", event -> {
            duel.getOptions().add(DuelOptions.BOUND_KIT_SELECTION);
            ChooseKitsMenu.showGUI(duel, null, this, 0);
        });

        menu.setItemOnXY(2, 2, name,"arena-edit-name-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), menu, input -> {
                if (!setName(input)) {
                    event.getWhoClicked().sendMessage(getPlugin().getLangFile().get(player, "error.no-letters"));
                    return;
                }
                ChooseArenaMenu.showGUI(new Duel(true).setFighter1(duel.getFighter1()), null);
            },
                    input -> {
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get(player, "info.input.arena-name"))
                    .setSubTitle(getPlugin().getLangFile().get(player, "info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfigManager().getConfig().getString("cancel-input")))
                    .sendInput();
        });

        menu.setItemOnXY(2, 3, desc,"arena-edit-description-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), menu, input -> {
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
                ChooseArenaMenu.showGUI(new Duel(true).setFighter1(duel.getFighter1()), null);
            },
                    input -> {
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get(player, "info.input.new-description"))
                    .setSubTitle(getPlugin().getLangFile().get(player, "info.input.new-description-subtitle"))
                    .sendInput();
        });

        menu.setItemOnXY(4, 2, item,"arena-edit-item-button", event -> {

            Page page = new Page(getPlugin(), BkDuel.getInstance().getAnimatorManager(), new GUI(ChatColor.stripColor(getName()), Rows.THREE), 1);
            page.getGuiSettings().setCanDrag(false);
            page.getGuiSettings().setCanEnterItems(true);
            page.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().equals(XMaterial.AIR.parseMaterial())) {
                    setDisplayItem(event.getCursor().getType());
                    player.sendMessage(getPlugin().getLangFile().get(player, "info.item-set"));
                }
            });

            page.setItemOnXY(5, 3, new ItemBuilder(Utils.createItem(XMaterial.REDSTONE_BLOCK.parseItem(), true,
                    getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.enter-item.name"),
                    Arrays.asList(getPlugin().getLangFile().get(player, "gui-buttons.arena-edit.enter-item.description"), " ", getPlugin().getLangFile().get(player, "gui-buttons.back-button.description")))),"arena-edit-item-input-button",
                    event1 -> {
                        ChooseKitsMenu.showGUI(new Duel(true).setFighter1(duel.getFighter1()), null);
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(8, 3, spec,"arena-edit-spectate-button", event -> {
            setLocation(player, "spectators");
        });
        menu.setItemOnXY(6, 2, price,"arena-edit-price-button", event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), menu, input -> {
                double newPrice = 0;
                try {
                    newPrice = Double.parseDouble(input);
                } catch (NumberFormatException ignored) {
                    event.getWhoClicked().sendMessage(getPlugin().getLangFile().get(player, "error.not-a-number").replace("{value}", input));
                    return;
                }
                setPrice(newPrice);
                event.getWhoClicked().sendMessage(getPlugin().getLangFile().get(player, "info.price-set"));
            },
                    input -> {
                    })
                    .setCancellable(false)
                    .setTitle(getPlugin().getLangFile().get(player, "info.input.price"))
                    .setSubTitle(getPlugin().getLangFile().get(player, "info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfigManager().getConfig().getString("cancel-input")))
                    .sendInput();
        });
        menu.setItemOnXY(4, 3, pos1,"arena-edit-position1-button", event -> {
            setLocation(player, "fighter1");
        });
        menu.setItemOnXY(8, 2, deleteButton,"arena-edit-delete-button", event -> {
            ItemBuilder greenPane = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get(player, "info.confirm"));
            ItemBuilder redPane = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).setName(getPlugin().getLangFile().get(player, "info.cancel"));

            String arenaDeletedMessage = getPlugin().getLangFile().get(player, "info.arena-deleted");
            Page page = new Page(getPlugin(), BkDuel.getInstance().getAnimatorManager(), new GUI(ChatColor.stripColor(getName()), Rows.SIX), 1);
            page.getGuiSettings().setCanDrag(false);
            page.getGuiSettings().setCanEnterItems(true);
            page.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                setDisplayItem(event.getCursor().getType());
                player.sendMessage(getPlugin().getLangFile().get(player, "info.item-set"));
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
                        event.getWhoClicked().closeInventory();
                    });
                }
            }

            ItemBuilder confirmBuilder = new ItemBuilder(XMaterial.WRITABLE_BOOK)
                    .setName(getPlugin().getLangFile().get(player, "gui-buttons.delete-confirm.name"))
                    .setLore(getPlugin().getLangFile().get(player, "gui-buttons.delete-confirm.description"))
                    .hideTags();

            page.setItemOnXY(5, 6, confirmBuilder, "arena-edit-comfirm-info-button",
                    event1 -> {
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(6, 3, pos2, "arena-edit-position2-button", event -> {
            setLocation(player, "fighter2");
        });

        menu.openGui(player);
    }

    private void setLocation(Player player, String key) {
        getConfig().setLocation("locations." + key, player.getLocation());
        getConfig().saveToFile();
        player.sendMessage(getPlugin().getLangFile().get(player, "info.location-set"));
    }

    public boolean isValidArena(Player player) {
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

        if (!player.hasPermission("bkduel.arenas") && !player.hasPermission("bkduel.arena." + getId())) {
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
        getConfig().saveToFile();

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
        getConfig().saveToFile();
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
