package me.frp.rideoperate.commands.subcommands;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubOpenPanel implements CommandExecutor {

    private final RideOperate plugin;

    public SubOpenPanel(RideOperate plugin) {
        this.plugin = plugin;
        //plugin.getCommand("openpanel").setExecutor(this);
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

        // Check if the player has the required permission to use the command
        if (!player.hasPermission("rideoperate.openpanel")) {
            String nopermission = plugin.getConfig().getString("messages.nopermission");
            player.sendMessage(nopermission);
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("Usage: /rp open <panelName>");
            return true;
        }

        String panelName = args[1].toLowerCase();

        // Load the panel from YAML
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);

        // Check if the panel exists
        if (!panelExists(panelName, panelConfig)) {
            String allexist = plugin.getConfig().getString("messages.allexist" + panelName);
            player.sendMessage(allexist);
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
                    Bukkit.dispatchCommand(event.getWhoClicked(), command);
                }
            }
        }
    }

    private String getCommandFromDisplayName(String panelName, String displayName) {
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        if (panelConfig.contains("panels." + panelName + ".Commands." + displayName)) {
            return panelConfig.getString("panels." + panelName + ".Commands." + displayName);
        }
        return null;
    }

    private boolean panelExists(String panelName, FileConfiguration panelConfig) {
        return panelConfig.contains("panels." + panelName);
    }

    private void openPanel(Player player, String panelName, FileConfiguration panelConfig) {
        Inventory panelInventory = Bukkit.createInventory(null, 9, "panel_" + panelName);

        // Get items from config and add them to the panel
        for (String itemName : panelConfig.getConfigurationSection("panels." + panelName).getKeys(false)) {
            Material material = Material.matchMaterial(panelConfig.getString("panels." + panelName + "." + itemName));
            if (material != null) {
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(itemName);
                item.setItemMeta(meta);
                panelInventory.addItem(item);
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

    private String getCommandFromDisplayName(String displayName) {
        String panelName = displayName.replace("panel_", "");
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        if (panelConfig.contains("panels." + panelName + ".Commands." + displayName)) {
            return panelConfig.getString("panels." + panelName + ".Commands." + displayName);
        }
        return null;
    }

}
