package me.sfoow.prokits.event;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.sfoow.prokits.Ect.VaultUtils.addMoney;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.event.EventStarter.*;
import static me.sfoow.prokits.mines.CurrentPitMat;

public class MoshPit implements Listener {

    private static final Random rand = new Random();

    private final static Location PossibleMoshPitLoc1 = new Location(Bukkit.getWorld("spawn"),246,3,-3);
    private final static Location PossibleMoshPitLoc2 = new Location(Bukkit.getWorld("spawn"),-3,3,246);

    private static Location CurrentMoshPitLoc1;
    private static Location CurrentMoshPitLoc2;
    private static byte depthglobal;
    public static long CurrentMoshPitTimer;

    private static BukkitRunnable moshTask;

    private static void DoMoshPitTick() {
        if (moshTask != null) moshTask.cancel();

        moshTask = new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                seconds++;

                if (CurrentEvent != 1) {
                    cancel();
                    return;
                }

                // Collect mosh pit players ONCE
                List<Player> moshPlayers = new ArrayList<>();
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (isInMoshPit(pl.getLocation())) {
                        moshPlayers.add(pl);
                    }
                }

                int moshCount = moshPlayers.size();
                int playerCount = Bukkit.getOnlinePlayers().size();
                int threshold = (int) Math.ceil(playerCount * 0.20);

                boolean tooMany = moshCount > threshold;

                String actionBar = ChatColor.translateAlternateColorCodes('&',
                        tooMany
                                ? "&cᴛᴏᴏ ᴍᴀɴʏ ᴘʟᴀʏᴇʀꜱ ɪɴ ᴍᴏꜱʜ ᴘɪᴛ &7(" + moshCount + "/" + threshold + ")"
                                : "&e+1 ᴄᴏɪɴ &7(" + moshCount + "/" + threshold + ")"
                );

                for (Player pl : moshPlayers) {
                    pl.sendActionBar(actionBar);

                    if (!tooMany) {
                        addMoney(pl, 1);
                        pl.playSound(pl, Sound.BLOCK_NOTE_BLOCK_PLING, 10f, 1f);
                    }
                }

                if (seconds >= 200) {
                    Endmoshevent();
                    cancel();
                }
            }
        };

        moshTask.runTaskTimer(plugin, 0L, 20L);
    }



    public static void Endmoshevent(){
        NextEventTimer = getUnix() + 1900L;
        CurrentEvent = -1;
        CurrentEventTitle = "";

        for (Player pl: Bukkit.getOnlinePlayers()){
            if (isInMosh(pl.getLocation())){
                Location loc = pl.getLocation();
                loc.setY(4);
                pl.teleport(loc);
                SendYes(pl);
            }
        }


        Location l1 = CurrentMoshPitLoc1.clone();
        Location l2 = CurrentMoshPitLoc2.clone();

        l1 = l1.set(l1.getX(),3,l1.getZ());
        l2 = l2.set(l2.getX(),3 - depthglobal,l2.getZ());

        setCuboid(
                CurrentMoshPitLoc2.getWorld(),
                l1,
                l2,
                CurrentPitMat
        );
    }

    public static void StartMoshEvent(){
        CurrentEvent = 1;
        CurrentMoshPitTimer = getUnix() + 200;
        NextEventTimer = -1;
        CurrentEventTitle = "&aMosh Pit";
        Random rand = new Random();

        int x = rand.nextInt(246 - (-3) + 1) + -3;
        int y = 3;
        int z = rand.nextInt(246 - (-3) + 1) + -3;

        World world = Bukkit.getWorld("spawn");
        Location center = new Location(world, x, y, z);

        int radius = rand.nextInt(4,16);
        int depth = rand.nextInt(2,10);
        depthglobal = (byte) depth;

        CurrentMoshPitLoc1 = center.clone().add(-radius - 1, -47, -radius -1);
        CurrentMoshPitLoc2 = center.clone().add(radius + 1, 62, radius +1);

        // 1️⃣ Top bedrock

        setCuboid(
                world,
                center.clone().add(-radius - 1, 0, -radius -1),
                center.clone().add(radius + 1, 40, radius +1),
                Material.AIR
        );

        setCuboid(
                world,
                center.clone().add(-radius - 1, 0, -radius -1),
                center.clone().add(radius + 1, 0, radius +1),
                Material.BEDROCK
        );

        // 2️⃣ Bedrock walls
        setCylinder(
                world,
                center,
                radius + 1,
                y - depth,
                y,
                Material.BEDROCK,
                false // hollow
        );

        // 4️⃣ AIR (LAST)
        setCylinder(
                world,
                center,
                radius,
                y - depth,
                y,
                Material.AIR,
                true // solid fill
        );

        setCuboid(
                world,
                center.clone().add(-radius, -depth - 1, -radius),
                center.clone().add(radius, -depth - 1, radius),
                Material.BEDROCK
        );

        Broadcast("");
        Broadcast(ServerPrefixFront + "&f&lActivating the Mosh Pit!");
        Broadcast(ServerPrefixFront + "&7Get one token per second while in the pit");
        Broadcast("");

        DoMoshPitTick();

    }


    /*

    events


     */

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (CurrentEvent == 1){
            if (isInMosh(event.getEntity().getLocation())){
                if (event.getEntityType() == EntityType.END_CRYSTAL) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void BlockPlace(BlockPlaceEvent event){
        if (CurrentEvent == 1){
            if (isInMosh(event.getBlock().getLocation())){
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void BlockBreak(BlockBreakEvent event){
        if (CurrentEvent == 1){
            if (isInMosh(event.getBlock().getLocation())){
                event.setCancelled(true);
            }
        }
    }

    /*

    misc

     */

    private static boolean isInMoshPit(Location loc){
        if (!loc.getWorld().equals(CurrentMoshPitLoc1.getWorld()) || !loc.getWorld().equals(CurrentMoshPitLoc2.getWorld())) {
            return false;
        }

        Location loc1 = CurrentMoshPitLoc1.clone();
        Location loc2 = CurrentMoshPitLoc2.clone();

        loc1.setY(3);
        loc2.setY(3 - depthglobal);

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());

        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());

        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    };

    private static boolean isInMosh(Location loc){
        if (!loc.getWorld().equals(CurrentMoshPitLoc1.getWorld()) || !loc.getWorld().equals(CurrentMoshPitLoc2.getWorld())) {
            return false;
        }

        int minX = Math.min(CurrentMoshPitLoc1.getBlockX(), CurrentMoshPitLoc2.getBlockX());
        int maxX = Math.max(CurrentMoshPitLoc1.getBlockX(), CurrentMoshPitLoc2.getBlockX());

        int minY = Math.min(CurrentMoshPitLoc1.getBlockY(), CurrentMoshPitLoc2.getBlockY());
        int maxY = Math.max(CurrentMoshPitLoc1.getBlockY(), CurrentMoshPitLoc2.getBlockY());

        int minZ = Math.min(CurrentMoshPitLoc1.getBlockZ(), CurrentMoshPitLoc2.getBlockZ());
        int maxZ = Math.max(CurrentMoshPitLoc1.getBlockZ(), CurrentMoshPitLoc2.getBlockZ());

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    };


    public static void setCuboid(World world, Location pos1, Location pos2, Material material) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
    }


    public static void setCylinder(
            World world,
            Location center,
            int radius,
            int minY,
            int maxY,
            Material material,
            boolean solid
    ) {
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int rSquared = radius * radius;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int dist = x * x + z * z;

                if (dist > rSquared) continue;

                // Hollow check (walls only)
                if (!solid) {
                    int innerRadius = radius - 1;
                    if (dist < innerRadius * innerRadius) continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    world.getBlockAt(cx + x, y, cz + z).setType(material, false);
                }
            }
        }
    }
}
