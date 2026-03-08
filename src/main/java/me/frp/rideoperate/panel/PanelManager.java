package me.frp.rideoperate.panel;

import me.frp.rideoperate.RideOperate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PanelManager {

    private final File panelFile;
    private final RideOperate plugin;

    public PanelManager(RideOperate plugin) {
        this.plugin = plugin;
        this.panelFile = new File(plugin.getDataFolder(), "panel.yml");

        // Zorg dat het bestand bestaat
        if (!panelFile.exists()) {
            plugin.saveResource("panel.yml", false); // kopie van jar naar datafolder
        }
    }

    public void spawnPanel(Player player, String panelName) {
        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        if (forward.lengthSquared() == 0) {
            forward = new Vector(0, 0, 1);
        }

        Location base = player.getLocation().clone()
                .add(forward.clone().multiply(2.0))
                .add(0, 1.4, 0);

        spawnPanelAt(player, panelName, base, forward);
    }

    public void spawnPanelAt(Player player, String panelName, Location baseLocation, Vector forwardHint) {
        if (!panelFile.exists()) {
            player.sendMessage("§cpanel.yml not found!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(panelFile);

        String resolvedPanelName = resolvePanelName(config, panelName);
        if (resolvedPanelName == null) {
            player.sendMessage("§cPanel bestaat niet!");
            return;
        }

        Location base = baseLocation == null
                ? player.getLocation().clone().add(0, 1.4, 0)
                : baseLocation.clone();

        World world = base.getWorld();
        if (world == null) {
            player.sendMessage("§cKan panel niet spawnen: world ontbreekt.");
            return;
        }

        Vector forward;
        if (forwardHint != null) {
            forward = forwardHint.clone().setY(0);
        } else {
            forward = player.getLocation().getDirection().setY(0);
        }

        if (forward.lengthSquared() == 0) {
            forward = new Vector(0, 0, 1);
        }
        forward.normalize();

        Vector right = forward.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        float yaw = yawFromForward(forward);

        String panelPath = "panels." + resolvedPanelName;
        ConfigurationSection panelSection = config.getConfigurationSection(panelPath);
        if (panelSection == null) {
            player.sendMessage("§cPanel bestaat niet!");
            return;
        }

        List<String> buttonKeys = new ArrayList<>();
        for (String key : panelSection.getKeys(false)) {
            if ("commands".equalsIgnoreCase(key)) {
                continue;
            }
            if (panelSection.isConfigurationSection(key)) {
                buttonKeys.add(key);
            }
        }

        if (buttonKeys.isEmpty()) {
            player.sendMessage("§cPanel heeft geen knoppen in panel.yml");
            return;
        }

        PanelSpawner.spawnBackBoard(plugin, base, yaw, resolvedPanelName, buttonKeys.size());

        for (int i = 0; i < buttonKeys.size(); i++) {
            String key = buttonKeys.get(i);
            String buttonPath = panelPath + "." + key;

            String materialName = firstNonBlank(
                    config.getString(buttonPath + ".material"),
                    config.getString(buttonPath + ".metial"),
                    config.getString(buttonPath + ".metail"),
                    "REDSTONE_TORCH");
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.REDSTONE_TORCH;
            }

            PanelSpawner.spawnButton(plugin, base, forward, right, material, i, buttonKeys.size(), yaw,
                    resolvedPanelName, key);
        }

        player.sendMessage("§aPanel '" + resolvedPanelName + "' spawned!");
    }

    public boolean panelExists(String panelName) {
        if (!panelFile.exists()) {
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(panelFile);
        return resolvePanelName(config, panelName) != null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim().toUpperCase(Locale.ROOT);
            }
        }
        return "REDSTONE_TORCH";
    }

    private String resolvePanelName(YamlConfiguration config, String panelNameInput) {
        ConfigurationSection panels = config.getConfigurationSection("panels");
        if (panels == null) {
            return null;
        }

        for (String key : panels.getKeys(false)) {
            if (key.equals(panelNameInput) || key.equalsIgnoreCase(panelNameInput)) {
                return key;
            }
        }

        return null;
    }

    private float yawFromForward(Vector forward) {
        return (float) Math.toDegrees(Math.atan2(-forward.getX(), forward.getZ()));
    }
}
