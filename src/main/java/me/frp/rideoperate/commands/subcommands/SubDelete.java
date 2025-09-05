package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SubDelete implements CommandExecutor {

    private final RideOperate plugin;

    public SubDelete(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rphelp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, args);
    }

    public boolean execute(CommandSender sender, String[] args) {
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

        if (args.length != 2) {
            player.sendMessage("Usage: /rp delete <panelname>");
            return true;
        }

        String panelName = args[1];

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
