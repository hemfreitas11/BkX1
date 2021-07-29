package me.bkrmt.bkduel.stats;

import me.bkrmt.bkcore.config.Configuration;
import me.bkrmt.bkduel.BkDuel;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.events.NewTopPlayerEvent;
import me.bkrmt.bkduel.events.StatsUpdateEvent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class StatsManager {

    private ArrayList<PlayerStats> statsList;
    private ArrayList<ArrayList<PlayerStats>> rankPages;
    private BkDuel plugin;

    public StatsManager(BkDuel plugin) {
        this.plugin = plugin;
        statsList = new ArrayList<>();
        rankPages = new ArrayList<>();
        updateStats();
    }

    public StatsManager updateStats() {
        ArrayList<PlayerStats> oldList = statsList;
        statsList = new ArrayList<>();
        Configuration config = plugin.getConfigManager().getConfig("player-data", "player-stats.yml");
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

        boolean hasNewTop = false;
        if (BkDuel.getInstance().getHookManager().hasHologramHook()) {
            PlayerStats oldTop = null;
            PlayerStats newTop = null;
            if (oldList.size() > 0) oldTop = oldList.get(0);
            if (statsList.size() > 0) newTop = statsList.get(0);

            if (oldTop != null) {
                if (newTop != null) {
                    if (!oldTop.getUUID().toString().equalsIgnoreCase(statsList.get(0).getUUID().toString())) {
                        Bukkit.getServer().getPluginManager().callEvent(new NewTopPlayerEvent(oldTop, newTop));
                        hasNewTop = true;
                    }
                }
            } else {
                if (newTop != null) {
                    Bukkit.getServer().getPluginManager().callEvent(new NewTopPlayerEvent(null, newTop));
                    hasNewTop = true;
                }
            }
        }
        if (!hasNewTop) plugin.getServer().getPluginManager().callEvent(new StatsUpdateEvent(oldList, statsList));
        oldList = null;
        return this;
    }

    public PlayerStats getPlayerStat(String name) {
        ArrayList<PlayerStats> stats = BkDuel.getInstance().getStatsManager().getStats();
        for (PlayerStats stat : stats) {
            if (stat.getPlayerName().equalsIgnoreCase(name)) return stat;
        }
        return null;
    }

    public PlayerStats getPlayerStat(UUID uuid) {
        ArrayList<PlayerStats> stats = BkDuel.getInstance().getStatsManager().getStats();
        for (PlayerStats stat : stats) {
            if (stat.getUUID().toString().equalsIgnoreCase(uuid.toString())) return stat;
        }
        return null;
    }

    public void sendRankPage(Player player, int page) {
        String message = page == 0 ? plugin.getLangFile().get(player, "info.rank-list.top") : plugin.getLangFile().get(player, "info.rank-list.rank");
        String[] components = message.split("\\{player-button}");
        String playerFormat = plugin.getLangFile().get(player, "info.rank-list.player-button");
        String hoverMessage = plugin.getLangFile().get(player, "info.rank-list.hover-message");

        ArrayList<PlayerStats> rankPage = new ArrayList<>(BkDuel.getInstance().getStatsManager().getRankPages().get(page));
        ArrayList<PlayerStats> statsList = BkDuel.getInstance().getStatsManager().getStats();

        String header = plugin.getLangFile().get(player, "info.rank-list.header");
        if (!header.isEmpty()) {
            player.sendMessage(header);
            player.sendMessage(" ");
        }

        for (PlayerStats stat : rankPage) {
            ComponentBuilder builder = new ComponentBuilder("")
                    .append(components[0].replace("{rank}", String.valueOf((statsList.indexOf(stat) + 1))))
                    .append(playerFormat.replace("{player}", stat.getPlayerName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getLangFile().get(player, "commands.duel.command") + " " + CmdDuel.getSubCommands().get("stats") + " " + stat.getPlayerName()));
            if (components.length == 2) {
                builder
                        .append(components[1].replace("{wins}", String.valueOf(stat.getWins())))
                        .reset();
            }
            player.spigot().sendMessage(builder.create());
        }

        String footer = plugin.getLangFile().get(player, "info.rank-list.footer");
        if (!footer.isEmpty()) {
            if (footer.contains("{previous-page}") && footer.contains("{page-numbers}") && footer.contains("{next-page}")) {
                player.sendMessage(" ");
                String[] footerParts = footer.split("\\{page-numbers}");
                String[] part1Components = footerParts[0].split("\\{previous-page}");
                String[] part2Components = footerParts[1].split("\\{next-page}");
                String next = plugin.getLangFile().get(player, "info.rank-list.next-page.button");
                String nextHover = plugin.getLangFile().get(player, "info.rank-list.next-page.hover");
                String previous = plugin.getLangFile().get(player, "info.rank-list.previous-page.button");
                String previousHover = plugin.getLangFile().get(player, "info.rank-list.previous-page.hover");
                String pageNumberFormat = plugin.getLangFile().get(player, "info.rank-list.page-numbers-format");
                page++;
                ComponentBuilder footerBuilder = new ComponentBuilder("")
                        .append(part1Components[0])
                        .append(previous)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(previousHover).create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getLangFile().get(player, "commands.duel.command") + " " + CmdDuel.getSubCommands().get("top") + " " + (page - 1)))
                        .append(part1Components[1])
                        .reset()
                        .append(pageNumberFormat.replace("{page-number}", String.valueOf(page)).replace("{total-pages}", String.valueOf(rankPages.size())))
                        .append(part2Components[0])
                        .append(next)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(nextHover).create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + plugin.getLangFile().get(player, "commands.duel.command") + " " + CmdDuel.getSubCommands().get("top") + " " + (page + 1)))
                        .append(part2Components[1])
                        .reset();
                player.spigot().sendMessage(footerBuilder.create());
            } else {
                player.sendMessage(footer);
            }
        }
    }

    public PlayerStats createStats(Player target) {
        Configuration statsConfig = BkDuel.getInstance().getConfigManager().getConfig("player-data", "player-stats.yml");
        statsConfig.set(target.getUniqueId().toString() + ".name", target.getName());
        statsConfig.set(target.getUniqueId().toString() + ".duels", 0);
        statsConfig.set(target.getUniqueId().toString() + ".wins", 0);
        statsConfig.set(target.getUniqueId().toString() + ".defeats", 0);
        statsConfig.set(target.getUniqueId().toString() + ".disconnects", 0);
        statsConfig.saveToFile();
        BkDuel.getInstance().getStatsManager().updateStats();
        return new PlayerStats(target.getName(), target.getUniqueId(), 0, 0, 0, 0);
    }

    public ArrayList<ArrayList<PlayerStats>> getRankPages() {
        return rankPages;
    }

    public ArrayList<PlayerStats> getStats() {
        return statsList;
    }
}
