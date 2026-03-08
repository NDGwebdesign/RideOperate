package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import me.frp.rideoperate.panel.PanelManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SpawnPanelCommand implements CommandExecutor, TabCompleter {

    private static final String PANEL_SPAWN_ITEM_KEY = "panel_spawn_item";
    private static final String PANEL_NAME_KEY = "panel_name";

    private final RideOperate plugin;

    public SpawnPanelCommand(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("rideoperate.spawnpanel")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§eGebruik: /spawnpanel <panel>");
            return true;
        }

        PanelManager manager = new PanelManager(plugin);
        if (!manager.panelExists(args[0])) {
            player.sendMessage("§cPanel bestaat niet!");
            return true;
        }

        ItemStack panelSign = createPanelSpawnerSign(args[0]);
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(panelSign);

        if (!leftovers.isEmpty()) {
            for (ItemStack left : leftovers.values()) {
                Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), left);
                dropped.setOwner(player.getUniqueId());
            }
            player.sendMessage("§eInventory vol, sign op de grond gedropt.");
        }

        player.sendMessage("§aJe hebt een panel sign gekregen. Plaats die om panel '" + args[0] + "' te spawnen.");
        return true;
    }

    // TAB COMPLETER
    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args) {

        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            File panelFile = new File(plugin.getDataFolder(), "panel.yml");
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(panelFile);

            if (cfg.contains("panels")) {
                for (String panel : cfg.getConfigurationSection("panels").getKeys(false)) {
                    if (panel.toLowerCase().startsWith(args[0].toLowerCase())) {
                        list.add(panel);
                    }
                }
            }
        }
        return list;
    }

    private ItemStack createPanelSpawnerSign(String panelName) {
        ItemStack sign = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = sign.getItemMeta();
        if (meta == null) {
            return sign;
        }

        meta.setDisplayName(ChatColor.GREEN + "Panel Spawner" + ChatColor.GRAY + " [" + panelName + "]");
        meta.setLore(Arrays.asList(
                ChatColor.YELLOW + "Plaats dit sign om een panel te spawnen.",
                ChatColor.DARK_GRAY + "Panel: " + panelName));

        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, PANEL_SPAWN_ITEM_KEY),
                PersistentDataType.BYTE,
                (byte) 1);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, PANEL_NAME_KEY),
                PersistentDataType.STRING,
                panelName);

        sign.setItemMeta(meta);
        return sign;
    }
}
