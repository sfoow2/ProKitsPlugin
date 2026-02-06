package me.sfoow.prokits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Optional;

import java.util.UUID;

import static me.sfoow.prokits.Data.PlayerData.*;
import static me.sfoow.prokits.Ect.VaultUtils.getBalance;
import static me.sfoow.prokits.Ect.utils.*;

public class Stats implements Listener {

    @Command("stats")
    @Cooldown(3L)
    public void statsCommand(Player sender, @Optional String targetName) {
        Player target;

        if (targetName == null) {
            // default to sender
            target = sender;
        } else {
            target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                SendMessage(sender, "&cThat player is not online!");
                return;
            }
        }

        UUID uuid = target.getUniqueId();

        int kills = PlayerKills.getOrDefault(uuid, 0);
        int deaths = PlayerDeaths.getOrDefault(uuid, 0);
        int killstreak = PlayerKillStreak.getOrDefault(uuid, 0);

        int tokens = (int) getBalance(target);

        int levels = PlayerLevels.getOrDefault(uuid, 0);

        double kd = deaths == 0 ? kills : (double) kills / deaths;

        SendMessage(sender,"");
        SendMessage(sender,"&eStats of " + target.getName());
        SendMessage(sender,"");
        SendMessage(sender,"&8● &fKills: &c" + kills);
        SendMessage(sender,"&8● &fDeaths: &4" + deaths);
        SendMessage(sender,"&8● &fKd: &a" + String.format("%.2f", kd));
        SendMessage(sender,"&8● &fKillstreak: &e" + killstreak);
        SendMessage(sender,"&8● &fLevels: &e" + levels);
        SendMessage(sender,"&8● &fTokens: &e" + tokens);
        SendMessage(sender,"");
    }


    private static void SendMessage(Player player, String st){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',st));
    }



}
