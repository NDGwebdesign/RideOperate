package me.frp.rideoperate.commands.subcommands;

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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class subPlaceholder implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;

    public subPlaceholder(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, args);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has the required permission
        if (!player.hasPermission("rideoperate.editlore")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /rp lore <panelName> <buttonName> <loreText>");
            return true;
        }

        String panelName = args[0].toLowerCase();
        String buttonName = args[1].toLowerCase();
        String loreText = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if the panel exists
        if (!panelConfig.contains("panels." + panelName)) {
            player.sendMessage(ChatColor.RED + "Panel " + panelName + " does not exist.");
            return true;
        }

        // Check if the button exists in the panel
        if (!panelConfig.contains("panels." + panelName + "." + buttonName + ".material")) {
            player.sendMessage(ChatColor.RED + "Button " + buttonName + " does not exist in panel " + panelName + ".");
            return true;
        }

        // Update lore
        panelConfig.set("panels." + panelName + "." + buttonName + ".lore", ChatColor.translateAlternateColorCodes('&', loreText));

        try {
            panelConfig.save(panelFile);
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Failed to save the configuration file.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Updated lore for " + buttonName + " in panel " + panelName + " to: " + loreText);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return getPanelNames();
        } else if (args.length == 2) {
            return getButtonNames(args[0]);
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
            buttonNames.remove("Commands"); // Remove Commands section
            return new ArrayList<>(buttonNames);
        }
        return new ArrayList<>();
    }
}
