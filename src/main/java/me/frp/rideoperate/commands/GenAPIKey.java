package me.frp.rideoperate.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class GenAPIKey implements CommandExecutor {

    private final JavaPlugin plugin;

    public GenAPIKey(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String newKey = UUID.randomUUID().toString();

        FileConfiguration config = plugin.getConfig();
        config.set("api-key", newKey);

        plugin.saveConfig();

        sender.sendMessage("§aNew API key generated: §e" + newKey);
        sender.sendMessage("§aMake sure to update your external applications with the new key.");
        return true;
    }
}
