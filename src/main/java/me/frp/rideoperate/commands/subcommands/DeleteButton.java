package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeleteButton implements CommandExecutor, TabCompleter {
    private final RideOperate plugin;

    public DeleteButton(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpdeletebutton").setExecutor(this);
        plugin.getCommand("rpdeletebutton").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rideoperate.deletebutton")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission", "§cYou don't have permission to use this command.");
            player.sendMessage(nopermission);
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("§eUsage: /rpdeletebutton <panelName> <buttonName>");
            return true;
        }

        String panelName = args[0];
        String buttonName = args[1];

        // Load panel.yml
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            player.sendMessage("§cPanel configuration file not found!");
            return true;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if panel exists
        if (!panelConfig.contains("panels." + panelName)) {
            player.sendMessage("§cPanel '" + panelName + "' does not exist!");
            return true;
        }

        // Check if button exists in the panel
        if (!panelConfig.contains("panels." + panelName + "." + buttonName)) {
            player.sendMessage("§cButton '" + buttonName + "' does not exist in panel '" + panelName + "'!");
            return true;
        }

        // Delete the button from the panel
        panelConfig.set("panels." + panelName + "." + buttonName, null);

        // Also delete the command associated with the button
        if (panelConfig.contains("panels." + panelName + ".Commands." + buttonName)) {
            panelConfig.set("panels." + panelName + ".Commands." + buttonName, null);
        }

        try {
            panelConfig.save(panelFile);
            player.sendMessage("§aButton '" + buttonName + "' has been deleted from panel '" + panelName + "'!");
        } catch (IOException e) {
            player.sendMessage("§cError saving configuration: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("rideoperate.deletebutton")) {
            return completions;
        }

        // Load panel.yml
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            return completions;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (args.length == 1) {
            // First argument -> panel names
            if (panelConfig.contains("panels")) {
                Set<String> panels = panelConfig.getConfigurationSection("panels").getKeys(false);
                for (String panel : panels) {
                    if (panel.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(panel);
                    }
                }
            }
        } else if (args.length == 2) {
            // Second argument -> button names in the specified panel
            String panelName = args[0];
            if (panelConfig.contains("panels." + panelName)) {
                Set<String> buttons = panelConfig.getConfigurationSection("panels." + panelName).getKeys(false);
                for (String button : buttons) {
                    // Skip the Commands section
                    if (!button.equals("Commands") && button.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(button);
                    }
                }
            }
        }

        return completions;
    }
}
