package me.sfoow.prokits;

import me.sfoow.prokits.Data.Economy;
import me.sfoow.prokits.Data.PayCommand;
import me.sfoow.prokits.Data.PlayerData;
import me.sfoow.prokits.Ect.*;

import me.sfoow.prokits.Plots.PlayerPlotData;
import me.sfoow.prokits.Plots.PlotCommand;
import me.sfoow.prokits.Plots.PlotData;
import me.sfoow.prokits.Plots.PlotsManager;
import me.sfoow.prokits.Quests.QuestGui;
import me.sfoow.prokits.Quests.QuestProgress;
import me.sfoow.prokits.event.CustomItemsDrop;
import me.sfoow.prokits.event.EventStarter;
import me.sfoow.prokits.event.MoshPit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitLamp;


import static me.sfoow.prokits.CustomRanks.LoadUpPrivateRankHashMapLocation;
import static me.sfoow.prokits.Ect.Afk.DefineAfkShard;
import static me.sfoow.prokits.Ect.Afk.DoAfkLoop;
import static me.sfoow.prokits.Ect.Walls.startDailyCheck;
import static me.sfoow.prokits.LevelSystem.getXpNeededPerLevel;
import static me.sfoow.prokits.PaidRanks.AutoReloadPaidRankYaml;
import static me.sfoow.prokits.PaidRanks.LoadIslandYaml;
import static me.sfoow.prokits.Data.PlayerData.*;

import static me.sfoow.prokits.Plots.PlayerPlotData.LoadPlayerDataPlots;
import static me.sfoow.prokits.Plots.PlayerPlotData.SavePlayerPlotData;
import static me.sfoow.prokits.Plots.PlotData.*;
import static me.sfoow.prokits.Quests.QuestGui.LoadInNewQuests;
import static me.sfoow.prokits.Quests.QuestPlayerData.DoPlayerSaveQuestsProg;
import static me.sfoow.prokits.Quests.QuestPlayerData.LoadPlayerQuests;
import static me.sfoow.prokits.Quests.Questdata.LoadUpQuestYaml;
import static me.sfoow.prokits.event.EventStarter.DoRandomEventLoops;
import static me.sfoow.prokits.event.EventStarter.TryToStopCurrentEvent;
import static me.sfoow.prokits.items.SetupItems;
import static me.sfoow.prokits.mines.ResetMainMine;
import static me.sfoow.prokits.mines.deResetMinesLoop;
import static me.sfoow.prokits.mines.CurrentPitMat;

public final class Prokits extends JavaPlugin implements Listener {


    public static JavaPlugin plugin;

    @Override
    public void onEnable() {

        for (Player player: Bukkit.getOnlinePlayers()){
            LoadPlayerData(player);
            LoadPlayerDataPlots(player.getUniqueId());
            LoadPlayerQuests(player);
            PlayerXpNeeded.put(player.getUniqueId(),getXpNeededPerLevel(PlayerLevels.get(player.getUniqueId())));
        }

        Bukkit.getLogger().info("Starting ProKits");
        long now = System.currentTimeMillis();
        plugin = this;

        LoadIslandYaml();

        Bukkit.getPluginManager().registerEvents(new server(), this);
        Bukkit.getPluginManager().registerEvents(new Stats(), this);
        Bukkit.getPluginManager().registerEvents(new LevelsGui(), this);
        Bukkit.getPluginManager().registerEvents(new Bank(), this);
        Bukkit.getPluginManager().registerEvents(new UpgradeGUI(), this);
        Bukkit.getPluginManager().registerEvents(new PaidRanks(), this);
        Bukkit.getPluginManager().registerEvents(new settings(), this);
        Bukkit.getPluginManager().registerEvents(new ShulkerRooms(), this);
        Bukkit.getPluginManager().registerEvents(new InvSee(), this);
        Bukkit.getPluginManager().registerEvents(new ExplosionLootProtect(), this);
        Bukkit.getPluginManager().registerEvents(new MoshPit(), this);
        Bukkit.getPluginManager().registerEvents(new PlotsManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerPlotData(), this);
        Bukkit.getPluginManager().registerEvents(new PlotCommand(), this);
        Bukkit.getPluginManager().registerEvents(new QuestGui(), this);
        Bukkit.getPluginManager().registerEvents(new QuestProgress(), this);

        var lamp = BukkitLamp.builder(this).build();
        lamp.register(new spawn());
        lamp.register(new Stats());
        lamp.register(new Economy());
        lamp.register(new items());
        lamp.register(new PaidRanks());
        lamp.register(new LevelsGui());
        lamp.register(new Daily());
        lamp.register(new PayCommand());
        lamp.register(new Bank());
        lamp.register(new server());
        lamp.register(new UpgradeGUI());
        lamp.register(new settings());
        lamp.register(new ShulkerRooms());
        lamp.register(new mines());
        lamp.register(new CustomRanks());
        lamp.register(new InvSee());
        lamp.register(new MoshPit());
        lamp.register(new ExplosionLootProtect());
        lamp.register(new Walls());
        lamp.register(new PlotData());
        lamp.register(new PlotCommand());
        lamp.register(new QuestGui());
        lamp.register(new PlayerPlotData());
        lamp.register(new CustomItemsDrop());
        lamp.register(new EventStarter());

        lamp.register(new Afk());


        DoBackUpLoop();
        DoAfkLoop();
        SetupItems();
        deResetMinesLoop();
        AutoReloadPaidRankYaml();
        LoadUpPrivateRankHashMapLocation();
        DoRandomEventLoops();
        startDailyCheck();
        LoadUpYamlDataForPlots();
        DoPlotsBackup();
        LoadUpQuestYaml();

        LoadInNewQuests();

        DefineAfkShard();

        if (!VaultUtils.setupEconomy()) {
            getLogger().severe("Vault not found or no economy plugin installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            for (Player player: Bukkit.getOnlinePlayers()){
                runServerCommand("ajleaderboards updateplayer * " + player.getName());
            }
        },6000L,6000L);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new placeholder().register();
        }

        Bukkit.getLogger().info("Finished loading ProKits in " + (System.currentTimeMillis() - now) + "ms");

        CurrentPitMat = Material.SANDSTONE;
        Bukkit.getScheduler().runTaskLater(this, mines::ResetMainMine,25L);

    }

    private static void runServerCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Doing A Player Backup");
        TryToStopCurrentEvent();
        SaveYamlDataForPlots();
        for (Player player : Bukkit.getOnlinePlayers()) {
            SavePlayerData(player);
            SavePlayerPlotData(player.getUniqueId());
            DoPlayerSaveQuestsProg(player);
        }
        if (plugin != null){
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }

}
