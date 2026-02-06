package me.sfoow.prokits.Ect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static me.sfoow.prokits.Prokits.plugin;

public class Walls {

    private static final Set<LocalDate> alreadyRunDays = new HashSet<>();

    public static void startDailyCheck() {
        // Run every 30 seconds
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // UK timezone
            ZoneId ukZone = ZoneId.of("Europe/London");
            LocalDateTime now = LocalDateTime.now(ukZone);

            // Check if it's midnight (00:00)
            if (now.getHour() == 0 && now.getMinute() == 0) {
                LocalDate today = now.toLocalDate();

                // Only run once per day
                if (!alreadyRunDays.contains(today)) {
                    doMidnightAction();
                    alreadyRunDays.add(today);
                }
            } else {
                // Optionally clear past days so memory doesn't grow
                alreadyRunDays.removeIf(date -> date.isBefore(LocalDate.now(ukZone)));
            }

        }, 0L, 20L * 30); // 20 ticks = 1 second, so 30*20 = 30 seconds
    }

    private static void doMidnightAction() {
        FAWEWalls.buildRandomConcreteWall(Bukkit.getWorld("spawn"));
    }

    @Command("newwall")
    @CommandPermission("op")
    public void newwalls(){
        doMidnightAction();
    }

}
