package me.frp.rideoperate.listener;

import me.frp.rideoperate.RideOperate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class ResourcePackListener implements Listener {

    private final RideOperate plugin;

    public ResourcePackListener(RideOperate plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        File resourcePack = new File(plugin.getDataFolder(), "resourcepack.zip");

        if (resourcePack.exists()) {
            String url = "https://yourwebsite.com/resourcepack.zip"; // Zorg ervoor dat je deze online host!
            event.getPlayer().setResourcePack(url);
        }
    }
}
