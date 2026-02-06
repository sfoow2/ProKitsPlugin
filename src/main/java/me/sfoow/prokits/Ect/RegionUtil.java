package me.sfoow.prokits.Ect;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionUtil {

    private static final Map<World, RegionManager> REGION_MANAGER_CACHE = new ConcurrentHashMap<>();

    private static RegionManager getRegionManager(World world) {
        return REGION_MANAGER_CACHE.computeIfAbsent(world, w ->
                WorldGuard.getInstance()
                        .getPlatform()
                        .getRegionContainer()
                        .get(BukkitAdapter.adapt(w))
        );
    }

    public static boolean isInRegion(Location location, String regionName) {
        World world = location.getWorld();
        if (world == null) return false;

        RegionManager regionManager = getRegionManager(world);
        if (regionManager == null) return false;

        ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(location)
        );

        return regions.getRegions().stream()
                .anyMatch(r -> r.getId().equalsIgnoreCase(regionName));
    }
}
