package me.sfoow.prokits.Ect;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sfoow.prokits.Data.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.sfoow.prokits.Ect.Afk.PlayerAFKShard;
import static me.sfoow.prokits.Ect.VaultUtils.getBalance;
import static me.sfoow.prokits.event.EventStarter.getNextEventTimer;
import static me.sfoow.prokits.mines.getPitMineTimer;
import static me.sfoow.prokits.mines.getStarterMineTimer;

public class placeholder extends PlaceholderExpansion {

    // This method tells PlaceholderAPI the identifier for your placeholders
    @Override
    public @NotNull String getIdentifier() {
        return "prokits";
    }

    // Author of the expansion
    @Override
    public @NotNull String getAuthor() {
        return "sfoow";
    }

    // Version of your expansion
    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    // This is where the actual placeholder logic happens
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        return switch (identifier) {
            case "event" -> getNextEventTimer();
            case "starter" -> getStarterMineTimer();
            case "mine" -> getPitMineTimer();
            case "kills" -> String.valueOf(PlayerData.PlayerKills.getOrDefault(player.getUniqueId(), 0));
            case "deaths" -> String.valueOf(PlayerData.PlayerDeaths.getOrDefault(player.getUniqueId(), 0));
            case "killstreak" -> String.valueOf(PlayerData.PlayerKillStreak.getOrDefault(player.getUniqueId(), 0));
            case "tokens" -> String.valueOf(getBalance(player));
            case "xp" -> String.valueOf(PlayerData.PlayerXp.getOrDefault(player.getUniqueId(), 0));
            case "xpneed" -> String.valueOf(PlayerData.PlayerXpNeeded.getOrDefault(player.getUniqueId(), 0));
            case "level" -> String.valueOf(PlayerData.PlayerLevels.getOrDefault(player.getUniqueId(), 0));
            case "afk" -> String.valueOf(PlayerAFKShard.getOrDefault(player.getUniqueId(),0));
            default -> null;
        };

    }

    @Override
    public boolean persist() {
        return true;
    }


    public static String getPlaceholder(Player player, String placeholder) {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }
}
