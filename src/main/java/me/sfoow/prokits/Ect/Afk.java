package me.sfoow.prokits.Ect;

import me.sfoow.prokits.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Suggest;
import revxrsal.commands.annotation.SuggestWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static me.sfoow.prokits.Ect.VaultUtils.addMoney;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;

public class Afk {

    public static HashMap<UUID,Integer> PlayerAFKShard = new HashMap<>();

    private static ItemStack AfkShard;

    public static void DefineAfkShard(){

        AfkShard = new ItemStack(Material.ECHO_SHARD);

        ItemMeta meta = AfkShard.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&x&9&C&2&5&E&B&lAfk Shard"));

        ArrayList<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("&7Used to trade for tokens or afk keys");
        lore.add("");
        lore.add("&8/afk (for trader)");

        ArrayList<String> lore2 = new ArrayList<>(lore.size());

        for (String data : lore){
            lore2.add(ChatColor.translateAlternateColorCodes('&',data));
        }

        meta.setLore(lore2);

        AfkShard.setItemMeta(meta);
    }

    public static void DoAfkLoop(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            for (Player player: Bukkit.getOnlinePlayers()){
                if (isInRegion(player.getLocation(),"afk")){
                    player.sendTitle(ChatColor.translateAlternateColorCodes('&',"&bAFK Reward"),ChatColor.translateAlternateColorCodes('&',"&7+1 Afk Shard"));
                    SendYes(player);
                    PlayerAFKShard.put(player.getUniqueId(), PlayerAFKShard.get(player.getUniqueId()) + 1);
                }
            }
        },1200L,1200L);
    }

    @Command("withdrawafkshard")
    public void withdrawafkshar(Player sender, @Named("amount") @Suggest({"1","10","25"}) int afkshardcount){
        if (afkshardcount == 0){
            SendNo(sender);
            Qwerty(sender,"&cYou cant do that?? (why did you even try)");
        } else if (afkshardcount <= PlayerAFKShard.get(sender.getUniqueId())) {
            if (hasSpaceFor(sender,AfkShard,afkshardcount)){
                giveItem(sender,AfkShard,afkshardcount);
                PlayerAFKShard.put(sender.getUniqueId(),PlayerAFKShard.get(sender.getUniqueId()) - afkshardcount);
                SendYes(sender);
                Qwerty(sender,"&aYou have withdrawn " + afkshardcount + " afk shards!");
            } else {
                Qwerty(sender,"&cYou dont have enough space to do this");
                SendNo(sender);
            }
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou dont have enough afk shards to do this");
            Qwerty(sender,"&cYou currently have: " + PlayerAFKShard.get(sender.getUniqueId()) + " afk shards");
        }
    }

    public static void giveItem(Player player, ItemStack baseItem, int amount) {
        int maxStack = baseItem.getMaxStackSize();

        while (amount > 0) {
            int give = Math.min(amount, maxStack);

            ItemStack stack = baseItem.clone();
            stack.setAmount(give);

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(stack);

            // If inventory is full, drop leftovers safely
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
                return;
            }

            amount -= give;
        }
    }


    public static boolean hasSpaceFor(Player player, ItemStack item, int amount) {
        int remaining = amount;

        // 1️⃣ Count free space in existing stacks
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null) continue;
            if (!invItem.isSimilar(item)) continue;

            int maxStack = invItem.getMaxStackSize();
            int space = maxStack - invItem.getAmount();

            remaining -= space;
            if (remaining <= 0) return true;
        }

        // 2️⃣ Count empty slots
        int emptySlots = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem == null) emptySlots++;
        }

        int maxPerSlot = item.getMaxStackSize();
        remaining -= emptySlots * maxPerSlot;

        return remaining <= 0;
    }


}
