package me.frp.rideoperate.commands.subcommands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SubCreate implements CommandExecutor, Listener {

    private final RideOperate plugin;

    public SubCreate(RideOperate plugin) {
        this.plugin = plugin;

        // Register the listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Load the default panel.yml if it doesn't exist
        plugin.saveDefaultConfig();
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
        if (!player.hasPermission("rideoperate.createpanel")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission");
            player.sendMessage(nopermission);
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("Usage: /rp create <panelName>");
            return true;
        }

        String panelName = args[1].toLowerCase();

        // Check if the panel already exists
        if (panelExists(panelName)) {
            String panelExistsMessage = plugin.getConfig().getString("messages.allexist");
            if (panelExistsMessage != null && !panelExistsMessage.isEmpty()) {
                panelExistsMessage = panelExistsMessage.replace("%panelName%", panelName);
                player.sendMessage(panelExistsMessage);
            } else {
                player.sendMessage("Panel '" + panelName + "' already exists!");
            }
            return true;
        }

        // Create the new panel
        createPanel(panelName);

        String successMessage = plugin.getConfig().getString("messages.successfullycreated");
        if (successMessage != null && !successMessage.isEmpty()) {
            successMessage = successMessage.replace("%panelName%", panelName);
            player.sendMessage(successMessage);
        } else {
            player.sendMessage("Panel '" + panelName + "' successfully created!");
        }

        return true;

    }

    private boolean panelExists(String panelName) {
        File configFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!configFile.exists()) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.contains("panels." + panelName);
    }

    private boolean createPanel(String panelName) {
        File configFile = new File(plugin.getDataFolder(), "panel.yml");

        // Load existing data from panel.yml
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        // Set default values for the new panel
        config.set("panels." + panelName + ".Power", "REDSTONE_TORCH");
        config.set("panels." + panelName + ".Gates", "OAK_FENCE_GATE");
        config.set("panels." + panelName + ".Dispatch", "MINECART");
        config.set("panels." + panelName + ".safety-bar", "IRON_TRAPDOOR");

        // Set default commands for the new panel
        config.set("panels." + panelName + ".Commands.Power", "/power_command");
        config.set("panels." + panelName + ".Commands.Gates", "/gates_command");
        config.set("panels." + panelName + ".Commands.Dispatch", "/dispatch_command");
        config.set("panels." + panelName + ".Commands.safety-bar", "/safetybar_command");

        // Save the configuration to the file
        try {
            config.save(new File(plugin.getDataFolder(), "panel.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Player) {
            Player player = (Player) event.getClickedInventory().getHolder();

            // Check if the clicked inventory has a custom name (Panel)
            if (event.getView().getTitle().equals("Panel")) {
                event.setCancelled(true); // Prevent item pickup

                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    // Perform the command associated with the clicked item
                    String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
                    String command = plugin.getConfig().getString("panels." + player.getName() + "." + itemName + ".Commands." + itemName);
                    if (command != null && !command.isEmpty()) {
                        player.performCommand(command);
                    }
                }
            }
        }
    }
}
