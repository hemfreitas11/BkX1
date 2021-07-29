package me.bkrmt.bkduel.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.*;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.opengui.gui.GUI;
import me.bkrmt.opengui.gui.Rows;
import me.bkrmt.opengui.item.ItemBuilder;
import me.bkrmt.opengui.page.Page;
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

public class ChooseKitsMenu {
    private static final int DROP_ITEM_SLOT = 29;
    private static final int DROP_EXP_SLOT = 33;
    private static final int NEXT_SLOT = 31;
    private static final int OWN_ITEMS_SLOT = 10;
    private static final int RANDOM_KIT_SLOT = 11;
    private static final int EXPANDED_MENU = 15;
    private static final int SMALL_MENU = 7;

    private static List<Kit> kits;
    private static ArrayList<Page> pages;

    public static void showGUI(Duel duel, Page previousMenu) {
        showGUI(duel, previousMenu, null, 0);
    }

    public static void showGUI(Duel duel, Page previousMenu, Arena bindingArena, int pageNumber) {
        kits = new ArrayList<>();
        pages = new ArrayList<>();
        
        File kitsFolder = BkDuel.getInstance().getFile("", "kits");
        if (kitsFolder.listFiles().length > 0) {
            for (File kit : kitsFolder.listFiles()) {
                try {
                    kits.add(new Kit(BkDuel.getInstance(), kit.getName().replace(".yml", "")));
                } catch (Exception ignored) {
                    BkDuel.getInstance().sendConsoleMessage(Utils.translateColor(InternalMessages.CORRUPT_KIT.getMessage(BkDuel.getInstance()).replace("{0}", kit.getName())));
                    duel.getFighter1().sendMessage(Utils.translateColor(InternalMessages.CORRUPT_KIT.getMessage(BkDuel.getInstance()).replace("{0}", kit.getName())));
                    return;
                }
            }
        }

        boolean isExpanded = false/*BkDuel.getInstance().getConfigManager().getConfig().getBoolean("expanded-kit-list")*/;

        Page previousPage = null;
        int tempSize = kits.size() > 0 ? kits.size() : 1;
        int rowSize = duel.getOptions().contains(DuelOptions.EDIT_MODE) ? (duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) ? 7 : 8) : 7;
        int pagesSize = (int) Math.ceil((double) tempSize / (double) rowSize);

        if (pages.isEmpty()) {
            for (int c = 0; c < pagesSize; c++) {
                Page page = new Page(BkDuel.getInstance(), BkDuel.getInstance().getAnimatorManager(), new GUI(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.choose-kit-title").replace("{page}", String.valueOf(c + 1)).replace("{total-pages}", String.valueOf(pagesSize)), (isExpanded ? Rows.FIVE : Rows.FOUR)), c + 1);
                pages.add(page);
                if (previousPage != null) {
                    previousPage.setNextPage(page);
                    page.setPreviousPage(previousPage);
                }
                previousPage = page;
            }
        }

        String name = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.no-kit-button.name");
        List<String> tempLore = Collections.singletonList(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.no-kit-button.description"));
        ItemStack playerHead = BkDuel.getInstance().getHandler().getItemManager().getHead();
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setDisplayName(name);
        headMeta.setLore(tempLore);
        headMeta.setOwningPlayer(BkDuel.getInstance().getServer().getOfflinePlayer(duel.getFighter1().getUniqueId()));
        playerHead.setItemMeta(headMeta);
        String tempName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.no-kit.name");
        ItemBuilder removeKit = new ItemBuilder(Utils.createItem(Material.GLASS, true,
                tempName,
                Collections.singletonList(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.no-kit.description"))));

        ItemBuilder ownItems = new ItemBuilder(playerHead);

        String tempName2 = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.random-kit-button.name");
        ItemBuilder randomItems = new ItemBuilder(Material.ENDER_PEARL)
                .setName(tempName2)
                .setLore(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.random-kit-button.description"))
                .hideTags();

        int kitIndex = pageNumber * rowSize;
        boolean second = false;

        for (int x = 0; x < pages.size(); x++) {
            Page page = pages.get(x);
            boolean first = x == 0;

            int index = duel.getOptions().contains(DuelOptions.EDIT_MODE) ? duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) ? x > 0 ? OWN_ITEMS_SLOT : RANDOM_KIT_SLOT : OWN_ITEMS_SLOT : x > 0 ? OWN_ITEMS_SLOT : 12;
            for (int i = 0; i < rowSize; i++) {
                if (kitIndex < tempSize) {
                    int inte = (int) (Math.random() * kits.size());
                    if (first) {
                        if (duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) && duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                            page.pageSetItem(OWN_ITEMS_SLOT, removeKit, "choose-kits-bound-own-items", event -> {
                                bindingArena.saveBoundKit(-1);
                                event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.bound-kit-set"));
                                duel.getOptions().remove(DuelOptions.BOUND_KIT_SELECTION);
                                bindingArena.showEditMenu(duel);
                            });
                        } else {
                            if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                page.pageSetItem(OWN_ITEMS_SLOT, ownItems, "choose-kits-own-items", event -> {
                                    Page.clearUnclickable(pages);
                                    List<String> lore = new ArrayList<>();
                                    lore.add(" ");
                                    lore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-selected"));
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
                            Page.clearUnclickable(pages);
                            List<String> lore = new ArrayList<>();
                            lore.add(" ");
                            lore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-selected"));
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
                            double playerMoney = BkDuel.getInstance().getEconomy().getBalance(duel.getFighter1());
                            List<String> extraLore = display.getItemMeta().getLore() == null ? new ArrayList<>() : display.getItemMeta().getLore();

                            if (!duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION)) {
                                if (kit.getPrice() == 0 || kit.isOwner(duel.getFighter1()) && !duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                    extraLore.add(" ");
                                    if (kit.getPrice() == 0)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.free-kit"));
                                    else
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-bought-lore"));
                                } else {
                                    extraLore.add(" ");
                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.price").replace("{price}", String.valueOf(kit.getPrice())));
                                    if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                        if (!extraLore.isEmpty()) extraLore.add(" ");
                                        if (playerMoney >= kit.getPrice())
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-buy"));
                                        else
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.not-enough-money"));
                                    }
                                }

                                if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                    if (!extraLore.isEmpty()) extraLore.add(" ");
                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-edit-kit"));
                                }
                            } else {
                                if (!extraLore.isEmpty()) extraLore.add(" ");
                                extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-select-kit"));
                            }


                            if (!duel.getFighter1().hasPermission("bkduel.kits") && !duel.getFighter1().hasPermission("bkduel.kit." + kit.getId())) {
                                if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                    extraLore.add(" ");
                                extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.no-kit-perm"));
                            }

                            if (duel.getFighter1().hasPermission("bkduel.admin") || duel.getFighter1().hasPermission("bkduel.edit")) {
                                if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                    extraLore.add(" ");
                                extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-id").replace("{id}", String.valueOf(kit.getId())));
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
                                        .setLore(extraLore);
                                page.pageSetItem(
                                        finalIndex, newDisplay, "choose-kits-display-" + finalIndex, event -> {
                                        }
                                );
                            } else {
                                page.pageSetItem(
                                        finalIndex, new ItemBuilder(display), "choose-kits-display-" + finalIndex,
                                        event -> {
                                            if (duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION)) {
                                                bindingArena.saveBoundKit(kit.getId());
                                                event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.bound-kit-set"));
                                                duel.getOptions().remove(DuelOptions.BOUND_KIT_SELECTION);
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
                                                            Page.clearUnclickable(pages);
                                                            List<String> lore = new ArrayList<>();
                                                            lore.add(" ");
                                                            lore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-selected"));
                                                            page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                                            duel.setKit(kits.get(finalKitIndex));
                                                            refreshButtons(duel);
                                                        }
                                                    } else {
                                                        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                            if (playerMoney >= kit.getPrice()) {
                                                                EconomyResponse r = BkDuel.getInstance().getEconomy().withdrawPlayer((OfflinePlayer) event.getWhoClicked(), kit.getPrice());
                                                                if (r.transactionSuccess()) {
                                                                    kit.addOwner((Player) event.getWhoClicked());
                                                                    event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-bought-message")
                                                                            .replace("{kit}", AnimatorManager.cleanText(Utils.translateColor(kit.getName())))
                                                                            .replace("{balance}", BkDuel.getInstance().getEconomy().format(r.balance)));
                                                                    ChooseKitsMenu.showGUI(duel, page);
                                                                } else {
                                                                    event.getWhoClicked().sendMessage(InternalMessages.ECONOMY_ERROR.getMessage(BkDuel.getInstance()).replace("{0}", r.errorMessage));
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


                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                        String temp2Name = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.create-kit.name");
                        page.pageSetItem(31, new ItemBuilder(Utils.createItem(BkDuel.getInstance().getHandler().getItemManager().getWritableBook(), true, temp2Name, Collections.singletonList(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.create-kit.description")))), "choose-kit-create-kit", event -> {
                            new PlayerInput(BkDuel.getInstance(), duel.getFighter1(), input -> {
                                String kitName = Utils.cleanString(input.toLowerCase()
                                        .replace(" ", "-")
                                        .replaceAll("\\P{L}+", ""));
                                if (!kitName.isEmpty()) {
                                    Kit newKit = new Kit(BkDuel.getInstance(), kitName);
                                    newKit.setName(input);
                                    duel.setKit(null);
                                    ChooseKitsMenu.showGUI(duel, null);
                                } else {
                                    for (Page tempPage : pages) {
                                        tempPage.setUnregisterOnClose(true);
                                    }
                                    ChooseKitsMenu.showGUI(duel, null);
                                    event.getWhoClicked().closeInventory();
                                    event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.no-letters"));
                                }
                            },
                                    input -> {
                                    })
                                    .setCancellable(false)
                                    .setTitle(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.input.kit-name"))
                                    .setSubTitle("")
                                    .sendInput();
                        });
                    }
                } else break;
            }

            if (duel.getOptions().contains(DuelOptions.RANDOM_KIT) && duel.getKit() != null) {
                List<String> randomLore = new ArrayList<>();
                randomLore.add(" ");
                randomLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.kit-selected"));
                page.setUnclickable(OWN_ITEMS_SLOT, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(OWN_ITEMS_SLOT).getPageItem().getItem().getItemMeta().getDisplayName()), randomLore);
            }

            refreshButtons(duel);
        }
        Page tempPage = pages.get(pageNumber);
        if (previousMenu != null) {
            tempPage.setPreviousPage(previousMenu);
            tempPage.setButtonSlots(new int[]{-1, -1});
        }
        tempPage.openGui(duel.getFighter1());
    }

    private static void refreshButtons(Duel duel) {
        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
            nextButton(duel);
            dropItemsButton(duel);
            dropExpButton(duel);
        }
    }

    private static void nextButton(Duel duel) {
        for (int x = 0; x < pages.size(); x++) {
            int finalX = x;
            Page page = pages.get(x);
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
                    String enabled = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.enabled");
                    String disabled = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.disabled");
                    if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                        kit = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.own-items");
                    } else if (duel.getOptions().contains(DuelOptions.RANDOM_KIT)) {
                        kit = Utils.translateColor(BkDuel.getInstance().getConfigManager().getConfig().getString("random-selection"));
                    } else {
                        kit = Utils.translateColor(AnimatorManager.cleanText(duel.getKit().getName()));
                    }
                    tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.you-selected").replace("{name}", Utils.translateColor(kit)));
                    if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                        tempLore.add(" ");
                        String itemDrop = duel.getOptions().contains(DuelOptions.DROP_ITEMS) ? enabled : disabled;
                        tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.item-drop").replace("{item-drop}", itemDrop));
                    }

                    if (!duel.getOptions().contains(DuelOptions.OWN_ITEMS)) tempLore.add(" ");
                    String dropExp = duel.getOptions().contains(DuelOptions.DROP_EXP) ? enabled : disabled;
                    tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.exp-drop").replace("{exp-drop}", dropExp));

                    tempLore.add(" ");
                    tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.next-button.enabled.description"));
                    displayName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.next-button.enabled.name");
                }

                nextButton = Page.buildButton(Material.SLIME_BALL, displayName, tempLore);

                double duelCost = BkDuel.getInstance().getConfigManager().getConfig().getDouble("duel-cost");

                page.pageSetItem(NEXT_SLOT, nextButton, "choose-kits-next-button", event -> {
                    if (BkDuel.getInstance().isInvalidChallenge(duel.getFighter1(), duel.getFighter2(), duelCost))
                        return;

                    if (!(duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS))) {
                        duel.startRequest();
                    }
                });
            } else {
                String tempName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.back-to-arenas.name");
                page.pageSetItem(NEXT_SLOT, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                        tempName,
                        Collections.singletonList(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.back-to-arenas.description")))), "choose-kits-next-button", event -> {
//                    pages.set(finalX, page);
                    ChooseArenaMenu.showGUI(duel, null);
                });
            }
        }
    }

    private static void dropExpButton(Duel duel) {
        for (Page page : pages) {
            ItemBuilder expButton;

            List<String> tempLore = new ArrayList<>();
            tempLore.add(" ");

            if (duel.getOptions().contains(DuelOptions.DROP_EXP)) {
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.enabled-exp-drop"));
                tempLore.add(" ");
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-disable"));
            } else {
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.disabled-exp-drop"));
                tempLore.add(" ");
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-enable"));
            }
            expButton = Page.buildButton(BkDuel.getInstance().getHandler().getItemManager().getExpBottle(), BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.drop-exp-title"), tempLore);


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
        for (Page page : pages) {
            ItemBuilder dropItems;
            List<String> tempLore = new ArrayList<>();

            if (!duel.getOptions().contains(DuelOptions.DROP_ITEMS)) {
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.disabled-item-drop"));
                tempLore.add(" ");
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-enable"));
            } else {
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.enabled-item-drop"));
                tempLore.add(" ");
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-disable"));
            }
            dropItems = Page.buildButton(Material.HOPPER, BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.drop-item-title"), tempLore);

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
            lore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.select-own-items"));
            if (duel.getKit() != null || !duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                page.setUnclickable(DROP_EXP_SLOT, false, BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.option-locked"), lore);
            }

        }
    }
}