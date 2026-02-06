    package me.sfoow.prokits;

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

    import java.util.*;

    import static me.sfoow.prokits.Data.PlayerData.*;
    import static me.sfoow.prokits.Ect.VaultUtils.getBalance;
    import static me.sfoow.prokits.Ect.VaultUtils.setBalance;
    import static me.sfoow.prokits.Ect.utils.*;

    public class UpgradeGUI implements Listener {

        private static final String GUI_NAME = "&8Upgrades";

        /* ===================== GUI OPEN ===================== */

        @Command("upgrade")
        public static void UpgradeGuiCommand(Player sender) {
            OpenUpgradeGui(sender);
        }

        public static void OpenUpgradeGui(Player player) {
            Inventory inv = getBasicInventory(GUI_NAME, 3);

            inv.setItem(11, createUpgradeItem(
                    Material.GOLD_INGOT,
                    "&6Token Upgrade",
                    PlayerUpgradeLevelTokens.getOrDefault(player.getUniqueId(), 0),
                    5,
                    false
            ));

            inv.setItem(13, createUpgradeItem(
                    Material.IRON_SWORD,
                    "&cKit Upgrade",
                    PlayerKitUpgradeLevel.getOrDefault(player.getUniqueId(), 0),
                    2,
                    true
            ));

            inv.setItem(15, createUpgradeItem(
                    Material.EXPERIENCE_BOTTLE,
                    "&aXP Upgrade",
                    PlayerUpgradeLevelXp.getOrDefault(player.getUniqueId(), 0),
                    5,
                    false
            ));

            player.openInventory(inv);
        }

        /* ===================== ITEM CREATION ===================== */

        private static ItemStack createUpgradeItem(
                Material mat,
                String name,
                int level,
                int maxLevel,
                boolean isKit
        ) {
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            List<String> lore = new ArrayList<>();
            lore.add("&7Level: &f" + level + "&7/&f" + maxLevel);

            if (level >= maxLevel) {
                lore.add("&cMAX LEVEL");
            } else {
                int cost = isKit
                        ? getCostOfKitUpgradePerLevel(level + 1)
                        : getCostOfUpgradePerLevel(level + 1);

                lore.add("&7Cost: &6" + cost + " Tokens");
                lore.add("");
                lore.add("&eClick to upgrade");
            }

            meta.setLore(color(lore));
            item.setItemMeta(meta);
            return item;
        }

        /* ===================== CLICK HANDLER ===================== */

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player player)) return;
            if (!ChatColor.stripColor(e.getView().getTitle()).equals("Upgrades")) return;

            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            int tokens = (int) getBalance(player);

            if (item.getType() == Material.GOLD_INGOT) {
                handleUpgrade(
                        player,
                        PlayerUpgradeLevelTokens,
                        tokens,
                        "Token Gain"
                );
            }

            if (item.getType() == Material.EXPERIENCE_BOTTLE) {
                handleUpgrade(
                        player,
                        PlayerUpgradeLevelXp,
                        tokens,
                        "XP Gain"
                );
            }

            if (item.getType() == Material.IRON_SWORD) {
                handleKitUpgrade(player, tokens);
            }
        }

        /* ===================== UPGRADE LOGIC ===================== */

        private static void handleUpgrade(
                Player player,
                Map<UUID, Integer> upgradeMap,
                int tokens,
                String name
        ) {
            UUID uuid = player.getUniqueId();
            int level = upgradeMap.getOrDefault(uuid, 0);
            int maxLevel = 5;

            if (level >= maxLevel) {
                Qwerty(player, "&cThis upgrade is already maxed!");
                SendNo(player);
                return;
            }

            int cost = getCostOfUpgradePerLevel(level + 1);

            if (tokens < cost) {
                Qwerty(player, "&cYou need &6" + cost + " Tokens &cto upgrade!");
                SendNo(player);
                return;
            }

            setBalance(player, tokens - cost);
            upgradeMap.put(uuid, level + 1);

            Qwerty(player, "&aSuccessfully upgraded &e" + name + " &ato level &6" + (level + 1));
            SendYes(player);

            OpenUpgradeGui(player);
        }

        private static void handleKitUpgrade(Player player, int tokens) {
            UUID uuid = player.getUniqueId();
            int level = PlayerKitUpgradeLevel.getOrDefault(uuid, 0);
            int maxLevel = 2;

            if (level >= maxLevel) {
                Qwerty(player, "&cYour kit upgrade is already maxed!");
                SendNo(player);
                return;
            }

            int cost = getCostOfKitUpgradePerLevel(level + 1);

            if (tokens < cost) {
                Qwerty(player, "&cYou need &6" + cost + " Tokens &cto upgrade your kits!");
                SendNo(player);
                return;
            }

            setBalance(player, tokens - cost);
            PlayerKitUpgradeLevel.put(uuid, level + 1);

            Qwerty(player, "&aKit upgrade increased to level &6" + (level + 1));
            SendYes(player);

            OpenUpgradeGui(player);
        }

        /* ===================== UTILS ===================== */

        private static List<String> color(List<String> list) {
            List<String> out = new ArrayList<>();
            for (String s : list) {
                out.add(ChatColor.translateAlternateColorCodes('&', s));
            }
            return out;
        }

        private static int getCostOfUpgradePerLevel(int level) {
            return (int) (Math.pow(level * 1.5, 2) + 25);
        }

        private static int getCostOfKitUpgradePerLevel(int level) {
            return (int) (Math.pow(level * 1.5, 4.5) + 85);
        }
    }
