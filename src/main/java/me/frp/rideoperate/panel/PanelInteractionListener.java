package me.frp.rideoperate.panel;

import me.frp.rideoperate.RideOperate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PanelInteractionListener implements Listener {

    private static final String PANEL_NAME_KEY = "panel_name";
    private static final String BUTTON_NAME_KEY = "button_name";

    private final RideOperate plugin;
    private final File panelFile;
    private final NamespacedKey panelKey;
    private final NamespacedKey buttonKey;
    private final Map<UUID, String> currentHoverButtonByPlayer = new HashMap<>();

    public PanelInteractionListener(RideOperate plugin) {
        this.plugin = plugin;
        this.panelFile = new File(plugin.getDataFolder(), "panel.yml");
        this.panelKey = new NamespacedKey(plugin, PANEL_NAME_KEY);
        this.buttonKey = new NamespacedKey(plugin, BUTTON_NAME_KEY);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startActionBarHoverTask();
    }

    private void startActionBarHoverTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHoverActionBar(player);
            }
        }, 1L, 5L);
    }

    private void updateHoverActionBar(Player player) {
        String hoveredButton = findHoveredButtonName(player);
        UUID playerId = player.getUniqueId();

        if (hoveredButton == null) {
            if (currentHoverButtonByPlayer.containsKey(playerId)) {
                player.sendActionBar(Component.empty());
                currentHoverButtonByPlayer.remove(playerId);
            }
            return;
        }

        player.sendActionBar(Component.text("Button: " + formatButtonName(hoveredButton), NamedTextColor.YELLOW));
        currentHoverButtonByPlayer.put(playerId, hoveredButton);
    }

    private String findHoveredButtonName(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                5.0,
                0.1,
                entity -> entity != player && entity.getScoreboardTags().contains("rideoperate_button"));

        if (result == null || result.getHitEntity() == null) {
            return null;
        }

        return result.getHitEntity().getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);
    }

    private String formatButtonName(String buttonName) {
        if (buttonName == null || buttonName.isBlank()) {
            return "Button";
        }

        String normalized = buttonName.replace('_', ' ').replace('-', ' ').trim();
        if (normalized.isEmpty()) {
            return "Button";
        }

        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase(Locale.ROOT));
            }
            if (i < words.length - 1) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    @EventHandler
    public void onPanelEntityInteract(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (!clicked.getScoreboardTags().contains("rideoperate_button")) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();

        String panelName = clicked.getPersistentDataContainer().get(panelKey, PersistentDataType.STRING);
        String buttonName = clicked.getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);

        if (panelName == null || buttonName == null) {
            player.sendMessage("§cDeze knop heeft geen geldige metadata.");
            return;
        }

        if (!panelFile.exists()) {
            player.sendMessage("§cpanel.yml not found!");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(panelFile);
        panelName = resolvePanelName(config, panelName);
        buttonName = resolveButtonName(config, panelName, buttonName);

        if (!playerHasButtonPermission(player, buttonName)) {
            String noPermission = plugin.getConfig().getString("messages.nopermission", "§cYou don't have permission.");
            player.sendMessage(noPermission);
            return;
        }

        String command = resolveButtonCommand(config, panelName, buttonName);
        if (command == null || command.trim().isEmpty()) {
            player.sendMessage("§eGeen command ingesteld voor knop: " + buttonName);
            return;
        }

        executeButtonCommand(player, command);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
        animateButtonPress(clicked, panelName, buttonName);
    }

    private boolean playerHasButtonPermission(Player player, String buttonName) {
        String exact = "rideoperate.execute." + buttonName;
        String normalized = "rideoperate.execute." + normalizeKey(buttonName);
        return player.hasPermission("rideoperate.execute.*")
                || player.hasPermission(exact)
                || player.hasPermission(normalized)
                || player.isOp();
    }

    private String resolveButtonCommand(YamlConfiguration config, String panelName, String buttonName) {
        ConfigurationSection commands = config.getConfigurationSection("panels." + panelName + ".Commands");
        if (commands == null) {
            return null;
        }

        String direct = commands.getString(buttonName);
        if (direct != null) {
            return direct;
        }

        for (String key : commands.getKeys(false)) {
            if (key.equalsIgnoreCase(buttonName)) {
                return commands.getString(key);
            }
        }
        return null;
    }

    private void executeButtonCommand(Player player, String command) {
        String trimmed = command.trim();
        CommandSender sender;
        String executable;

        if (trimmed.startsWith("/")) {
            sender = player;
            executable = trimmed.substring(1);
        } else {
            sender = Bukkit.getConsoleSender();
            executable = trimmed;
        }

        Bukkit.dispatchCommand(sender, executable);
    }

    private void animateButtonPress(Entity clicked, String panelName, String buttonName) {
        ItemDisplay targetDisplay;
        if (clicked instanceof ItemDisplay) {
            targetDisplay = (ItemDisplay) clicked;
        } else {
            targetDisplay = findItemDisplayForButton(clicked, panelName, buttonName);
        }

        if (targetDisplay == null || !targetDisplay.isValid()) {
            return;
        }

        Transformation original = targetDisplay.getTransformation();
        Transformation pressed = new Transformation(
                new Vector3f(original.getTranslation()),
                new Quaternionf(original.getLeftRotation()),
                new Vector3f(original.getScale()).mul(0.85f),
                new Quaternionf(original.getRightRotation()));

        targetDisplay.setInterpolationDelay(0);
        targetDisplay.setInterpolationDuration(2);
        targetDisplay.setTransformation(pressed);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!targetDisplay.isValid()) {
                return;
            }
            targetDisplay.setInterpolationDelay(0);
            targetDisplay.setInterpolationDuration(3);
            targetDisplay.setTransformation(original);
        }, 3L);
    }

    private ItemDisplay findItemDisplayForButton(Entity clicked, String panelName, String buttonName) {
        Collection<Entity> nearby = clicked.getWorld().getNearbyEntities(clicked.getLocation(), 0.8, 0.8, 0.8);
        for (Entity entity : nearby) {
            if (!(entity instanceof ItemDisplay)) {
                continue;
            }
            String entityPanel = entity.getPersistentDataContainer().get(panelKey, PersistentDataType.STRING);
            String entityButton = entity.getPersistentDataContainer().get(buttonKey, PersistentDataType.STRING);
            if (panelName.equals(entityPanel) && buttonName.equals(entityButton)) {
                return (ItemDisplay) entity;
            }
        }
        return null;
    }

    private String resolvePanelName(YamlConfiguration config, String panelName) {
        ConfigurationSection panels = config.getConfigurationSection("panels");
        if (panels == null) {
            return panelName;
        }

        for (String key : panels.getKeys(false)) {
            if (key.equals(panelName) || key.equalsIgnoreCase(panelName)) {
                return key;
            }
        }
        return panelName;
    }

    private String resolveButtonName(YamlConfiguration config, String panelName, String buttonName) {
        ConfigurationSection panelSection = config.getConfigurationSection("panels." + panelName);
        if (panelSection == null) {
            return buttonName;
        }

        for (String key : panelSection.getKeys(false)) {
            if ("Commands".equalsIgnoreCase(key)) {
                continue;
            }
            if (key.equals(buttonName) || key.equalsIgnoreCase(buttonName)) {
                return key;
            }
        }
        return buttonName;
    }

    private String normalizeKey(String input) {
        return input.toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
