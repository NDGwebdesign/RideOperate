package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class Help implements CommandExecutor {

    private final RideOperate plugin;

    public Help(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rphelp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "RideOperate Commands:");
        Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
        if (commands != null) {
            for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
                String commandName = entry.getKey();
                Map<String, Object> commandInfo = entry.getValue();
                String description = (String) commandInfo.get("description");
                String permission = (String) commandInfo.get("permission");
                sender.sendMessage(ChatColor.YELLOW + "/" + commandName + ": " + ChatColor.WHITE + description);
                if (permission != null) {
                    sender.sendMessage(ChatColor.GRAY + "  Permission: " + permission);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No commands found.");
        }
        return true;
    }
}
