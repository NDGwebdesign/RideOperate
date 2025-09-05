package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public class DeletePanel implements CommandExecutor {

    private final RideOperate plugin;

    public DeletePanel(RideOperate plugin) {
        this.plugin = plugin;

        plugin.getCommand("deletepanel").setExecutor(this);

        // Load the default config.yml if it doesn't exist
        plugin.saveDefaultConfig();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }


        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.deletepanel")) {
            player.sendMessage("You don't have permission to use this command!");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("Usage: /deletepanel <name>");
            return true;
        }

        String panelName = args[0];

        // Delete the warp and get status message
        String deleteMessage = deletePanel(panelName);

        // Translate color codes and send the message to the player
       //player.sendMessage(translateColorCodes(deleteMessage.replace("%panelName%", panelName)));

        return true;
    }

    private String deletePanel(String panelName) {
        File warpFile = new File(plugin.getDataFolder(), "panel.yml");

        // Load existing data from warp.yml
        YamlConfiguration config = YamlConfiguration.loadConfiguration(warpFile);

        // Check if the warp exists
        if (config.contains("panels." + panelName)) {
            // Delete the warp data
            config.set("panels." + panelName, null);

            // Save the configuration to the file
            try {
                config.save(warpFile);
                return plugin.getConfig().getString("messages.successfullydeleted");
            } catch (IOException e) {
                e.printStackTrace();
                return "An error occurred while deleting the panel.";
            }
        } else {
            return plugin.getConfig().getString("messages.deleteWarpNotFound");
        }
    }

    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
