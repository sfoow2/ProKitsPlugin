package me.sfoow.prokits.Plots;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.sfoow.prokits.Ect.VaultUtils.*;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Plots.PlayerPlotData.PlayerPlots;
import static me.sfoow.prokits.Plots.PlotData.*;
import static me.sfoow.prokits.Prokits.plugin;


public class PlotsManager implements Listener {

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event){
        if (event.getPlayer().getWorld().getName().equals("plots")){
            if (event.getClickedBlock() == null){
                return;
            }
            Byte id = getPlotColorFromLoc(event.getClickedBlock().getLocation());
            if (id != -1){
                Byte PlotId = getPlotIdFromLocation(ListOfAllPlots[id], Objects.requireNonNull(event.getClickedBlock().getLocation())); //needs to set byte with uppercase b since needs to make it possible for it to be null
                if (PlotId != null){
                    Boolean result = isPlayerAllowedToDoThis(id, PlotId, event.getPlayer().getUniqueId());
                    if (Boolean.TRUE.equals(result)){

                    } else if (Boolean.FALSE.equals(result)){
                        event.setCancelled(true);
                        Qwerty(event.getPlayer(),"&cYou don't have access to this plot");
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                            if (event.getPlayer().getInventory().getItemInHand().getType().equals(Material.WOODEN_AXE)){
                                event.setCancelled(true);
                            }
                        }
                    } else if (result == null){
                        event.setCancelled(true);
                        event.getPlayer().sendActionBar(ChatColor.translateAlternateColorCodes('&',"&cThis Plot is not bought!"));
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                        }
                    }
                } else {
                    event.setCancelled(true);
                    if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)){
                        if (isByableSign(event.getClickedBlock().getLocation())) {
                            Byte Plotidsecond = getPlotidFromSign(ListOfAllPlots[id], Objects.requireNonNull(event.getClickedBlock().getLocation()));
                            if (plots[id][Plotidsecond].PlotOwner == null){
                                if (event.getPlayer().isSneaking()) {
                                    event.setCancelled(true);
                                    event.getPlayer().closeInventory();
                                    Bukkit.getScheduler().runTaskLater(plugin,()->{
                                        PlayerTriesToBuyPlot(event.getPlayer(), id, Plotidsecond);
                                    },4L);
                                } else {
                                    Qwerty(event.getPlayer(), "&aSneak To Buy This Plot.");
                                }
                            } else {
                                if (plots[id][Plotidsecond].PlotOwner.equals(event.getPlayer().getUniqueId())){
                                    if (event.getPlayer().isSneaking()){
                                        TryToAddMoreTimeToPlot(event.getPlayer(),id,Plotidsecond);
                                    } else {
                                        Qwerty(event.getPlayer(), "&aShift right click to pay for another week");
                                        long remaining = getUnix() - plots[id][Plotidsecond].TimeLeft;
                                        long days = TimeUnit.SECONDS.toDays(remaining);
                                        long hours = TimeUnit.SECONDS.toHours(remaining) % 24;
                                        long minutes = TimeUnit.SECONDS.toMinutes(remaining) % 60;
                                        long seconds = remaining % 60;

                                        String TimeLeft = Math.abs(days) + "d " + Math.abs(hours) + "h " + Math.abs(minutes) + "m " + Math.abs(seconds);
                                        Qwerty(event.getPlayer(), "&fThere is: " + TimeLeft + "s left");
                                    }
                                } else {
                                    Qwerty(event.getPlayer(), "&aThat plot is already bought.");
                                }
                            }
                        }
                    } if (event.getPlayer().hasPermission("op")){
                        event.setCancelled(false);
                    }
                }
            } else {
                event.setCancelled(true);
                if (event.getPlayer().hasPermission("op")){
                    event.setCancelled(false);
                }
            }
        }
    }

    private static void TryToAddMoreTimeToPlot(Player player, byte color, byte id){
        if (plots[color][id].PlotOwner.equals(player.getUniqueId())){
            byte PlotSize = getSizeOfPlotForColor(color);
            int price = getPriceOfPlot(PlotSize);
            if (getBalance(player) > price){
                removeMoney(player,price);

                plots[color][id].TimeLeft += NextWeek;
                SendYes(player);
                Qwerty(player,"&aAdded one more week to your plot (no refunds)");

                Location signloc = getLocationPlot(ListOfAllPlots[color],id);
                Block block = signloc.getBlock();
                block.setType(Material.AIR);

                Bukkit.getScheduler().runTaskLater(plugin,() ->{
                    block.setType(Material.OAK_SIGN);

                    Rotatable signRotatable = (Rotatable) block.getBlockData();
                    signRotatable.setRotation(BlockFace.WEST);
                    block.setBlockData(signRotatable);

                    if (!(block.getState() instanceof Sign sign)) return;
                    SignSide side = sign.getSide(Side.FRONT);

                    side.setLine(0, "Claimed By:");
                    side.setLine(1, player.getName());
                    side.setLine(2, "Expiration Date:");
                    side.setLine(3, String.valueOf(plots[color][id].TimeLeft));

                    sign.setColor(DyeColor.RED);
                    sign.setWaxed(true);
                    sign.setGlowingText(true);

                    sign.update();
                },5L);
            }
        }
    }



    public static Byte getPlotidFromSign(String plotColor, Location loc) {

        byte plotColorId = getPlotColorIdInArray(plotColor);
        ColorPlotData data = GlobalPlots[plotColorId];

        Location start = data.StartLocation;

        int plotSize = data.PlotSize;
        int step = data.PlotSize + data.PlotOffSet;
        int plotXZSize = data.PlotXZSize;

        int dx = loc.getBlockX() - start.getBlockX();
        int dz = loc.getBlockZ() - (start.getBlockZ() - 1);

        // Outside grid entirely
        if (dx < 0 || dz < 0) return null;

        int row = dx / step;
        int col = dz / step;

        // Outside plot grid bounds
        if (row >= plotXZSize || col >= plotXZSize) return null;

        // Local position inside this plot
        int localX = dx % step;
        int localZ = dz % step;

        // Inside hallway / offset area → not a plot
        if (localX >= plotSize || localZ >= plotSize) return null;

        int id = row * plotXZSize + col;

        return (byte) id;
    }

    private static boolean isByableSign(Location loc){
        if (loc.clone().add(new Vector(0,-2,0)).getBlock().getType().equals(Material.FLETCHING_TABLE)){
            return loc.clone().add(new Vector(0, -3, 0)).getBlock().getType().equals(Material.SMITHING_TABLE);
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if (event.getPlayer().getWorld().getName().equals("plots")){
            byte id = getPlotColorFromLoc(event.getBlock().getLocation());
            if (id != -1){
                Byte PlotId = getPlotIdFromLocation(ListOfAllPlots[id],event.getBlock().getLocation()); //needs to set byte with uppercase b since needs to make it possible for it to be null
                if (PlotId != null){
                    Boolean result = isPlayerAllowedToDoThis(id, PlotId, event.getPlayer().getUniqueId());
                    if (Boolean.TRUE.equals(result)){

                    } else if (Boolean.FALSE.equals(result)){
                        event.setCancelled(true);
                        Qwerty(event.getPlayer(),"&cYou don't have access to this plot");
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                        }
                    } else if (result == null){
                        event.setCancelled(true);
                        Qwerty(event.getPlayer(),"&cThis plot is not claimed");
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                        }
                    }
                } else {
                    event.setCancelled(true);
                    if (event.getPlayer().hasPermission("op")){
                        event.setCancelled(false);
                    }
                }
            }else {
                event.setCancelled(true);
                if (event.getPlayer().hasPermission("op")){
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if (event.getPlayer().getWorld().getName().equals("plots")){
            byte id = getPlotColorFromLoc(event.getBlock().getLocation());
            if (id != -1){
                Byte PlotId = getPlotIdFromLocation(ListOfAllPlots[id],event.getBlock().getLocation()); //needs to set byte with uppercase b since needs to make it possible for it to be null
                if (PlotId != null){
                    Boolean result = isPlayerAllowedToDoThis(id, PlotId, event.getPlayer().getUniqueId());
                    if (Boolean.TRUE.equals(result)){

                    } else if (Boolean.FALSE.equals(result)){
                        event.setCancelled(true);
                        Qwerty(event.getPlayer(),"&cYou don't have access to this plot");
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                        }
                    } else if (result == null){
                        event.setCancelled(true);
                        Qwerty(event.getPlayer(),"&cThis plot is not claimed");
                        if (event.getPlayer().hasPermission("op")){
                            event.setCancelled(false);
                        }
                    }
                } else {
                    event.setCancelled(true);
                    if (event.getPlayer().hasPermission("op")){
                        event.setCancelled(false);
                        if (event.getPlayer().getInventory().getItemInHand().getType().equals(Material.WOODEN_AXE)){
                            event.setCancelled(true);
                        }
                    }
                }
            }else {
                event.setCancelled(true);
                if (event.getPlayer().hasPermission("op")){
                    event.setCancelled(false);
                    if (event.getPlayer().getInventory().getItemInHand().getType().equals(Material.WOODEN_AXE)){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    //true means player has access to that plot, false means that player does not have access to that plot and null means that the plot is not owned
    private static Boolean isPlayerAllowedToDoThis(byte ColorId, byte PlotId, UUID player){
        /*
        Needs to check if the plot is taken, if it is needs to check if owner if not owner than needs to check if in player list
         */

        PlotData.PlayerPlot data = plots[ColorId][PlotId];

        if (data.PlotOwner == null){
            return null;
        } else if (data.PlotOwner.equals(player)){
            return true;
        } else return data.Members.contains(player);
    }

    private static void PlayerTriesToBuyPlot(Player player, byte color, byte id){
        byte PlotSize = getSizeOfPlotForColor(color);
        int price = getPriceOfPlot(PlotSize);
        if (getBalance(player) > price){
            if (!hasPlot(player.getUniqueId())){
                removeMoney(player,price);
                Qwerty(player,"&fYou bough plot " + ListOfAllPlots[color] + " and plot id " + id + "# (" + getUnix() + ")");
                Qwerty(player,"&fTake picture if you want too keep this as proof");
                SendYes(player);


                //does player data
                PlayerPlots.put(player.getUniqueId(),new PlayerPlotData.PlayerPlotOwns());
                PlayerPlots.get(player.getUniqueId()).PlotColor = ListOfAllPlots[color];
                PlayerPlots.get(player.getUniqueId()).PlotId = id;

                //does server data
                plots[color][id].PlotOwner = player.getUniqueId();
                plots[color][id].TimeLeft = getUnix() + NextWeek;
                plots[color][id].Members = new ArrayList<>(1);


                player.closeInventory();
                player.closeInventory();
                player.closeInventory();

                Location signloc = getLocationPlot(ListOfAllPlots[color],id);
                Block block = signloc.getBlock();
                block.setType(Material.OAK_SIGN);


                Rotatable signRotatable = (Rotatable) block.getBlockData();
                signRotatable.setRotation(BlockFace.WEST);
                block.setBlockData(signRotatable);


                if (!(block.getState() instanceof Sign sign)) return;
                SignSide side = sign.getSide(Side.FRONT);

                side.setLine(0, "Claimed By:");
                side.setLine(1, player.getName());
                side.setLine(2, "Expiration Date:");
                side.setLine(3, String.valueOf(getUnix() + NextWeek));

                sign.setColor(DyeColor.RED);
                sign.setGlowingText(true);
                sign.setWaxed(true);

                sign.update();

                player.closeInventory();
                player.closeInventory();

                Bukkit.getScheduler().runTaskLater(plugin, () -> {

                    if (!(block.getState() instanceof Sign sign2)) return;
                    SignSide side2 = sign2.getSide(Side.FRONT);

                    side2.setLine(0, "Claimed By:");
                    side2.setLine(1, player.getName());
                    side2.setLine(2, "Expiration Date:");
                    side2.setLine(3, String.valueOf(getUnix() + NextWeek));

                    sign2.setColor(DyeColor.RED);
                    sign2.setGlowingText(true);

                    sign2.update();
                }, 20L);

            } else {
                Qwerty(player,"&cYou already own a plot");
                SendNo(player);
            }
        } else {
            Qwerty(player,"&cMissing Money!");
            SendNo(player);
        }
    }


    static int getPriceOfPlot(byte size){
        return (size / 10) * 1000;
    }

    static long getUnix(){
        return (System.currentTimeMillis()/1000);
    }

    private static final long NextWeek = 604800;

    private static boolean hasPlot(UUID uuid){
        return PlayerPlots.containsKey(uuid);
    }

}
