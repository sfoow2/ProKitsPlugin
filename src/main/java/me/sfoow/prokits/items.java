package me.sfoow.prokits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.ArrayList;

import static me.sfoow.prokits.Ect.utils.*;

public class items {

    public static ItemStack Token;
    public static ItemStack CToken;

    public static void SetupItems(){
        Token = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = Token.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&eToken"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("&8ᴄᴜꜱᴛᴏᴍ ɪᴛᴇᴍ");
        lore.add("");
        lore.add("&fUsed to trade for goods");
        lore.add("");
        lore.add("&7/Bank to deposit");
        for (int x = 0; x < lore.size(); x++){
            lore.set(x,ChatColor.translateAlternateColorCodes('&',lore.get(x)));
        }
        meta.setLore(lore);
        Token.setItemMeta(meta);


        CToken = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta2 = Token.getItemMeta();
        meta2.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&eCompressed Tokens"));
        ArrayList<String> lore2 = new ArrayList<>();
        lore2.add("&8ᴄᴜꜱᴛᴏᴍ ɪᴛᴇᴍ");
        lore2.add("");
        lore2.add("&fUsed to trade for goods");
        lore2.add("");
        lore2.add("&7/Bank to deposit");
        for (int x = 0; x < lore2.size(); x++){
            lore2.set(x,ChatColor.translateAlternateColorCodes('&',lore.get(x)));
        }
        meta2.setLore(lore);
        CToken.setItemMeta(meta2);

    }

    @Command("getitem")
    @CommandPermission("op")
    public void getitem(Player sender, String itemname) {
        ItemStack item = null;

        switch (itemname.toLowerCase()) {
            case "token":
                item = Token.clone();
                break;
            case "ctoken":
                item = CToken.clone();
                break;
            case "sfoow":
                item = new ItemStack(Material.STICK);
                break;
        }

        if (item != null) {
            sender.getInventory().addItem(item);
            SendYes(sender);
            Qwerty(sender, "&aAdded item to inventory");
        } else {
            Qwerty(sender, "&cInvalid Item");
            SendNo(sender);
        }
    }


}
