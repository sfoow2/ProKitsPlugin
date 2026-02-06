package me.sfoow.prokits.event;

import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static me.sfoow.prokits.Ect.RandomKit.executeServerItem;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Ect.utils.Broadcast;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.event.EventStarter.*;

public class CustomItemsDrop {

    private static final byte YCord = 4;

    private static final Location pos1 = new Location(Bukkit.getWorld("spawn"),264.5,5,264.5);
    private static final Location pos2 = new Location(Bukkit.getWorld("spawn"),-14.5,4,-14.5);

    private static final Random rand = new Random();

    public static long CurrentItemDropTimer;


    private static BukkitRunnable eventTask;

    @Command("dodrops")
    public void StartDropEvent(){
        StartItemDropEvent();
    }


    private static void DoCustomItemDropTick() {
        if (eventTask != null) eventTask.cancel();

        eventTask = new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                seconds++;

                if (CurrentEvent == -1) {
                    cancel();
                    return;
                }

                Location loc = pos2.clone().add(getNewRandomLoc());
                loc.setY(4);
                loc.setWorld(Bukkit.getWorld("spawn"));
                DoNewCustomItemDrop(loc);


                if (seconds >= 200) {
                    EndCustomDropEvent();
                    cancel();
                }
            }
        };

        eventTask.runTaskTimer(plugin, 0L, 20L);
    }


    public static void EndCustomDropEvent(){
        NextEventTimer = getUnix() + 1860L;
        CurrentEvent = -1;
        CurrentEventTitle = "";

    }

    public static void StartItemDropEvent(){
        CurrentEvent = 2;
        CurrentItemDropTimer = getUnix() + 200;
        NextEventTimer = -1;
        CurrentEventTitle = "&aCustom Item Drops";

        Broadcast("");
        Broadcast(ServerPrefixFront + "&f&lActivating random custom item drops!");
        Broadcast(ServerPrefixFront + "&7Custom items will drop around the map go grab them!");
        Broadcast("");

        DoCustomItemDropTick();
    }

    private static void DoNewCustomItemDrop(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        loc.setY(3);

        Location center = loc.clone();
        Location itemLoc = loc.clone().add(0, 3, 0);

        // ---- Spawn fake item (SYNC) ----
        ItemStack itemrew = new ItemStack(getRandomCustomItem());
        Item item = world.dropItem(itemLoc, itemrew);

        item.setGravity(false);
        item.setGlowing(true);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setCanPlayerPickup(false);
        item.setUnlimitedLifetime(true);
        item.setVelocity(new Vector(0, 0, 0));

        // Tag for safety / identification
        item.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "fake_drop"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        // ---- Runnable controller ----
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                // ---- Particles every 5 ticks ----
                if (ticks % 5 == 0) {
                    spawnGreenCircle(center, 3);
                }

                // ---- Player checks every 30 ticks (1.5s) ----
                if (ticks % 30 == 0) {
                    checkPlayersAsync(center, item, this);
                }

                // ---- Timeout ----
                if (ticks >= 2000) {
                    cleanup(item);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnGreenCircle(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;

        Location base = center.clone().add(0, 1.2, 0);

        int points = 40;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            world.spawnParticle(
                    Particle.HAPPY_VILLAGER,
                    base.clone().add(x, 0, z),
                    1,
                    0, 0, 0,
                    0
            );
        }
    }


    private static void checkPlayersAsync(Location center, Item item, BukkitRunnable task) {
        // Cache player data SYNC
        List<PlayerData> players = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().getName().equals("spawn")) continue;
            if (p.getLocation().getBlockY() != 4) continue;

            players.add(new PlayerData(
                    p.getUniqueId(),
                    p.getLocation().toVector()
            ));
        }

        Vector centerVec = center.toVector();

        // Async math
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (PlayerData data : players) {
                if (data.pos.distanceSquared(centerVec) <= 9) { // 3^2
                    // Back to sync for reward & cleanup
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player p = Bukkit.getPlayer(data.uuid);
                        if (p == null) return;

                        ItemStack rew = item.getItemStack();

                        givePlayerRandomItemFromMat(p, rew.getType());
                        cleanup(item);
                        task.cancel();
                    });
                    return;
                }
            }
        });
    }

    private static void cleanup(Item item) {
        if (item != null && !item.isDead()) {
            item.remove();
        }
    }


    private static class PlayerData {
        UUID uuid;
        Vector pos;

        PlayerData(UUID uuid, Vector pos) {
            this.uuid = uuid;
            this.pos = pos;
        }
    }




    private static Vector getNewRandomLoc() {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());

        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        double x = minX + (rand.nextDouble() * (maxX - minX));
        double z = minZ + (rand.nextDouble() * (maxZ - minZ));

        Vector v = new Vector(x, 0, z);

        return v;
    }


    private static Material getRandomCustomItem(){
        int randit = rand.nextInt(1,9);

        switch (randit){
            case 1 -> {
                return Material.BONE;
            }
            case 2 -> {
                return Material.EGG;
            }
            case 3 -> {
                return Material.RED_CANDLE;
            }
            case 4 -> {
                return Material.BLUE_ICE;
            }
            case 5 -> {
                return Material.FEATHER;
            }
            case 6 -> {
                return Material.CHORUS_FLOWER;
            }
            case 7 -> {
                return Material.SUGAR;
            }
            case 8 -> {
                return Material.FIRE_CHARGE;
            }
            case 9 -> {
                return Material.FISHING_ROD;
            }
            case 10 -> {
                return Material.BREEZE_ROD;
            }
            case 11 -> {
                return Material.SNOWBALL;
            }

        }

        return Material.AIR;
    }


    private static void givePlayerRandomItemFromMat(Player player, Material mat){

        switch (mat) {
            case Material.BONE -> executeServerItem(player, "antit1runner");
            case Material.EGG -> executeServerItem(player, "bridgebuilder");
            case Material.RED_CANDLE -> executeServerItem(player, "dynamite");
            case Material.BLUE_ICE -> executeServerItem(player, "icebuble");
            case Material.FEATHER -> executeServerItem(player, "lightasfeather");
            case Material.CHORUS_FLOWER -> executeServerItem(player, "magicfruit");
            case Material.SUGAR -> executeServerItem(player, "speedpowder");
            case Material.FIRE_CHARGE -> executeServerItem(player, "fireball");
            case Material.FISHING_ROD -> executeServerItem(player, "graplehook");
            case Material.BREEZE_ROD -> executeServerItem(player, "iciclewand");
            case Material.SNOWBALL -> executeServerItem(player, "sawper");
        }

    }


}
