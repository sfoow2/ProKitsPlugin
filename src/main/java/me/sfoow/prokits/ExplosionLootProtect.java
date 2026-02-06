package me.sfoow.prokits;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class ExplosionLootProtect implements Listener {

    private static final Set<Material> PROTECTED_ITEMS = EnumSet.of(
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS,
            Material.NETHERITE_PICKAXE,
            Material.NETHERITE_SWORD,
            Material.TOTEM_OF_UNDYING,
            Material.OBSIDIAN,
            Material.END_CRYSTAL,
            Material.RESPAWN_ANCHOR,
            Material.GLOWSTONE,
            Material.EXPERIENCE_BOTTLE,
            Material.GOLDEN_APPLE,
            Material.ENDER_PEARL
    );

    @EventHandler(ignoreCancelled = true)
    public void onItemExplosionDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;

        if (event.getEntity().getType().equals(EntityType.WIND_CHARGE)){

        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        // Only block explosion damage (NOT despawn)
        if (cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        Material type = item.getItemStack().getType();

        if (PROTECTED_ITEMS.contains(type)) {
            event.setCancelled(true);
        }
    }


}
