package me.sfoow.prokits;

import me.sfoow.prokits.Data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class LevelSystem {

    private static Random rand = new Random();

    public static void DoPlayerLevelUpCheck(Player player){
        int xp = PlayerData.PlayerXp.get(player.getUniqueId());
        int xpneeded = PlayerData.PlayerXpNeeded.get(player.getUniqueId());
        int level = PlayerData.PlayerLevels.get(player.getUniqueId());
        if (xp >= xpneeded){
            UUID uuid = player.getUniqueId();
            xp = xp - xpneeded;
            level++;

            PlayerData.PlayerXp.put(uuid,xp);
            PlayerData.PlayerLevels.put(uuid,level);
            PlayerData.PlayerXpNeeded.put(uuid,getXpNeededPerLevel(level));

            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE,10,1);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST,10,1);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE,10,1);

            player.sendTitle(ChatColor.translateAlternateColorCodes('&',"&4&lLevel Up!"),ChatColor.translateAlternateColorCodes('&',"&f" + (level - 1) +"ʟᴠ " + "&7\uD83E\uDC1A &f" + level + "ʟᴠ"));
        }
    }


    public static int getXpNeededPerLevel(int level){
        return (int) (Math.pow(level * 1.5, 1.3) + 10);
    }
}
