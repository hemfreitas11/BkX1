package me.bkrmt.bkduel.commands;

import me.bkrmt.bkduel.BkDuel;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdDuelCompleter implements TabCompleter {
    private final BkDuel plugin;

    public CmdDuelCompleter() {
        this.plugin = BkDuel.getInstance();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();

        String challenge = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.challenge.command");
        String top = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.top.command");
        String stats = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.stats.command");
        String spectate = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.spectate.command");
        String accept = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.accept.command");
        String decline = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.decline.command");
        String edit = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.edit.command");
        String edit_arenas = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.edit.subcommands.arenas.command");
        String edit_kits = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.edit.subcommands.kits.command");
        String npc = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.npc.command");
        String enable = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.enable.command");
        String disable = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.disable.command");
        String npc_update = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.npc.subcommands.update.command");
        String npc_location = plugin.getLangFile().get((OfflinePlayer) sender,  "commands.duel.subcommands.npc.subcommands.location.command");

        List<String> subCommands = new ArrayList<>();

        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.challenge"))
            subCommands.add(challenge);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.top"))
            subCommands.add(top);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.stats"))
            subCommands.add(stats);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.spectate"))
            subCommands.add(spectate);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.reply"))
            subCommands.add(accept);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.reply"))
            subCommands.add(decline);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.toggle"))
            subCommands.add(enable);
        if (sender.hasPermission("bkduel.player") || sender.hasPermission("bkduel.toggle"))
            subCommands.add(disable);
        if (sender.hasPermission("bkduel.admin") || sender.hasPermission("bkduel.edit"))
            subCommands.add(edit);
        if (sender.hasPermission("bkduel.admin") || sender.hasPermission("bkduel.edit"))
            subCommands.add(npc);

        if (args.length == 1) {
            String partialCommand = args[0];
            StringUtil.copyPartialMatches(partialCommand, subCommands, completions);
        } else if (subCommands.contains(challenge) && args.length == 2 && (args[0].equalsIgnoreCase(challenge) || args[0].equalsIgnoreCase(stats))) {
            List<String> players = new ArrayList<>();
            for (Player player : plugin.getHandler().getMethodManager().getOnlinePlayers()) {
                if (args[0].equalsIgnoreCase(challenge)) {
                    if (!player.getName().equalsIgnoreCase(sender.getName()))
                        players.add(player.getName());
                } else {
                    players.add(player.getName());
                }
            }
            String partialName = args[1];
            StringUtil.copyPartialMatches(partialName, players, completions);
        } else if (subCommands.contains(npc) && args.length == 2 && (args[0].equalsIgnoreCase(npc))) {
            String partialSubCommand = args[1];
            StringUtil.copyPartialMatches(partialSubCommand, Arrays.asList(npc_location, npc_update), completions);
        } else if (subCommands.contains(edit) && args.length == 2 && (args[0].equalsIgnoreCase(edit))) {
            String partialSubCommand = args[1];
            StringUtil.copyPartialMatches(partialSubCommand, Arrays.asList(edit_arenas, edit_kits), completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
