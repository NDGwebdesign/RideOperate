package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import me.frp.rideoperate.commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;

    public MainCommand(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("rp").setExecutor(this);
        plugin.getCommand("rp").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Handle main command functionality here
            sender.sendMessage("Usage: /rp <subcommand>");
            return false;
        } else {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "help":
                    new SubHelp(plugin).execute(sender, args);
                    return true;
                case "info":
                    new SubInfo(plugin).execute(sender, args);
                    return true;
                case "reload":
                    new SubReload(plugin).execute(sender, args);
                    return true;
                case "panels":
                    new SubPanels(plugin).execute(sender, args);
                    return true;
                case "addcommand":
                    new SubAddCommand(plugin).execute(sender, args);
                    return true;
                case "open":
                    if (args.length == 2) {
                        new SubOpenPanel(plugin).execute(sender, args);
                        return true;
                    } else {
                        sender.sendMessage("Usage: /rp open <panel>");
                        return false;
                    }
                case "delete":
                    if (args.length == 2) {
                        new SubDelete(plugin).execute(sender, args);
                        return true;
                    } else {
                        sender.sendMessage("Usage: /rp delete <panel>");
                        return false;
                    }
                case "create":
                    if (args.length == 2) {
                        new SubCreate(plugin).execute(sender, args);
                        return true;
                    } else {
                        sender.sendMessage("Usage: /rp create <panel>");
                        return false;
                    }
                case "lore":
                    if (args.length >= 3) {
                        new subPlaceholder(plugin).execute(sender, args);
                        return true;
                    } else {
                        sender.sendMessage("Usage: /rp lore <panelName> <buttonName> <loreText>");
                        return false;
                    }
                default:
                    sender.sendMessage("Unknown sub-command. Usage: /rp <subcommand>");
                    return false;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("help");
            completions.add("info");
            completions.add("open");
            completions.add("delete");
            completions.add("create");
            completions.add("reload");
            completions.add("link");
            completions.add("lore");
            completions.add("addcommand");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("lore")) {
                completions.addAll(getPanelNames());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("lore")) {
            completions.addAll(getButtonNames(args[1]));
        }
        return completions;
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
