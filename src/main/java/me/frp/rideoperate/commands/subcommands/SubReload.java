package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class SubReload implements CommandExecutor {

    private final RideOperate plugin;

    public SubReload(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rpreload")) {
            return execute(sender, args);
        }
        return false;
    }

    public boolean execute(CommandSender sender, String[] args) {
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
        } else {
            String noPermissionMessage = plugin.getConfig().getString("messages.nopermission", ChatColor.RED + "You don't have permission to do this.");
            sender.sendMessage(noPermissionMessage);
        }
        return true;
    }
}
