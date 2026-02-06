package me.sfoow.prokits;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.eclipse.sisu.Priority;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;
import static me.sfoow.prokits.Ect.placeholder.getPlaceholder;
import static me.sfoow.prokits.Ect.utils.Qwerty;
import static me.sfoow.prokits.Ect.utils.ServerPrefixFront;
import static me.sfoow.prokits.LevelSystem.DoPlayerLevelUpCheck;
import static me.sfoow.prokits.LevelSystem.getXpNeededPerLevel;
import static me.sfoow.prokits.Data.PlayerData.*;
import static me.sfoow.prokits.Prokits.plugin;
import static me.sfoow.prokits.Ect.RandomKit.giveRandomKit;
import static me.sfoow.prokits.Quests.QuestPlayerData.*;
import static me.sfoow.prokits.items.Token;

public class server implements Listener {

    public static final Location SpawnLocation = new Location(Bukkit.getWorld("spawn"),103.5,73,125,270,0);
    private static Random rand = new Random();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(ChatColor.translateAlternateColorCodes(
                '&', "&a&l+ &f" + player.getName()
        ));

        player.teleport(SpawnLocation);

        LoadPlayerData(player);
        PlayerXpNeeded.put(player.getUniqueId(),getXpNeededPerLevel(PlayerLevels.get(player.getUniqueId())));

        if (!event.getPlayer().hasPlayedBefore()) {
            giveRandomKit(player);
        }

        DoPlayerLevelUpCheck(player);

        PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 1);
        player.addPotionEffect(effect);

        LoadPlayerQuests(player);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&',"&c&l- &r" + event.getPlayer().getName()));
        SavePlayerData(event.getPlayer());
        UnLoadPlayerData(event.getPlayer());

        DoPlayerSaveQuestsProg(event.getPlayer());
        SafeDeleteQuestsData(event.getPlayer());
    }



    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        Player victim = e.getEntity();

        Player attacker;

        if (e.getDamageSource().getCausingEntity() instanceof Player p) {
            attacker = p;
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',"&4☠ &f" + p.getName() + "&c Killed &f" + victim.getName() + "&c " + Math.floor(attacker.getHealth()) + "&4❤"));

        } else if (e.getDamageSource().getCausingEntity() instanceof Projectile proj &&
                proj.getShooter() instanceof Player p) {
            attacker = p;
        } else {
            attacker = null;
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&',"&4☠ &f" + victim.getName() + "&c Died"));

        }

        if (attacker == null) return;

        UUID aUUID = attacker.getUniqueId();
        UUID vUUID = victim.getUniqueId();

        // Stats
        PlayerDeaths.put(vUUID, PlayerDeaths.getOrDefault(vUUID, 0) + 1);
        PlayerKills.put(aUUID, PlayerKills.getOrDefault(aUUID, 0) + 1);

        PlayerKillStreak.put(vUUID, 0);
        PlayerKillStreak.put(aUUID, PlayerKillStreak.getOrDefault(aUUID, 0) + 1);

        int streak = PlayerKillStreak.get(aUUID);
        if (streak % 5 == 0) {
            Broadcast(ServerPrefixFront + "&c" + attacker.getName()
                    + " is on a &e" + streak + " killstreak!");
        }

        // Effects
        attacker.sendActionBar(ChatColor.RED + "[-" + victim.getName() + "-]");
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1f, 1f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            attacker.sendActionBar(ChatColor.translateAlternateColorCodes(
                    '&', "&c[-&m" + victim.getName() + "-&r&c]"));
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
        }, 10L);


        int levelxp = PlayerUpgradeLevelXp.get(attacker.getUniqueId());
        int leveltoken = PlayerUpgradeLevelTokens.get(attacker.getUniqueId());

        int xpMin = (int) (1 + (levelxp * 1.5));
        int xpMax = (int) (6 + (levelxp * 1.2));
        if (xpMax <= xpMin) xpMax = xpMin + 1; // ensure bound > origin
        int xp = ThreadLocalRandom.current().nextInt(xpMin, xpMax);

        int tokensMin = (int) (1 + (leveltoken * 1.5));
        int tokensMax = (int) (3 + (leveltoken * 1.2));
        if (tokensMax <= tokensMin) tokensMax = tokensMin + 1;
        int tokens = ThreadLocalRandom.current().nextInt(tokensMin, tokensMax);

        attacker.sendTitle("", ChatColor.translateAlternateColorCodes('&', "&e+ " + tokens + " tokens &b" + xp + " xp"));

        PlayerXp.put(aUUID, PlayerXp.getOrDefault(aUUID, 0) + xp);
        DoPlayerLevelUpCheck(attacker);

        // Drops
        Location dropLoc = victim.getLocation();

        ItemStack tokenDrop = Token.clone();
        tokenDrop.setAmount(tokens);
        dropLoc.getWorld().dropItemNaturally(dropLoc, tokenDrop);

    }

    private static void Broadcast(String st){
        String message = ChatColor.translateAlternateColorCodes('&',st);
        for (Player player: Bukkit.getOnlinePlayers()){
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        event.getPlayer().teleport(SpawnLocation);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getPlayer().teleport(SpawnLocation);
            giveRandomKit(event.getPlayer());
        },2L);
    }

    @EventHandler
    public void onPearlTeleport(PlayerTeleportEvent event) {

        // Only care about ender pearls
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

        // Safety check (Paper/Bukkit sometimes)
        if (event.getTo() == null) return;

        if (!getPlaceholder(event.getPlayer(),"%combatlogx_time_left%").equals("0")) {
            if (isInRegion(event.getTo(), "spawn")) {
                event.setCancelled(true);
            }
        }
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getType().equals(Material.TNT)){
            if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)){
                    event.getBlock().setType(Material.AIR);
                    TNTPrimed tnt = event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(new Vector(0.5,0,0.5)), TNTPrimed.class);
                    tnt.setFuseTicks(100);
                    if (rand.nextDouble() < 0.05){
                        tnt.setYield(12f);
                    } else {
                        tnt.setYield(rand.nextInt(5,6));
                    }
                    tnt.setSource(event.getPlayer());
            }
        } else if (event.getBlock().getType().equals(Material.REDSTONE_WIRE) || event.getBlock().getType().equals(Material.REDSTONE_TORCH)) {
            if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)){
                event.setCancelled(true);
            }
        } if (isInRegion(event.getBlock().getLocation(),"shulkeroutside")){
            Location loc = event.getBlock().getLocation().add(new Vector(0,-1,0));
            if (!loc.getBlock().getType().equals(Material.BEDROCK)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.SHULKER_BOX) return;

        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String sealed = ChatColor.translateAlternateColorCodes('&', "&7(Sealed)");

        if (meta.getDisplayName().contains(sealed)) {
            meta.setDisplayName(meta.getDisplayName().replace(sealed, ""));
            item.setItemMeta(meta);
            event.getPlayer().getInventory().setItemInMainHand(item);
        }
    }



    @EventHandler
    public void OnBlockBreak(BlockBreakEvent event){
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE){
            if (isInRegion(event.getBlock().getLocation(),"arena")) {
                if (event.getBlock().getType().equals(Material.IRON_ORE) || event.getBlock().getType().equals(Material.GOLD_ORE)){
                    Player player = event.getPlayer();
                    ItemStack tool = player.getInventory().getItemInMainHand();
                    Collection<ItemStack> drops = event.getBlock().getDrops(tool, player);
                    Location droploc = event.getBlock().getLocation().add(new Vector(0.5, 0.5, 0.5));
                    for (ItemStack drop : drops) {
                        if (drop.getType().equals(Material.RAW_IRON)) {
                            drop.setType(Material.IRON_INGOT);
                            player.getWorld().dropItem(droploc, drop);
                        } else if (drop.getType().equals(Material.RAW_GOLD)) {
                            drop.setType(Material.GOLD_INGOT);
                            player.getWorld().dropItem(droploc, drop);
                        }
                    }
                    event.getBlock().setType(Material.AIR);
                    event.setCancelled(true);
            }}
        }
    }

    @EventHandler
    public void OnAnvilDamage(AnvilDamagedEvent event){
        event.setCancelled(true);
    }

    @Command("clearchat")
    @CommandPermission("op")
    public void ClearChat(Player sender){
        for (int x = 0; x < 150; x++){
            for (Player player : Bukkit.getOnlinePlayers()){
                player.sendMessage("                                             ");
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aChat Was Cleared By " + sender.getName())  );
        }

    }

    @EventHandler
    public void onTotemPop(EntityResurrectEvent event){
        PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 1);
        event.getEntity().addPotionEffect(effect);
    }


    private static final Set<Material> NO_DROP_BLOCKS = new HashSet<>(Arrays.asList(
            Material.SANDSTONE,
            Material.MOSS_BLOCK,
            Material.GRASS_BLOCK,
            Material.RED_SANDSTONE,
            Material.PODZOL
    ));

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity().getType().equals(EntityType.WIND_CHARGE)){
            return;
        }
        for (Block block : event.blockList()) {
            if (NO_DROP_BLOCKS.contains(block.getType())) {
                block.setType(Material.AIR);
            }
        }
        event.blockList().removeIf(block -> NO_DROP_BLOCKS.contains(block.getType()));
    }

    @Command("playtimeset")
    @CommandPermission("op")
    public void setPlayerTimeOfPlayer(Player sender, Player target, int time){
        target.setStatistic(Statistic.PLAY_ONE_MINUTE,time);


    }


}
