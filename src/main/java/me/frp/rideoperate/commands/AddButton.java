package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import me.frp.rideoperate.panel.PanelManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddButton implements CommandExecutor, TabCompleter {
    private final RideOperate plugin;

    public AddButton(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rpaddbutton").setExecutor(this);
        plugin.getCommand("rpaddbutton").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("rideoperate.addbutton")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§eUse: /rpaddbutton <panelname> <buttonname> <material>");
            return true;
        }

        String panelName = args[0];
        String buttonName = args[1];
        String materialName = args[2].toUpperCase();

        // Check if material is valid
        if (Material.getMaterial(materialName) == null) {
            player.sendMessage("§cOngeldig materiaal: " + materialName);
            return true;
        }

        // Load panel.yml
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if panel exists
        if (!panelConfig.contains("panels." + panelName)) {
            player.sendMessage("§cPanel '" + panelName + "' bestaat niet!");
            return true;
        }

        // Add button to the panel
        panelConfig.set("panels." + panelName + "." + buttonName + ".metial", materialName);

        // Add default lore
        panelConfig.set("panels." + panelName + "." + buttonName + ".lore", "This is the " + buttonName + " button!");

        // Add default command for the button
        panelConfig.set("panels." + panelName + ".Commands." + buttonName, "/" + buttonName.toLowerCase() + "_command");

        try {
            panelConfig.save(panelFile);
            new PanelManager(plugin).spawnPanel(player, panelName);
            player.sendMessage("§aButton '" + buttonName + "' with meterial '" + materialName + "' added to panel '" + panelName + "'!");
        } catch (Exception e) {
            player.sendMessage("§cThere is an error: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Load panel.yml
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            return completions;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (args.length == 1) {
            // First argument -> panel names from panel.yml
            if (panelConfig.contains("panels")) {
                for (String panel : panelConfig.getConfigurationSection("panels").getKeys(false)) {
                    if (panel.toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(panel);
                    }
                }
            }
        } else if (args.length == 2) {
            // Second argument -> suggest a new button name (no completion needed)
            // We could suggest existing buttons, but typically you'd want to create a new one
            return completions;
        } else if (args.length == 3) {
            // Third argument -> Minecraft materials
            String input = args[2].toUpperCase();
            for (Material material : Material.values()) {
                if (material.name().startsWith(input)) {
                    completions.add(material.name());
                }
            }
        }

        return completions;
    }
}
