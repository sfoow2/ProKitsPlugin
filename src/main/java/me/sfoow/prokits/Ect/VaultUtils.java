package me.sfoow.prokits.Ect;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtils {

    private static Economy economy = null;

    /** Call this in onEnable() */
    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    /* =========================
       Economy helper methods
       ========================= */

    public static double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public static void setBalance(Player player, double amount) {
        double current = economy.getBalance(player);
        double diff = amount - current;

        if (diff > 0) {
            economy.depositPlayer(player, diff);
        } else if (diff < 0) {
            economy.withdrawPlayer(player, Math.abs(diff));
        }
    }

    public static void addMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public static void removeMoney(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    public static boolean hasMoney(Player player, double amount) {
        return economy.has(player, amount);
    }
}
