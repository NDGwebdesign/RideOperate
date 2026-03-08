package me.frp.rideoperate.panel;

import me.frp.rideoperate.RideOperate;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PanelSpawner {

        private static final String PANEL_NAME_KEY = "panel_name";
        private static final String BUTTON_NAME_KEY = "button_name";

        /*
         *
         * PUBLIC PANEL SPAWN
         *
         */

        public static BlockDisplay spawnBackBoard(
                        RideOperate plugin,
                        Location center,
                        float yaw,
                        String panelName,
                        int buttonCount) {

                World world = center.getWorld();
                if (world == null) {
                        throw new IllegalStateException("Cannot spawn panel without world");
                }

                Location origin = center.clone();
                origin.setYaw(yaw);
                origin.setPitch(0f);

                spawnModelPanel(plugin, origin, panelName);

                // dummy display (voor compatibiliteit met bestaande code)
                BlockDisplay root = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);
                root.setBlock(Material.BARRIER.createBlockData());
                root.setTransformation(new Transformation(
                                new Vector3f(0, 0, 0),
                                new Quaternionf(),
                                new Vector3f(0, 0, 0),
                                new Quaternionf()));

                root.setPersistent(true);
                root.addScoreboardTag("rideoperate_panel");
                root.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                root.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);

                return root;
        }

        /*
         *
         * BLOCKBENCH MODEL
         *
         */

        private static void spawnModelPanel(
                        RideOperate plugin,
                        Location origin,
                        String panelName) {

                World world = origin.getWorld();
                if (world == null)
                        return;

                // BASE
                spawnPart(world, origin, Material.BLACK_CONCRETE,
                                vec(1, 0, -4),
                                size(14, 1, 24),
                                plugin, panelName);

                // LEFT WALL
                spawnPart(world, origin, Material.POLISHED_ANDESITE,
                                vec(0, 0, -4),
                                size(1, 13, 23),
                                plugin, panelName);

                // RIGHT WALL
                spawnPart(world, origin, Material.POLISHED_ANDESITE,
                                vec(15, 0, -5),
                                size(1, 16, 25),
                                plugin, panelName);

                // BACK WALL
                spawnPart(world, origin, Material.POLISHED_DEEPSLATE,
                                vec(0, 0, -5),
                                size(16, 13, 1),
                                plugin, panelName);

                // FRONT WALL
                spawnPart(world, origin, Material.POLISHED_DEEPSLATE,
                                vec(0, 0, 19),
                                size(16, 13, 1),
                                plugin, panelName);

                // SLOPED CONTROL SURFACE
                spawnPartRotated(world, origin, Material.GRAY_CONCRETE,
                                vec(1, 13.4f, -5),
                                size(15, 1, 25),
                                12.5f,
                                plugin, panelName);

                // SIDE TRIMS
                spawnPartRotated(world, origin, Material.POLISHED_ANDESITE,
                                vec(10.2f, 13.1f, -5),
                                size(8.3f, 2, 1),
                                12.5f,
                                plugin, panelName);

                spawnPartRotated(world, origin, Material.POLISHED_ANDESITE,
                                vec(10.2f, 13.3f, 19),
                                size(8.3f, 2, 1),
                                12.5f,
                                plugin, panelName);
        }

        /*
         *
         * BUTTON SPAWNER
         *
         */

        public static ItemDisplay spawnButton(
                        RideOperate plugin,
                        Location panelCenter,
                        Vector forward,
                        Vector right,
                        Material material,
                        int index,
                        int total,
                        float yaw,
                        String panelName,
                        String buttonName) {

                World world = panelCenter.getWorld();
                if (world == null)
                        throw new IllegalStateException("Cannot spawn button without world");

                int columns = total > 4 ? 2 : 1;
                int row = index / columns;
                int column = index % columns;
                int rows = (int) Math.ceil(total / (double) columns);

                double spacingY = 0.22;
                double spacingX = 0.30;
                double firstOffsetY = (rows - 1) * spacingY / 2.0;

                double yOffset = firstOffsetY - (row * spacingY);
                double xOffset = columns == 1 ? 0.0 : ((column - (columns - 1) / 2.0) * spacingX);
                double forwardOffset = 0.52 - yOffset;
                double verticalLift = 0.80;

                Location location = panelCenter.clone()
                                .add(right.clone().multiply(xOffset))
                                .add(-0.5, Math.toRadians(12.5) + verticalLift, 0.0)
                                .add(forward.clone().multiply(forwardOffset));

                location.setYaw(yaw);
                location.setPitch(0f);

                Quaternionf tilt = new Quaternionf()
                                .rotateZ((float) Math.toRadians(12.5))
                                .rotateY((float) Math.toRadians(90f));

                // Button item
                ItemDisplay display = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
                display.setItemStack(new ItemStack(material));
                display.setTransformation(new Transformation(
                                new Vector3f(0f, 0f, 0.015f),
                                tilt,
                                new Vector3f(0.19f, 0.19f, 0.19f),
                                new Quaternionf()));
                display.setPersistent(true);
                applyButtonMetadata(plugin, display, panelName, buttonName);

                // Hitbox
                Interaction hitbox = (Interaction) world.spawnEntity(
                                location.clone().add(0.0, 0.01, 0.0),
                                EntityType.INTERACTION);
                hitbox.setInteractionWidth(0.24f);
                hitbox.setInteractionHeight(0.16f);
                hitbox.setPersistent(true);
                applyButtonMetadata(plugin, hitbox, panelName, buttonName);

                return display;
        }

        /*
         *
         * MODEL PART SPAWNING
         *
         */

        private static void spawnPart(
                        World world,
                        Location origin,
                        Material material,
                        Vector3f translation,
                        Vector3f scale,
                        RideOperate plugin,
                        String panelName) {

                BlockDisplay part = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);

                part.setBlock(material.createBlockData());

                part.setTransformation(new Transformation(
                                translation,
                                new Quaternionf(),
                                scale,
                                new Quaternionf()));

                part.setPersistent(true);

                part.addScoreboardTag("rideoperate_panel");
                part.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                part.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);
        }

        private static void spawnPartRotated(
                        World world,
                        Location origin,
                        Material material,
                        Vector3f translation,
                        Vector3f scale,
                        float angle,
                        RideOperate plugin,
                        String panelName) {

                Quaternionf rotation = new Quaternionf().rotateZ((float) Math.toRadians(angle));

                BlockDisplay part = (BlockDisplay) world.spawnEntity(origin, EntityType.BLOCK_DISPLAY);

                part.setBlock(material.createBlockData());

                part.setTransformation(new Transformation(
                                translation,
                                rotation,
                                scale,
                                new Quaternionf()));

                part.setPersistent(true);

                part.addScoreboardTag("rideoperate_panel");
                part.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));

                part.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);
        }

        /*
         *
         * VECTOR HELPERS (Blockbench -> Minecraft scale)
         *
         */

        private static Vector3f vec(float x, float y, float z) {
                return new Vector3f(x / 16f, y / 16f, z / 16f);
        }

        private static Vector3f size(float x, float y, float z) {
                return new Vector3f(x / 16f, y / 16f, z / 16f);
        }

        /*
         *
         * BUTTON METADATA
         *
         */

        private static void applyButtonMetadata(
                        RideOperate plugin,
                        Entity entity,
                        String panelName,
                        String buttonName) {

                entity.addScoreboardTag("rideoperate_button");
                entity.addScoreboardTag("rideoperate_panel_" + sanitize(panelName));
                entity.addScoreboardTag("rideoperate_button_" + sanitize(buttonName));

                entity.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, PANEL_NAME_KEY),
                                PersistentDataType.STRING,
                                panelName);

                entity.getPersistentDataContainer().set(
                                new NamespacedKey(plugin, BUTTON_NAME_KEY),
                                PersistentDataType.STRING,
                                buttonName);
        }

        private static String sanitize(String value) {
                return value.toLowerCase().replace(' ', '_');
        }
}