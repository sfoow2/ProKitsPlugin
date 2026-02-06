package me.sfoow.prokits;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.util.*;

import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Prokits.plugin;

public class PaidRanks implements Listener {

    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Rank Islands";
    public static HashMap<String, Location> IslandTeleportLocations = new HashMap<>();

    private static final String[] RANKS = {
            "vip", "vip+", "vip++",
            "mvp", "mvp+", "mvp++",
            "pro", "legend", "ultimate"
    };

    /* ================= COMMAND ================= */

    @Command("ranktp")
    public void openRankGui(Player player) {
        player.openInventory(createGui(player));
    }

    /* ================= GUI ================= */

    private Inventory createGui(Player player) {
        Inventory inv = getBasicInventory(GUI_TITLE,4);

        int slot = 10;
        for (String rank : RANKS) {
            inv.setItem(slot, createRankItem(player, rank));
            slot++;
            if (slot == 17) slot = 19; // skip middle gap
        }

        return inv;
    }

    private static Material getMatFromRank(String rank){
        switch (rank){
            case "vip":
                return Material.EMERALD_ORE;
            case "vip+":
                return Material.EMERALD;
            case "vip++":
                return Material.EMERALD_BLOCK;
            case "mvp":
                return Material.DIAMOND_ORE;
            case "mvp+":
                return Material.DIAMOND;
            case "mvp++":
                return Material.DIAMOND_BLOCK;

            case "pro":
                return Material.GOLDEN_APPLE;
            case "legend":
                return Material.ENCHANTED_GOLDEN_APPLE;
            case "ultimate":
                return Material.NETHER_STAR;

        }
        return null;
    }


    private ItemStack createRankItem(Player player, String rank) {
        Material mat = player.hasPermission(rank) ? getMatFromRank(rank) : Material.REDSTONE_BLOCK;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + capitalize(rank));

        List<String> lore = new ArrayList<>();
        if (player.hasPermission(rank)) {
            lore.add(ChatColor.GREEN + "Click to teleport!");
        } else {
            lore.add(ChatColor.RED + "You do not own this rank.");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /* ================= CLICK HANDLER ================= */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String rank = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase();

        if (!IslandTeleportLocations.containsKey(rank)) return;

        if (!player.hasPermission(rank)) {
            SendNo(player);
            Qwerty(player, "&cYou do not have permission for this island!");
            return;
        }

        if (!isInRegion(player.getLocation(), "spawn") && !player.hasPermission("op")) {
            SendNo(player);
            Qwerty(player, "&cYou can only do this in spawn!");
            return;
        }

        Location loc = IslandTeleportLocations.get(rank);
        if (loc == null) {
            SendNo(player);
            Qwerty(player, "&cTeleport location not set!");
            return;
        }

        SendYes(player);
        Qwerty(player, "&aSending you to " + capitalize(rank) + " Island!");
        player.closeInventory();
        player.teleport(loc.add(new Vector(0.5,0.5,0.5)));
    }

    /* ================= YAML ================= */

    public static void LoadIslandYaml() {
        File file = new File("plugins/prokits/islands.yml");

        if (!file.exists()) {
            YamlManager data = new YamlManager(file.getPath());
            for (String rank : RANKS) {
                data.set(rank + ".x", 0);
                data.set(rank + ".y", 0);
                data.set(rank + ".z", 0);
            }
            data.save();
        }

        YamlManager data = new YamlManager(file.getPath());

        for (String rank : RANKS) {
            int x = data.getInt(rank + ".x");
            int y = data.getInt(rank + ".y");
            int z = data.getInt(rank + ".z");

            Location loc = new Location(Bukkit.getWorld("paidrank"), x, y, z);
            IslandTeleportLocations.put(rank, loc);
        }
    }

    /* ================= UTIL ================= */

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static void AutoReloadPaidRankYaml(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            LoadIslandYaml();
        },6000L,6000L);
    }


}
