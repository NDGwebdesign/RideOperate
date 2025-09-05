package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class changeItem implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;
    private File panelFile;
    private FileConfiguration panelConfig;

    public changeItem(RideOperate plugin) {
        this.plugin = plugin;
        this.panelFile = new File(plugin.getDataFolder(), "panel.yml");
        this.panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        if (plugin.getCommand("rpchangeitem") != null) {
            plugin.getCommand("rpchangeitem").setExecutor(this);
            plugin.getCommand("rpchangeitem").setTabCompleter(this);
        } else {
            plugin.getLogger().warning("Command 'rpchangeitem' is not defined in plugin.yml!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpchangeitem <panel name> <item name> <material>");
            return true;
        }

        String panelName = args[0];
        String itemName = args[1];
        String materialName = args[2].toUpperCase();

        // Controleer of het paneel bestaat in panel.yml
        ConfigurationSection panelSection = panelConfig.getConfigurationSection("panels." + panelName);
        if (panelSection == null) {
            player.sendMessage("§cPanel '" + panelName + "' does not exist!");
            return true;
        }

        // Controleer of het opgegeven materiaal geldig is
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            player.sendMessage("§cInvalid material: " + materialName);
            return true;
        }

        // Update het item in panel.yml
        panelConfig.set("panels." + panelName + "." + itemName + ".material", materialName);
        savePanelConfig();

        player.sendMessage("§aSuccessfully changed '" + itemName + "' to '" + materialName + "' in panel '" + panelName + "'.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab-completion voor panel namen uit panel.yml
            ConfigurationSection panelsSection = panelConfig.getConfigurationSection("panels");
            if (panelsSection != null) {
                completions.addAll(filterStartsWith(args[0], panelsSection.getKeys(false)));
            }
        } else if (args.length == 2) {
            // Tab-completion voor item namen binnen een paneel
            ConfigurationSection panelSection = panelConfig.getConfigurationSection("panels." + args[0]);
            if (panelSection != null) {
                completions.addAll(filterStartsWith(args[1], panelSection.getKeys(false)));
            }
        } else if (args.length == 3) {
            // Tab-completion voor Bukkit materialen
            completions = Arrays.stream(Material.values())
                    .map(Material::name)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return completions;
    }

    private List<String> filterStartsWith(String input, Set<String> options) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void savePanelConfig() {
        try {
            panelConfig.save(panelFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save panel.yml: " + e.getMessage());
        }
    }
}
