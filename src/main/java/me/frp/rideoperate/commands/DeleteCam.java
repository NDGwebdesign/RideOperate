package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteCam implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;
    private final File cameraFile;
    private FileConfiguration cameraConfig;

    public DeleteCam(RideOperate plugin) {
        this.plugin = plugin;
        this.cameraFile = new File(plugin.getDataFolder(), "cams.yml");
        loadConfig();
        plugin.getCommand("rpdeletecam").setExecutor(this);
        plugin.getCommand("rpdeletecam").setTabCompleter(this);
    }

    private void loadConfig() {
        if (!cameraFile.exists()) {
            try {
                cameraFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create cams.yml: " + e.getMessage());
            }
        }
        cameraConfig = YamlConfiguration.loadConfiguration(cameraFile);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rideoperate.deletecam")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /rpdeletecam <camera name>");
            return false;
        }

        String cameraName = args[0].toLowerCase();

        if (!cameraConfig.contains("cams." + cameraName)) {
            player.sendMessage(ChatColor.RED + "Camera '" + cameraName + "' does not exist.");
            return false;
        }

        // Remove camera ArmorStand
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                if (stand.getCustomName() != null && stand.getCustomName().equalsIgnoreCase(cameraName)) {
                    stand.remove();
                    break;
                }
            }
        }

        // Remove from config
        cameraConfig.set("cams." + cameraName, null);
        try {
            cameraConfig.save(cameraFile);
            player.sendMessage(ChatColor.GREEN + "Camera '" + cameraName + "' has been deleted.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to delete camera from configuration.");
            plugin.getLogger().severe("Failed to save cams.yml: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && cameraConfig.contains("cams")) {
            return new ArrayList<>(cameraConfig.getConfigurationSection("cams").getKeys(false));
        }
        return null;
    }
}
