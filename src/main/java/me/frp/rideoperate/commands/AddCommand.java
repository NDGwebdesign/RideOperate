package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AddCommand implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;

    public AddCommand(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpaddcommand").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof org.bukkit.command.ConsoleCommandSender)) {
            sender.sendMessage("Only players or console can use this command!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /rpaddcommand <panelname> <buttonname> <command (without the /)>");
            return true;
        }

        String panelName = args[0];
        String buttonName = args[1];
        StringBuilder commandBuilder = new StringBuilder();

        // Rebuild the command from args
        for (int i = 2; i < args.length; i++) {
            commandBuilder.append(args[i]);
            if (i < args.length - 1) {
                commandBuilder.append(" ");
            }
        }

        String commandToAdd = commandBuilder.toString();

        // Load the panel.yml file
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            sender.sendMessage("The panel.yml file does not exist!");
            return true;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if the panel and button exist
        if (!panelConfig.contains("panels." + panelName)) {
            sender.sendMessage("The panel '" + panelName + "' does not exist!");
            return true;
        }

        if (!panelConfig.contains("panels." + panelName + ".Commands." + buttonName)) {
            sender.sendMessage("The button '" + buttonName + "' does not exist in the panel '" + panelName + "'!");
            return true;
        }

        // Add the command to the button
        panelConfig.set("panels." + panelName + ".Commands." + buttonName, commandToAdd);

        try {
            panelConfig.save(panelFile);
            sender.sendMessage("Command successfully added to the button '" + buttonName + "' in panel '" + panelName + "'.");
        } catch (IOException e) {
            sender.sendMessage("An error occurred while saving the panel.yml file!");
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getPanelNames();
        } else if (args.length == 2) {
            return getButtonNames(args[0]);
        }

        return Collections.emptyList();
    }

    private List<String> getPanelNames() {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            return Collections.emptyList();
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        Set<String> panelNames = panelConfig.getConfigurationSection("panels").getKeys(false);

        return new ArrayList<>(panelNames);
    }

    private List<String> getButtonNames(String panelName) {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            return Collections.emptyList();
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (!panelConfig.contains("panels." + panelName)) {
            return Collections.emptyList();
        }

        Set<String> buttonNames = panelConfig.getConfigurationSection("panels." + panelName + ".Commands").getKeys(false);

        return new ArrayList<>(buttonNames);
    }
}
