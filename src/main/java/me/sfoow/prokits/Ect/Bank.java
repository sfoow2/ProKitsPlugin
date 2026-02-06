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

import java.util.Arrays;

import static me.sfoow.prokits.Ect.VaultUtils.*;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.items.Token;
import static me.sfoow.prokits.items.CToken;

public class Bank implements Listener {

    private static final String TITLE = ChatColor.DARK_GRAY + "Bank";

    /* ================= COMMAND ================= */

    @Command("bank")
    public void bankCommand(Player sender) {
        sender.openInventory(createBankInventory(sender));
    }

    /* ================= GUI ================= */

    private Inventory createBankInventory(Player p) {
        Inventory inv = getBasicInventory("&8Bank", 6);

        int balance = (int) getBalance(p);

        inv.setItem(13, createItem(Material.GOLD_INGOT,
                "&6Your Balance",
                "&7Tokens: &e" + balance));

        inv.setItem(28, createItem(Material.HONEY_BLOCK, "&aDeposit ALL"));
        inv.setItem(29, createItem(Material.HONEYCOMB, "&aDeposit 64"));
        inv.setItem(30, createItem(Material.SUNFLOWER, "&aDeposit 1"));

        inv.setItem(32, createItem(Material.OAK_PLANKS, "&cWithdraw 1"));
        inv.setItem(33, createItem(Material.CHEST, "&cWithdraw 64"));
        inv.setItem(34, createItem(Material.CHEST_MINECART, "&cWithdraw ALL"));

        inv.setItem(32+9-2, createItem(Material.GOLD_NUGGET, "&cWithdraw 16"));
        inv.setItem(33+9-2, createItem(Material.GOLD_INGOT, "&cWithdraw 32"));
        inv.setItem(34+9-2, createItem(Material.GOLD_BLOCK, "&cWithdraw 48"));

        return inv;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.stream(lore)
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .toList());

        item.setItemMeta(meta);
        return item;
    }

    /* ================= CLICKS ================= */

    @EventHandler
    public void onBankClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        e.setCancelled(true);

        switch (e.getRawSlot()) {
            case 28 -> deposit(p, countTokens(p));
            case 29 -> deposit(p, 64);
            case 30 -> deposit(p, 1);

            case 32 -> withdraw(p, 1);
            case 33 -> withdraw(p, 64);
            case 34 -> withdraw(p, (int) getBalance(p));


            case 32+9-2 -> withdraw(p, 16);
            case 33+9-2 -> withdraw(p, 32);
            case 34+9-2 -> withdraw(p, 48);

        }

        p.openInventory(createBankInventory(p));
    }

    /* ================= LOGIC ================= */

    private void deposit(Player p, int amount) {
        int tokens = countTokens(p);
        if (tokens <= 0) {
            Qwerty(p, "&cYou have no tokens to deposit.");
            SendNo(p);
            return;
        }

        int toDeposit = Math.min(tokens, amount);

        // Remove physical tokens
        removeTokens(p, toDeposit);

        // Add Vault money
        addMoney(p, toDeposit);

        SendYes(p);
        Qwerty(p, "&aDeposited " + toDeposit + " tokens.");
    }

    private void withdraw(Player p, int amount) {
        int balance = (int) getBalance(p);

        if (balance <= 0) {
            Qwerty(p, "&cYou have no tokens in the bank.");
            SendNo(p);
            return;
        }

        int toWithdraw = Math.min(balance, amount);

        if (!hasInventorySpace(p, toWithdraw)) {
            Qwerty(p, "&cNot enough inventory space.");
            SendNo(p);
            return;
        }

        // Remove Vault money
        setBalance(p, balance - toWithdraw);

        // Give physical tokens
        giveTokens(p, toWithdraw);

        SendYes(p);
        Qwerty(p, "&aWithdrew " + toWithdraw + " tokens.");
    }


    /* ================= TOKEN UTILS ================= */

    private int countTokens(Player p) {
        int count = 0;

        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            if (item.isSimilar(Token)) {
                count += item.getAmount();
            } else if (item.isSimilar(CToken)) {
                count += item.getAmount() * 64;
            }
        }
        return count;
    }

    private void removeTokens(Player p, int amount) {
        int remaining = amount;

        // 1️⃣ Remove normal tokens first
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || !item.isSimilar(Token)) continue;

            int take = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - take);
            remaining -= take;

            if (remaining <= 0) return;
        }

        // 2️⃣ Remove CTokens accurately
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null || !item.isSimilar(CToken)) continue;
            if (remaining <= 0) return;

            int availableBlocks = item.getAmount();
            int neededBlocks = (int) Math.ceil(remaining / 64.0);

            int removeBlocks = Math.min(availableBlocks, neededBlocks);
            item.setAmount(availableBlocks - removeBlocks);

            int removedValue = removeBlocks * 64;
            remaining -= removedValue;

            // Refund excess if we broke too much
            if (remaining < 0) {
                int refund = -remaining;
                ItemStack refundTokens = Token.clone();
                refundTokens.setAmount(refund);
                p.getInventory().addItem(refundTokens);
                remaining = 0;
                return;
            }
        }
    }




    private void giveTokens(Player p, int amount) {
        int blocks = amount / 64;
        int remainder = amount % 64;

        // Give CTokens
        while (blocks > 0) {
            ItemStack c = CToken.clone();
            int give = Math.min(64, blocks);
            c.setAmount(give);
            p.getInventory().addItem(c);
            blocks -= give;
        }

        // Give normal tokens
        while (remainder > 0) {
            ItemStack t = Token.clone();
            int give = Math.min(64, remainder);
            t.setAmount(give);
            p.getInventory().addItem(t);
            remainder -= give;
        }
    }

    private boolean hasInventorySpace(Player p, int amount) {

        int blocks = amount / 64;
        int remainder = amount % 64;

        int neededSlots = 0;

        // CTokens slots
        if (blocks > 0) {
            neededSlots += (int) Math.ceil(blocks / 64.0);
        }

        // Normal token slots
        if (remainder > 0) {
            neededSlots++;
        }

        int freeSlots = 0;
        for (ItemStack item : p.getInventory().getStorageContents()) {
            if (item == null) freeSlots++;
        }

        return freeSlots >= neededSlots;
    }

}
