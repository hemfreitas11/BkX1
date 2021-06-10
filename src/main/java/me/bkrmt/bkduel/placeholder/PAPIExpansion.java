package me.bkrmt.bkduel.placeholder;

import me.bkrmt.bkduel.BkDuel;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PAPIExpansion extends PlaceholderExpansion {

    private final BkDuel bkDuel;

    public PAPIExpansion(BkDuel bkDuel) {
        this.bkDuel = bkDuel;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return bkDuel.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return bkDuel.getName();
    }

    @Override
    public String getVersion(){
        return bkDuel.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier){
        return bkDuel.getPlaceholderValue(player, identifier);
    }
}
