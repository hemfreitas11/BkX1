package me.bkrmt.bkx1.menus;

import me.bkrmt.bkx1.Arena;
import me.bkrmt.bkx1.Page;

import java.util.ArrayList;

public class SpectateMenu {
    private static ArrayList<Arena> arenasInUse;
    private static ArrayList<Page> pages;

//    public static void showGUI(Player player) {
//        arenasInUse = new ArrayList<>();
//        pages = new ArrayList<>();
//
//        int useSize = BkX1.getOngoingDuels().keySet().size();
//        if (useSize > 0) {
//            int[] arenaIds = new int[BkX1.getOngoingDuels().keySet().size()];
//
//            int tempIndex = 0;
//            for (Duel duel : BkX1.getOngoingDuels().values()) {
//                arenaIds[tempIndex] = duel.getArena().getId();
//                tempIndex++;
//            }
//
//            Page previousPage = null;
//
//            int pagesSize = (int) Math.ceil((double) useSize / (double) 7);
//            for (int c = 0; c < pagesSize; c++) {
//                Page page = new Page(plugin, new SimpleGUI(new GUI("&cChoose the arena " + (c + 1) + "/" + pagesSize, Rows.FOUR)), c + 1);
//                pages.add(page);
//                if (previousPage != null) {
//                    previousPage.setNextPage(page);
//                    page.setPreviousPage(previousPage);
//                }
//                previousPage = page;
//            }
//
//            int arenaIndex = 0;
//            for (Page page : pages) {
//                int index = 10;
//                for (int i = 0; i < 7; i++) {
//                    page.setItem(index++, new ItemBuilder());
//                }
//            }
//
//            pages.get(0).openGui(player);
//        } else {
//            player.sendMessage("lkadlkj hasdjh askj h");
//        }
//    }
}
