package me.sfoow.prokits.Ect;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.sfoow.prokits.Data.PlayerData.*;
import static me.sfoow.prokits.Ect.VaultUtils.addMoney;
import static me.sfoow.prokits.Ect.utils.*;

public class LevelsGui implements Listener {

    private static final int LEVELS_PER_PAGE = 28;
    private static final int MAX_PAGES = 15;

    @Command("levels")
    public void LevelsCommand(Player sender) {
        OpenLevelsGui(sender, 1);
    }

    private static void OpenLevelsGui(Player player, int page) {
        if (page < 1) page = 1;
        if (page > MAX_PAGES) page = MAX_PAGES;

        Inventory inv = getBasicInventory("&8Level Page " + page, (byte) 6);

        int startLevel = (page - 1) * LEVELS_PER_PAGE + 1;
        int level = startLevel;

        // Main claim area
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                int slot = row * 9 + col;
                inv.setItem(slot, getLevelItem(player, level));
                level++;
            }
        }

        // Previous page button
        if (page > 1) {
            inv.setItem(45, createNavItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }

        // Next page button
        if (page < MAX_PAGES) {
            inv.setItem(53, createNavItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        player.openInventory(inv);
    }

    private static ItemStack createNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Level Page ")) return;

        event.setCancelled(true);

        int page = Integer.parseInt(title.replace("Level Page ", ""));
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();

        // Page navigation
        if (meta.hasDisplayName()) {
            String name = ChatColor.stripColor(meta.getDisplayName());

            if (name.equalsIgnoreCase("Previous Page")) {
                OpenLevelsGui(player, page - 1);
                return;
            }

            if (name.equalsIgnoreCase("Next Page")) {
                OpenLevelsGui(player, page + 1);
                return;
            }
        }

        // Level claiming
        if (!meta.hasDisplayName()) return;
        String name = ChatColor.stripColor(meta.getDisplayName());
        if (!name.startsWith("Level ")) return;

        int level = Integer.parseInt(name.replace("Level ", ""));
        int playerLevel = PlayerLevels.getOrDefault(player.getUniqueId(), 0);
        int claimedLevel = PlayerLevelClaim.getOrDefault(player.getUniqueId(), 0);

        if (level > playerLevel) {
            Qwerty(player,ChatColor.RED + "You are not high enough level.");
            SendNo(player);
            return;
        }

        if (level != claimedLevel + 1) {
            Qwerty(player,ChatColor.RED + "You must claim previous levels first.");
            SendNo(player);
            return;
        }

        int reward = getMoneyRewardPerLevel(level);
        addMoney(player, reward);

        PlayerLevelClaim.put(player.getUniqueId(), level);
        Qwerty(player,ChatColor.GREEN + "Claimed level " + level + "! +" + reward + " tokens");
        SendYes(player);
        OpenLevelsGui(player, page);
    }

    private static ItemStack getLevelItem(Player player, int level) {
        int playerLevel = PlayerLevels.getOrDefault(player.getUniqueId(), 0);
        int claimedLevel = PlayerLevelClaim.getOrDefault(player.getUniqueId(), 0);

        ItemStack item;
        String name;
        ChatColor color;

        if (level > playerLevel) {
            item = new ItemStack(Material.BARRIER);
            color = ChatColor.DARK_RED;
        } else if (level == claimedLevel + 1) {
            item = new ItemStack(Material.CHEST_MINECART);
            color = ChatColor.GREEN;
        } else if (level <= claimedLevel) {
            item = new ItemStack(Material.MINECART);
            color = ChatColor.GRAY;
        } else {
            item = new ItemStack(Material.CHEST_MINECART);
            color = ChatColor.RED;
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "Level " + level);

        meta.setLore(java.util.List.of(
                ChatColor.GRAY + "Reward: " + ChatColor.GOLD + getMoneyRewardPerLevel(level) + " tokens",
                "",
                level > playerLevel
                        ? ChatColor.RED + "Locked"
                        : level == claimedLevel + 1
                        ? ChatColor.GREEN + "Click to claim"
                        : level <= claimedLevel
                        ? ChatColor.GRAY + "Already claimed"
                        : ChatColor.RED + "Claim previous levels first"
        ));


        item.setItemMeta(meta);
        return item;
    }

    private static int getMoneyRewardPerLevel(int level) {
        if (level > 100) level = 100;
        return (int) ((level * 5) + 80);
    }
}
