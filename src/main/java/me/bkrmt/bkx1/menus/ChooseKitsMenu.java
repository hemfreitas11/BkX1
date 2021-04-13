package me.bkrmt.bkx1.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkx1.*;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
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
import java.util.List;
import java.util.logging.Level;

import static me.bkrmt.bkx1.BkX1.plugin;

public class ChooseKitsMenu {
    private static List<Kit> kits;

    public static void showGUI(Duel duel) {
        kits = new ArrayList<>();
        File kitsFolder = plugin.getFile("", "kits");
        if (kitsFolder.listFiles().length > 0) {
            for (File kit : kitsFolder.listFiles()) {
                try {
                    kits.add(new Kit(plugin, kit.getName().replace(".yml", "")));
                } catch (Exception ignored) {
                    plugin.getServer().getLogger().log(Level.SEVERE, "The kit in the file '" + kit.getName() + "' is corrupted and could not be loaded.");
                    ignored.printStackTrace();
                    return;
                }
            }
        }

        boolean isExpanded = plugin.getConfig().getBoolean("expanded-kit-list");

        Page previousPage = null;
        int pagesSize = (int) Math.ceil((double)kits.size()/(double) (isExpanded ? 15 : 7));
        for (int c = 0; c < pagesSize; c++) {
            Page page = new Page(plugin, new SimpleGUI(new GUI(plugin.getLangFile().get("info.choose-kit-title").replace("{page}", String.valueOf(c+1)).replace("{total-pages}", String.valueOf(pagesSize)), (isExpanded ? Rows.FIVE : Rows.FOUR))), c+1);
            duel.getKitPages().add(page);
            if (previousPage != null) {
                previousPage.setNextPage(page);
                page.setPreviousPage(previousPage);
            }
            previousPage = page;
        }

        String name = plugin.getConfig().getItemStack("gui-buttons.no-kit-button").getItemMeta().getDisplayName();
        List<String> tempLore = plugin.getConfig().getItemStack("gui-buttons.no-kit-button").getItemMeta().getLore();
        ItemStack playerHead = plugin.getHandler().getItemManager().getHead();
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
        headMeta.setDisplayName(name);
        headMeta.setLore(tempLore);
        headMeta.setOwningPlayer(plugin.getServer().getOfflinePlayer(duel.getFighter1().getUniqueId()));
        playerHead.setItemMeta(headMeta);

        ItemBuilder ownItems = new ItemBuilder(playerHead);

        ItemBuilder randomItems = new ItemBuilder(plugin.getConfig().getItemStack("gui-buttons.random-kit-button"));

        int kitIndex = 0;
        boolean first = true;
        boolean second = false;

        for (Page page : duel.getKitPages()) {
            if (!page.isBuilt()) {
                int index = 10;
                for (int i = 0; i < 7; i++) {
                    if (kitIndex < kits.size()) {
                        int finalKitIndex = kitIndex++;
                        int finalIndex = index;
                        int inte = (int) (Math.random() * kits.size());
                        if (first) {
                            page.setItem(10, ownItems, event -> {
                                Page.clearUnclickable(duel.getKitPages());
                                List<String> lore = new ArrayList<>();
                                lore.add(" ");
                                lore.add(plugin.getLangFile().get("info.kit-selected"));
                                page.setUnclickable(10, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(10).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                duel.setKit(null);
                                duel.getOptions().add(DuelOptions.OWN_ITEMS);
                                duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                duel.getOptions().remove(DuelOptions.RANDOM_KIT);
                                refreshButtons(duel);
                            });
                            first = false;
                            second = true;
                        } else if (second) {
                            page.setItem(11, randomItems, event -> {
                                Page.clearUnclickable(duel.getKitPages());
                                List<String> lore = new ArrayList<>();
                                lore.add(" ");
                                lore.add(plugin.getLangFile().get("info.kit-selected"));
                                page.setUnclickable(11, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(11).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                duel.setKit(kits.get(inte));
                                duel.getOptions().remove(DuelOptions.OWN_ITEMS);
                                duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                duel.getOptions().add(DuelOptions.RANDOM_KIT);
                                page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                refreshButtons(duel);
                            });
                            second = false;
                        } else {
                            Kit kit = kits.get(finalKitIndex);
                            ItemStack display = kit.getDisplayItem();
                            List<String> economyLore = display.getItemMeta().getLore() == null ? new ArrayList<>() : display.getItemMeta().getLore();
                            double playerMoney = BkX1.econ.getBalance(duel.getFighter1());
                            if (kit.getPrice() == 0 || kit.isOwner(duel.getFighter1())) {
                                economyLore.add(" ");
                                if (kit.getPrice() == 0) economyLore.add(plugin.getLangFile().get("info.free-kit"));
                                else economyLore.add(plugin.getLangFile().get("info.kit-bought"));
                            } else {
                                economyLore.add(" ");
                                economyLore.add("§aPreco: §2" + kit.getPrice());
                                economyLore.add(" ");
                                if (playerMoney >= kit.getPrice()) economyLore.add(plugin.getLangFile().get("info.click-to-buy"));
                                else economyLore.add(plugin.getLangFile().get("info.not-enough-money"));
                            }
                            ItemMeta meta = display.getItemMeta();
                            meta.setLore(economyLore);
                            display.setItemMeta(meta);

                            page.setItem(finalIndex,
                                    new ItemBuilder(display),
                                    event -> {
                                        if (kit.getPrice() == 0 || kit.isOwner(duel.getFighter1())) {
                                            duel.setKit(kits.get(finalKitIndex));
                                            if (!page.getItems().get(event.getSlot()).isUnclickable()) {
                                                duel.getOptions().remove(DuelOptions.OWN_ITEMS);
                                                duel.getOptions().remove(DuelOptions.DROP_ITEMS);
                                                duel.getOptions().remove(DuelOptions.RANDOM_KIT);
                                                Page.clearUnclickable(duel.getKitPages());
                                                List<String> lore = new ArrayList<>();
                                                lore.add(" ");
                                                lore.add(plugin.getLangFile().get("info.kit-selected"));
                                                page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), lore);
                                                duel.setKit(kits.get(finalKitIndex));
                                                refreshButtons(duel);
                                            }
                                        } else {
                                            if (playerMoney >= kit.getPrice()) {
                                                EconomyResponse r = BkX1.econ.withdrawPlayer((OfflinePlayer) event.getWhoClicked(), kit.getPrice());
                                                if(r.transactionSuccess()) {
                                                    kit.addOwner((Player) event.getWhoClicked());
                                                    event.getWhoClicked().sendMessage("§aVoce comprou o kit " + kit.getName() + "§a. Novo saldo: " + BkX1.econ.format(r.balance));
                                                    event.getWhoClicked().closeInventory();
                                                    page.setBuilt(false);
                                                    ChooseKitsMenu.showGUI(duel);
                                                } else {
                                                    event.getWhoClicked().sendMessage(String.format("An error occured: %s", r.errorMessage));
                                                }
                                            }
                                        }
                                    });
                        }
                        index++;
                    } else break;
                }
                page.setBuilt(true);
            }
            refreshButtons(duel);
        }

        duel.getKitPages().get(0).openGui(duel.getFighter1());
    }

    private static void refreshButtons(Duel duel) {
        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
            nextButton(duel);
            dropItemsButton(duel);
            dropExpButton(duel);
        }
    }

    private static void nextButton(Duel duel) {
        for (Page page : duel.getKitPages()) {
            ItemBuilder nextButton;

            List<String> tempLore = new ArrayList<>();
            tempLore.add(" ");
            String displayName;

            if (duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                tempLore.add("§cYou need to select a kit");
                tempLore.add("§cbefore continuing.");
                displayName = "§cNext";
            } else {
                String kit;
                String enabled = plugin.getLangFile().get("info.enabled");
                String disabled = plugin.getLangFile().get("info.disabled");
                if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)){
                    kit = plugin.getLangFile().get("info.own-items");
                } else if (duel.getOptions().contains(DuelOptions.RANDOM_KIT)) {
                    kit = Utils.translateColor(plugin.getConfig().getString("random-selection"));
                } else {
                    kit = Utils.translateColor(duel.getKit().getName());
                }
                tempLore.add("§7You selected: §a" + Utils.translateColor(kit));
                if (duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                    String itemDrop = duel.getOptions().contains(DuelOptions.DROP_ITEMS) ? enabled : disabled;
                    tempLore.add("§7Item drop is: " + itemDrop);
                }

                String dropExp = duel.getOptions().contains(DuelOptions.DROP_EXP) ? enabled : disabled;
                tempLore.add("§7Experience drop is: " + dropExp);

                tempLore.add(" ");
                tempLore.add("§aClick here to continue.");
                displayName = "§aNext";
            }

            nextButton = Page.buildButton(Material.SLIME_BALL, displayName, tempLore);

            page.setItem(31, nextButton, event -> {
                if (!(duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS))) {
                        ChooseArenaMenu.showGUI(duel);
                }
            });
        }
    }

    private static void dropExpButton(Duel duel) {
        for (Page page : duel.getKitPages()) {
            ItemBuilder expButton;

            List<String> tempLore = new ArrayList<>();
            tempLore.add(" ");

            if (duel.getOptions().contains(DuelOptions.DROP_EXP)) {
                tempLore.add("§7Experience drop is: §aEnabled");
                tempLore.add(" ");
                tempLore.add("§7Click to disable.");
            } else {
                tempLore.add("§7Experience drop is: §cDisabled");
                tempLore.add(" ");
                tempLore.add("§7Click to enable.");
            }
            expButton = Page.buildButton(plugin.getHandler().getItemManager().getExpBottle(), "§7Drop Experience", tempLore);


            page.setItem(29, expButton, event -> {
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
                tempLore.add("§7Item drop is: §cDisabled");
                tempLore.add(" ");
                tempLore.add("§7Click to enable.");
                dropItems = Page.buildButton(Material.HOPPER, "§7Drop items", tempLore);
            } else {
                tempLore.add("§7Item drop is: §aEnabled");
                tempLore.add(" ");
                tempLore.add("§7Click to disable.");
                dropItems = Page.buildButton(Material.HOPPER, "§7Drop items", tempLore);
            }

            page.setItem(33, dropItems, event -> {
                if (!(duel.getKit() == null && !duel.getOptions().contains(DuelOptions.OWN_ITEMS))) {
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
            lore.add("§cYou need to select your own");
            lore.add("§citems to use this option!");
            if (duel.getKit() != null || !duel.getOptions().contains(DuelOptions.OWN_ITEMS)) {
                page.setUnclickable(33, false, "§cOption Locked", lore);
            }

        }
    }
}