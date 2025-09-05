package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenPanel implements CommandExecutor, Listener {

    private final RideOperate plugin;

    public OpenPanel(RideOperate plugin) {
        this.plugin = plugin;
        plugin.getCommand("panel").setExecutor(this);
        plugin.getCommand("panel").setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                return getPanelNames();
            }
            return null;
        });
        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.openpanel")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission");
            player.sendMessage(nopermission);
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /panel <panelName>");
            return true;
        }

        String panelName = args[0].toLowerCase();

        // Load the panel from YAML
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if the panel exists
        if (!panelExists(panelName, panelConfig)) {
            String allexist = plugin.getConfig().getString("messages.allexist");
            player.sendMessage(allexist.replace("%panelName%", panelName));
            return true;
        }

        // Open the panel for the player
        openPanel(player, panelName, panelConfig);

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() != null && event.getView().getTitle().startsWith("panel_")) {
            // Cancel the event to prevent items from being moved
            event.setCancelled(true);

            // Execute the command if the item has a display name
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                String panelName = event.getView().getTitle().substring(6); // Remove "panel_" prefix
                String command = getCommandFromDisplayName(panelName, displayName);
                if (command != null && !command.isEmpty()) {
                    // Execute the command
                    if (command.startsWith("/")) {
                        // For commands starting with '/', execute as player
                        Bukkit.dispatchCommand(event.getWhoClicked(), command.substring(1));
                    } else {
                        // For commands not starting with '/', execute as console
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            }
        }
    }

    private String getCommandFromDisplayName(String panelName, String displayName) {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        return panelConfig.getString("panels." + panelName + ".Commands." + displayName);
    }

    private boolean panelExists(String panelName, FileConfiguration panelConfig) {
        return panelConfig.contains("panels." + panelName);
    }

    private void openPanel(Player player, String panelName, FileConfiguration panelConfig) {
        Inventory panelInventory = Bukkit.createInventory(null, 9, "panel_" + panelName);

        // Get items from config and add them to the panel
        for (String itemName : panelConfig.getConfigurationSection("panels." + panelName).getKeys(false)) {
            if (itemName.equals("Commands")) continue; // Skip commands section

            // Check for both "material" and "metial" to handle the typo in the config
            String materialName = panelConfig.getString("panels." + panelName + "." + itemName + ".material");
            if (materialName == null) {
                materialName = panelConfig.getString("panels." + panelName + "." + itemName + ".metial");
            }

            String lore = panelConfig.getString("panels." + panelName + "." + itemName + ".lore");

            // Add null check for materialName
            if (materialName == null) {
                plugin.getLogger().warning("Missing material for item " + itemName + " in panel " + panelName);
                continue;
            }

            Material material = Material.matchMaterial(materialName);

            if (material != null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(itemName);
                    if (lore != null) {
                        meta.setLore(List.of(lore)); // Set lore
                    }
                    item.setItemMeta(meta);
                }
                panelInventory.addItem(item);
            } else {
                plugin.getLogger().warning("Invalid material name: " + materialName + " for item " + itemName + " in panel " + panelName);
            }
        }

        // Open the panel inventory for the player
        player.openInventory(panelInventory);
    }


    private List<String> getPanelNames() {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        Set<String> panelNames = panelConfig.getConfigurationSection("panels").getKeys(false);
        return new ArrayList<>(panelNames);
    }
}
