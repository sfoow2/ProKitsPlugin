package me.sfoow.prokits.Ect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Suggest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.sfoow.prokits.Data.PlayerData.*;
import static me.sfoow.prokits.Ect.utils.*;

public class Daily {

    private static final long COOLDOWN_SECONDS = 12 * 60 * 60; // 16 hours

    private static void givePlayerCrateKey(Player player, String st){
        st = st.replace('+','p');
        String cmd = "crates key give " + player.getName() + " " + st + " 1";
        runServerCommand(cmd);
    }


    @Command("daily")
    public void DailyCommand(Player sender, @Suggest({"custom","ultimate","legend","pro","mvp++","mvp+","mvp","vip++","vip+","vip"}) @Optional String Claim) {
        String whatclaim;
        if (Claim == null){
            whatclaim = "daily";
        } else {
            whatclaim = Claim;
        }
        boolean found = false;
        for (String st: new String[]{"custom","daily","ultimate", "legend", "pro", "mvp++", "mvp+", "mvp", "vip++", "vip+", "vip"}){
            if (st.equals(whatclaim)){
                found = true;
                break;
            }
        }
        if (!found){
            SendNo(sender);
            Qwerty(sender,"&cInvalid Daily!");
            return;
        }
        if (!sender.hasPermission(whatclaim)){
            SendNo(sender);
            Qwerty(sender,"&cInvalid Permission!");
            return;
        }

        long now = getUnixTime();
        long lastClaim = getLastPickBasedOnInput(sender,whatclaim);
        if (lastClaim == -1){
            return;
        }
        long timeSinceLast = now - lastClaim;

        if (timeSinceLast >= COOLDOWN_SECONDS) {
            givePlayerCrateKey(sender,whatclaim);
            putnowaslast(sender,whatclaim);
            Qwerty(sender,"&aYou have claimed your daily reward!");
            SendYes(sender);
        } else {
            long remaining = COOLDOWN_SECONDS - timeSinceLast;

            long hours = TimeUnit.SECONDS.toHours(remaining);
            long minutes = TimeUnit.SECONDS.toMinutes(remaining) % 60;
            long seconds = remaining % 60;

            Qwerty(sender,String.format(
                    "&cYou can claim your daily reward in %02d:%02d:%02d (hh:mm:ss)",
                    hours, minutes, seconds
            ));
            SendNo(sender);

        }
    }

    private static long getLastPickBasedOnInput(Player player, String st){
        UUID uuid = player.getUniqueId();
        Long value = null;

        switch (st){
            case "daily": value = PlayerDailyClaim.get(uuid); break;
            case "vip": value = PlayerDailyClaimVip.get(uuid); break;
            case "vip+": value = PlayerDailyClaimVipp.get(uuid); break;
            case "vip++": value = PlayerDailyClaimVippp.get(uuid); break;
            case "mvp": value = PlayerDailyClaimMvp.get(uuid); break;
            case "mvp+": value = PlayerDailyClaimMvpp.get(uuid); break;
            case "mvp++": value = PlayerDailyClaimMvppp.get(uuid); break;
            case "pro": value = PlayerDailyClaimPro.get(uuid); break;
            case "legend": value = PlayerDailyClaimLegend.get(uuid); break;
            case "ultimate": value = PlayerDailyClaimUltimate.get(uuid); break;
            case "custom": value = PlayerDailyClaimCustom.get(uuid); break;
        }

        // NEVER claimed before → allow claim
        return value == null ? 0L : value;
    }


    private static long putnowaslast(Player player, String st){
        UUID uuid = player.getUniqueId();
        switch (st){
            case "daily":
                return PlayerDailyClaim.put(uuid,getUnixTime());
            case "vip":
                return PlayerDailyClaimVip.put(uuid,getUnixTime());
            case "vip+":
                return PlayerDailyClaimVipp.put(uuid,getUnixTime());
            case "vip++":
                return PlayerDailyClaimVippp.put(uuid,getUnixTime());
            case "mvp":
                return PlayerDailyClaimMvp.put(uuid,getUnixTime());
            case "mvp+":
                return PlayerDailyClaimMvpp.put(uuid,getUnixTime());
            case "mvp++":
                return PlayerDailyClaimMvppp.put(uuid,getUnixTime());
            case "pro":
                return PlayerDailyClaimPro.put(uuid,getUnixTime());
            case "legend":
                return PlayerDailyClaimLegend.put(uuid,getUnixTime());
            case "ultimate":
                return PlayerDailyClaimUltimate.put(uuid,getUnixTime());
            case "custom":
                return PlayerDailyClaimCustom.put(uuid,getUnixTime());

        }
        return -1L;
    }

    private static long getUnixTime() {
        return (System.currentTimeMillis() / 1000);
    }

    private static void runServerCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

}
