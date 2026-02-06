package me.sfoow.prokits.Quests;

import me.sfoow.prokits.Ect.VaultUtils;
import me.sfoow.prokits.Ect.utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

import static me.sfoow.prokits.Quests.QuestPlayerData.PlayerQuests;
import static me.sfoow.prokits.Quests.Questdata.QuestsList;
import static me.sfoow.prokits.Ect.utils.*;

public class QuestProgress implements Listener {

    /**
     * Check if player is in arena.
     */
    private boolean isInArena(Player player) {
        return player.getWorld().getName().equalsIgnoreCase("spawn") && player.getLocation().getY() <= 63;
    }

    /**
     * Add progress to a quest for a player.
     */
    private void addProgress(Player player, QuestPlayerData.QuestData q, byte amount) {
        if (q.id == -1) return; // no active quest in this slot

        // Increment progress safely
        int newProgress = q.progress + amount;
        if (newProgress > q.maxprogress) newProgress = q.maxprogress;
        q.progress = (byte) newProgress;

        // Fetch quest details from stored id
        Questdata.QuestDetails quest = Questdata.QuestsList[q.id];

        // Check completion
        if (q.progress >= q.maxprogress) {
            int reward = (int) (quest.RewardBase + quest.RewardPow * q.maxprogress);
            VaultUtils.addMoney(player, reward);
            Qwerty(player, "&aCompleted Quest: " + quest.Name + " &f+ $" + reward);
            SendYes(player);

            // Reset quest slot
            q.id = -1;
            q.progress = -1;
            q.maxprogress = -1;
            q.slot = -1;
            q.updown = -1;

            QuestPlayerData.DoPlayerSaveQuestsProg(player);
        }
    }


    /**
     * Efficiently handle player kills
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = victim.getKiller();

        // Must have a real killer
        if (killer == null) return;
        if (!isInArena(killer)) return;

        ArrayList<QuestPlayerData.QuestData> quests = PlayerQuests.get(killer.getUniqueId());
        if (quests == null) return;

        for (QuestPlayerData.QuestData q : quests) {
            if (q.id == -1) continue;

            Questdata.QuestDetails quest = QuestsList[q.id];
            String desc = quest.Description1.toLowerCase();

            // Only kill-player quests here
            if (!desc.contains("kill players")) continue;

            boolean counts = false;

            Material weapon = killer.getInventory().getItemInMainHand().getType();
            String weaponName = weapon.name().toLowerCase();

            // Weapon-specific quests
            if (desc.contains("wooden") && weaponName.contains("wooden_sword")) {
                counts = true;
            } else if (desc.contains("stone") && weaponName.contains("stone_sword")) {
                counts = true;
            } else if (!desc.contains("wooden") && !desc.contains("stone")) {
                // Generic "kill players" quest
                counts = true;
            }

            if (counts) {
                addProgress(killer, q, (byte) 1);
            }
        }
    }


    /**
     * Handle block breaking efficiently
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInArena(player)) return;

        Material block = event.getBlock().getType();
        ArrayList<QuestPlayerData.QuestData> quests = PlayerQuests.get(player.getUniqueId());
        if (quests == null) return;

        for (QuestPlayerData.QuestData q : quests) {
            if (q.id == -1) continue;
            Questdata.QuestDetails quest = QuestsList[q.id];
            String desc = quest.Description1.toLowerCase();

            // Only handle block/mining quests here
            if (desc.contains("break any block")) {
                addProgress(player, q, (byte) 1);
            } else if (desc.contains("mine")) {
                if (block == Material.IRON_ORE || block == Material.GOLD_ORE
                        || block == Material.DIAMOND_ORE || block == Material.EMERALD_ORE) {
                    addProgress(player, q, (byte) 1);
                }
            }
            // Kill quests are ignored here → no accidental progress
        }
    }
}
