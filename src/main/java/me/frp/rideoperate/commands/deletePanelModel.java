package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class deletePanelModel implements CommandExecutor, TabCompleter {

    private final RideOperate plugin;

    public deletePanelModel(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("rideoperate.deletepanelmodel")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§eGebruik: /deletepanelmodel <panel>");
            return true;
        }

        String panelName = args[0];
        String panelTag = "rideoperate_panel_" + sanitize(panelName);

        int removed = 0;
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!entity.getScoreboardTags().contains(panelTag)) {
                    continue;
                }
                entity.remove();
                removed++;
            }
        }

        if (removed == 0) {
            sender.sendMessage("§cGeen gespawned panel gevonden met naam: " + panelName);
            return true;
        }

        sender.sendMessage("§aPanel model verwijderd: §f" + panelName + "§a (" + removed + " entities)");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(panelFile);
        ConfigurationSection panelsSection = cfg.getConfigurationSection("panels");
        if (panelsSection == null) {
            return Collections.emptyList();
        }

        String prefix = args[0].toLowerCase();
        List<String> results = new ArrayList<>();
        for (String panel : panelsSection.getKeys(false)) {
            if (panel.toLowerCase().startsWith(prefix)) {
                results.add(panel);
            }
        }
        return results;
    }

    private String sanitize(String value) {
        return value.toLowerCase().replace(' ', '_');
    }
}
