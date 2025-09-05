package me.frp.rideoperate.listener;

import org.bukkit.Material;

public class BlockColor {

    public static int getBlockColor(Material material) {
        switch (material) {
            // Original blocks
            case GRASS_BLOCK: return 0x6AB04C;
            case DIRT: return 0x8B4513;
            case STONE: return 0x7F7F7F;
            case SAND: return 0xFFE4B5;
            case WATER: return 0x1E90FF;
            case LAVA: return 0xFF4500;
            case OAK_LOG: return 0x9B7653;
            case COBBLESTONE: return 0x666666;
            case BRICKS: return 0xB22222;
            case GLASS: return 0xADD8E6;
            case OBSIDIAN: return 0x1F1F1F;

            // Ores
            case DIAMOND_ORE: return 0x1ED6D6;
            case EMERALD_ORE: return 0x17DD62;
            case REDSTONE_ORE: return 0xFF0000;
            case GOLD_ORE: return 0xFFD700;
            case COAL_ORE: return 0x2F2F2F;
            case LAPIS_ORE: return 0x1034A6;
            case IRON_ORE: return 0xD3D3D3;
            case COPPER_ORE: return 0xB87333;

            // Dimension blocks
            case NETHERRACK: return 0x8B0000;
            case END_STONE: return 0xFFFACD;
            case WARPED_NYLIUM: return 0x0C8E8E;
            case CRIMSON_NYLIUM: return 0x8E0C0C;

            // Wood types
            case OAK_PLANKS: return 0xB8945F;
            case SPRUCE_PLANKS: return 0x785836;
            case BIRCH_PLANKS: return 0xD1B784;
            case JUNGLE_PLANKS: return 0xB88764;
            case ACACIA_PLANKS: return 0xBA683C;
            case DARK_OAK_PLANKS: return 0x4C3223;

            //terracotta
            case TERRACOTTA: return 0x9E5B4B;
            case WHITE_TERRACOTTA: return 0xD1B1A1;
            case RED_TERRACOTTA: return 0x9E4137;
            case BLUE_TERRACOTTA: return 0x4A3B5B;
            case GREEN_TERRACOTTA: return 0x4B522A;
            case BLACK_TERRACOTTA: return 0x251610;
            case YELLOW_TERRACOTTA: return 0xB88B2C;
            case LIME_TERRACOTTA: return 0x677535;
            case PINK_TERRACOTTA: return 0x9C4B4B;
            case GRAY_TERRACOTTA: return 0x392A24;
            case LIGHT_GRAY_TERRACOTTA: return 0x876B62;
            case CYAN_TERRACOTTA: return 0x565B5B;
            case PURPLE_TERRACOTTA: return 0x764656;
            case ORANGE_TERRACOTTA: return 0x9B4F1E;
            case LIGHT_BLUE_TERRACOTTA: return 0x5A7A84;
            case MAGENTA_TERRACOTTA: return 0x945271;
            case BROWN_TERRACOTTA: return 0x4D3224;

            // Wool colors
            case WHITE_WOOL: return 0xFFFFFF;
            case RED_WOOL: return 0xFF0000;
            case BLUE_WOOL: return 0x0000FF;
            case GREEN_WOOL: return 0x00FF00;
            case BLACK_WOOL: return 0x000000;
            case PURPLE_WOOL: return 0x800080;
            case ORANGE_WOOL: return 0xFFA500;
            case BROWN_WOOL: return 0x8B4513;
            case CYAN_WOOL: return 0x00FFFF;
            case LIGHT_GRAY_WOOL: return 0xC0C0C0;
            case GRAY_WOOL: return 0x808080;
            case PINK_WOOL: return 0xFFC0CB;
            case LIME_WOOL: return 0x32CD32;
            case YELLOW_WOOL: return 0xFFFF00;
            case LIGHT_BLUE_WOOL: return 0x87CEEB;
            case MAGENTA_WOOL: return 0xFF00FF;

            //carpet
            case WHITE_CARPET: return 0xFFFFFF;
            case RED_CARPET: return 0xFF0000;
            case BLUE_CARPET: return 0x0000FF;
            case GREEN_CARPET: return 0x00FF00;
            case BLACK_CARPET: return 0x000000;
            case PURPLE_CARPET: return 0x800080;
            case ORANGE_CARPET: return 0xFFA500;
            case BROWN_CARPET: return 0x8B4513;
            case CYAN_CARPET: return 0x00FFFF;
            case LIGHT_GRAY_CARPET: return 0xC0C0C0;
            case GRAY_CARPET: return 0x808080;
            case PINK_CARPET: return 0xFFC0CB;
            case LIME_CARPET: return 0x32CD32;
            case YELLOW_CARPET: return 0xFFFF00;
            case LIGHT_BLUE_CARPET: return 0x87CEEB;
            case MAGENTA_CARPET: return 0xFF00FF;

            // Concrete powder colors
            case WHITE_CONCRETE_POWDER: return 0xF0F0F0;
            case RED_CONCRETE_POWDER: return 0x9E2B27;
            case BLUE_CONCRETE_POWDER: return 0x2E2B8F;
            case GREEN_CONCRETE_POWDER: return 0x4B7F21;
            case BLACK_CONCRETE_POWDER: return 0x1D1D21;
            case YELLOW_CONCRETE_POWDER: return 0xF0AF15;
            case LIME_CONCRETE_POWDER: return 0x70B919;
            case PINK_CONCRETE_POWDER: return 0xF07B9F;
            case GRAY_CONCRETE_POWDER: return 0x4C4C4C;
            case LIGHT_GRAY_CONCRETE_POWDER: return 0x8C8C8C;
            case CYAN_CONCRETE_POWDER: return 0x157788;
            case PURPLE_CONCRETE_POWDER: return 0x792AAC;
            case ORANGE_CONCRETE_POWDER: return 0xEA7E35;
            case LIGHT_BLUE_CONCRETE_POWDER: return 0x3498DB;
            case MAGENTA_CONCRETE_POWDER: return 0xB82E93;
            case BROWN_CONCRETE_POWDER: return 0x6B4226;

            //fence
            case OAK_FENCE: return 0xB8945F;
            case SPRUCE_FENCE: return 0x785836;
            case BIRCH_FENCE: return 0xD1B784;
            case JUNGLE_FENCE: return 0xB88764;
            case ACACIA_FENCE: return 0xBA683C;
            case DARK_OAK_FENCE: return 0x4C3223;
            //fence gate
            case OAK_FENCE_GATE: return 0xB8945F;
            case SPRUCE_FENCE_GATE: return 0x785836;
            case BIRCH_FENCE_GATE: return 0xD1B784;
            case JUNGLE_FENCE_GATE: return 0xB88764;
            case ACACIA_FENCE_GATE: return 0xBA683C;
            case DARK_OAK_FENCE_GATE: return 0x4C3223;


            // Concrete colors
            case WHITE_CONCRETE: return 0xF0F0F0;
            case RED_CONCRETE: return 0x9E2B27;
            case BLUE_CONCRETE: return 0x2E2B8F;
            case GREEN_CONCRETE: return 0x4B7F21;
            case BLACK_CONCRETE: return 0x1D1D21;
            case YELLOW_CONCRETE: return 0xF0AF15;
            case LIME_CONCRETE: return 0x70B919;
            case PINK_CONCRETE: return 0xF07B9F;
            case GRAY_CONCRETE: return 0x4C4C4C;
            case LIGHT_GRAY_CONCRETE: return 0x8C8C8C;
            case CYAN_CONCRETE: return 0x157788;
            case PURPLE_CONCRETE: return 0x792AAC;
            case ORANGE_CONCRETE: return 0xEA7E35;
            case LIGHT_BLUE_CONCRETE: return 0x3AB3DA;
            case MAGENTA_CONCRETE: return 0xC64FDA;
            case BROWN_CONCRETE: return 0x724728;

            // Natural blocks
            case SNOW: return 0xFFFFFF;
            case ICE: return 0x9DD1FF;
            case PACKED_ICE: return 0x95C5FF;
            case BLUE_ICE: return 0x7FB5FF;
            case PODZOL: return 0x815431;
            case MYCELIUM: return 0x6A5B5B;
            case CLAY: return 0xA7A4A4;
            case GRAVEL: return 0x857F7F;
            case COARSE_DIRT: return 0x77553311;
            case ROOTED_DIRT: return 0x906C50;
            case RED_SAND: return 0xBD6A24;
            case SOUL_SAND: return 0x534336;
            case SOUL_SOIL: return 0x4B3E35;


            case BASALT: return 0x505050;
            case BLACKSTONE: return 0x2F2F2F;
            case ANCIENT_DEBRIS: return 0x6B4C41;
            case NETHER_GOLD_ORE: return 0x8B6914;
            case NETHER_QUARTZ_ORE: return 0x8B736A;

            case PURPUR_BLOCK: return 0xA97BA9;
            case END_STONE_BRICKS: return 0xE2E5A5;
            case CHORUS_PLANT: return 0x6B2B6B;
            case CHORUS_FLOWER: return 0x9B4B9B;

            // Decorative blocks
            case QUARTZ_BLOCK: return 0xFFFFFA;
            case PRISMARINE: return 0x7AAFB9;
            case SEA_LANTERN: return 0xF9FFFF;
            case GLOWSTONE: return 0xFFBC5E;
            case CHISELED_QUARTZ_BLOCK: return 0xFFFFFA;
            case QUARTZ_PILLAR: return 0xFFFFFA;
            case QUARTZ_STAIRS: return 0xFFFFFA;
            case QUARTZ_SLAB: return 0xFFFFFA;
            case CHISELED_NETHER_BRICKS: return 0x6B4C41;
            case CHISELED_RED_SANDSTONE: return 0xBD6A24;
            case CHISELED_SANDSTONE: return 0xBD6A24;
            case CHISELED_STONE_BRICKS: return 0x505050;

            //doors
            case OAK_DOOR: return 0xB8945F;
            case SPRUCE_DOOR: return 0x785836;
            case BIRCH_DOOR: return 0xD1B784;
            case JUNGLE_DOOR: return 0xB88764;
            case ACACIA_DOOR: return 0xBA683C;
            case DARK_OAK_DOOR: return 0x4C3223;
            case CRIMSON_DOOR: return 0x744141;
            case WARPED_DOOR: return 0x6B3A3A;
            case MANGROVE_DOOR: return 0x705030;
            case BAMBOO_DOOR: return 0x705030;

            // Clouds (if implemented as blocks)
            case STRUCTURE_VOID: return 0xFFFFFF; // For white clouds
            case BARRIER: return 0xDDDDDD; // For gray clouds
            case LIGHT: return 0xEEEEEE; // For light clouds

            // Utility blocks
            case BARREL: return 0x8B6B4F;  // Wooden barrel color
            case BEEHIVE: return 0xD2B48C;  // Natural beehive color
            case BEE_NEST: return 0xDEB887;  // Natural bee nest color
            case BEE_SPAWN_EGG: return 0xEEC170;  // Bee spawn egg color
            case BLAST_FURNACE: return 0x505050;  // Dark stone color
            case BREWING_STAND: return 0x8B4513;  // Brown brewing stand
            case CAKE: return 0xFFEFD5;  // Cream white cake
            case CARTOGRAPHY_TABLE: return 0x8B4513;  // Brown wood with blue top
            case COMPOSTER: return 0x8B5A2B;  // Brown wooden composter
            case CRAFTING_TABLE: return 0x8B5A2B;  // Classic crafting table brown
            case FURNACE: return 0x696969;  // Stone gray furnace
            case GRINDSTONE: return 0x808080;  // Gray stone texture
            case HOPPER: return 0x4A4A4A;  // Dark metal gray
            case LECTERN: return 0xDEB887;  // Light wooden color
            case LOOM: return 0x8B7355;  // Wooden loom color
            case SMOKER: return 0x696969;  // Stone gray smoker
            case SMITHING_TABLE: return 0x8B4513;  // Dark brown wood
            case STONECUTTER: return 0x808080;  // Stone gray cutter

            // Command blocks
            case COMMAND_BLOCK: return 0xD4A017;  // Orange command block
            case COMMAND_BLOCK_MINECART: return 0xD4A017;  // Orange command block
            case CHAIN_COMMAND_BLOCK: return 0x48D1CC;  // Turquoise chain command
            case REPEATING_COMMAND_BLOCK: return 0x9370DB;  // Purple repeating command
            case STRUCTURE_BLOCK: return 0x4B0082;  // Deep purple structure

            // Redstone components
            case REDSTONE_BLOCK: return 0xFF0000;  // Bright red
            case REDSTONE_TORCH: return 0xFF3030;  // Lit redstone torch
            case REDSTONE_WIRE: return 0xFF0000;  // Active redstone
            case REPEATER: return 0xCD5C5C;  // Redstone repeater
            case COMPARATOR: return 0xCD5C5C;  // Redstone comparator
            case DAYLIGHT_DETECTOR: return 0xDEB887;  // Wooden detector top
            case REDSTONE_LAMP: return 0xFFD700;  // Golden lamp color

            case MINECART: return 0x8B4513;  // Brown minecart

            default:
                return material.name().hashCode() & 0x7F7F7F; // Unique color for unknown blocks

        }
    }
}
