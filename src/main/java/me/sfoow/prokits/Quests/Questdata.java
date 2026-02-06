package me.sfoow.prokits.Quests;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Questdata {

    private static final Random rand = new Random();
    public static QuestDetails[] QuestsList = null;

    public static class QuestDetails {
        String Name = "";
        String Description1 = "";
        String Description2 = "";
        byte min = -1;
        byte max = -1;
        float RewardPow = -1;
        short RewardBase = -1;
    }

    public static ItemStack getQuestItem(byte questId, byte progressNeeded, byte prog) {

        if (QuestsList == null || QuestsList.length == 0) {
            throw new IllegalStateException("QuestsList not loaded or empty! Did you call LoadUpQuestYaml()?");
        }

        if (questId < 0 || questId >= QuestsList.length) {
            throw new IllegalArgumentException("Invalid QuestId: " + questId);
        }

        QuestDetails quest = QuestsList[questId];

        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> lore = new ArrayList<>();
        lore.add("&8ǫᴜᴇꜱᴛ ɪɴꜰᴏ");
        lore.add("");
        lore.add(quest.Description1);
        lore.add(quest.Description2);
        lore.add("&fProgress: " + prog + "/" + progressNeeded);

        short base = quest.RewardBase;
        float pow = quest.RewardPow;
        byte num = progressNeeded;

        lore.add("&fPayment: " + getMoneyPaymentFromQuest(base,pow,num) + "&e$");
        lore.add("");
        lore.add("&aClick To Start Quest");

        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(coloredLore);
        meta.setDisplayName(ChatColor.translateAlternateColorCodes(
                '&',
                quest.Name + " &aᴄʟɪᴄᴋ ᴛᴏ ᴀᴄᴄᴇᴘᴛ"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public static void LoadUpQuestYaml() {

        YamlManager data = new YamlManager("plugins/prokits/questsdata.yml");

        byte count = 0;
        while (data.get("quest" + count + ".Name") != null) {
            count++;
        }

        if (count == 0) {
            throw new IllegalStateException(
                    "No quests found! Ensure questsdata.yml starts with quest0.Name"
            );
        }

        QuestsList = new QuestDetails[count];

        for (byte i = 0; i < count; i++) {
            QuestDetails quest = new QuestDetails();
            quest.Name = data.getString("quest" + i + ".Name");
            quest.Description1 = data.getString("quest" + i + ".Description1");
            quest.Description2 = data.getString("quest" + i + ".Description2");
            quest.min = data.getByte("quest" + i + ".Min");
            quest.max = data.getByte("quest" + i + ".Max");

            String pow = data.get("quest" + i + ".RewardPow").toString();
            quest.RewardPow = Float.parseFloat(pow);

            String base = data.get("quest" + i + ".RewardBase").toString();
            quest.RewardBase = Short.parseShort(base);

            QuestsList[i] = quest;
        }
    }

    private static int getMoneyPaymentFromQuest(short BasePayment, float Power, byte num){
        return (int) (BasePayment + (num * Power));
    }

}
