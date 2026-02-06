package me.sfoow.prokits.Ect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class utils {

    private static final String ServerPrefix = "&x&F&F&0&0&0&0&lPROKITS";
    public static final String ServerPrefixFront = "&x&F&F&0&0&0&0&lPROKITS &8» &f";

    public static void Broadcast(String message) {
        String colored = ChatColor.translateAlternateColorCodes('&', message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(colored);
        }
    }

    public static void Qwerty(Player player, String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', ServerPrefix + " &8» &f" + message));
    }

    public static void SendYes(Player player){
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 10,1);
    }

    public static void SendNo(Player player){
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 10,1);
    }

    public static Inventory getBasicInventory(String name, int rows) {
        Inventory inv = Bukkit.createInventory(
                null,
                rows * 9,
                ChatColor.translateAlternateColorCodes('&', name)
        );

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        ItemStack inner = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta innerMeta = inner.getItemMeta();
        innerMeta.setDisplayName(" ");
        inner.setItemMeta(innerMeta);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row * 9 + col;

                boolean isBorder =
                        row == 0 ||
                                row == rows - 1 ||
                                col == 0 ||
                                col == 8;

                inv.setItem(slot, isBorder ? border : inner);
            }
        }

        return inv;
    }



}
