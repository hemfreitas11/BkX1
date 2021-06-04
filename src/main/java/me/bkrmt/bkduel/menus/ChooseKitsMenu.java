package me.bkrmt.bkduel.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.*;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Page;
import me.bkrmt.opengui.Rows;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.bkrmt.bkduel.BkDuel.PLUGIN;

public class ChooseKitsMenu {
    private static final int DROP_ITEM_SLOT = 29;
    private static final int DROP_EXP_SLOT = 33;
    private static final int NEXT_SLOT = 31;
    private static final int OWN_ITEMS_SLOT = 10;
    private static final int RANDOM_KIT_SLOT = 11;
    private static final int EXPANDED_MENU = 15;
    private static final int SMALL_MENU = 7;

    private static List<Kit> kits;

    public static void showGUI(Duel duel) {
        showGUI(duel, null, 0);
    }

    public static void showGUI(Duel duel, Arena bindingArena, int pageNumber) {
        kits = new ArrayList<>();
        File kitsFolder = PLUGIN.getFile("", "kits");
        if (kitsFolder.listFiles().length > 0) {
            for (File kit : kitsFolder.listFiles()) {
                try {
                    kits.add(new Kit(PLUGIN, kit.getName().replace(".yml", "")));
                } catch (Exception ignored) {
                    PLUGIN.sendConsoleMessage(Utils.translateColor(InternalMessages.CORRUPT_KIT.getMessage(PLUGIN).replace("{0}", kit.getName())));
                    duel.getFighter1().sendMessage(Utils.translateColor(InternalMessages.CORRUPT_KIT.getMessage(PLUGIN).replace("{0}", kit.getName())));
                    return;
                }
            }
        }

        boolean isExpanded = false/*PLUGIN.getConfig().getBoolean("expanded-kit-list")*/;

        Page previousPage = null;
        int tempSize = kits.size() > 0 ? kits.size() : 1;
        int rowSize = duel.getOptions().contains(DuelOptions.EDIT_MODE) ? duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) ? 7 : 8 : 7;
        int pagesSize = (int) Math.ceil((double) tempSize / (double) rowSize);
        if (duel.getKitPages().isEmpty()) {
            for (int c = 0; c < pagesSize; c++) {
                Page page = new Page(PLUGIN, BkDuel.getAnimatorManager(), new GUI(PLUGIN.getLangFile().get("info.choose-kit-title").replace("{page}", String.valueOf(c + 1)).replace("{total-pages}", String.valueOf(pagesSize)), (isExpanded ? Rows.FIVE : Rows.FOUR)), c + 1);
                duel.getKitPages().add(page);
                if (previousPage != null) {
                    previousPage.setNextPage(page);
                    page.setPreviousPage(previousPage);
                }
                previousPage = page;
            }
        }

        String name = PLUGIN.getLangFile().get("gui-buttons.no-kit-button.name");
        List<String> tempLore = Collections.singletonList(PLUGIN.getLangFile().get("gui-buttons.no-kit-button.description"));
        ItemStack playerHead = PLUGIN.getHandler().getItemManager().getHead();
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setDisplayName(name);
        headMeta.setLore(tempLore);
        headMeta.setOwningPlayer(PLUGIN.getServer().getOfflinePlayer(duel.getFighter1().getUniqueId()));
        playerHead.setItemMeta(headMeta);
        String tempName = PLUGIN.getLangFile().get("gui-buttons.no-kit.name");
        ItemBuilder removeKit = new ItemBuilder(Utils.createItem(Material.GLASS, true,
                tempName,
                Collections.singletonList(PLUGIN.getLangFile().get("gui-buttons.no-kit.description")))).setUnchangedName(tempName);

        ItemBuilder ownItems = new ItemBuilder(playerHead).setUnchangedName(name);

        String tempName2 = PLUGIN.getLangFile().get("gui-buttons.random-kit-button.name");
        ItemBuilder randomItems = new ItemBuilder(Material.ENDER_PEARL)
                .setName(tempName2)
                .setLore(PLUGIN.getLangFile().get("gui-buttons.random-kit-button.description"))
                .setUnchangedName(tempName2)
                .hideTags();

        int kitIndex = pageNumber * rowSize;
        boolean second = false;

        for (int x = 0; x < duel.getKitPages().size(); x++) {
            Page page = duel.getKitPages().get(x);
            boolean first = x == 0;

            int index = duel.getOptions().contains(DuelOptions.EDIT_MODE) ? duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) ? x > 0 ? OWN_ITEMS_SLOT : RANDOM_KIT_SLOT : OWN_ITEMS_SLOT : x > 0 ? OWN_ITEMS_SLOT : 12;
            for (int i = 0; i < rowSize; i++) {
                if (kitIndex < tempSize) {
                    int inte = (int) (Math.random() * kits.size());
                    if (/*!page.isBuilt() || duel.getOptions().contains(DuelOptions.EDIT_MODE)*/true) {
                        if (first) {
                            if (duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) && duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                page.pageSetItem(OWN_ITEMS_SLOT, removeKit, "choose-kits-bound-own-items", event -> {
                                    bindingArena.saveBoundKit(-1);
                                    event.getWhoClicked().sendMessage(PLUGIN.getLangFile().get("info.bound-kit-set"));
                                    duel.getOptions().remove(DuelOptions.BOUND_KIT_SELECTION);
                                    page.setSwitchingPages(true);
                                    bindingArena.showEditMenu(duel);
                                });
                            } else {
                                if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                    page.pageSetItem(OWN_ITEMS_SLOT, ownItems, "choose-kits-own-items", event -> {
                                        Page.clearUnclickable(duel.getKitPages());
                                        List<String> lore = new ArrayList<>();
                                        lore.add(" ");
                                        lore.add(PLUGIN.getLangFile().get("info.kit-selected"));
                                        page.setUnclickable(OWN_ITEMS_SLOT, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(OWN_ITEMS_SLOT).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                        duel.setKit(null);
                                        duel.getOptions().add(DuelOptions.OWN_ITEMS);
                                        duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                        duel.getOptions().remove(DuelOptions.RANDOM_KIT);
                                        refreshButtons(duel);
                                    });
                                }
                            }
                            first = false;
                            second = true;
                        } else if (second && !duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) && !duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                            page.pageSetItem(RANDOM_KIT_SLOT, randomItems, "choose-kits-random-kit", event -> {
                                Page.clearUnclickable(duel.getKitPages());
                                List<String> lore = new ArrayList<>();
                                lore.add(" ");
                                lore.add(PLUGIN.getLangFile().get("info.kit-selected"));
                                page.setUnclickable(RANDOM_KIT_SLOT, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(RANDOM_KIT_SLOT).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                duel.setKit(kits.get(inte));
                                duel.getOptions().remove(DuelOptions.OWN_ITEMS);
                                duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                duel.getOptions().add(DuelOptions.RANDOM_KIT);
//                                page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                refreshButtons(duel);
                            });
                            second = false;
                        } else {
                            int finalKitIndex = kitIndex++;
                            int finalIndex = index;
                            if (kits.size() > 0) {
                                Kit kit = kits.get(finalKitIndex);
                                ItemStack display = kit.getDisplayItem();
                                double playerMoney = BkDuel.getEconomy().getBalance(duel.getFighter1());
                                List<String> extraLore = display.getItemMeta().getLore() == null ? new ArrayList<>() : display.getItemMeta().getLore();

                                if (!duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION)) {
                                    if (kit.getPrice() == 0 || kit.isOwner(duel.getFighter1()) && !duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                        extraLore.add(" ");
                                        if (kit.getPrice() == 0)
                                            extraLore.add(PLUGIN.getLangFile().get("info.free-kit"));
                                        else extraLore.add(PLUGIN.getLangFile().get("info.kit-bought-lore"));
                                    } else {
                                        extraLore.add(" ");
                                        extraLore.add(PLUGIN.getLangFile().get("info.price").replace("{price}", String.valueOf(kit.getPrice())));
                                        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                            if (!extraLore.isEmpty()) extraLore.add(" ");
                                            if (playerMoney >= kit.getPrice())
                                                extraLore.add(PLUGIN.getLangFile().get("info.click-to-buy"));
                                            else
                                                extraLore.add(PLUGIN.getLangFile().get("info.not-enough-money"));
                                        }
                                    }

                                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                        if (!extraLore.isEmpty()) extraLore.add(" ");
                                        extraLore.add(PLUGIN.getLangFile().get("info.click-to-edit-kit"));
                                    }
                                } else {
                                    if (!extraLore.isEmpty()) extraLore.add(" ");
                                    extraLore.add(PLUGIN.getLangFile().get("info.click-to-select-kit"));
                                }


                                if (!duel.getFighter1().hasPermission("bkduel.kits") && !duel.getFighter1().hasPermission("bkduel.kit." + kit.getId())) {
                                    if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                        extraLore.add(" ");
                                    extraLore.add(PLUGIN.getLangFile().get("error.no-kit-perm"));
                                }

                                if (duel.getFighter1().hasPermission("bkduel.admin") || duel.getFighter1().hasPermission("bkduel.edit")) {
                                    if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                        extraLore.add(" ");
                                    extraLore.add(PLUGIN.getLangFile().get("info.kit-id").replace("{id}", String.valueOf(kit.getId())));
                                }

                                ItemMeta meta = display.getItemMeta();
                                meta.setLore(extraLore);
                                display.setItemMeta(meta);


                                if (!duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) && !duel.getOptions().contains(DuelOptions.EDIT_MODE) && !duel.getFighter1().hasPermission("bkduel.kits") && !duel.getFighter1().hasPermission("bkduel.kit." + kit.getId())) {
                                    String kitName = kit.getConfig().getString("name");
                                    for (String line : extraLore) {
                                        if (extraLore.indexOf(line) < (extraLore.size() - (duel.getFighter1().hasPermission("bkduel.edit") || duel.getFighter1().hasPermission("bkduel.admin") ? 3 : 2))) {
                                            extraLore.set(extraLore.indexOf(line), ChatColor.DARK_GRAY + ChatColor.stripColor(Utils.translateColor(line)));
                                        }
                                    }

                                    ItemBuilder newDisplay = new ItemBuilder(Material.BARRIER)
                                            .setName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + AnimatorManager.cleanText(kitName))
                                            .setUnchangedName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + AnimatorManager.cleanText(kitName))
                                            .setLore(extraLore);
                                    page.pageSetItem(
                                            finalIndex, newDisplay, "choose-kits-display-" + finalIndex, event -> {
                                            }
                                    );
                                } else {
                                    page.pageSetItem(
                                            finalIndex, new ItemBuilder(display).setUnchangedName(kit.getConfig().getString("name")), "choose-kits-display-" + finalIndex,
                                            event -> {
                                                if (duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION)) {
                                                    bindingArena.saveBoundKit(kit.getId());
                                                    event.getWhoClicked().sendMessage(PLUGIN.getLangFile().get("info.bound-kit-set"));
                                                    duel.getOptions().remove(DuelOptions.BOUND_KIT_SELECTION);
                                                    page.setSwitchingPages(true);
                                                    bindingArena.showEditMenu(duel);
                                                } else {
                                                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                        kit.showEditMenu(duel);
                                                    } else {
                                                        if (kit.getPrice() == 0 || kit.isOwner(duel.getFighter1())) {
                                                            duel.setKit(kits.get(finalKitIndex));
                                                            if (!page.getItems().get(event.getSlot()).isUnclickable()) {
                                                                duel.getOptions().remove(DuelOptions.OWN_ITEMS);
                                                                duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                                                duel.getOptions().remove(DuelOptions.RANDOM_KIT);
                                                                Page.clearUnclickable(duel.getKitPages());
                                                                List<String> lore = new ArrayList<>();
                                                                lore.add(" ");
                                                                lore.add(PLUGIN.getLangFile().get("info.kit-selected"));
                                                                page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                                                duel.setKit(kits.get(finalKitIndex));
                                                                refreshButtons(duel);
                                                            }
                                                        } else {
                                                            if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                                if (playerMoney >= kit.getPrice()) {
                                                                    EconomyResponse r = BkDuel.getEconomy().withdrawPlayer((OfflinePlayer) event.getWhoClicked(), kit.getPrice());
                                                                    if (r.transactionSuccess()) {
                                                                        kit.addOwner((Player) event.getWhoClicked());
                                                                        event.getWhoClicked().sendMessage(PLUGIN.getLangFile().get("info.kit-bought-message")
                                                                                .replace("{kit}", AnimatorManager.cleanText(Utils.translateColor(kit.getName())))
                                                                                .replace("{balance}", BkDuel.getEconomy().format(r.balance)));
                                                                        event.getWhoClicked().closeInventory();
                                                                        page.setBuilt(false);
                                                                        ChooseKitsMenu.showGUI(duel/*, null, finalX*/);
                                                                    } else {
                                                                        event.getWhoClicked().sendMessage(InternalMessages.ECONOMY_ERROR.getMessage(PLUGIN).replace("{0}", r.errorMessage));
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    );
                                }
                                index++;
                            }
                        }
                    }

                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                        String temp2Name = PLUGIN.getLangFile().get("gui-buttons.create-kit.name");
                        page.pageSetItem(31, new ItemBuilder(Utils.createItem(PLUGIN.getHandler().getItemManager().getWritableBook(), true, temp2Name, Collections.singletonList(PLUGIN.getLangFile().get("gui-buttons.create-arena.description")))).setUnchangedName(tempName), "choose-arena-create-kit", event -> {
                            new PlayerInput(PLUGIN, duel.getFighter1(), input -> {
                                String kitName = Utils.cleanString(input.toLowerCase()
                                        .replace(" ", "-")
                                        .replaceAll("\\P{L}+", ""));
                                if (!kitName.isEmpty()) {
                                    Kit newKit = new Kit(PLUGIN, kitName);
                                    newKit.setName(input);
                                    duel.setKit(null);
                                    page.setSwitchingPages(true);
                                    page.setBuilt(false);
                                    ChooseKitsMenu.showGUI(duel);
                                } else {
                                    event.getWhoClicked().sendMessage(PLUGIN.getLangFile().get("error.no-letters"));
                                }
                            },
                                    input -> {
                                    })
                                    .setCancellable(false)
                                    .setTitle(PLUGIN.getLangFile().get("info.input.kit-name"))
                                    .setSubTitle("")
                                    .sendInput();
                        });
                    }
                } else break;
            }
            page.setBuilt(true);

            if (duel.getOptions().contains(DuelOptions.RANDOM_KIT) && duel.getKit() != null) {
                List<String> randomLore = new ArrayList<>();
                randomLore.add(" ");
                randomLore.add(PLUGIN.getLangFile().get("info.kit-selected"));
                page.setUnclickable(OWN_ITEMS_SLOT, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(OWN_ITEMS_SLOT).getPageItem().getItem().getItemMeta().getDisplayName()), randomLore);
            }

            refreshButtons(duel);
        }

        duel.getKitPages().get(pageNumber).openGui(duel.getFighter1());
    }

    private static void refreshButtons(Duel duel) {
        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
            nextButton(duel);
            dropItemsButton(duel);
            dropExpButton(duel);
        }
    }

    private static void nextButton(Duel duel) {

        for (int x = 0; x < duel.getKitPages().size(); x++) {
            int finalX = x;
            Page page = duel.getKitPages().get(x);
            if (duel.getKit() != null || duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                ItemBuilder nextButton;

                List<String> tempLore = new ArrayList<>();
                tempLore.add(" ");
                String displayName;

                if (duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                    tempLore.add("§cYou need to select a kit before continuing.");
                    displayName = "§cNext";
                } else {
                    String kit;
                    String enabled = PLUGIN.getLangFile().get("info.enabled");
                    String disabled = PLUGIN.getLangFile().get("info.disabled");
                    if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                        kit = PLUGIN.getLangFile().get("info.own-items");
                    } else if (duel.getOptions().contains(DuelOptions.RANDOM_KIT)) {
                        kit = Utils.translateColor(PLUGIN.getConfig().getString("random-selection"));
                    } else {
                        kit = Utils.translateColor(BkDuel.getAnimatorManager().cleanText(duel.getKit().getName()));
                    }
                    tempLore.add(PLUGIN.getLangFile().get("info.you-selected").replace("{name}", Utils.translateColor(kit)));
                    if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                        tempLore.add(" ");
                        String itemDrop = duel.getOptions().contains(DuelOptions.DROP_ITEMS) ? enabled : disabled;
                        tempLore.add(PLUGIN.getLangFile().get("info.item-drop").replace("{item-drop}", itemDrop));
                    }

                    if (!duel.getOptions().contains(DuelOptions.OWN_ITEMS)) tempLore.add(" ");
                    String dropExp = duel.getOptions().contains(DuelOptions.DROP_EXP) ? enabled : disabled;
                    tempLore.add(PLUGIN.getLangFile().get("info.exp-drop").replace("{exp-drop}", dropExp));

                    tempLore.add(" ");
                    tempLore.add(PLUGIN.getLangFile().get("gui-buttons.next-button.enabled.description"));
                    displayName = PLUGIN.getLangFile().get("gui-buttons.next-button.enabled.name");
                }

                nextButton = Page.buildButton(Material.SLIME_BALL, displayName, tempLore);

                page.pageSetItem(NEXT_SLOT, nextButton, "choose-kits-next-button", event -> {
                    if (!(duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS))) {
                        duel.startRequest();
                    }
                });
            } else {
                String tempName = PLUGIN.getLangFile().get("gui-buttons.back-to-arenas.name");
                page.pageSetItem(NEXT_SLOT, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                        tempName,
                        Collections.singletonList(PLUGIN.getLangFile().get("gui-buttons.back-to-arenas.description")))).setUnchangedName(tempName), "choose-kits-next-button", event -> {
                    page.setSwitchingPages(false);
                    duel.getKitPages().set(finalX, page);
                    ChooseArenaMenu.showGUI(duel);
                });
            }
        }
    }

    private static void dropExpButton(Duel duel) {
        for (Page page : duel.getKitPages()) {
            ItemBuilder expButton;

            List<String> tempLore = new ArrayList<>();
            tempLore.add(" ");

            if (duel.getOptions().contains(DuelOptions.DROP_EXP)) {
                tempLore.add(PLUGIN.getLangFile().get("info.enabled-exp-drop"));
                tempLore.add(" ");
                tempLore.add(PLUGIN.getLangFile().get("info.click-to-disable"));
            } else {
                tempLore.add(PLUGIN.getLangFile().get("info.disabled-exp-drop"));
                tempLore.add(" ");
                tempLore.add(PLUGIN.getLangFile().get("info.click-to-enable"));
            }
            expButton = Page.buildButton(PLUGIN.getHandler().getItemManager().getExpBottle(), PLUGIN.getLangFile().get("info.drop-exp-title"), tempLore);


            page.pageSetItem(DROP_ITEM_SLOT, expButton, "choose-kits-drop-exp-button", event -> {
                if (duel.getOptions().contains(DuelOptions.DROP_EXP)) {
                    duel.getOptions().remove(DuelOptions.DROP_EXP);
                } else {
                    duel.getOptions().add(DuelOptions.DROP_EXP);
                }
                refreshButtons(duel);
            });
        }
    }

    private static void dropItemsButton(Duel duel) {
        for (Page page : duel.getKitPages()) {
            ItemBuilder dropItems;
            List<String> tempLore = new ArrayList<>();

            if (!duel.getOptions().contains(DuelOptions.DROP_ITEMS)) {
                tempLore.add(PLUGIN.getLangFile().get("info.disabled-item-drop"));
                tempLore.add(" ");
                tempLore.add(PLUGIN.getLangFile().get("info.click-to-enable"));
            } else {
                tempLore.add(PLUGIN.getLangFile().get("info.enabled-item-drop"));
                tempLore.add(" ");
                tempLore.add(PLUGIN.getLangFile().get("info.click-to-disable"));
            }
            dropItems = Page.buildButton(Material.HOPPER, PLUGIN.getLangFile().get("info.drop-item-title"), tempLore);

            page.pageSetItem(DROP_EXP_SLOT, dropItems, "choose-kits-drop-items-button", event -> {
                if (duel.getKit() == null && duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                    if (duel.getOptions().contains(DuelOptions.DROP_ITEMS)) {
                        duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                    } else {
                        duel.getOptions().add(DuelOptions.DROP_ITEMS);
                    }
                    refreshButtons(duel);
                }
            });


            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.add(PLUGIN.getLangFile().get("info.select-own-items"));
            if (duel.getKit() != null || !duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                page.setUnclickable(DROP_EXP_SLOT, false, PLUGIN.getLangFile().get("info.option-locked"), lore);
            }

        }
    }
}