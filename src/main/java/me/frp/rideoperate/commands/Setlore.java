package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Setlore implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;

    public Setlore(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpsetlore").setExecutor(this);
        plugin.getCommand("rpsetlore").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Handle command for both players, console, and command blocks
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /rpsetlore <panelName> <buttonName> <lore>");
            return true;
        }

        String panelName = args[0].toLowerCase();
        String buttonName = args[1];
        String lore = ChatColor.translateAlternateColorCodes('&', args[2]);

        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (!panelConfig.contains("panels." + panelName)) {
            sender.sendMessage(ChatColor.RED + "Panel '" + panelName + "' does not exist.");
            return true;
        }

        if (!panelConfig.contains("panels." + panelName + "." + buttonName)) {
            sender.sendMessage(ChatColor.RED + "Button '" + buttonName + "' does not exist in panel '" + panelName + "'.");
            return true;
        }

        panelConfig.set("panels." + panelName + "." + buttonName + ".lore", lore);

        try {
            panelConfig.save(panelFile);
            sender.sendMessage(ChatColor.GREEN + "Lore updated successfully for button '" + buttonName + "' in panel '" + panelName + "'.");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An error occurred while saving the lore.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Return panel names for the first argument
            return getPanelNames();
        } else if (args.length == 2) {
            // Return button names for the second argument
            return getButtonNames(args[0].toLowerCase());
        }
        return null;
    }

    private List<String> getPanelNames() {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        Set<String> panelNames = panelConfig.getConfigurationSection("panels").getKeys(false);
        return new ArrayList<>(panelNames);
    }

    private List<String> getButtonNames(String panelName) {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        if (panelConfig.contains("panels." + panelName)) {
            Set<String> buttonNames = panelConfig.getConfigurationSection("panels." + panelName).getKeys(false);
            List<String> filteredButtonNames = new ArrayList<>();
            for (String buttonName : buttonNames) {
                if (!buttonName.equals("Commands")) { // Skip the Commands section
                    filteredButtonNames.add(buttonName);
                }
            }
            return filteredButtonNames;
        }
        return new ArrayList<>();
    }
}
