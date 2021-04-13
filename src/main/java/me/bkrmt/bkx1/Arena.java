package me.bkrmt.bkx1;

import me.bkrmt.bkcore.BkPlugin;
import me.bkrmt.bkcore.Utils;
import me.bkrmt.bkcore.input.PlayerInput;
import me.bkrmt.bkx1.menus.ChooseArenaMenu;
import me.bkrmt.opengui.GUI;
import me.bkrmt.opengui.ItemBuilder;
import me.bkrmt.opengui.Rows;
import me.bkrmt.opengui.SimpleGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Arena extends Purchasable {
    private Location location1;
    private Location location2;
    private Location spectators;

    public Arena(BkPlugin plugin, String arenaName) {
        super(plugin, arenaName, "arenas");

        if (getConfig().get("locations") == null) {
            location1 = null;
            location2 = null;
            spectators = null;
        } else {
            location1 = getConfig().getLocation("locations.fighter1");
            location2 = getConfig().getLocation("locations.fighter2");
            spectators = getConfig().getLocation("locations.spectators");
        }
    }

    public void setLocation1(Location location1) {
        this.location1 = location1;
        getConfig().setLocation("locations.fighter1", location1);
        getConfig().save(false);
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
        getConfig().setLocation("locations.fighter2", location2);
        getConfig().save(false);
    }

    public void setSpectators(Location spectators) {
        this.spectators = spectators;
        getConfig().setLocation("locations.spectators", spectators);
        getConfig().save(false);
    }

    public void showEditMenu(Duel duel) {
        Player player = duel.getFighter1();
        SimpleGUI gui = new SimpleGUI(new GUI(ChatColor.stripColor(getName()), Rows.FIVE));

        Page menu = new Page(getPlugin(), gui, 1);

        ItemBuilder name = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getSign(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-name.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-name.description"))));

        ItemBuilder pos1 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getCyanDye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position1.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position1.description"))));

        ItemBuilder pos2 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getRedDye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position2.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-position2.description"))));

        ItemBuilder spec = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getEnderEye(), true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.set-spectator.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.set-spectator.description"))));

        ItemBuilder item = new ItemBuilder(Utils.createItem(Material.ITEM_FRAME, true,
                getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-display-item.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.edit-display-item.description"))));

        ItemBuilder backButton = new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                getPlugin().getLangFile().get("gui-buttons.back-button.name"),
                Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.back-button.description"))));

        menu.setItemOnXY(5, 5, backButton, event -> {
            ChooseArenaMenu.showGUI(duel);
        });

        menu.setItemOnXY(3, 2, name, event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                setName(input);
                ChooseArenaMenu.showGUI(duel);
            },
                    input -> {
                        event.getWhoClicked().sendMessage(getPlugin().getLangFile().get("info.input.cancelled"));
                    })
                    .setCancellable(true)
                    .setTitle(getPlugin().getLangFile().get("info.input.arena-name"))
                    .setSubTitle(getPlugin().getLangFile().get("info.input.type-to-cancel").replace("{cancel-input}", getPlugin().getConfig().getString("cancel-input")))
                    .sendInput();
        });
        menu.setItemOnXY(7, 2, item, event -> {
            SimpleGUI tempGui = new SimpleGUI(new GUI(ChatColor.stripColor(getName()), Rows.THREE));
            tempGui.getGuiSettings().setCanDrag(false);
            tempGui.getGuiSettings().setCanEnterItems(true);
            tempGui.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                player.sendMessage(getPlugin().getLangFile().get("info.item-set"));
                setDisplayItem(event.getCursor());
            });

            Page page = new Page(getPlugin(), tempGui, 1);

            page.setItemOnXY(5, 3, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true,
                    getPlugin().getLangFile().get("gui-buttons.arena-edit.enter-item.name"),
                    Collections.singletonList(getPlugin().getLangFile().get("gui-buttons.arena-edit.enter-item.description")))),
                    event1 -> {
                        showEditMenu(duel);
                    });

            page.openGui(player);
        });
        menu.setItemOnXY(5, 2, spec, event -> {
            setLocation(player, "spectators");
        });
        menu.setItemOnXY(4, 3, pos1, event -> {
            setLocation(player, "fighter1");
        });
        menu.setItemOnXY(6, 3, pos2, event -> {
            setLocation(player, "fighter2");
        });

        menu.openGui(player);
    }

    private void setLocation(Player player, String key) {
        player.closeInventory();
        getConfig().setLocation("locations." + key, player.getLocation());
        getConfig().save(false);
        player.sendMessage("Location set.");
    }

    public boolean isInUse() {
        return getConfig().getBoolean("in-use");
    }

    public void setInUse(boolean inUse) {
        getConfig().set("in-use", inUse);
        getConfig().save(false);
    }

    public Location getLocation1() {
        return location1;
    }

    public Location getLocation2() {
        return location2;
    }

    public Location getSpectators() {
        return spectators;
    }

}
