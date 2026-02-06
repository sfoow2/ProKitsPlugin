package me.sfoow.prokits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;

import static me.sfoow.prokits.Ect.ShulkerRooms.removePlayerFromRoom;
import static me.sfoow.prokits.Ect.placeholder.getPlaceholder;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;
import static me.sfoow.prokits.server.SpawnLocation;
import static me.sfoow.prokits.Ect.utils.*;

public class spawn {

    private static Location AfkArea = new Location(Bukkit.getWorld("spawn"),-139.5,109,59.5);
    private static Location KothArea = new Location(Bukkit.getWorld("spawn"),-341.5,111,-18.5);
    private static Location CrateArea = new Location(Bukkit.getWorld("spawn"),167.5,73,124.5);

    @Command("spawn")
    @Cooldown(10L)
    public void SpawnCommand(Player sender) {

        if (isInRegion(sender.getLocation(), "spawn") || isInRegion(sender.getLocation(), "afk") || sender.getWorld().getName().equals("plots")) {
            sender.teleport(SpawnLocation);
            SendYes(sender);
            Qwerty(sender, "&aSending You To Spawn!");
            return;
        }

        if (isInRegion(sender.getLocation(),"shulkeroutside")){
            removePlayerFromRoom(sender);
            SendYes(sender);
            sender.teleport(SpawnLocation);
            return;
        }

        if (sender.getWorld().getName().equals("paidrank")){
            SendYes(sender);
            sender.teleport(SpawnLocation);
            Qwerty(sender, "&aSending You To Spawn!");
            return;
        }

        Vector startLocation = new Vector(Math.floor(sender.getX()),Math.floor(sender.getY()),Math.floor(sender.getZ()));

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {

                if (!startLocation.equals(new Vector(Math.floor(sender.getX()),Math.floor(sender.getY()),Math.floor(sender.getZ())))) {
                    SendNo(sender);
                    Qwerty(sender, "&cStopping The Teleport Since You Moved!");
                    cancel();
                    return;
                } else {
                    Qwerty(sender,"&aSending You To Spawn In: &f" + (5 - ticks) + "s");
                }

                if (!getPlaceholder(sender,"%combatlogx_time_left%").equals("0")){
                    SendNo(sender);
                    Qwerty(sender, "&cStopping The Teleport Since You Moved!");
                    cancel();
                    return;
                }

                ticks++;

                if (ticks >= 5) {
                    sender.teleport(SpawnLocation);
                    SendYes(sender);
                    Qwerty(sender, "&aSending You To Spawn!");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    @Command("afk")
    @Cooldown(3L)
    public void AfkCommand(Player sender) {

        if (isInRegion(sender.getLocation(), "spawn")) {
            sender.teleport(AfkArea);
            SendYes(sender);
            Qwerty(sender, "&aSending You To AFK Area!");
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou can only do this in spawn!");
        }
    }

    @Command("Kothtp")
    @Cooldown(3L)
    public void KothCommand(Player sender) {
        if (isInRegion(sender.getLocation(), "spawn")) {
            sender.teleport(KothArea);
            SendYes(sender);
            Qwerty(sender, "&aSending You To Koth Area!");
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou can only do this in spawn!");
        }
    }

    @Command("crate")
    @Cooldown(3L)
    public void CrateCommand(Player sender) {
        if (isInRegion(sender.getLocation(), "spawn")) {
            sender.teleport(CrateArea);
            SendYes(sender);
            Qwerty(sender, "&aSending You To Crate Area!");
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou can only do this in spawn!");
        }
    }




}
