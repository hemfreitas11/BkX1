package me.bkrmt.bkx1.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkx1.*;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import me.bkrmt.teleport.Teleport;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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


        if (duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
            for (Duel ongoingDuel : BkX1.getOngoingDuels().values()) {
                boolean contains = false;
                for (Arena arena : arenas) {
                    if (arena.getId() == ongoingDuel.getArena().getId()) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) arenas.add(ongoingDuel.getArena());
            }
        } else {
            File[] listFiles = plugin.getFile("", "arenas").listFiles();
            if (listFiles.length > 0) {
                for (File arena : listFiles) {
                    arenas.add(new Arena(plugin, arena.getName().replace(".yml", "")));
                }
            }
        }

        Page previousPage = null;
        int tempSize = getArenas().size() > 0 ? getArenas().size() : 1;
        int rowSize = duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) ? 5 : 7;
        int pagesSize = (int) Math.ceil((double) tempSize / (double) rowSize);
        for (int c = 0; c < pagesSize; c++) {
            Page page = new Page(plugin, new SimpleGUI(new GUI("&cChoose the arena " + (c + 1) + "/" + pagesSize, duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) ? Rows.THREE : Rows.FOUR)), c + 1);
            pages.add(page);
            if (previousPage != null) {
                page.setPreviousPage(previousPage);
                previousPage.setNextPage(page);
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
        boolean first = true;
        for (Page page : pages) {
            if (!page.isBuilt() || duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                int index = duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) ? 11 : 10;
                for (int i = 0; i < rowSize; i++) {
                    if (arenaIndex < tempSize) {
                        if (getArenas().size() > 0) {
                            int finalIndex = index;
                            if (first && !duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                ItemStack randomDisplay = plugin.getConfig().getItemStack("gui-buttons.random-arena-button");
                                ItemBuilder randomArena = new ItemBuilder(randomDisplay);
                                int inte = (int) (Math.random() * arenas.size());

                                page.setItem(10, randomArena, event -> {
                                    Page.clearUnclickable(duel.getKitPages());
                                    List<String> randomLore = new ArrayList<>();
                                    randomLore.add(" ");
                                    randomLore.add(plugin.getLangFile().get("info.arena-selected"));

                                    page.setUnclickable(10, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(10).getPageItem().getItem().getItemMeta().getDisplayName()), randomLore);
                                    duel.setArena(arenas.get(inte));
                                    duel.getOptions().add(DuelOptions.RANDOM_ARENA);
                                    refreshButtons(duel);
                                });
                                first = false;
                            } else {
                                Arena arena = getArenas().get(arenaIndex++);
                                ItemStack display = arena.getDisplayItem();
                                double playerMoney = BkX1.econ.getBalance(duel.getFighter1());

                                if (!duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                    List<String> economyLore = display.getItemMeta().getLore() == null ? new ArrayList<>() : display.getItemMeta().getLore();
                                    if (arena.getPrice() == 0 || arena.isOwner(duel.getFighter1())) {
                                        economyLore.add(" ");
                                        if (arena.getPrice() == 0) economyLore.add("§aArena Grátis");
                                        else economyLore.add("§aVoce comprou essa arena");
                                    } else {
                                        economyLore.add(" ");
                                        economyLore.add("§aPreco: §2" + arena.getPrice());
                                        economyLore.add(" ");
                                        if (playerMoney >= arena.getPrice()) economyLore.add("§aClique para comprar");
                                        else economyLore.add("§cSaldo insuficiente para comprar");
                                    }
                                    ItemMeta meta = display.getItemMeta();
                                    meta.setLore(economyLore);
                                    display.setItemMeta(meta);
                                } else {
                                    Duel spectatedDuel = findDuel(arena);

                                    if (spectatedDuel != null) {
                                        List<String> economyLore = arena.getConfig().getLore("display-item.lore");
                                        economyLore.add(" ");
                                        economyLore.add("§7Fighter 1: §a" + spectatedDuel.getFighter1().getName());
                                        economyLore.add("§7Fighter 2: §a" + spectatedDuel.getFighter2().getName());
                                        economyLore.add(" ");
                                        economyLore.add("§aClick to go to the arena!");
                                        ItemMeta meta = display.getItemMeta();
                                        meta.setLore(economyLore);
                                        display.setItemMeta(meta);
                                    }
                                }

                                if (duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                    if (arena.isInUse()) {
                                        page.setItem(finalIndex, new ItemBuilder(display), event -> {
                                            duel.getFighter1().closeInventory();
                                            new Teleport(plugin, duel.getFighter1(), false)
                                                    .setLocation(arena.getName(), arena.getSpectators())
                                                    .setDuration(3)
                                                    .setIsCancellable(true)
                                                    .startTeleport();
                                        });
                                        index++;
                                    }
                                } else {
                                    page.setItem(finalIndex,
                                            new ItemBuilder(display),
                                            event -> {
                                                if (arena.getPrice() == 0 || arena.isOwner(duel.getFighter1())) {
                                                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                        arena.showEditMenu(duel);
                                                    } else if (!page.getItems().get(event.getSlot()).isUnclickable()) {
                                                        Page.clearUnclickable(pages);
                                                        page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), newLore);
                                                        duel.setArena(arena);
                                                        duel.getOptions().remove(DuelOptions.RANDOM_ARENA);
                                                        refreshButtons(duel);
                                                    }
                                                } else {
                                                    if (playerMoney >= arena.getPrice()) {
                                                        EconomyResponse r = BkX1.econ.withdrawPlayer((OfflinePlayer) event.getWhoClicked(), arena.getPrice());
                                                        if (r.transactionSuccess()) {
                                                            arena.addOwner((Player) event.getWhoClicked());
                                                            event.getWhoClicked().sendMessage("§aVoce comprou a arena " + arena.getName() + "§a. Novo saldo: " + BkX1.econ.format(r.balance));
                                                            event.getWhoClicked().closeInventory();
                                                            page.setBuilt(false);
                                                            ChooseArenaMenu.showGUI(duel);
                                                        } else {
                                                            event.getWhoClicked().sendMessage(String.format("An error occured: %s", r.errorMessage));
                                                        }
                                                    }
                                                }
                                            }
                                    );
                                    if (duel.getArena() != null && duel.getArena().getName().equals(arena.getName())) {
                                        page.setUnclickable(finalIndex, false, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(finalIndex).getPageItem().getItem().getItemMeta().getDisplayName()), newLore);
                                    }
                                    index++;
                                }
                            }
                        }

                        if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                            page.setItem(31, new ItemBuilder(Utils.createItem(plugin.getHandler().getItemManager().getWritableBook(), true, "§aCreate Arena", lore)), event -> {
                                new PlayerInput(plugin, duel.getFighter1(), input -> {
                                    String arenaName = Utils.cleanString(input.toLowerCase()
                                            .replace(" ", "-")
                                            .replaceAll("\\P{L}+", ""));
                                    Arena newArena = new Arena(plugin, arenaName);
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
                            if (!duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                page.setItem(29, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, "§aGo back to kits", lore)), event -> {
                                    ChooseKitsMenu.showGUI(duel);
                                });
                            }
                        }
                    } else break;
                }
                page.setBuilt(true);
            }
            refreshButtons(duel);
        }

        pages.get(0).openGui(duel.getFighter1());
    }

    private static Duel findDuel(Arena arena) {
        Duel spectatedDuel = null;
        for (Duel ongoingDuel : BkX1.getOngoingDuels().values()) {
            if (arena.getId() == ongoingDuel.getArena().getId()) {
                spectatedDuel = ongoingDuel;
                break;
            }
        }
        return spectatedDuel;
    }

    private static void refreshButtons(Duel duel) {
        if (!duel.getOptions().contains(DuelOptions.EDIT_MODE) && !duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
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
                String arena = duel.getOptions().contains(DuelOptions.RANDOM_ARENA) ?
                        "&1&kI&2&ki&3&kI&4&ki&5&kI&6&ki&e&ki&b&kI" :
                        Utils.translateColor(duel.getArena().getName());
                tempLore.add("§7You selected: §a" + Utils.translateColor(arena));

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
