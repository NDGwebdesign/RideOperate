package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapCanvas;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ViewCam implements CommandExecutor {
    private final RideOperate plugin;

    public ViewCam(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpviewcam").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rideoperate.viewcam")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /rpviewcam <camera name>");
            return false;
        }

        String cameraName = args[0];
        File cameraFolder = new File(plugin.getDataFolder(), "cams/" + cameraName);

        if (!cameraFolder.exists()) {
            player.sendMessage(ChatColor.RED + "Camera '" + cameraName + "' does not exist.");
            return false;
        }

        // Create a new map item
        ItemStack map = new ItemStack(Material.FILLED_MAP);
        MapMeta mapMeta = (MapMeta) map.getItemMeta();
        MapView view = plugin.getServer().createMap(player.getWorld());

        // Clear existing renderers
        for (MapRenderer renderer : view.getRenderers()) {
            view.removeRenderer(renderer);
        }

        // Add custom renderer
        view.addRenderer(new MapRenderer() {
            @Override
            public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
                File[] images = cameraFolder.listFiles((dir, name) -> name.endsWith(".png"));
                if (images != null && images.length > 0) {
                    try {
                        // Get latest image
                        File latestImage = images[images.length - 1];
                        BufferedImage image = ImageIO.read(latestImage);
                        canvas.drawImage(0, 0, image);
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to render camera image: " + e.getMessage());
                    }
                }
            }
        });

        mapMeta.setMapView(view);
        map.setItemMeta(mapMeta);

        // Give map to player
        player.getInventory().addItem(map);
        player.sendMessage(ChatColor.GREEN + "Viewing camera '" + cameraName + "'.");

        return true;
    }
}
