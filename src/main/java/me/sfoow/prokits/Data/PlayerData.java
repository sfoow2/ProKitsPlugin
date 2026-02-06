package me.sfoow.prokits.Data;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.sfoow.prokits.Ect.Afk.PlayerAFKShard;
import static me.sfoow.prokits.Ect.VaultUtils.setBalance;
import static me.sfoow.prokits.Prokits.plugin;

public class PlayerData {

    private static String DataBasePath = "plugins/prokits/database/";

    public static final Map<UUID, Integer> PlayerDeaths = new HashMap<>();
    public static final Map<UUID, Integer> PlayerKills = new HashMap<>();
    public static final Map<UUID, Integer> PlayerKillStreak = new HashMap<>();
    public static final Map<UUID, Integer> PlayerLevels = new HashMap<>();
    public static final Map<UUID, Integer> PlayerXp = new HashMap<>();
    public static final Map<UUID, Integer> PlayerXpNeeded = new HashMap<>();
    public static final Map<UUID, Integer> PlayerLevelClaim = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaim = new HashMap<>();

    public static final Map<UUID, Integer> PlayerUpgradeLevelTokens = new HashMap<>();
    public static final Map<UUID, Integer> PlayerUpgradeLevelXp = new HashMap<>();
    public static final Map<UUID, Integer> PlayerKitUpgradeLevel = new HashMap<>();

    /*
    Oh yes this will add a lot of overhead but since luc wants to add this at the last second might as well
     */
    public static final Map<UUID, Long> PlayerDailyClaimVip = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimVipp = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimVippp = new HashMap<>();

    public static final Map<UUID, Long> PlayerDailyClaimMvp = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimMvpp = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimMvppp = new HashMap<>();

    public static final Map<UUID, Long> PlayerDailyClaimPro = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimLegend = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimUltimate = new HashMap<>();
    public static final Map<UUID, Long> PlayerDailyClaimCustom = new HashMap<>();


    //PlayerAfkShard is in the afk class

    public static void SavePlayerData(Player player){
        UUID uuid = player.getUniqueId();
        YamlManager data = new YamlManager(DataBasePath + uuid + ".yml");

        data.set("PlayerDeaths", PlayerDeaths.getOrDefault(uuid, 0));
        data.set("PlayerKills", PlayerKills.getOrDefault(uuid, 0));
        data.set("PlayerKillStreak", PlayerKillStreak.getOrDefault(uuid, 0));
        data.set("PlayerLevels", PlayerLevels.getOrDefault(uuid, 0));
        data.set("PlayerXp", PlayerXp.getOrDefault(uuid, 0));
        data.set("PlayerLevelClaim", PlayerLevelClaim.getOrDefault(uuid, 0));
        data.set("PlayerDailyClaim",PlayerDailyClaim.getOrDefault(uuid,0L));

        data.set("PlayerUpgradeLevelTokens", PlayerUpgradeLevelTokens.getOrDefault(uuid, 0));
        data.set("PlayerUpgradeLevelXp", PlayerUpgradeLevelXp.getOrDefault(uuid, 0));
        data.set("PlayerKitUpgradeLevel", PlayerKitUpgradeLevel.getOrDefault(uuid, 0));

        data.set("PlayerDailyClaimVip", PlayerDailyClaimVip.getOrDefault(uuid,0L));
        data.set("PlayerDailyClaimVipp", PlayerDailyClaimVipp.getOrDefault(uuid,0L));
        data.set("PlayerDailyClaimVippp", PlayerDailyClaimVippp.getOrDefault(uuid, 0L));

        data.set("PlayerDailyClaimMvp", PlayerDailyClaimMvp.getOrDefault(uuid, 0L));
        data.set("PlayerDailyClaimMvpp", PlayerDailyClaimMvpp.getOrDefault(uuid, 0L));
        data.set("PlayerDailyClaimMvppp", PlayerDailyClaimMvppp.getOrDefault(uuid, 0L));

        data.set("PlayerDailyClaimPro", PlayerDailyClaimPro.getOrDefault(uuid, 0L));
        data.set("PlayerDailyClaimLegend", PlayerDailyClaimLegend.getOrDefault(uuid, 0L));
        data.set("PlayerDailyClaimUltimate", PlayerDailyClaimUltimate.getOrDefault(uuid, 0L));
        data.set("PlayerDailyClaimCustom", PlayerDailyClaimCustom.getOrDefault(uuid, 0L));

        data.set("PlayerAfkShard",PlayerAFKShard.getOrDefault(uuid,0));

        data.save();
    }

    public static void UnLoadPlayerData(Player player){
        UUID uuid = player.getUniqueId();
        PlayerUpgradeLevelTokens.remove(uuid);
        PlayerUpgradeLevelXp.remove(uuid);
        PlayerKitUpgradeLevel.remove(uuid);

        PlayerDeaths.remove(uuid);
        PlayerKills.remove(uuid);
        PlayerKillStreak.remove(uuid);
        PlayerLevels.remove(uuid);

        PlayerXp.remove(uuid);
        PlayerXpNeeded.remove(uuid);
        PlayerLevelClaim.remove(uuid);
        PlayerDailyClaim.remove(uuid);

        PlayerDailyClaimVip.remove(uuid);
        PlayerDailyClaimVipp.remove(uuid);
        PlayerDailyClaimVippp.remove(uuid);

        PlayerDailyClaimMvp.remove(uuid);
        PlayerDailyClaimMvpp.remove(uuid);
        PlayerDailyClaimMvppp.remove(uuid);

        PlayerDailyClaimPro.remove(uuid);
        PlayerDailyClaimLegend.remove(uuid);
        PlayerDailyClaimUltimate.remove(uuid);
        PlayerDailyClaimCustom.remove(uuid);

        PlayerAFKShard.remove(uuid);

    }

    public static void LoadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        YamlManager data = new YamlManager(DataBasePath + uuid + ".yml");

        int deaths = data.getIntOrDefault("PlayerDeaths", 0);
        int kills  = data.getIntOrDefault("PlayerKills", 0);
        int streak = data.getIntOrDefault("PlayerKillStreak", 0);
        int levels = data.getIntOrDefault("PlayerLevels", 0);

        int tokens = data.getIntOrDefault("PlayerTokens", 0);

        int xp = data.getIntOrDefault("PlayerXp", 0);
        int levelclaim = data.getIntOrDefault("PlayerLevelClaim",0);
        long daily = data.getIntOrDefault("PlayerDailyClaim",0);
        int uplevel = data.getIntOrDefault("PlayerUpgradeLevelTokens",0);
        int upxp = data.getIntOrDefault("PlayerUpgradeLevelXp",0);
        int upkits = data.getIntOrDefault("PlayerKitUpgradeLevel",0);


        long dailyvip = data.getIntOrDefault("PlayerDailyClaimVip", 0);
        long dailyvipp = data.getIntOrDefault("PlayerDailyClaimVipp", 0);
        long dailyvippp = data.getIntOrDefault("PlayerDailyClaimVippp", 0);

        long dailymvp = data.getIntOrDefault("PlayerDailyClaimMvp", 0);
        long dailymvpp = data.getIntOrDefault("PlayerDailyClaimMvpp", 0);
        long dailymvppp = data.getIntOrDefault("PlayerDailyClaimMvppp", 0);

        long dailypro = data.getIntOrDefault("PlayerDailyClaimPro", 0);
        long dailylegend = data.getIntOrDefault("PlayerDailyClaimLegend", 0);
        long dailyultimate = data.getIntOrDefault("PlayerDailyClaimUltimate", 0);
        long dailycustom = data.getIntOrDefault("PlayerDailyClaimCustom", 0);

        int afkshard = data.getIntOrDefault("PlayerAfkShard",0);

        PlayerDeaths.put(uuid, deaths);
        PlayerKills.put(uuid, kills);
        PlayerKillStreak.put(uuid, streak);
        PlayerLevels.put(uuid, levels);

        if (tokens != 0){
            setBalance(player,tokens);
            data.set("PlayerTokens", 0);
        }

        PlayerXp.put(uuid,xp);
        PlayerLevelClaim.put(uuid,levelclaim);
        PlayerDailyClaim.put(uuid,daily);
        PlayerUpgradeLevelTokens.put(uuid,uplevel);
        PlayerUpgradeLevelXp.put(uuid,upxp);
        PlayerKitUpgradeLevel.put(uuid,upkits);

        PlayerDailyClaimVip.put(uuid,dailyvip);
        PlayerDailyClaimVipp.put(uuid,dailyvipp);
        PlayerDailyClaimVippp.put(uuid,dailyvippp);

        PlayerDailyClaimMvp.put(uuid,dailymvp);
        PlayerDailyClaimMvpp.put(uuid,dailymvpp);
        PlayerDailyClaimMvppp.put(uuid,dailymvppp);

        PlayerDailyClaimPro.put(uuid,dailypro);
        PlayerDailyClaimLegend.put(uuid,dailylegend);
        PlayerDailyClaimUltimate.put(uuid,dailyultimate);
        PlayerDailyClaimCustom.put(uuid,dailycustom);

        PlayerAFKShard.put(uuid,afkshard);

        // Ensure file has defaults written (first join or missing keys)
        data.set("PlayerDeaths", deaths);
        data.set("PlayerKills", kills);
        data.set("PlayerKillStreak", streak);
        data.set("PlayerLevels", levels);

        data.set("PlayerXp", xp);
        data.set("PlayerLevelClaim",levelclaim);
        data.set("PlayerDailyClaim",daily);
        data.set("PlayerUpgradeLevelTokens",uplevel);
        data.set("PlayerUpgradeLevelXp",upxp);
        data.set("PlayerKitUpgradeLevel",upkits);

        data.set("PlayerDailyClaimVip",dailyvip);
        data.set("PlayerDailyClaimVipp",dailyvipp);
        data.set("PlayerDailyClaimVippp",dailyvippp);
        data.set("PlayerDailyClaimMvp",dailymvp);
        data.set("PlayerDailyClaimMvpp",dailymvpp);
        data.set("PlayerDailyClaimMvppp",dailymvppp);
        data.set("PlayerDailyClaimPro",dailypro);
        data.set("PlayerDailyClaimLegend",dailylegend);
        data.set("PlayerDailyClaimUltimate",dailyultimate);
        data.set("PlayerDailyClaimCustom",dailycustom);

        data.set("PlayerAfkShard",afkshard);

        data.save();
    }

    public static void DoBackUpLoop(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Bukkit.getLogger().info("Doing A Player Backup");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SavePlayerData(player);
                }
            }
        },6000L,6000L);
    }
}
