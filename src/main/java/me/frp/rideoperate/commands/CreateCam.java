package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class CreateCam implements CommandExecutor, TabCompleter {

    private final File cameraFile;
    private FileConfiguration cameraConfig;
    private final RideOperate plugin;

    public CreateCam(RideOperate plugin) {
        this.plugin = plugin;
        this.cameraFile = new File(plugin.getDataFolder(), "cams.yml");
        createFiles();
        loadConfigs();
        plugin.getCommand("rpcreatecam").setExecutor(this);
        plugin.getCommand("rpcreatecam").setTabCompleter(this);
    }

    private void createFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            if (!cameraFile.exists()) {
                cameraFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create configuration files: " + e.getMessage());
        }
    }

    private void loadConfigs() {
        cameraConfig = YamlConfiguration.loadConfiguration(cameraFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rideoperate.createcam")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /createcam <camera name>");
            return false;
        }

        String cameraName = args[0];

        if (cameraConfig.contains("cams." + cameraName.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "Camera '" + cameraName + "' already exists.");
            return false;
        }

        String panelName = args[1];

        if (panelName.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Panel name cannot be empty.");
            return false;
        }

        Location location = player.getLocation();
        ArmorStand camera = spawnCameraArmorStand(location, cameraName);

        if (camera != null) {
            saveCameraToYAML(cameraName, panelName, location);
            player.sendMessage(ChatColor.GREEN + "Camera '" + cameraName + "' has been created.");
        }

        return true;
    }

    private ArmorStand spawnCameraArmorStand(Location location, String cameraName) {
        try {
            ArmorStand camera = location.getWorld().spawn(location, ArmorStand.class);
            camera.setCustomName(cameraName);
            camera.setCustomNameVisible(true);
            camera.setVisible(true);
            camera.setSmall(true);
            camera.setGravity(false);
            camera.setBasePlate(false);
            camera.setArms(false);
            camera.setInvulnerable(true);

            ItemStack head = createCameraHead();
            camera.getEquipment().setHelmet(head);

            return camera;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to spawn camera armor stand: " + e.getMessage());
            return null;
        }
    }

    private ItemStack createCameraHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwner("MHF_Camera");
            head.setItemMeta(meta);
        }
        return head;
    }

    private void saveCameraToYAML(String cameraName, String panelName, Location location) {
        String camId = cameraName.toLowerCase();
        String path = "cams." + camId;

        cameraConfig.set(path + ".name", cameraName);
        cameraConfig.set(path + "panel", panelName);
        cameraConfig.set(path + ".world", location.getWorld().getName());
        cameraConfig.set(path + ".x", location.getX());
        cameraConfig.set(path + ".y", location.getY());
        cameraConfig.set(path + ".z", location.getZ());
        cameraConfig.set(path + ".yaw", location.getYaw());
        cameraConfig.set(path + ".pitch", location.getPitch());

        // Create camera directory and placeholder image
        File cameraDir = new File(plugin.getDataFolder(), "cams/" + camId);
        cameraDir.mkdirs();

        File photoFile = new File(cameraDir, "photo.png");
        if (!photoFile.exists()) {
            try {
                BufferedImage placeholder = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
                ImageIO.write(placeholder, "png", photoFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create placeholder camera image: " + e.getMessage());
            }
        }

        try {
            cameraConfig.save(cameraFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save camera configuration: " + e.getMessage());
        }
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}