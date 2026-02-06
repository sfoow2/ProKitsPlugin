package me.sfoow.prokits.Quests;

import me.sfoow.prokits.Ect.VaultUtils;
import org.bukkit.Bukkit;
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
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;

import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Quests.QuestPlayerData.PlayerQuests;
import static me.sfoow.prokits.Quests.Questdata.QuestsList;
import static me.sfoow.prokits.Quests.Questdata.getQuestItem;

public class QuestGui implements Listener {

    public static quest[] OptionA;
    public static quest[] OptionB;

    private static Random rand = new Random();

    private static class quest{
        byte id = -1;
        byte neededprog = -1;
    }

    public static void LoadInNewQuests(){
        OptionA = new quest[5];
        OptionB = new quest[5];

        for (byte x = 0; x < 5; x++){
            OptionA[x] = generateQuestOption();
            OptionB[x] = generateQuestOptionExcluding(OptionA[x].id);
        }
    }

    private static quest generateQuestOption(){
        quest q = new quest();
        byte id = (byte) rand.nextInt(0, QuestsList.length);
        q.id = id;
        q.neededprog = getRandomProg(QuestsList[id].min, QuestsList[id].max);
        return q;
    }

    private static quest generateQuestOptionExcluding(byte excludeId){
        quest q = new quest();
        byte id;
        do {
            id = (byte) rand.nextInt(0, QuestsList.length);
        } while(id == excludeId);
        q.id = id;
        q.neededprog = getRandomProg(QuestsList[id].min, QuestsList[id].max);
        return q;
    }

    private static byte getRandomProg(byte min, byte max){
        if(max < min) max = min; // safety
        int range = max - min + 1; // make it inclusive
        return (byte) (rand.nextInt(range) + min);
    }


    private static byte getRandomNumFilter(byte min, byte max,byte filter){
        byte dat = (byte) rand.nextInt(min,max);

        while (dat == filter){
            dat = (byte) rand.nextInt(min,max);
        }
        return dat;
    }



    @Command("quests")
    private void OpenQuestGuiCommand(Player player){
        OpenQuestGui(player);
    }

    public static void OpenQuestGui(Player player) {
        Inventory inv = getBasicInventory(ChatColor.translateAlternateColorCodes('&',"&8Quests Gui"), 4);

        for (int x = 0; x < 5; x++){
            ArrayList<QuestPlayerData.QuestData> PlayerQuestslist = PlayerQuests.get(player.getUniqueId());
            QuestPlayerData.QuestData qData = PlayerQuestslist.get(x);

            // No active quest
            if (qData.slot == -1 || qData == null){
                inv.setItem(1+9+x,getQuestItem(OptionA[x].id,OptionA[x].neededprog, (byte) 0));
                inv.setItem(1+9+x+9,getQuestItem(OptionB[x].id,OptionB[x].neededprog, (byte) 0));
            } else {
                if (qData.updown == 0){
                    inv.setItem(1+9+x,getQuestItem(qData.id,qData.maxprogress,qData.progress));
                    inv.setItem(1+9+9+x,getNoQuestItem());
                } else if (qData.updown == 1) {
                    inv.setItem(1+9+9+x,getQuestItem(qData.id,qData.maxprogress,qData.progress));
                    inv.setItem(1+9+x,getNoQuestItem());
                }

                // Add red dye below Option B to cancel quest for $50
                int cancelSlot = 1+9+9+x+9; // slot under Option B
                inv.setItem(cancelSlot,getCancelQuestItem());
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack getCancelQuestItem() {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Cancel Quest");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to cancel this quest for $50");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!ChatColor.stripColor(e.getView().getTitle()).equals("Quests Gui")) return;

        int index = e.getSlot();
        UUID uuid = player.getUniqueId();
        ArrayList<QuestPlayerData.QuestData> quests = PlayerQuests.get(uuid);

        // Cancel quest button clicked
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.RED_DYE) {
            byte questSlot = (byte) ((index - 1 - 9) % 9); // map back to the quest index
            QuestPlayerData.QuestData q = quests.get(questSlot);

            if (q.id != -1) {
                if (VaultUtils.getBalance(player) >= 50) {
                    VaultUtils.removeMoney(player, 50);
                    // Reset quest slot
                    q.id = -1;
                    q.progress = -1;
                    q.maxprogress = -1;
                    q.slot = -1;
                    q.updown = -1;
                    QuestPlayerData.DoPlayerSaveQuestsProg(player);
                    SendYes(player);
                    Qwerty(player,"&cQuest cancelled for $50");

                    // Reload the global quests for this slot for everyone
                    reloadQuestSlotForAll(questSlot);
                } else {
                    SendNo(player);
                    Qwerty(player,"&cNot enough money to cancel quest");
                }
            }

            e.setCancelled(true);
            return;
        }

        // Quest acceptance
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BOOK){
            byte QuestSlot = (byte) ((index - 1) % 9);
            QuestPlayerData.QuestData qData = quests.get(QuestSlot);

            if (qData.id == -1){
                byte updown = (10 <= index && index <= 7+9) ? (byte)0 : (byte)1;

                qData.id = getQuestIdUpDownSlot(updown,QuestSlot);
                qData.slot = QuestSlot;
                qData.updown = updown;
                qData.progress = 0;
                qData.maxprogress = getQuestProg(updown,QuestSlot);

                SendYes(player);
                Qwerty(player,"&aStarted quest");

                // Reload the global quests for this slot for everyone
                reloadQuestSlotForAll(QuestSlot);
            } else {
                SendNo(player);
                Qwerty(player,"&cThat quest is already active");
            }
        }

        e.setCancelled(true);
    }

    // Reload the quests for a specific slot and reopen the GUI for all players who have it open
    private static void reloadQuestSlotForAll(byte slot) {
        OptionA[slot] = generateQuestOption();
        OptionB[slot] = generateQuestOptionExcluding(OptionA[slot].id);

        // Reopen GUI for all online players who have it open
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory() != null &&
                    ChatColor.stripColor(p.getOpenInventory().getTitle()).equals("Quests Gui")) {
                OpenQuestGui(p);
            }
        }
    }


    private static ItemStack getNoQuestItem() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(" ");

        item.setItemMeta(meta);
        return item;
    }


    private static byte getQuestIdUpDownSlot(byte updown, byte questslot){
        if (updown == 0){
            return OptionA[questslot].id;
        } else if (updown == 1){
            return OptionB[questslot].id;
        }
        return -1;
    }

    private static byte getQuestProg(byte updown, byte questslot){
        if (updown == 0){
            return OptionA[questslot].neededprog;
        } else if (updown == 1){
            return OptionB[questslot].neededprog;
        }
        return -1;
    }

}
