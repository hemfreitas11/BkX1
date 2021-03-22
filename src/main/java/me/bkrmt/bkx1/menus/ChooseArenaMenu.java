package me.bkrmt.bkx1.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkx1.*;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.bkrmt.bkx1.BkX1.plugin;

public class ChooseArenaMenu {
    private static ArrayList<Arena> arenas;
    private static ArrayList<Page> pages;

    public static void showGUI(Duel duel) {
        arenas = new ArrayList<>();
        pages = new ArrayList<>();

        File[] listFiles = plugin.getFile("", "arenas").listFiles();
        if (listFiles.length > 0) {
            for (File arena : listFiles) {
                arenas.add(new Arena(plugin, arena.getName()));
            }
        }

        Page previousPage = null;
        int tempSize = getArenas().size() > 0 ? getArenas().size() : 1;

        int pagesSize = (int) Math.ceil((double) tempSize/(double) 7);
        for (int c = 0; c < pagesSize; c++) {
            Page page = new Page(plugin, new SimpleGUI(new GUI("&cChoose the arena " + (c+1) + "/" + pagesSize, Rows.FOUR)), c+1);
            pages.add(page);
            if (previousPage != null) {
                previousPage.setNextPage(page);
                page.setPreviousPage(previousPage);
            }
            previousPage = page;
        }

        List<String> lore = new ArrayList<>();
        lore.add("aksjdlaksjdlk");
        lore.add("sakqplek");
        lore.add("sakqpleksaldk");
        List<String> newLore = new ArrayList<>();
        newLore.add(" ");
        newLore.add(plugin.getLangFile().get("info.arena-selected"));

        int arenaIndex = 0;
        for (Page page : pages) {
            if (!page.isBuilt()) {
                int index = 10;
                for (int i = 0; i < 7; i++) {
                    if (arenaIndex < tempSize) {
                        if (getArenas().size() > 0) {
                            int finalIndex = index++;
                            Arena arena = getArenas().get(arenaIndex++);
                            page.setItem(finalIndex,
                                    new ItemBuilder(arena.getDisplayItem()),
                                    event -> {
                                        if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                            arena.showEditMenu(duel);
                                        } else if (!page.getItems().get(event.getSlot()).isUnclickable()) {
                                            Page.clearUnclickable(pages);
                                            page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()),  newLore);
                                            duel.setArena(arena);
                                            refreshButtons(duel);
                                        }
                                    }
                            );
                            if (duel.getArena() != null && duel.getArena().getName().equals(arena.getName())) {
                                page.setUnclickable(finalIndex, false, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(finalIndex).getPageItem().getItem().getItemMeta().getDisplayName()), newLore);
                            }
                        }

                        if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                            page.setItem(31, new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getWritableBook(), true, "§aCreate Arena", lore)), event -> {
                                new PlayerInput(plugin, duel.getFighter1(), input -> {
                                    String arenaName = Utils.cleanString(input.toLowerCase()
                                            .replace(" ", "-")
                                            .replaceAll("\\P{L}+", ""));
                                    Arena newArena = new Arena(plugin, arenaName + ".yml");
                                    newArena.setName(input);
                                    duel.setArena(null);
                                    ChooseArenaMenu.showGUI(duel);
                                },
                                        input -> {
                                        })
                                        .setCancellable(false)
                                        .setSubTitle("asdasd")
                                        .setTitle("Digite o nome da arena")
                                        .sendInput();
                            });
                        } else {
                            page.setItem(29, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, "§aGo back to kits", lore)), event -> {
                                ChooseKitsMenu.showGUI(duel);
                            });
                        }
                    } else break;
                }
                page.setBuilt(true);
            }
            refreshButtons(duel);
        }

        pages.get(0).openGui(duel.getFighter1());
    }

    private static void refreshButtons(Duel duel) {
        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
            nextButton(duel);
        }
    }

    private static void nextButton(Duel duel) {
        for (Page page : pages) {
            ItemBuilder nextButton;

            List<String> tempLore = new ArrayList<>();
            tempLore.add(" ");
            String displayName;

            if (duel.getArena() == null) {
                tempLore.add("§cYou need to select an arena");
                tempLore.add("§cbefore continuing.");
                displayName = "§cNext";
            } else {
                String arena = Utils.translateColor(duel.getArena().getName());
                tempLore.add("§7You selected: §a" + arena);

                tempLore.add(" ");
                tempLore.add("§aClick here to continue.");
                displayName = "§aNext";
            }

            nextButton = Page.buildButton(Material.SLIME_BALL, displayName, tempLore);

            page.setItem(31, nextButton, event -> {
                if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                    if (duel.getArena() != null) {
                        duel.getFighter1().closeInventory();
                        duel.getFighter1().sendMessage("Duel request sent.");
                        new DuelRequest(duel).sendMessage();
                    }
                } else {
                    //Edit mode
                }
            });
        }
    }

    public static ArrayList<Arena> getArenas() {
        return arenas;
    }
}
