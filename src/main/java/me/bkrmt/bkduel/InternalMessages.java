package me.bkrmt.bkduel;

import me.bkrmt.bkcore.BkPlugin;

public enum InternalMessages {
    LOADING_CONFIGS("&7[&6BkDuel&7] &eLoading configs...",
    "&7[&6BkDuel&7] &eCarregando configs..."),
    RESETING_ARENAS("&7[&6BkDuel&7] &eReseting Arenas...",
    "&7[&6BkDuel&7] &eResetando arenas..."),
    NO_CITIZENS("&7[&6BkDuel&7] &eCitizens not found, disabling NPC feature.",
    "&7[&6BkDuel&7] &eCitizens nao encontrado, desativando funcao NPC."),
    CITIZENS_FOUND("&7[&6BkDuel&7] &eCitizens found, searching for a hologram plugin...",
    "&7[&6BkDuel&7] &eCitizens encontrado, procurando plugin de hologramas..."),
    NO_HOLOGRAM("&7[&6BkDuel&7] &eHologram plugin not found. This is required for the NPC feature. Compatible plugins: Holograms and HolographicDisplays",
    "&7[&6BkDuel&7] &eCitizens nao encontrado, desativando funcao NPC."),
    HOLOGRAM_FOUND("&7[&6BkDuel&7] &eHologram plugin found, the NPC feature is now enabled!",
    "&7[&6BkDuel&7] &ePlugin de hologramas encontrado, a funcao NPC foi ativada!"),
    NEW_VERSION("&7[&6BkDuel&7] &eA new version is available, go to &bdiscord.gg/2MHgyjCuPc&e to download it! Current Version: {0} New version: {1}",
    "&7[&6BkDuel&7] &eUma nova versao esta disponivel, acesse &bdiscord.gg/2MHgyjCuPc &epara baixar! Versao atual: {0} Nova versao: {1}!"),
    NO_CHAT("&7[&6BkDuel&7] &eChat plugin not found, disabling chat tags. Compatible plugins: LegendChat, nChat and HeroChat",
    "&7[&6BkDuel&7] &ePlugin de chat nao encontrado, desativando tags de chat. Plugins compativeis: LegendChat, nChat e HeroChat"),
    CHAT_FOUND("&7[&6BkDuel&7] &eChat plugin found, chat tags enabled!",
    "&7[&6BkDuel&7] &ePlugin de chat encontrado, tags de chat habilitadas."),
    CORRUPT_ARENA("&7[&4BkDuel&7] &cThe arena in the file {0} is corrupt and could not be loaded.",
    "&7[&4BkDuel&7] &cA arena no arquivo {0} esta corrompida e nao pode ser carregada..."),
    CORRUPT_KIT("&7[&4BkDuel&7] &cThe kit in the file {0} is corrupt and could not be loaded.",
    "&7[&4BkDuel&7] &cO kit no arquivo {0} esta corrompido e nao pode ser carregado..."),
    ECONOMY_ERROR("&7[&4BkDuel&7] &cThere was an economy error, please contact an admin! Error: {0}",
    "&7[&4BkDuel&7] &cHouve um erro na economia, por favor contate um administrador! Erro: {0}"),
    HOLOGRAM_LINES_ERROR("&7[&4BkDuel&7] &cThere was an error getting the hologram lines from the config!",
    "&7[&4BkDuel&7] &cHouve um erro ao tentar pegar as linhas do holograma na config!"),
    NPC_LOCATION_ERROR("&7[&4BkDuel&7] &cThere was an error getting the NPC location from the config!",
    "&7[&4BkDuel&7] &cHouve um erro ao tentar pegar o local do NPC na config!"),
    RESTORING_INVENTORIES("&7[&6BkDuel&7] &eRestoring player inventories...",
    "&7[&6BkDuel&7] &eRestaurando inventario dos jogadores...");

    private final String[] message;

    InternalMessages(String message) {
        this.message = new String[1];
        this.message[0] = message;
    }

    InternalMessages(String enMessage, String brMessage) {
        message = new String[2];
        this.message[0] = enMessage;
        this.message[1] = brMessage;
    }

    public String getMessage() {
        return message[0];
    }

    public String getMessage(BkPlugin plugin) {
        if (plugin.getLangFile().getLanguage().equalsIgnoreCase("pt_br"))  return message[1];
        else return message[0];
    }

}
