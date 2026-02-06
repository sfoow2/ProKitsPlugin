package me.sfoow.prokits.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.event.CustomItemsDrop.CurrentItemDropTimer;
import static me.sfoow.prokits.event.CustomItemsDrop.StartItemDropEvent;
import static me.sfoow.prokits.event.MoshPit.*;


public class EventStarter {

    private static final Random rand = new Random();

    public static long NextEventTimer;
    public static byte CurrentEvent = -1;
    public static String CurrentEventTitle;

    public static void DoRandomEventLoops(){
        NextEventTimer = getUnix() + 1900L;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            TryToStartNewEvent();
            NextEventTimer = getUnix() + 1900L;
        },1900*20L,1900*20L);

    }

    @Command("randomevent")
    @CommandPermission("op")
    public void StartRandomEvent(){
        TryToStartNewEvent();
        NextEventTimer = getUnix() + 1900L;
    }

    private static void TryToStartNewEvent(){
        if (CurrentEvent == -1) {
            int randomint = rand.nextInt(0,1);
            if (randomint == 0){
                StartMoshEvent();
            } else if (randomint == 1){
                StartItemDropEvent();
            }

        } else {
            Bukkit.getLogger().severe("Somehow there is an event going on?? = " + CurrentEvent);
        }
    }

    public static void TryToStopCurrentEvent(){
        if (CurrentEvent != -1){
            if (CurrentEvent == 1){
                Endmoshevent();
            }
        }
    }

    static long getUnix(){
        return (System.currentTimeMillis() / 1000);
    }

    public static String getNextEventTimer(){
        if (CurrentEvent == -1) {
            long remaining = getUnix() - NextEventTimer;

            long minutes = TimeUnit.SECONDS.toMinutes(remaining) % 60;
            return String.format("%02dm", Math.abs(minutes));
        } else {
            return getEventFormat();
        }
    }

    private static String getEventFormat(){
        if (CurrentEvent == 1) {
            long remaining = getUnix() - CurrentMoshPitTimer;
            long seconds = TimeUnit.SECONDS.toSeconds(remaining);
            return CurrentEventTitle + " &7" + Math.abs(seconds) + "s";
        } else if (CurrentEvent == 2){
            long remaining = getUnix() - CurrentItemDropTimer;
            long seconds = TimeUnit.SECONDS.toSeconds(remaining);
            return CurrentEventTitle + " &7" + Math.abs(seconds) + "s";
        }
        return "";
    }



}
