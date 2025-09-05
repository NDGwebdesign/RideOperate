package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;


public class Panels implements CommandExecutor {

    private final RideOperate plugin;

    public Panels(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("panels").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Check if the player has the required permission to use the command
        if (!sender.hasPermission("rideoperate.panels")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission");
            sender.sendMessage(nopermission);
            return true;
        }

        // Load panel.yml file
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        YamlConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if panel.yml exists
        if (!panelFile.exists()) {
            sender.sendMessage("The panel.yml file does not exist!");
            return true;
        }

        sender.sendMessage("Your panels:");

        // Get a list of all panel names
        for (String panelName : panelConfig.getConfigurationSection("panels").getKeys(false)) {
            sender.sendMessage("- " + panelName);
        }

        return true;
    }
}
