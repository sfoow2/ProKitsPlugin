package me.sfoow.prokits.Ect;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Random;

import static me.sfoow.prokits.Data.PlayerData.PlayerKitUpgradeLevel;

public class RandomKit {

    private static final Random random = new Random();

    // Main method to give a random kit to a player
    public static void giveRandomKit(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        // Equip armor
        player.getInventory().setHelmet(colorItem(returnHelmet(player)));
        player.getInventory().setChestplate(colorItem(returnRandomChestplate(player)));
        player.getInventory().setLeggings(colorItem(returnRandomLeggings(player)));
        player.getInventory().setBoots(colorItem(returnRandomBoots(player)));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 0.5f, 1f);

        // Apply night vision
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 72000, 1, false, false));

        // Give weapons
        ItemStack sword = colorItem(returnRandomSword());
        player.getInventory().addItem(sword);

        if (chance(60)) {
            player.getInventory().setItem(40, colorItem(new ItemStack(Material.SHIELD)));
        } else {
            player.getInventory().setItem(40, colorItem(new ItemStack(Material.TOTEM_OF_UNDYING)));
        }

        if (PlayerKitUpgradeLevel.get(player.getUniqueId()) > 0){
            if (chance(20)){
                player.getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
            }
        }


        ItemStack axe = colorItem(returnRandomAxe());
        player.getInventory().addItem(axe);

        ItemStack pickaxe = colorItem(returnRandomPickaxe());
        player.getInventory().addItem(pickaxe);

        // Give blocks, food, special items
        player.getInventory().addItem(colorItem(returnRandomBlocks()));
        player.getInventory().addItem(colorItem(returnRandomFood()));
        player.getInventory().addItem(Objects.requireNonNull(colorItem(returnSpecial())));
        giveRandomCustomItem(player,10);

        // Give PvP gear
        givePvPGear(player);

        // Give bow
        returnRandomBow(player);

        // Send feedback
        sendKitInfo(player);

        // Play pickup sound
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1f);
    }

    // ---------- ITEM COLORING ----------
    private static ItemStack colorItem(ItemStack item) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String typeName = item.getType().toString().replace("_", " ");
            meta.setDisplayName("§e" + toPascalCase(typeName));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.toLowerCase().split(" ")) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return result.toString().trim();
    }

    // ---------- RANDOM CHANCE ----------
    private static boolean chance(double percent) {
        return random.nextDouble() * 100 < percent;
    }

    // ---------- ARMOR ----------
    private static ItemStack returnHelmet(Player player) {
        int luck = returnLuck(player);
        if (chance(25 + luck)) {
            if (chance(80)) return new ItemStack(Material.NETHERITE_HELMET);
            return enchant(new ItemStack(Material.NETHERITE_HELMET), Enchantment.PROJECTILE_PROTECTION, 1);
        } else {
            int r = random.nextInt(101);
            ItemStack item = (r <= 60) ? new ItemStack(Material.IRON_HELMET) : new ItemStack(Material.DIAMOND_HELMET);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.PROTECTION, 1);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.BLAST_PROTECTION, 1);
            return item;
        }
    }

    private static ItemStack returnRandomChestplate(Player player) {
        int luck = returnLuck(player);
        if (chance(25 + luck)) {
            if (chance(80)) return new ItemStack(Material.NETHERITE_CHESTPLATE);
            return enchant(new ItemStack(Material.NETHERITE_CHESTPLATE), Enchantment.PROTECTION, 1);
        } else {
            int r = random.nextInt(101);
            ItemStack item = (r <= 60) ? new ItemStack(Material.IRON_CHESTPLATE) : new ItemStack(Material.DIAMOND_CHESTPLATE);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.PROTECTION, 1);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.BLAST_PROTECTION, 1);
            return item;
        }
    }

    private static ItemStack returnRandomLeggings(Player player) {
        int luck = returnLuck(player);
        if (chance(25 + luck)) {
            if (chance(80)) return new ItemStack(Material.NETHERITE_LEGGINGS);
            return enchant(new ItemStack(Material.NETHERITE_LEGGINGS), Enchantment.PROTECTION, 1);
        } else {
            int r = random.nextInt(101);
            ItemStack item = (r <= 60) ? new ItemStack(Material.IRON_LEGGINGS) : new ItemStack(Material.DIAMOND_LEGGINGS);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.PROTECTION, 1);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.BLAST_PROTECTION, 1);
            return item;
        }
    }

    private static ItemStack returnRandomBoots(Player player) {
        int luck = returnLuck(player);
        if (chance(25 + luck)) {
            if (chance(80)) return new ItemStack(Material.NETHERITE_BOOTS);
            return enchant(new ItemStack(Material.NETHERITE_BOOTS), Enchantment.PROTECTION, 1);
        } else {
            int r = random.nextInt(101);
            ItemStack item = (r <= 60) ? new ItemStack(Material.IRON_BOOTS) : new ItemStack(Material.DIAMOND_BOOTS);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.PROTECTION, 1);
            if (random.nextInt(3) != 0) item.addEnchantment(Enchantment.BLAST_PROTECTION, 1);
            return item;
        }
    }

    // ---------- WEAPONS ----------
    private static ItemStack returnRandomSword() {
        int r = random.nextInt(101);
        ItemStack sword = (r <= 70) ? new ItemStack(Material.DIAMOND_SWORD) : new ItemStack(Material.NETHERITE_SWORD);
        int p = random.nextInt(3);
        if (p != 0) sword.addEnchantment(Enchantment.SHARPNESS, p);
        return sword;
    }

    private static ItemStack returnRandomAxe() {
        int n = random.nextInt(301);
        if (n <= 5) return new ItemStack(Material.NETHERITE_AXE);
        if (n <= 20) return enchant(new ItemStack(Material.DIAMOND_AXE), Enchantment.SHARPNESS, 1);
        if (n <= 60) return enchant(new ItemStack(Material.IRON_AXE), Enchantment.SHARPNESS, 2);
        if (n <= 140) return enchant(new ItemStack(Material.IRON_AXE), Enchantment.SHARPNESS, 1);
        return new ItemStack(Material.IRON_AXE);
    }

    private static ItemStack returnRandomPickaxe() {
        int n = random.nextInt(251);
        if (n <= 10) return enchant(new ItemStack(Material.NETHERITE_PICKAXE), Enchantment.EFFICIENCY, 3);
        if (n <= 30) return enchant(new ItemStack(Material.NETHERITE_PICKAXE), Enchantment.EFFICIENCY, 2);
        if (n <= 100) return enchant(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.EFFICIENCY, 4);
        return enchant(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.EFFICIENCY, 3);
    }

    private static ItemStack returnSpecial() {
        if (chance(30)) return new ItemStack(Material.GOLDEN_APPLE, 1);
        return new ItemStack(Material.AIR);
    }

    private static ItemStack returnRandomFood() {
        int n = random.nextInt(4) + 1;
        return switch (n) {
            case 1 -> new ItemStack(Material.COOKED_BEEF, 18);
            case 2 -> new ItemStack(Material.COOKED_PORKCHOP, 12);
            case 3 -> new ItemStack(Material.GOLDEN_CARROT, 12);
            default -> new ItemStack(Material.COOKED_SALMON, 24);
        };
    }

    private static ItemStack returnRandomBlocks() {
        int n = random.nextInt(3) + 1;
        int r = random.nextInt(3) + 1;
        int amount = switch (r) {
            case 1 -> 32;
            case 2 -> 64;
            default -> 128;
        };
        return switch (n) {
            case 1 -> new ItemStack(Material.OAK_PLANKS, amount);
            case 2 -> new ItemStack(Material.SPRUCE_PLANKS, amount);
            default -> new ItemStack(Material.STONE, amount);
        };
    }

    private static ItemStack enchant(ItemStack item, Enchantment enchantment, int level) {
        item.addEnchantment(enchantment, level);
        return item;
    }

    // ---------- PvP Gear ----------
    private static void givePvPGear(Player player) {
        if (chance(35)) player.getInventory().addItem(new ItemStack(Material.COBWEB, 8));
        if (chance(45)) player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
        if (chance(25)) player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
        if (chance(45)) player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        for (int i = 0; i < 5; i++) {
            if (chance(25)) player.getInventory().addItem(new ItemStack(Material.TNT));
        }
    }

    // ---------- BOW ----------
    private static void returnRandomBow(Player player) {
        if (!chance(50)) return;
        int n = random.nextInt(1000);
        ItemStack bow;
        if (n <= 50) bow = enchant(new ItemStack(Material.CROSSBOW), Enchantment.PIERCING, 1);
        else if (n <= 150) bow = enchant(new ItemStack(Material.BOW), Enchantment.POWER, 2);
        else if (n <= 300) bow = enchant(new ItemStack(Material.BOW), Enchantment.POWER, 1);
        else if (n <= 500) bow = new ItemStack(Material.CROSSBOW);
        else bow = new ItemStack(Material.BOW);
        player.getInventory().addItem(colorItem(bow));

        int arrows = random.nextInt(16) + 10;
        player.getInventory().addItem(new ItemStack(Material.ARROW, arrows));
    }

    // ---------- SEND KIT INFO ----------
    private static void sendKitInfo(Player player) {
        player.sendMessage("§a§lKit Info");
        player.sendMessage("§7Effect: " + returnRandomEffect(player));
    }

    public static void giveRandomCustomItem(Player player, int times) {
        int luck = returnLuck(player); // your existing luck method
        for (int i = 0; i < times; i++) {
            double chance = 25 + luck * 2; // 25 + returnluck(p) + returnluck(p)%
            if (random.nextDouble() * 100 < chance) {
                int n = random.nextInt(11) + 1; // 1 to 11
                switch (n) {
                    case 1 -> executeServerItem(player, "antit1runner");
                    case 2 -> executeServerItem(player, "bridgebuilder");
                    case 3 -> executeServerItem(player, "dynamite");
                    case 4 -> executeServerItem(player, "icebuble");
                    case 5 -> executeServerItem(player, "lightasfeather");
                    case 6 -> executeServerItem(player, "magicfruit");
                    case 7 -> executeServerItem(player, "speedpowder");
                    case 8 -> {
                        if (random.nextDouble() * 100 < 30) executeServerItem(player, "graplehook");
                        else executeServerItem(player, "speedpowder");
                    }
                    case 9 -> executeServerItem(player, "fireball");
                    case 10 -> executeServerItem(player, "iciclewand");
                    case 11 -> executeServerItem(player, "sawper");
                }

                // Optional: add a tiny delay per tick if needed
                // In Bukkit you would usually use a scheduler for actual ticks
            }
        }
    }

    public static void executeServerItem(Player player, String itemName) {
        String command = String.format("serveritem give %s 1 %s true", itemName, player.getName());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }


    private static String returnRandomEffect(Player player) {
        int n = random.nextInt(1000) + 1;
        if (n <= 10) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40 * 20, 1));
            return "Strength II for 40 seconds";
        } else if (n <= 30) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 35 * 20, 2));
            return "Regeneration III for 35 seconds";
        } else if (n <= 60) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 50 * 20, 0));
            return "Fire Resistance for 50 seconds";
        } else if (n <= 100) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40 * 20, 1));
            return "Resistance II for 40 seconds";
        } else if (n <= 150) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 45 * 20, 0));
            return "Strength I for 45 seconds";
        } else if (n <= 220) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 45 * 20, 1));
            return "Speed II for 45 seconds";
        } else if (n <= 300) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 35 * 20, 1));
            return "Regeneration II for 35 seconds";
        } else if (n <= 400) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40 * 20, 0));
            return "Resistance I for 40 seconds";
        } else if (n <= 530) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50 * 20, 0));
            return "Speed I for 50 seconds";
        } else if (n <= 680) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40 * 20, 1));
            return "Haste II for 40 seconds";
        } else if (n <= 800) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 45 * 20, 1));
            return "Jump Boost II for 45 seconds";
        } else if (n <= 900) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 50 * 20, 0));
            return "Haste I for 50 seconds";
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 35 * 20, 0));
            return "Regeneration I for 35 seconds";
        }
    }

    // ---------- MOCK LUCK ----------
    private static int returnLuck(Player player) {
        return PlayerKitUpgradeLevel.getOrDefault(player.getUniqueId(),0);
    }
}
