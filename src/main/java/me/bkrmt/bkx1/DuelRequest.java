package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DuelRequest {
    Player senderPlayer;
    Player targetPlayer;
    BkPlugin plugin;
    TextComponent tpaMessage;
    Duel duel;
    String kit;
    String arena;
    String itemDrop;
    String expDrop;
    String price;
    String expire;

    public DuelRequest(Duel duel) {
        this.senderPlayer = duel.getFighter1();
        this.targetPlayer = duel.getFighter2();
        this.duel = duel;
        this.plugin = duel.getPlugin();
        tpaMessage = new TextComponent("");
        kit = duel.getKit() != null ? ChatColor.stripColor(Utils.translateColor(duel.getKit().getName())) : "Own Items";
        arena = duel.getArena().getName();
        itemDrop = duel.getOptions().contains(DuelOptions.DROP_ITEMS) ? Utils.translateColor("&2Yes") : Utils.translateColor("&4No");
        expDrop = duel.getOptions().contains(DuelOptions.DROP_EXP) ? Utils.translateColor("&2Yes") : Utils.translateColor("&4No");
        price = "0000";
        expire = String.valueOf(plugin.getConfig().getInt("challenge-expire"));
        buildMessage();
    }

    public void sendMessage() {
        BkX1.getOngoingDuels().put(senderPlayer.getUniqueId(), duel);
        BkX1.getOngoingDuels().put(targetPlayer.getUniqueId(), duel);
        if (plugin.getConfig().getBoolean("broadcast-to-all")) broadcastToAll();
        targetPlayer.spigot().sendMessage(tpaMessage);
        duel.getFighter2().performCommand("x1 accept");
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
            if ((!player.getUniqueId().equals(senderPlayer.getUniqueId()) && !player.getUniqueId().equals(targetPlayer.getUniqueId())) || player.isOp() || player.hasPermission("bkx1.edit-mode")) {
                for (String line : broadcastMessage) {
                    player.sendMessage(line);
                }
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
                    duelReply += "x1 accept";
                    hover = plugin.getLangFile().get(sectionName + ".accept-hover");
                    buttonAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, duelReply));
                    buttonAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
                    break;
                case "deny-button":
                    buttonDeny = new TextComponent(plugin.getLangFile().get(sectionName + "." + section));
                    duelReply += "x1 decline";
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
}
