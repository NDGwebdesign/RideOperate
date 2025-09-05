package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public class SubPanels implements CommandExecutor {

    private final RideOperate plugin;

    public SubPanels(RideOperate plugin) {
        this.plugin = plugin;
        //plugin.getCommand("panels").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, args);
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.panels")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission");
            player.sendMessage(nopermission);
            return true;
        }

        // Load panel.yml file
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        YamlConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if panel.yml exists
        if (!panelFile.exists()) {
            player.sendMessage("The panel.yml file does not exist!");
            return true;
        }

        player.sendMessage("Your panels:");

        // Get a list of all panel names
        for (String panelName : panelConfig.getConfigurationSection("panels").getKeys(false)) {
            player.sendMessage("- " + panelName);
        }

        return true;
    }

}
