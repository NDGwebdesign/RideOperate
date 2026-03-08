package me.frp.rideoperate.panel;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class PanelSpawnItemListener implements Listener {

    private static final String PANEL_SPAWN_ITEM_KEY = "panel_spawn_item";
    private static final String PANEL_NAME_KEY = "panel_name";

    private final RideOperate plugin;
    private final PanelManager panelManager;
    private final NamespacedKey panelSpawnItemKey;
    private final NamespacedKey panelNameKey;

    public PanelSpawnItemListener(RideOperate plugin) {
        this.plugin = plugin;
        this.panelManager = new PanelManager(plugin);
        this.panelSpawnItemKey = new NamespacedKey(plugin, PANEL_SPAWN_ITEM_KEY);
        this.panelNameKey = new NamespacedKey(plugin, PANEL_NAME_KEY);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPanelSpawnerPlaced(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isPanelSpawnerItem(item)) {
            return;
        }

        Player player = event.getPlayer();
        String panelName = getPanelName(item);
        if (panelName == null || panelName.trim().isEmpty()) {
            player.sendMessage("§cDit sign heeft geen geldig panel.");
            return;
        }

        if (!panelManager.panelExists(panelName)) {
            event.setCancelled(true);
            player.sendMessage("§cPanel bestaat niet meer: " + panelName);
            return;
        }

        Vector forward = player.getLocation().getDirection().setY(0);
        if (forward.lengthSquared() == 0) {
            forward = new Vector(0, 0, 1);
        }

        event.getBlockPlaced().setType(Material.AIR, false);

        panelManager.spawnPanelAt(
                player,
                panelName,
                event.getBlockPlaced().getLocation().add(0.5, 0.0, 0.5),
                forward);
    }

    private boolean isPanelSpawnerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(panelSpawnItemKey, PersistentDataType.BYTE);
    }

    private String getPanelName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(panelNameKey, PersistentDataType.STRING);
    }
}
