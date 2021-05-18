package me.bkrmt.bkduel;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkduel.commands.CmdDuel;
import me.bkrmt.bkduel.enums.DuelOptions;
import me.bkrmt.bkduel.enums.DuelStatus;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DuelRequest {
    private Player senderPlayer;
    private Player targetPlayer;
    private BkPlugin plugin;
    private BukkitTask expireRunnable;
    private TextComponent tpaMessage;
    private Duel duel;
    private String kit;
    private String arena;
    private String itemDrop;
    private String expDrop;
    private String price;
    private String expire;

    public DuelRequest(Duel duel) {
        this.senderPlayer = duel.getFighter1();
        this.targetPlayer = duel.getFighter2();
        this.duel = duel;
        this.plugin = duel.getPlugin();
        tpaMessage = new TextComponent("");
        kit = duel.getKit() != null ? ChatColor.stripColor(Utils.translateColor(BkDuel.getAnimatorManager().cleanText(duel.getKit().getName()))) : plugin.getLangFile().get("info.own-items");
        arena = Utils.translateColor(BkDuel.getAnimatorManager().cleanText(duel.getArena().getName()));
        String yes = plugin.getLangFile().get("info.true");
        String no = plugin.getLangFile().get("info.false");
        itemDrop = duel.getOptions().contains(DuelOptions.DROP_ITEMS) ? yes : no;
        expDrop = duel.getOptions().contains(DuelOptions.DROP_EXP) ? yes : no;
        price = String.valueOf((int) plugin.getConfig().getDouble("duel-cost"));
        expire = String.valueOf(plugin.getConfig().getInt("challenge-expire"));
        buildMessage();
    }

    public DuelRequest sendMessage() {
        BkDuel.getOngoingDuels().put(senderPlayer.getUniqueId(), duel);
        BkDuel.getOngoingDuels().put(targetPlayer.getUniqueId(), duel);
        if (plugin.getConfig().getBoolean("broadcast-to-all")) broadcastToAll();
        targetPlayer.spigot().sendMessage(tpaMessage);
        duel.setStatus(DuelStatus.AWAITING_REPLY);
        expireRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                duel.getFighter1().sendMessage(plugin.getLangFile().get("info.request-expired.self").replace("{player}", duel.getFighter2().getName()));
                duel.getFighter2().sendMessage(plugin.getLangFile().get("info.request-expired.others").replace("{player}", duel.getFighter1().getName()));
                duel.endWithoutPlayers();
                BkDuel.getOngoingDuels().remove(duel.getFighter1().getUniqueId());
                BkDuel.getOngoingDuels().remove(duel.getFighter2().getUniqueId());
            }
        }.runTaskLater(plugin, 20 * Integer.parseInt(expire));
        return this;
    }

    private void broadcastToAll() {
        List<String> broadcastMessage = new ArrayList<>();

        for (String line : plugin.getLangFile().getConfig().getStringList("info.broadcast-to-all")) {
            broadcastMessage.add(Utils.translateColor(line)
                    .replace("{fighter1}", senderPlayer.getDisplayName())
                    .replace("{fighter2}", targetPlayer.getDisplayName())
                    .replace("{kit}", kit)
                    .replace("{arena}", arena)
                    .replace("{item-drop}", itemDrop)
                    .replace("{exp-drop}", expDrop)
                    .replace("{price}", price)
                    .replace("{expire}", expire)
            );
        }
        for (Player player : plugin.getHandler().getMethodManager().getOnlinePlayers()) {
            for (String line : broadcastMessage) {
                player.sendMessage(line);
            }
        }
    }

    private void buildMessage() {
        String sectionName = "info.request-message";
        String[] configSection = Utils.objectToString(plugin.getLangFile().getConfig().getConfigurationSection(sectionName).getKeys(false).toArray());
        TextComponent buttonAccept = null;
        TextComponent buttonDeny = null;
        boolean warned = false;
        for (String section : configSection) {
            String duelReply = "/";
            String hover;
            switch (section) {
                case "accept-button":
                    buttonAccept = new TextComponent(plugin.getLangFile().get(sectionName + "." + section));
                    duelReply += plugin.getLangFile().get("commands.duel.command") + " " + CmdDuel.getSubCommands().get("accept");
                    hover = plugin.getLangFile().get(sectionName + ".accept-hover");
                    buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, duelReply));
                    buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                    break;
                case "deny-button":
                    buttonDeny = new TextComponent(plugin.getLangFile().get(sectionName + "." + section));
                    duelReply += plugin.getLangFile().get("commands.duel.command") + " " + CmdDuel.getSubCommands().get("decline");
                    hover = plugin.getLangFile().get(sectionName + ".deny-hover");
                    buttonDeny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, duelReply));
                    buttonDeny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                    break;
                case "message":
                    List<String> lines = plugin.getLangFile().getConfig().getStringList(sectionName + "." + section);
                    Iterator<String> iterator = lines.listIterator();
                    while (iterator.hasNext()) {
                        String line = Utils.translateColor(iterator.next());
                        if (line.contains("{accept-button}") && line.contains("{deny-button}")) {
                            String[] temp1 = line.split(" ");
                            StringBuilder temp2 = new StringBuilder();
                            for (int c = 0; c < temp1.length; c++) {
                                if (temp1[c].equals("{accept-button}")) {
                                    tpaMessage.addExtra(temp2.toString());
                                    tpaMessage.addExtra(buttonAccept);
                                    tpaMessage.addExtra(" ");
                                    temp2 = new StringBuilder();
                                } else if (temp1[c].equals("{deny-button}")) {
                                    tpaMessage.addExtra(temp2.toString());
                                    tpaMessage.addExtra(buttonDeny);
                                    tpaMessage.addExtra(" ");
                                    temp2 = new StringBuilder();
                                } else {
                                    temp2.append(Utils.translateColor(temp1[c] + " "));
                                }
                                if (c == temp1.length - 1) {
                                    tpaMessage.addExtra(temp2.toString());
                                    tpaMessage.addExtra(" ");
                                }
                            }
                        } else if ((line.contains("{accept-button}") && !line.contains("{deny-button}")) || (!line.contains("{accept-button}") && line.contains("{deny-button}"))) {
                            line = "§4The {accept-button} and {deny-button}§4 need to be in the §4same line. §4Fix it and try again!";
                            if (!warned) addLine(line);
                            warned = true;
                        } else {
                            addLine(line);
                        }
                        if (iterator.hasNext()) tpaMessage.addExtra("\n");
                    }
                    break;
            }
        }
    }

    private void addLine(String line) {
        if (!line.isEmpty()) {
            line = line
                    .replace("{player}", senderPlayer.getName())
                    .replace("{kit}", kit)
                    .replace("{arena}", arena)
                    .replace("{item-drop}", itemDrop)
                    .replace("{exp-drop}", expDrop)
                    .replace("{price}", price)
                    .replace("{seconds}", expire);
            tpaMessage.addExtra(line);
        }
    }

    public BukkitTask getExpireRunnable() {
        return expireRunnable;
    }
}
