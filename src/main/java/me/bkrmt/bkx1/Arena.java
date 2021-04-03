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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        List<String> test = new ArrayList<>();
        test.add("aksjdlkasjdl jasdj ");
        test.add("asdkqiworsd");
        test.add("slçakj dd");

        ItemBuilder name = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getSign(), true, "Teste", test));
        ItemBuilder pos1 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getCyanDye(), true, "Teste", test));
        ItemBuilder pos2 = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getRedDye(), true, "Teste", test));
        ItemBuilder spec = new ItemBuilder(Utils.createItem(getPlugin().getHandler().getItemManager().getEnderEye(), true, "Teste", test));
        ItemBuilder item = new ItemBuilder(Utils.createItem(Material.ITEM_FRAME, true, "Teste", test));
        ItemBuilder backButton = new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, Utils.translateColor("Go back"), test));

        menu.setItemOnXY(5, 5, backButton, event -> {
            ChooseArenaMenu.showGUI(duel);
        });

        menu.setItemOnXY(3, 2, name, event -> {
            new PlayerInput(getPlugin(), duel.getFighter1(), input -> {
                setName(input);
                ChooseArenaMenu.showGUI(duel);
            },
                    input -> {
                    })
                    .setCancellable(false)
                    .setSubTitle("asdasd")
                    .setTitle("Digite o nome da arena")
                    .sendInput();
        });
        menu.setItemOnXY(7, 2, item, event -> {
            SimpleGUI tempGui = new SimpleGUI(new GUI(ChatColor.stripColor(getName()), Rows.THREE));
            tempGui.getGuiSettings().setCanDrag(false);
            tempGui.getGuiSettings().setCanEnterItems(true);
            AtomicInteger cout = new AtomicInteger();
            tempGui.getGuiSettings().setEnteredItemResponse(event1 -> {
                event.setCancelled(true);
                player.sendMessage("Item set.");
                setDisplayItem(event.getCursor());
            });

            Page page = new Page(getPlugin(), tempGui, 1);

            List<String> lore = new ArrayList<>();
            lore.add(Utils.translateColor("&aasd lkaskdj lsajk "));
            lore.add(Utils.translateColor("&4asd saldka"));
            lore.add(Utils.translateColor("&basd saldka"));

            page.setItemOnXY(5, 3, new ItemBuilder(Utils.createItem(Material.REDSTONE_BLOCK, true, Utils.translateColor("&6Enter Item"), lore)), event1 -> {
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
