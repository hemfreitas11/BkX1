package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.SimpleGUI;
import me.bkrmt.opengui.event.ElementResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page {
    private final int pageNumber;
    private final SimpleGUI gui;
    private Page nextPage;
    private Page previousPage;
    private boolean isBuilt;
    private final ItemStack backButton;
    private final ItemStack nextButton;
    private final Map<Integer, PageItem> itemStorage;
    private BkPlugin plugin;

    public Page(BkPlugin plugin, SimpleGUI gui, int pageNumber) {
        this.plugin = plugin;
        this.gui = gui;
        this.pageNumber = pageNumber;
        this.itemStorage = new HashMap<>();
        this.isBuilt = false;
        nextButton = plugin.getConfig().getItemStack("gui-buttons.next-page");
        backButton = plugin.getConfig().getItemStack("gui-buttons.previous-page");
        nextPage = null;
        previousPage = null;
    }

    public void openGui(Player player) {
        buildPage();
        getGui().openInventory(player);
    }

    private Page buildPage() {
        int[] slots = getButtonSlots();
        if (nextPage != null) {
            if (nextButton.getItemMeta().getLore() != null) {
                nextButton.setItemMeta(translateLore(nextButton, nextPage));
            }
            gui.setItem(slots[1], new ItemBuilder(nextButton), event -> nextPage.openGui((Player) event.getWhoClicked()));
        }
        if (previousPage != null) {
            if (backButton.getItemMeta().getLore() != null) {
                backButton.setItemMeta(translateLore(backButton, previousPage));
            }
            gui.setItem(slots[0], new ItemBuilder(backButton), event -> previousPage.openGui((Player) event.getWhoClicked()));
        }
        return this;
    }

    private int[] getButtonSlots() {
        int[] temp = new int[] {0, 8};
        switch (getGui().getGui().getRows()) {
            case ONE:
                temp[0] = 0;
                temp[1] = 8;
                break;
            case FOUR:
                temp[0] = 27;
                temp[1] = 35;
                break;
            case THREE:
            case FIVE:
                temp[0] = 18;
                temp[1] = 26;
                break;
        }
        return temp;
    }

    private ItemMeta translateLore(ItemStack nextButton2, Page nextPage2) {
        List<String> newLore = new ArrayList<>();
        for (String line : nextButton2.getItemMeta().getLore()) {
            newLore.add(line.replace("{page-number}", String.valueOf(nextPage2.getPageNumber())));
        }
        ItemMeta newMeta = nextButton2.getItemMeta();
        newMeta.setLore(newLore);
        return newMeta;
    }

    public Page setNextPage(Page page) {
        this.nextPage = page;
        return this;
    }

    public Page setPreviousPage(Page page) {
        this.previousPage = page;
        return this;
    }

    public void setItemOnXY(int x, int y, ItemBuilder item, ElementResponse response) {
        for (int lin = 0; lin < getGui().getBukkitInventory().getSize() / 9; lin++) {
            int start = 9 * lin;
            int end = start + 8;
            int count = start;
            while (count <= end) {
                for (int col = 0; col < 9; col++) {
                    if (lin == y - 1 && col == x - 1) {
                        setItem(count, item, response);
                        return;
                    }
                    count++;
                }
            }
        }
    }

    public BkPlugin getPlugin() {
        return plugin;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public SimpleGUI getGui() {
        return gui;
    }

    public Map<Integer, PageItem> getItems() {
        return itemStorage;
    }

    public void setItem(int slot, ItemBuilder item, ElementResponse response) {
        itemStorage.put(slot, new PageItem(item, response, false));
        getGui().setItem(slot, item, response);
    }

    public void removeItem(int slot) {
        itemStorage.remove(slot);
        getGui().removeItem(slot);
    }

    public static void clearUnclickable(ArrayList<Page> pages) {
        for (Page page : pages) {
            page.clearUnclickable(false);
        }
    }

    public void setUnclickable(int slot, boolean clearOthers, String displayName, List<String> newLore) {
        if (clearOthers) clearUnclickable(true);

        itemStorage.get(slot).setUnclickable(true);


        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta tempMeta = barrier.getItemMeta();

        List<String> oldLore = itemStorage.get(slot).getPageItem().getItem().getItemMeta().getLore();
        if (!newLore.isEmpty()) {
            List<String> tempLore = oldLore == null ? new ArrayList<>() : oldLore;
            tempLore.addAll(newLore);
            tempMeta.setLore(tempLore);
        } else {
            tempMeta.setLore(oldLore);
        }

        tempMeta.setDisplayName(displayName);


        barrier.setItemMeta(tempMeta);

        getGui().setItem(slot, new ItemBuilder(barrier));
    }

    public static ItemBuilder buildButton(Material material, String displayName, List<String> newLore) {
        return new ItemBuilder(Utils.createItem(material, true, displayName, newLore));
    }

    public void clearUnclickable(boolean onlyFirst) {
        for (int keySlot : itemStorage.keySet()) {
            if (itemStorage.get(keySlot).isUnclickable()) {
                itemStorage.get(keySlot).setUnclickable(false);
                unsetUnclickable(keySlot);
                if (onlyFirst) break;
            }
        }
    }

    public void unsetUnclickable(int slot) {
        getGui().setItem(slot, itemStorage.get(slot).getPageItem(), itemStorage.get(slot).getPageItemResponse());
    }
}
