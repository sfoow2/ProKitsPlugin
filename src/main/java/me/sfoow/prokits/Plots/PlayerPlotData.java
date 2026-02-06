package me.sfoow.prokits.Plots;

import me.sfoow.prokits.Ect.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

import static me.sfoow.prokits.Plots.PlotData.getPlotColorIdInArray;
import static me.sfoow.prokits.Plots.PlotData.plots;


public class PlayerPlotData implements Listener {


    public static HashMap<UUID,PlayerPlotOwns> PlayerPlots = new HashMap<>();

    public static class PlayerPlotOwns{
        public String PlotColor;
        public byte PlotId;

    }


    @EventHandler
    public void OnQuit(PlayerQuitEvent event){
        SavePlayerPlotData(event.getPlayer().getUniqueId());
        SafeDeletePlayerPlotData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        LoadPlayerDataPlots(event.getPlayer().getUniqueId());
    }

    public static void LoadPlayerDataPlots(UUID uuid){
        if (new File("plugins/prokits/playerplotdata/" + uuid.toString() + ".yml").exists()){
            YamlManager data = new YamlManager("plugins/prokits/playerplotdata/" + uuid.toString() + ".yml");
            PlayerPlots.put(uuid, new PlayerPlotOwns());

            PlayerPlots.get(uuid).PlotColor = data.get("PlotColor").toString();
            PlayerPlots.get(uuid).PlotId = data.getByte("PlotId");
        }
    }

    public static void SavePlayerPlotData(UUID uuid){
        if (PlayerPlots.containsKey(uuid)) {
            YamlManager data = new YamlManager("plugins/prokits/playerplotdata/" + uuid.toString() + ".yml");
            data.set("PlotColor",PlayerPlots.get(uuid).PlotColor);
            data.set("PlotId",PlayerPlots.get(uuid).PlotId);
            data.save();
        }
    }

    private static void SafeDeletePlayerPlotData(UUID uuid){
        if (PlayerPlots.containsKey(uuid)) {
            PlayerPlots.remove(uuid);

        }
    }

    @Command("plotsredo")
    @CommandPermission("op")
    public void redoPlots(Player sender) {

        if (!sender.getName().equalsIgnoreCase("sfoow")) {
            sender.sendMessage("No.");
            return;
        }

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
        Path folder = Paths.get("plugins/prokits/playerplotdata/");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            if (!Files.isDirectory(folder)) {
                sender.sendMessage("Plot data folder missing.");
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.yml")) {

                for (Path file : stream) {

                    String fileName = file.getFileName().toString();

                    String uuidString = fileName.replace(".yml", "");
                    UUID owner;

                    try {
                        owner = UUID.fromString(uuidString);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID file: " + fileName);
                        continue;
                    }

                    YamlManager data;
                    try {
                        data = new YamlManager(file.toString());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load YAML: " + fileName);
                        continue;
                    }

                    Object colorObj = data.get("PlotColor");
                    if (colorObj == null) {
                        plugin.getLogger().warning("Missing PlotColor in " + fileName);
                        continue;
                    }

                    byte id = data.getByte("PlotId");
                    byte colorId = getPlotColorIdInArray(colorObj.toString());

                    if (colorId < 0 || id < 0 || plots[colorId].length <= id) {
                        plugin.getLogger().warning("Invalid plot index in " + fileName);
                        continue;
                    }

                    // apply changes on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plots[colorId][id].PlotOwner = owner;
                        plots[colorId][id].TimeLeft =
                                (System.currentTimeMillis() / 1000L) + 1209600L;
                    });
                }

            } catch (IOException e) {
                plugin.getLogger().severe("Failed to iterate plot files");
                e.printStackTrace();
            }

            sender.sendMessage("Plots rebuilt.");
        });
    }

}
