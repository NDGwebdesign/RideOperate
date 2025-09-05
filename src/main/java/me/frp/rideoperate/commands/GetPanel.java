package me.frp.rideoperate.commands;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetPanel implements CommandExecutor {

    private final RideOperate plugin;
    private static final int PANEL_MODEL_ID = 1001;

    public GetPanel(RideOperate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Controleer of panel.yml bestaat
        File panelFile = new File(plugin.getDataFolder(), "panel.yml");
        if (!panelFile.exists()) {
            player.sendMessage("§cError: panel.yml not found!");
            return true;
        }

        FileConfiguration panelConfig = YamlConfiguration.loadConfiguration(panelFile);
        ConfigurationSection panelsSection = panelConfig.getConfigurationSection("panels.example");

        if (panelsSection == null) {
            player.sendMessage("§cError: Invalid panel configuration!");
            return true;
        }

        // Maak het paneel item
        ItemStack panelItem = new ItemStack(Material.IRON_BLOCK);
        ItemMeta meta = panelItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bControl Panel");
            meta.setCustomModelData(PANEL_MODEL_ID);

            List<String> lore = new ArrayList<>();
            lore.add("§7Available controls:");

            for (String buttonKey : panelsSection.getKeys(false)) {
                if (!buttonKey.equals("Commands")) {
                    String buttonLore = panelsSection.getString(buttonKey + ".lore");
                    lore.add("§e" + buttonKey + ": §7" + buttonLore);
                }
            }

            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(plugin, "panel_item");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "control_panel");

            NamespacedKey panelTypeKey = new NamespacedKey(plugin, "panel_type");
            meta.getPersistentDataContainer().set(panelTypeKey, PersistentDataType.STRING, "example");

            panelItem.setItemMeta(meta);
        }

        player.getInventory().addItem(panelItem);
        player.sendMessage("§aYou have received the control panel!");

        // Genereer resource pack
        generateResourcePack();

        return true;
    }

    private void generateResourcePack() {
        File resourcePackDir = new File(plugin.getDataFolder(), "rideoperate_pack");
        if (!resourcePackDir.exists()) {
            resourcePackDir.mkdirs();
        }

        File modelsDir = new File(resourcePackDir, "assets/minecraft/models/item");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }

        // JSON voor custom model data
        File modelFile = new File(modelsDir, "iron_block.json");
        try (FileWriter writer = new FileWriter(modelFile)) {
            writer.write("{\n" +
                    "  \"parent\": \"minecraft:block/iron_block\",\n" +
                    "  \"overrides\": [\n" +
                    "    {\n" +
                    "      \"predicate\": { \"custom_model_data\": " + PANEL_MODEL_ID + " },\n" +
                    "      \"model\": \"minecraft:item/panel\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create model file: " + e.getMessage());
        }

        File panelModelFile = new File(plugin.getDataFolder(), "models/panel.json");
        if (panelModelFile.exists()) {
            try {
                File dest = new File(modelsDir, "panel.json");
                if (!dest.exists()) {
                    panelModelFile.renameTo(dest);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to copy panel model: " + e.getMessage());
            }
        }

        plugin.getLogger().info("Resource pack generated!");
    }
}
