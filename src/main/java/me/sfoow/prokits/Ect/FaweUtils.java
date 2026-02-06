package me.sfoow.prokits.Ect;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.Material;

public class FaweUtils {

    /**
     * Sets a cuboid region to a single block type as fast as possible.
     *
     * @param loc1 First corner
     * @param loc2 Second corner
     * @param material Bukkit material to fill with
     */
    public static void setCuboidFast(Location loc1, Location loc2, Material material) {

        if (loc1 == null || loc2 == null || material == null) return;
        if (loc1.getWorld() == null || loc2.getWorld() == null) return;
        if (!loc1.getWorld().equals(loc2.getWorld())) return;

        World weWorld = BukkitAdapter.adapt(loc1.getWorld());

        BlockVector3 min = BlockVector3.at(
                Math.min(loc1.getBlockX(), loc2.getBlockX()),
                Math.min(loc1.getBlockY(), loc2.getBlockY()),
                Math.min(loc1.getBlockZ(), loc2.getBlockZ())
        );

        BlockVector3 max = BlockVector3.at(
                Math.max(loc1.getBlockX(), loc2.getBlockX()),
                Math.max(loc1.getBlockY(), loc2.getBlockY()),
                Math.max(loc1.getBlockZ(), loc2.getBlockZ())
        );

        CuboidRegion region = new CuboidRegion(weWorld, min, max);

        BlockState blockState = BlockTypes.get(material.name().toLowerCase()).getDefaultState();

        try (EditSession editSession = WorldEdit.getInstance()
                .newEditSessionBuilder()
                .world(weWorld)
                .fastMode(true)
                .build()) {

            editSession.setBlocks((Region) region, blockState);

            // FAWE flush (important for massive edits)
            editSession.flushQueue();
        }
    }
}
