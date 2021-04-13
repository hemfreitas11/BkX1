package me.bkrmt.bkx1.commands.subcommands.x1;

import me.bkrmt.bkx1.BkX1;
import me.bkrmt.bkx1.Duel;
import me.bkrmt.bkx1.DuelOptions;
import me.bkrmt.bkx1.menus.ChooseArenaMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stats {
    public void run(CommandSender commandSender, Command command, String[] args, String label) {
        if (args.length == 1) {
            if (BkX1.getOngoingDuels().keySet().size() > 0) {
                Duel spectate = new Duel(BkX1.plugin).setFighter1((Player) commandSender);
                spectate.getOptions().add(DuelOptions.SPECTATOR_MODE);
                ChooseArenaMenu.showGUI(spectate);
            } else {
                // no duels
            }
        } else {

        }
    }
}
