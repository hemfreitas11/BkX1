package me.bkrmt.bkduel.menus;

import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkcore.textanimator.AnimatorManager;
import me.bkrmt.bkduel.Arena;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.Duel;
import me.bkrmt.bkduel.InternalMessages;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.opengui.gui.GUI;
import me.bkrmt.opengui.gui.Rows;
import me.bkrmt.opengui.item.ItemBuilder;
import me.bkrmt.opengui.page.Page;
import me.bkrmt.teleport.Teleport;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChooseArenaMenu {
    private static ArrayList<Arena> arenas;
    private static ArrayList<Page> pages;

    public static void showGUI(Duel duel, Page previousMenu) {
        arenas = new ArrayList<>();
        pages = new ArrayList<>();

        boolean hasValidArena = false;
        if (duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
            for (Duel ongoingDuel : BkDuel.getInstance().getOngoingDuels().values()) {
                boolean contains = false;
                for (Arena arena : arenas) {
                    try {
                        if (arena.isValidArena(duel.getFighter1())) hasValidArena = true;
                        if (arena.getId() == ongoingDuel.getArena().getId()) {
                            contains = true;
                            break;
                        }
                    } catch (Exception ignored) {
                        BkDuel.getInstance().sendConsoleMessage(Utils.translateColor(InternalMessages.CORRUPT_ARENA.getMessage(BkDuel.getInstance()).replace("{0}", arena.getName())));
                        duel.getFighter1().sendMessage(Utils.translateColor(InternalMessages.CORRUPT_ARENA.getMessage(BkDuel.getInstance()).replace("{0}", arena.getName())));
                        return;
                    }
                }
                if (!contains) arenas.add(ongoingDuel.getArena());
            }
        } else {
            File[] listFiles = BkDuel.getInstance().getFile("", "arenas").listFiles();
            if (listFiles.length > 0) {
                for (File arena : listFiles) {
                    try {
                        Arena tempArena = new Arena(BkDuel.getInstance(), arena.getName().replace(".yml", ""));
                        if (tempArena.isValidArena(duel.getFighter1())) hasValidArena = true;
                        arenas.add(tempArena);
                    } catch (Exception ignored) {
                        BkDuel.getInstance().sendConsoleMessage(Utils.translateColor(InternalMessages.CORRUPT_ARENA.getMessage(BkDuel.getInstance()).replace("{0}", arena.getName())));
                        duel.getFighter1().sendMessage(Utils.translateColor(InternalMessages.CORRUPT_ARENA.getMessage(BkDuel.getInstance()).replace("{0}", arena.getName())));
                        return;
                    }
                }
            }
        }

        Page previousPage = null;
        int tempSize = getArenas().size() > 0 ? getArenas().size() : 1;
        int rowSize = duel.getOptions().contains(DuelOptions.EDIT_MODE) ? 7 : duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) ? 5 : 7;

        int pagesSize = (int) Math.ceil((double) tempSize / (double) rowSize);

        for (int c = 0; c < pagesSize; c++) {
            Page page = new Page(BkDuel.getInstance(), BkDuel.getInstance().getAnimatorManager(), new GUI(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.choose-arena-title").replace("{page}", String.valueOf(c + 1)).replace("{total-pages}", String.valueOf(pagesSize)), duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) ? Rows.THREE : Rows.FOUR), c + 1);
            pages.add(page);
            if (previousPage != null) {
                page.setPreviousPage(previousPage);
                previousPage.setNextPage(page);
            }
            previousPage = page;
        }

        List<String> newLore = new ArrayList<>();
        newLore.add(" ");
        newLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-selected"));

        int arenaIndex = 0;
        boolean first = true;
        for (Page page : pages) {
            int index = duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) || duel.getOptions().contains(DuelOptions.EDIT_MODE) ? 10 : pages.indexOf(page) > 0 ? 10 : 11;

            for (int i = 0; i < rowSize; i++) {
                if (arenaIndex < tempSize) {
                    if (getArenas().size() > 0) {
                        int finalIndex = index;
                        if (first && !duel.getOptions().contains(DuelOptions.SPECTATOR_MODE) && !duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                            String tempName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.random-arena-button.name");
                            ItemBuilder randomArena = new ItemBuilder(Material.ENDER_PEARL)
                                    .setName(tempName)
                                    .setLore(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.random-arena-button.description"))
                                    .hideTags();
                            if (!hasValidArena) {
                                randomArena.setName(Utils.translateColor("&4" + ChatColor.stripColor(randomArena.getItem().getItemMeta().getDisplayName())));
                                randomArena.setLore(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.no-valid-arenas"));
                            }
                            AtomicInteger inte = new AtomicInteger((int) (Math.random() * arenas.size()));

                            boolean finalHasValidArena = hasValidArena;
                            page.pageSetItem(10, randomArena, "choose-arena-random-arena", event -> {
                                List<String> randomLore = new ArrayList<>();
                                randomLore.add(" ");
                                randomLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-selected"));
                                if (finalHasValidArena) {
                                    Page.clearUnclickable(pages);

                                    page.setUnclickable(10, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(10).getPageItem().getItem().getItemMeta().getDisplayName()), randomLore);
                                    Arena arena = arenas.get(inte.get());
                                    while (!arena.isValidArena(duel.getFighter1())) {
                                        inte.set((int) (Math.random() * arenas.size()));
                                        arena = arenas.get(inte.get());
                                    }

                                    if (arena.getBoundKit() != null) {
                                        duel.getOptions().add(DuelOptions.BOUND_KIT);
                                        duel.setKit(arena.getBoundKit());
                                    }
                                    duel.setArena(arena);
                                    duel.getOptions().add(DuelOptions.RANDOM_ARENA);
                                    refreshButtons(duel);
                                } else {
                                    event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.not-valid-arena"));
                                }
                            });
                            first = false;
                        } else {
                            Arena arena = getArenas().get(arenaIndex++);
                            ItemStack display = arena.getDisplayItem();
                            double playerMoney = BkDuel.getInstance().getEconomy().getBalance(duel.getFighter1());

                            List<String> extraLore = display.getItemMeta().getLore() == null ? new ArrayList<>() : display.getItemMeta().getLore();
                            if (!duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                if (arena.isInUse()) {
                                    Duel inUse = Duel.findDuel(arena);
                                    extraLore = new ArrayList<>();
                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.already-in-use"));
                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.currently-fighting"));
                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.fighter-vs-fighter")
                                            .replace("{fighter1}", inUse.getFighter1().getName())
                                            .replace("{fighter2}", inUse.getFighter2().getName()));
                                } else {
                                    boolean isValidArena = arena.isValidArena(duel.getFighter1());

                                    if (arena.getBoundKit() != null || !isValidArena) extraLore.add(" ");

                                    if (arena.getBoundKit() != null)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.bound-kit-lore")
                                                .replace("{kit}", AnimatorManager.cleanText(Utils.translateColor(arena.getBoundKit().getName()))));

                                    if (arena.getName() == null)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.invalid-name"));
                                    if (arena.getLocation1() == null)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.no-location-1"));
                                    if (arena.getLocation2() == null)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.no-location-2"));
                                    if (arena.getSpectators() == null)
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.no-location-spectators"));
                                    if (arena.getLocation1() != null && arena.getLocation2() != null) {
                                        if (!arena.getLocation1().getWorld().getName().equals(arena.getLocation2().getWorld().getName()))
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.not-same-world"));
                                        if (Bukkit.getServer().getWorld(arena.getLocation1().getWorld().getName()) == null)
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.invalid-world"));
                                    }

                                    if (isValidArena) {
                                        if (arena.getPrice() == 0 || arena.isOwner(duel.getFighter1()) && !duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                            if (!extraLore.isEmpty()) extraLore.add(" ");
                                            if (arena.getPrice() == 0)
                                                extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.free-arena"));
                                            else
                                                extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-bought"));
                                        } else {
                                            if (!extraLore.isEmpty()) extraLore.add(" ");
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.price").replace("{price}", String.valueOf((int) arena.getPrice())));
                                            if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                if (!extraLore.isEmpty()) extraLore.add(" ");
                                                if (playerMoney >= arena.getPrice())
                                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-buy"));
                                                else
                                                    extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.not-enough-money"));
                                            }
                                        }
                                    }

                                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                        if (arena.isInUse()) {
                                            if (!extraLore.isEmpty()) extraLore.add(" ");
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-in-use"));
                                        } else {
                                            if (!extraLore.isEmpty()) extraLore.add(" ");
                                            extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-edit-arena"));
                                        }
                                    }

                                    if (!duel.getFighter1().hasPermission("bkduel.arenas") && !duel.getFighter1().hasPermission("bkduel.arena." + arena.getId())) {
                                        if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                            extraLore.add(" ");
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.no-arena-perm"));
                                    }

                                    if (duel.getFighter1().hasPermission("bkduel.admin") || duel.getFighter1().hasPermission("bkduel.edit")) {
                                        if (extraLore.size() > 0 && !extraLore.get(extraLore.size() - 1).equalsIgnoreCase(" "))
                                            extraLore.add(" ");
                                        extraLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-id").replace("{id}", String.valueOf(arena.getId())));
                                    }
                                }

                                ItemMeta meta = display.getItemMeta();
                                meta.setLore(extraLore);
                                display.setItemMeta(meta);
                            } else {
                                Duel spectatedDuel = Duel.findDuel(arena);

                                if (spectatedDuel != null) {
                                    List<String> specLore = arena.getConfig().getLore("display-item.lore");
                                    specLore.add(" ");
                                    specLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.fighter1").replace("{fighter}", spectatedDuel.getFighter1().getName()));
                                    specLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.fighter2").replace("{fighter}", spectatedDuel.getFighter2().getName()));
                                    specLore.add(" ");
                                    specLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.click-to-teleport"));
                                    ItemMeta meta = display.getItemMeta();
                                    meta.setLore(specLore);
                                    display.setItemMeta(meta);
                                }
                            }

                            if (duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                if (arena.isInUse()) {
                                    page.pageSetItem(finalIndex, new ItemBuilder(display), "choose-arena-spectator-display-" + finalIndex, event -> {
                                        duel.getFighter1().closeInventory();
                                        if (!BkDuel.getInstance().getOngoingDuels().containsKey(duel.getFighter1().getUniqueId())) {
                                            new Teleport(BkDuel.getInstance(), duel.getFighter1(), true)
                                                    .setLocation(arena.getName(), arena.getSpectators())
                                                    .setDuration(duel.getPlugin().getConfigManager().getConfig().getInt("teleport-countdown.spectator-countdown"))
                                                    .setIsCancellable(true)
                                                    .startTeleport();
                                        } else {
                                            duel.getFighter1().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.cant-spectate"));
                                        }
                                    });
                                    index++;
                                }
                            } else {
                                if (!duel.getOptions().contains(DuelOptions.BOUND_KIT_SELECTION) && !duel.getOptions().contains(DuelOptions.EDIT_MODE) && !duel.getFighter1().hasPermission("bkduel.arenas") && !duel.getFighter1().hasPermission("bkduel.arena." + arena.getId())) {
                                    String arenaName = arena.getName();
                                    for (String line : extraLore) {
                                        if (extraLore.indexOf(line) < (extraLore.size() - (duel.getFighter1().hasPermission("bkduel.edit") || duel.getFighter1().hasPermission("bkduel.admin") ? 3 : 2))) {
                                            extraLore.set(extraLore.indexOf(line), ChatColor.DARK_GRAY + ChatColor.stripColor(Utils.translateColor(line)));
                                        }
                                    }

                                    ItemBuilder newDisplay = new ItemBuilder(Material.BARRIER)
                                            .setName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + AnimatorManager.cleanText(arenaName))
                                            .setLore(extraLore);
                                    page.pageSetItem(
                                            finalIndex, newDisplay, "choose-arena-display-" + finalIndex, event -> {
                                            }
                                    );
                                } else {
                                    page.pageSetItem(
                                            finalIndex, new ItemBuilder(display), "choose-arena-display-" + finalIndex,
                                            event -> {
                                                if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                    if (!arena.isInUse()) {
                                                        arena.showEditMenu(duel);
                                                    }
                                                } else {
                                                    if (arena.isValidArena(duel.getFighter1())) {
                                                        if (!arena.isInUse()) {
                                                            if (arena.getPrice() == 0 || arena.isOwner(duel.getFighter1())) {
                                                                if (!page.getItems().get(event.getSlot()).isUnclickable()) {
                                                                    Page.clearUnclickable(pages);
                                                                    page.setUnclickable(finalIndex, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(event.getSlot()).getPageItem().getItem().getItemMeta().getDisplayName()), newLore);
                                                                    duel.setArena(arena);
                                                                    duel.getOptions().remove(DuelOptions.RANDOM_ARENA);

                                                                    if (arena.getBoundKit() != null) {
                                                                        duel.setKit(arena.getBoundKit());
                                                                        duel.getOptions().add(DuelOptions.BOUND_KIT);
                                                                    } else {
                                                                        duel.setKit(null);
                                                                        duel.getOptions().remove(DuelOptions.BOUND_KIT);
                                                                    }
                                                                    refreshButtons(duel);
                                                                }
                                                            } else {
                                                                if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                                                                    if (playerMoney >= arena.getPrice()) {
                                                                        EconomyResponse r = BkDuel.getInstance().getEconomy().withdrawPlayer((OfflinePlayer) event.getWhoClicked(), arena.getPrice());
                                                                        if (r.transactionSuccess()) {
                                                                            arena.addOwner((Player) event.getWhoClicked());
                                                                            event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile()
                                                                                    .get("info.player-bought-message.arena")
                                                                                    .replace("{arena}", AnimatorManager.cleanText(Utils.translateColor(arena.getName())))
                                                                                    .replace("{balance}", BkDuel.getInstance().getEconomy().format(r.balance)));
                                                                            page.setSwitchingPages(true);
                                                                            ChooseArenaMenu.showGUI(duel, null);
                                                                        } else {
                                                                            event.getWhoClicked().sendMessage(InternalMessages.ECONOMY_ERROR.getMessage(BkDuel.getInstance()).replace("{0}", r.errorMessage));
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.arena-error.not-valid-arena"));
                                                    }
                                                }
                                            }
                                    );
                                }
                                if (duel.getArena() != null && duel.getArena().getName().equals(arena.getName())) {
                                    page.setUnclickable(finalIndex, false, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(finalIndex).getPageItem().getItem().getItemMeta().getDisplayName()), newLore);
                                }

                                index++;
                            }
                        }
                    }

                    if (duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                        String tempName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.create-arena.name");
                        page.pageSetItem(31, new ItemBuilder(Utils.createItem(BkDuel.getInstance().getHandler().getItemManager().getWritableBook(), true, tempName, Collections.singletonList(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.create-arena.description")))), "choose-arena-create-arena", event -> {
                            new PlayerInput(BkDuel.getInstance(), duel.getFighter1(), input -> {
                                String arenaName = Utils.cleanString(input.toLowerCase()
                                        .replace(" ", "-")
                                        .replaceAll("\\P{L}+", ""));
                                if (!arenaName.isEmpty()) {
                                    Arena newArena = new Arena(BkDuel.getInstance(), arenaName);
                                    newArena.setName(input);
                                    duel.setArena(null);
                                    ChooseArenaMenu.showGUI(duel, null);
                                } else {
                                    event.getWhoClicked().sendMessage(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "error.no-letters"));
                                }
                            },
                                    input -> {
                                    })
                                    .setCancellable(false)
                                    .setTitle(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.input.arena-name"))
                                    .setSubTitle("")
                                    .sendInput();
                        });
                    }/* else {
                            if (!duel.getOptions().contains(DuelOptions.SPECTATOR_MODE)) {
                                page.pageSetItem(29, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                                        plugin.getLangFile().get(duel.getFighter1(), "gui-buttons.back-to-kits.name"),
                                        Collections.singletonList(plugin.getLangFile().get(duel.getFighter1(), "gui-buttons.back-to-kits.description")))), event -> {
                                    ChooseKitsMenu.showGUI(duel);
                                });
                            }
                        }*/
                } else break;
            }


            if (duel.getOptions().contains(DuelOptions.RANDOM_ARENA) && duel.getArena() != null) {
                List<String> randomLore = new ArrayList<>();
                randomLore.add(" ");
                randomLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.arena-selected"));
                page.setUnclickable(10, true, ChatColor.GREEN + ChatColor.stripColor(page.getItems().get(10).getPageItem().getItem().getItemMeta().getDisplayName()), randomLore);
            }
            refreshButtons(duel);
        }
        Page tempPage = pages.get(0);
        if (previousMenu != null) {
            tempPage.setPreviousPage(previousMenu);
            tempPage.setButtonSlots(new int[]{-1, -1});
        }
        tempPage.openGui(duel.getFighter1());
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
                tempLore.addAll(BkDuel.getInstance().getLangFile().getConfig().getStringList("gui-buttons.next-button.disabled.description"));
                displayName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.next-button.disabled.name");
            } else {
                String arena = duel.getOptions().contains(DuelOptions.RANDOM_ARENA) ?
                        Utils.translateColor(BkDuel.getInstance().getConfigManager().getConfig().getString("random-selection")) :
                        Utils.translateColor(AnimatorManager.cleanText(duel.getArena().getName()));
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.you-selected").replace("{name}", Utils.translateColor(arena)));

                if (duel.getOptions().contains(DuelOptions.BOUND_KIT) && duel.getArena().getBoundKit() != null) {
                    tempLore.add(" ");
                    tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "info.selected-kit-lore")
                            .replace("{kit}", Utils.translateColor(duel.getArena().getBoundKit().getName())));
                }

                tempLore.add(" ");
                tempLore.add(BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.next-button.enabled.description"));
                displayName = BkDuel.getInstance().getLangFile().get(duel.getFighter1(), "gui-buttons.next-button.enabled.name");
            }
            nextButton = Page.buildButton(Material.SLIME_BALL, displayName, tempLore);

            page.pageSetItem(31, nextButton, "choose-arena-next-button", event -> {
                if (!duel.getOptions().contains(DuelOptions.EDIT_MODE)) {
                    if (duel.getArena() != null) {
                        if (duel.getOptions().contains(DuelOptions.BOUND_KIT) && duel.getArena().getBoundKit() != null) {
                            duel.startRequest();
                        } else {
                            ChooseKitsMenu.showGUI(duel, null);
                        }
                    }
                }
            });
        }
    }

    public static ArrayList<Arena> getArenas() {
        return arenas;
    }
}
