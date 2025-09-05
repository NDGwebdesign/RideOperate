package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
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
import java.util.List;

public class CreatePanel implements CommandExecutor, Listener {

    private final RideOperate plugin;

    public CreatePanel(RideOperate plugin) {
        this.plugin = plugin;

        // Register the listener
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Load the default panel.yml if it doesn't exist
        plugin.saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        if (args.length != 1) {
            player.sendMessage("Usage: /createpanel <panelName>");
            return true;
        }

        String panelName = args[0].toLowerCase();

        // Check if the panel already exists
        if (panelExists(panelName)) {
            String allexist = plugin.getConfig().getString("messages.allexist");
            player.sendMessage(allexist.replace("%panelName%", panelName));
            return true;
        } else {
            createPanel(panelName);

            String successMessage = plugin.getConfig().getString("messages.successfullycreated");
            if (successMessage != null && !successMessage.isEmpty()) {
                // Add the panel name to the message
                successMessage = successMessage.replace("%panelName%", panelName);
                player.sendMessage(successMessage);
            } else {
                // Default message if no message is configured
                player.sendMessage("Panel '" + panelName + "' successfully created!");
            }

            return true;
        }
    }

    private boolean panelExists(String panelName) {
        File configFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!configFile.exists()) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.contains("panels." + panelName);
    }

    private void createPanel(String panelName) {
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
        config.set("panels." + panelName + ".Power.material", "REDSTONE_TORCH");
        config.set("panels." + panelName + ".Power.lore", "This is the power button!");
        config.set("panels." + panelName + ".Gates.material", "OAK_FENCE_GATE");
        config.set("panels." + panelName + ".Gates.lore", "This is the gates button!");
        config.set("panels." + panelName + ".Dispatch.material", "MINECART");
        config.set("panels." + panelName + ".Dispatch.lore", "This is the dispatch button!");
        config.set("panels." + panelName + ".safety-bar.material", "IRON_TRAPDOOR");
        config.set("panels." + panelName + ".safety-bar.lore", "This is the safety-bar button!");
        config.set("panels." + panelName + ".status.material", "IRON_TRAPDOOR");
        config.set("panels." + panelName + ".status.lore", "This is the status button!");

        // Set default commands for the new panel
        config.set("panels." + panelName + ".Commands.Power", "/power_command");
        config.set("panels." + panelName + ".Commands.Gates", "/gates_command");
        config.set("panels." + panelName + ".Commands.Dispatch", "/dispatch_command");
        config.set("panels." + panelName + ".Commands.safety-bar", "/safetybar_command");
        config.set("panels." + panelName + ".Commands.status", "/status_command");

        // Save the configuration to the file
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    String materialPath = "panels." + player.getName() + "." + itemName + ".material";
                    String lorePath = "panels." + player.getName() + "." + itemName + ".lore";
                    String commandPath = "panels." + player.getName() + ".Commands." + itemName;

                    String materialName = plugin.getConfig().getString(materialPath);
                    String lore = plugin.getConfig().getString(lorePath);
                    String command = plugin.getConfig().getString(commandPath);

                    // Check if the player has permission for the specific item
                    if (!player.hasPermission("rideoperate.execute." + itemName)) {
                        String nopermission = plugin.getConfig().getString("messages.nopermission");
                        player.sendMessage(nopermission);
                        return;
                    }

                    // Execute the command if it exists
                    if (command != null && !command.isEmpty()) {
                        if (command.startsWith("/")) {
                            // For regular Minecraft commands, dispatch as player
                            player.performCommand(command.substring(1));
                        } else {
                            // For plugin commands, dispatch as console
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    }
                }
            }
        }
    }
}
