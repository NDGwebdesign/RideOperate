package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class ReloadCommand implements CommandExecutor {

    private final RideOperate plugin;

    public ReloadCommand(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpreload").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rpreload")) {
            if (sender.hasPermission("rideoperate.reload")) {
                // Schedule a task to run on the next tick (async-safe)
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    // Reload plugin configuration
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "The file is reloaded.");

                    // Reload JSON files if needed
                    plugin.saveDefaultConfig(); // Save default config (including JSON files) if they don't exist
                    plugin.reloadConfig(); // Reload the config to apply changes
                });
            } else if(!sender.hasPermission("rideoperate.reload")){
                String nopermission = plugin.getConfig().getString("messages.nopermission");
                sender.sendMessage(nopermission);
            } else {
                sender.sendMessage(ChatColor.RED + "Sorry. You can't do this.");
            }
            return true;
        }
        return false;
    }
}
