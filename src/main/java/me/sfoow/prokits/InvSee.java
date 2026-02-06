package me.sfoow.prokits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class InvSee implements Listener {

    @Command("invsee")
    @CommandPermission("op")
    public void InventorySeeCommand(Player sender, Player target) {

        Inventory inv = Bukkit.createInventory(
                sender,
                45,
                "InvSee: " + target.getName()
        );

        // Player inventory (0–35)
        for (int i = 0; i < 36; i++) {
            inv.setItem(i, target.getInventory().getItem(i));
        }

        // Armor + offhand (row 5)
        inv.setItem(36, target.getInventory().getHelmet());
        inv.setItem(37, target.getInventory().getChestplate());
        inv.setItem(38, target.getInventory().getLeggings());
        inv.setItem(39, target.getInventory().getBoots());
        inv.setItem(40, target.getInventory().getItemInOffHand());

        sender.openInventory(inv);
    }

    @EventHandler
    public void onInvSeeClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inv = event.getInventory();
        if (inv == null || !event.getView().getTitle().startsWith("InvSee: ")) return;

        Player viewer = (Player) event.getWhoClicked();
        String targetName = event.getView().getTitle().replace("InvSee: ", "");
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            viewer.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        // Only handle GUI slots
        if (slot < 0 || slot >= 45) return;

        // Allow taking items
        event.setCancelled(false);

        ItemStack cursor = event.getCursor();

        // Main inventory
        if (slot < 36) {
            target.getInventory().setItem(slot, cursor);
        }
        // Armor + offhand
        else {
            switch (slot) {
                case 36 -> target.getInventory().setHelmet(cursor);
                case 37 -> target.getInventory().setChestplate(cursor);
                case 38 -> target.getInventory().setLeggings(cursor);
                case 39 -> target.getInventory().setBoots(cursor);
                case 40 -> target.getInventory().setItemInOffHand(cursor);
            }
        }

        target.updateInventory();
    }

}
