package me.sfoow.prokits;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Suggest;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import static me.sfoow.prokits.Ect.utils.*;

public class CustomRanks {

    private static YamlManager CustomRankYaml;
    private static HashMap<String, Location> PaidRanksHashMapLocation = new HashMap<>(50);
    private static HashMap<String, UUID> PaidRankPlayer = new HashMap<>();

    private static String[] AllCustomIslands = new String[]{"yapple","polar","ducky","penguin"};

    public static void LoadUpPrivateRankHashMapLocation(){
        CustomRankYaml = new YamlManager("plugins/prokits/customranktp.yml");

        for (String st: AllCustomIslands){
            Integer x = CustomRankYaml.getInt(st + ".x");
            Integer y = CustomRankYaml.getInt(st + ".y");
            Integer z = CustomRankYaml.getInt(st + ".z");

            if (x == null){
                return;
            }

            UUID uuid = UUID.fromString((String) CustomRankYaml.get(st + ".uuid"));

            Location loc = new Location(Bukkit.getWorld("paidrank"),x,y,z).add(new Vector(0.5,0,0.5));


            PaidRanksHashMapLocation.put(st,loc);
            PaidRankPlayer.put(st,uuid);
        }



    }

    private final static String[] ListOfCustomIslands = new String[]{"yapple","polar","ducky","penguin"};

    @Command("customranktp")
    public void DoCustomRankTp(Player sender, @Suggest({"yapple","polar","ducky","penguin"}) String island){
        if (sender.hasPermission("customranktp")){
            boolean found = false;
            for (String st : ListOfCustomIslands){
                if (st.equals(island)){
                    found = true;
                    break;
                }
            }
            if (found){
                if (PaidRankPlayer.get(island).equals(sender.getUniqueId())) {
                    sender.teleport(PaidRanksHashMapLocation.get(island));
                    SendYes(sender);
                    Qwerty(sender,"&aSending You To Your Island!");
                } else {
                    if (sender.hasPermission("op")){
                        sender.teleport(PaidRanksHashMapLocation.get(island));
                        SendYes(sender);
                    } else {
                        SendNo(sender);
                        Qwerty(sender,"&cYou dont have permissions for this!");
                    }
                }
            } else {
                SendNo(sender);
                Qwerty(sender,"&cThat island does not exist");
            }
        } else {
            Qwerty(sender,"&cYou dont have permssions for this");
            SendNo(sender);
        }
    }
}
