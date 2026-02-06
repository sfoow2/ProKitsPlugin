package me.sfoow.prokits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.tokens.FlowMappingEndToken;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static me.sfoow.prokits.Ect.FaweUtils.setCuboidFast;
import static me.sfoow.prokits.Ect.utils.Broadcast;
import static me.sfoow.prokits.Ect.utils.ServerPrefixFront;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.event.EventStarter.TryToStopCurrentEvent;

public class mines {

    public static long NextMineReset = getUnix() + 300L;
    public static long NextPitMineReset = getUnix() + 900L;


    public static void ResetAllMines(){
        TryToStopCurrentEvent();

        NextPitMineReset = getUnix() + 900L;

        Broadcast("");
        Broadcast(ServerPrefixFront + "&aReseting The Mines");
        Broadcast("");

        runServerCommand("arena reset pit extreme -silent");

        runServerCommand("arena reset mvp extreme -silent");
        runServerCommand("arena reset mvpplus extreme -silent");
        runServerCommand("arena reset mvpplusplus extreme -silent");
        runServerCommand("arena reset vip extreme -silent");
        runServerCommand("arena reset vipplus extreme -silent");
        runServerCommand("arena reset vipplusplus extreme -silent");
        runServerCommand("arena reset legend extreme -silent");
        runServerCommand("arena reset pro extreme -silent");
        runServerCommand("arena reset ultimate extreme -silent");

        //custom ranks VVVV

        runServerCommand("arena reset yapple extreme -silent");
        runServerCommand("arena reset polar extreme -silent");
        runServerCommand("arena reset penguin extreme -silent");

        ResetMainMine();

    }

    public static Material CurrentPitMat;
    private static Random rand = new Random();

    private static Material[] AllPossibleMats = {Material.SANDSTONE,Material.MOSS_BLOCK,Material.GRASS_BLOCK,Material.RED_SANDSTONE,Material.PODZOL};

    private static Material getNextRandomMat(){
        Material mat = AllPossibleMats[rand.nextInt(AllPossibleMats.length)];
        while (mat.equals(CurrentPitMat)){
            mat = AllPossibleMats[rand.nextInt(AllPossibleMats.length)];
        }
        return mat;
    }

    static void ResetMainMine(){

        Location l1 = new Location(Bukkit.getWorld("spawn"),274,-7,-25);
        Location l2 = new Location(Bukkit.getWorld("spawn"),-25,3,274);

        CurrentPitMat = getNextRandomMat();

        setCuboidFast(l1,l2, CurrentPitMat);

        runServerCommand("arena reset pitair extreme -silent");
        runServerCommand("arena reset pitores extreme -silent");
    }


    @Command("resetmine")
    @CommandPermission("op")
    public void resetMine(Player sender){
        ResetAllMines();
    }

    public static void DoSmallMinesReset(){
        runServerCommand("arena reset startermine1 extreme -silent");
        runServerCommand("arena reset startermine2 extreme -silent");
    }

    private static void runServerCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static void deResetMinesLoop(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            ResetAllMines();
        },18000L,18000L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            NextMineReset = getUnix() + 300;
            DoSmallMinesReset();
        },6000L,6000L);

    }

    public static String getStarterMineTimer(){
        long remaining = getUnix() - NextMineReset;

        long hours = TimeUnit.SECONDS.toHours(remaining);
        long minutes = TimeUnit.SECONDS.toMinutes(remaining) % 60;
        long seconds = remaining % 60;
        return String.format("%02dm:%02ds",Math.abs(minutes),Math.abs(seconds));
    }

    public static String getPitMineTimer(){
        long remaining = getUnix() - NextPitMineReset;

        long hours = TimeUnit.SECONDS.toHours(remaining);
        long minutes = TimeUnit.SECONDS.toMinutes(remaining) % 60;
        long seconds = remaining % 60;
        return String.format("%02dm:%02ds",Math.abs(minutes),Math.abs(seconds));
    }

    private static long getUnix(){
        return (System.currentTimeMillis() / 1000);
    }

}
