package me.sfoow.prokits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static me.sfoow.prokits.Ect.utils.*;

public class settings implements Listener {

    public static HashMap<UUID,Boolean> PublicChatSettings = new HashMap<>();
    public static HashMap<UUID,Boolean> PrivateChatSettings = new HashMap<>();
    public static HashMap<UUID,Boolean> PaymentsSettings = new HashMap<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        PublicChatSettings.remove(uuid);
        PrivateChatSettings.remove(uuid);
        PaymentsSettings.remove(uuid);
    }

    @Command("settings")
    public void SettingsCommand(Player sender){
        OpenSettingsGui(sender);
    }
    private static void OpenSettingsGui(Player player){
        Inventory inv = getBasicInventory("&8Settings",3);
        UUID uuid = player.getUniqueId();
        inv.setItem(12,createItem(Material.OAK_SIGN,"&aᴘᴜʙʟɪᴄ ᴄʜᴀᴛ", "&fDisables and enables public message","",getYesOrNo(PublicChatSettings.getOrDefault(uuid,false))));
        inv.setItem(13,createItem(Material.OAK_SIGN,"&aᴘʀɪᴠᴀᴛᴇ ᴄʜᴀᴛ", "&fDisables and enables private message","",getYesOrNo(PrivateChatSettings.getOrDefault(uuid,false))));
        inv.setItem(14,createItem(Material.OAK_SIGN,"&aᴘᴀʏᴍᴇɴᴛꜱ", "&fDisables and enables payments","",getYesOrNo(PaymentsSettings.getOrDefault(uuid,false))));

        player.openInventory(inv);
    }

    private static String getYesOrNo(boolean bol){
        if (bol){
            return "&cDisabled";
        } else {
            return "&aEnabled";
        }
    }

    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.stream(lore)
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList());

        item.setItemMeta(meta);
        return item;
    }


    @EventHandler
    public void onSettingsClicked(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',"&8Settings"))) return;

        e.setCancelled(true);

        switch (e.getRawSlot()) {
            case 12 -> TogglePublicChat(p);
            case 13 -> TogglePrivateChat(p);
            case 14 -> TooglePayments(p);
        }

        OpenSettingsGui(p);
    }

    private static void TogglePublicChat(Player player){
        UUID uuid = player.getUniqueId();
        if (!PublicChatSettings.containsKey(uuid)){
            PublicChatSettings.put(uuid,true);
            SendNo(player);
        } else if (PublicChatSettings.get(uuid) == false){
            PublicChatSettings.put(uuid,true);
            SendYes(player);
        } else {
            PublicChatSettings.put(uuid,false);
            SendNo(player);
        }
    }

    private static void TogglePrivateChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PrivateChatSettings.containsKey(player.getUniqueId())){
            PrivateChatSettings.put(uuid,true);
            SendNo(player);
        } else if (PrivateChatSettings.get(uuid) == false){
            PrivateChatSettings.put(uuid,true);
            SendYes(player);
        } else {
            PrivateChatSettings.put(uuid,false);
            SendNo(player);
        }
    }

    private static void TooglePayments(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PaymentsSettings.containsKey(player.getUniqueId())){
            PaymentsSettings.put(uuid,true);
            SendNo(player);
        } else if (PaymentsSettings.get(uuid) == false){
            PaymentsSettings.put(uuid,true);
            SendYes(player);
        } else {
            PaymentsSettings.put(uuid,false);
            SendNo(player);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.getRecipients().removeIf(player -> {
            Boolean setting = PublicChatSettings.get(player.getUniqueId());
            return setting != null && setting; // true = hide chat
        });
    }


    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String msg = event.getMessage().toLowerCase();

        // Commands that send private messages
        if (msg.startsWith("/msg ")
                || msg.startsWith("/tell ")
                || msg.startsWith("/w ")
                || msg.startsWith("/whisper ")) {

            String[] args = event.getMessage().split(" ", 3);
            if (args.length < 3) return;

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) return;

            Boolean disabled = PrivateChatSettings.get(target.getUniqueId());
            if (disabled != null && disabled) {
                sender.sendMessage(ChatColor.RED + "That player has private messages disabled.");
                event.setCancelled(true);
            }
        }
    }


}
