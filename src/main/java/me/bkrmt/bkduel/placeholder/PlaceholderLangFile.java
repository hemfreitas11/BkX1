package me.bkrmt.bkduel.placeholder;

import me.bkrmt.bkcore.message.LangFile;
import me.bkrmt.bkduel.BkDuel;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

public class PlaceholderLangFile extends LangFile {
    public PlaceholderLangFile(BkDuel plugin, ArrayList<String> langList) {
        super(plugin, langList);
    }

    @Override
    public String get(OfflinePlayer player, String key) {
        try {
            String text = super.get(key);
            return BkDuel.getInstance().isValidPlaceholder(text) ? PlaceholderAPI.setPlaceholders(player, text) : text;
        } catch (Exception e) {
            e.printStackTrace();
            return "§cError when trying to get the message " + key;
        }
    }
}
