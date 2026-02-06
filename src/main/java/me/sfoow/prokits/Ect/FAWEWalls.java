package me.sfoow.prokits.Ect;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FAWEWalls {

    private static final Random RANDOM = new Random();

    // Minecraft dye colors
    private static final String[] COLORS = {
            "white", "light_gray", "gray", "black",
            "red", "pink", "orange", "yellow",
            "lime", "green", "cyan", "light_blue",
            "blue", "purple", "magenta", "brown"
    };

    // Simplified color wheel for analogous colors
    private static final String[] COLOR_WHEEL = {
            "red", "orange", "yellow", "lime", "green", "cyan",
            "light_blue", "blue", "purple", "magenta", "pink", "red"
    };

    // Generate 3 analogous colors for a random base color
    public static List<Material> generatePalette(String blockType) {
        String baseColor = COLORS[RANDOM.nextInt(COLORS.length)];

        List<String> analogues = new ArrayList<>();
        int baseIndex = 0;
        for (int i = 0; i < COLOR_WHEEL.length; i++) {
            if (COLOR_WHEEL[i].equals(baseColor)) {
                baseIndex = i;
                break;
            }
        }
        // pick next 3 colors in the wheel
        for (int i = 1; i <= 3; i++) {
            analogues.add(COLOR_WHEEL[(baseIndex + i) % COLOR_WHEEL.length]);
        }

        // Convert color names to Minecraft Materials
        List<Material> materials = new ArrayList<>();
        materials.add(Material.valueOf(baseColor.toUpperCase() + "_" + blockType.toUpperCase()));
        for (String color : analogues) {
            try {
                materials.add(Material.valueOf(color.toUpperCase() + "_" + blockType.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // Skip invalid colors
            }
        }
        return materials;
    }

    // Build walls using FAWE
    public static void buildWalls(Location l1, Location l2, List<Material> materials) {
        if (materials == null || materials.isEmpty()) return;

        World world = BukkitAdapter.adapt(l1.getWorld());

        BlockVector3 min = BlockVector3.at(
                Math.min(l1.getBlockX(), l2.getBlockX()),
                Math.min(l1.getBlockY(), l2.getBlockY()),
                Math.min(l1.getBlockZ(), l2.getBlockZ())
        );

        BlockVector3 max = BlockVector3.at(
                Math.max(l1.getBlockX(), l2.getBlockX()),
                Math.max(l1.getBlockY(), l2.getBlockY()),
                Math.max(l1.getBlockZ(), l2.getBlockZ())
        );

        try (EditSession session = WorldEdit.getInstance()
                .newEditSessionBuilder()
                .world(world)
                .fastMode(true)
                .build()) {

            for (int y = min.y(); y <= max.y(); y++) {
                for (int x = min.x(); x <= max.x(); x++) {
                    for (int z = min.z(); z <= max.z(); z++) {

                        // Only outer walls
                        if (x != min.x() && x != max.x() &&
                                z != min.z() && z != max.z()) {
                            continue;
                        }

                        // Random block from palette
                        Material mat = materials.get(RANDOM.nextInt(materials.size()));
                        BlockState state = BukkitAdapter
                                .asBlockType(mat)
                                .getDefaultState();

                        session.setBlock(BlockVector3.at(x, y, z), state);
                    }
                }
            }

            session.flushQueue();
        }
    }


    // Example usage: build a concrete wall in your area with random analogous colors
    public static void buildRandomConcreteWall(org.bukkit.World bukkitWorld) {
        Location l1 = new Location(bukkitWorld, -26, 63, 275);
        Location l2 = new Location(bukkitWorld, 275, -47, -26);
        List<Material> palette = generatePalette("concrete");
        buildWalls(l1, l2, palette);
    }
}
