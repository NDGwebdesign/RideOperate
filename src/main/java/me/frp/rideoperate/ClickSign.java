package me.frp.rideoperate;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class ClickSign implements Listener {

    private final Plugin plugin;

    public ClickSign(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getState() instanceof Sign) {
                Sign sign = (Sign) clickedBlock.getState();
                if (sign.getLine(0).equalsIgnoreCase("[rppanel]")) {
                    event.setCancelled(true);
                    String panelName = sign.getLine(1);
                    openPanel(event.getPlayer(), panelName);
                }
            }
        }
    }

    private void openPanel(Player player, String panelName) {
        // Check if the player has permission to open panels
        if (!player.hasPermission("rideoperate.sign")) {
            String noPermissionMessage = plugin.getConfig().getString("messages.nopermission");
            player.sendMessage(noPermissionMessage);
            return;
        }

        player.sendMessage("Opening panel: " + panelName);
        // Implement your logic to open the panel here
        player.performCommand("panel " + panelName);
    }
}
