package me.bkrmt.bkx1;

import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.event.ElementResponse;

public class PageItem {
    private ItemBuilder item;
    private final ElementResponse response;
    private boolean unclickable;

    public PageItem(ItemBuilder item, ElementResponse response, boolean unclickable) {
        this.item = item;
        this.response = response;
        this.unclickable = unclickable;
    }

    public ElementResponse getPageItemResponse() {
        return response;
    }

    public ItemBuilder getPageItem() {
        return item;
    }

    public void setPageItem(ItemBuilder item) {
        this.item = item;
    }

    public boolean isUnclickable() {
        return unclickable;
    }

    public void setUnclickable(boolean unclickable) {
        this.unclickable = unclickable;
    }
}
