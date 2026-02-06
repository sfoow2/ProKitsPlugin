package me.sfoow.prokits.Quests;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class QuestPlayerData {

    private static String QuestPaths = "plugins/prokits/quests/";

    public static HashMap<UUID, ArrayList<QuestData>> PlayerQuests = new HashMap<>();

    public static class QuestData{
        byte id = -1;
        byte progress = -1;
        byte maxprogress = -1;
        byte slot = -1;
        byte updown = -1;
    }

    public static void DoPlayerSaveQuestsProg(Player player){
        if (PlayerQuests.containsKey(player.getUniqueId())){

            if (!isQuestsAllNull(player.getUniqueId())){
                return;
            }

            YamlManager data = new YamlManager(QuestPaths + player.getUniqueId() + ".yml");

            ArrayList<QuestData> quests = PlayerQuests.get(player.getUniqueId());

            byte count = 0;

            for (QuestData que : quests){
                data.set("q" + count + ".id", que.id);
                data.set("q" + count + ".progress", que.progress);
                data.set("q" + count + ".maxprogress", que.maxprogress);
                data.set("q" + count + ".slot", que.slot);
                data.set("q" + count + ".updown", que.updown);
                count++;
            }

            data.save();
        }
    }

    private static boolean isQuestsAllNull(UUID uuid){
        boolean data = true;
        byte counter = 0;
        for (me.sfoow.prokits.Quests.QuestPlayerData.QuestData qest : PlayerQuests.get(uuid)){
            if (qest.id != -1){
                data = false;
                return true;
            }
            counter++;
        }
        return false;
    }


    public static void SafeDeleteQuestsData(Player player){
        PlayerQuests.remove(player.getUniqueId());
    }

    public static void LoadPlayerQuests(Player player){
        if (new File(QuestPaths + player.getUniqueId() + ".yml").exists()){
            YamlManager data = new YamlManager(QuestPaths + player.getUniqueId() + ".yml");

            byte count = 0;
            while (data.get("q" + count + ".id") != null){
                count++;
            }

            ArrayList<QuestData> list = new ArrayList<>(count);

            count = 0;
            while (data.get("q" + count + ".id") != null){
                QuestData questdata = new QuestData();
                questdata.id =data.getByte("q" + count + ".id");
                questdata.progress =data.getByte("q" + count + ".progress");
                questdata.maxprogress =data.getByte("q" + count + ".maxprogress");
                questdata.slot =data.getByte("q" + count + ".slot");
                questdata.updown =data.getByte("q" + count + ".updown");
                list.add(questdata);
                count++;
            }

            PlayerQuests.put(player.getUniqueId(),list);
        } else {
            PlayerQuests.put(player.getUniqueId(),new ArrayList<>(5));
            for (int x = 0; x < 5; x++){
                PlayerQuests.get(player.getUniqueId()).add(new QuestPlayerData.QuestData());
            }

        }
    }




}
