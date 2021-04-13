package me.bkrmt.bkx1.stats;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkx1.BkX1;
import me.bkrmt.bkx1.events.StatsUpdateEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class StatsManager {

    private ArrayList<PlayerStats> statsList;
    private ArrayList<ArrayList<PlayerStats>> rankPages;
    private BkPlugin plugin;

    public StatsManager(BkPlugin plugin) {
        this.plugin = plugin;
        statsList = new ArrayList<>();
        rankPages = new ArrayList<>();
        updateStats();
    }

    public StatsManager updateStats() {
        ArrayList<PlayerStats> oldList = statsList;
        statsList = new ArrayList<>();
        Configuration config = plugin.getConfig("player-data", "player-stats.yml");
        Set<String> keys = config.getKeys(false);

        for (String key : keys) {
            int wins = config.getInt(key + ".wins");
            int defeats = config.getInt(key + ".defeats");
            int duels = config.getInt(key + ".duels");
            int disconnects = config.getInt(key + ".disconnects");
            String name = config.getString(key + ".name");
            statsList.add(new PlayerStats(name, UUID.fromString(key), wins, defeats, duels, disconnects));
        }
        statsList.sort((stat1, stat2) -> Integer.compare(stat2.getWins(), stat1.getWins()));

        rankPages = new ArrayList<>();
        int pageItems = 5;
        int pagesSize = (int) Math.ceil((double) statsList.size() / (double) pageItems);
        int index = 0;

        for (int c = 0; c < pagesSize; c++) {
            ArrayList<PlayerStats> pageArray = new ArrayList<>();
            for (int i = 0; i < pageItems; i++) {
                int finalIndex = index++;
                if (finalIndex >= statsList.size()) break;
                pageArray.add(statsList.get(finalIndex));
            }
            rankPages.add(pageArray);
        }

        plugin.getServer().getPluginManager().callEvent(new StatsUpdateEvent(oldList, statsList));
        oldList = null;
        return this;
    }

    public void sendRankPage(Player player, int page) {
        String message = page == 0 ? plugin.getLangFile().get("info.rank-list.top") : plugin.getLangFile().get("info.rank-list.rank");
        String[] components = message.split("\\{player-button}");
        String playerFormat = plugin.getLangFile().get("info.rank-list.player-button");
        String hoverMessage = plugin.getLangFile().get("info.rank-list.hover-message");

        ArrayList<PlayerStats> rankPage = new ArrayList<>(BkX1.getStatsManager().getRankPages().get(page));
        ArrayList<PlayerStats> statsList = BkX1.getStatsManager().getStats();

        String header = plugin.getLangFile().get("info.rank-list.header");
        if (!header.isEmpty()) {
            player.sendMessage(header);
            player.sendMessage(" ");
        }

        for (PlayerStats stat : rankPage) {
            ComponentBuilder builder = new ComponentBuilder("")
                    .append(components[0].replace("{rank}", String.valueOf((statsList.indexOf(stat) + 1))))
                    .append(playerFormat.replace("{player}", stat.getPlayerName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/x1 stats " + stat.getPlayerName()));
            if (components.length == 2) {
                builder
                        .append(components[1].replace("{wins}", String.valueOf(stat.getWins())))
                        .reset();
            }
            player.spigot().sendMessage(builder.create());
        }

        String footer = plugin.getLangFile().get("info.rank-list.footer");
        if (!footer.isEmpty()) {
            if (footer.contains("{previous-page}") && footer.contains("{page-numbers}") && footer.contains("{next-page}")) {
                player.sendMessage(" ");
                String[] footerParts = footer.split("\\{page-numbers}");
                String[] part1Components = footerParts[0].split("\\{previous-page}");
                String[] part2Components = footerParts[1].split("\\{next-page}");
                String next = plugin.getLangFile().get("info.rank-list.next-page.button");
                String nextHover = plugin.getLangFile().get("info.rank-list.next-page.hover");
                String previous = plugin.getLangFile().get("info.rank-list.previous-page.button");
                String previousHover = plugin.getLangFile().get("info.rank-list.previous-page.hover");
                String pageNumberFormat = plugin.getLangFile().get("info.rank-list.page-numbers-format");
                page++;
                ComponentBuilder footerBuilder = new ComponentBuilder("")
                        .append(part1Components[0])
                        .append(previous)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(previousHover).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/x1 top " + (page-1)))
                        .append(part1Components[1])
                            .reset()
                        .append(pageNumberFormat.replace("{page-number}", String.valueOf(page)).replace("{total-pages}", String.valueOf(rankPages.size())))
                        .append(part2Components[0])
                        .append(next)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(nextHover).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/x1 top " + (page+1)))
                        .append(part2Components[1])
                            .reset();
                player.spigot().sendMessage(footerBuilder.create());
            } else {
                player.sendMessage(footer);
            }
        }
    }

    public ArrayList<ArrayList<PlayerStats>> getRankPages() {
        return rankPages;
    }

    public ArrayList<PlayerStats> getStats() {
        return statsList;
    }

    public static void printStats(ArrayList<PlayerStats> statsList) {
        for (PlayerStats stat : statsList) {
            System.out.println(" ");
            System.out.println("------------");
            System.out.println("Player: " + stat.getPlayerName());
            System.out.println("Wins: " + stat.getWins());
            System.out.println("Defeats: " + stat.getDefeats());
            System.out.println("Duels: " + stat.getDuels());
            System.out.println("Disconnects: " + stat.getDisconnects());
            System.out.println("------------");
        }
    }
}
