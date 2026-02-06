package me.sfoow.prokits.Plots;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static me.sfoow.prokits.Ect.RegionUtil.isInRegion;
import static me.sfoow.prokits.Ect.utils.*;
import static me.sfoow.prokits.Plots.PlayerPlotData.PlayerPlots;
import static me.sfoow.prokits.Plots.PlotData.*;
import static me.sfoow.prokits.Plots.PlotsManager.getPriceOfPlot;


@Command("plots")
public class PlotCommand implements Listener {

    private static Location loc = new Location(Bukkit.getWorld("plots"),0.5,66,0.5);

    @Subcommand("tp")
    public void doPlotTp(Player sender){
        if (isInRegion(sender.getLocation(), "spawn") || sender.getWorld().getName().equals("plots")){
            sender.teleport(loc);
            SendYes(sender);
            Qwerty(sender,"&aSending you to the red plots");

        } else {
            Qwerty(sender,"&cYou can only do this in spawn!");
            SendNo(sender);
        }

    }

    @Subcommand("info")
    public void InfoPlots(Player sender) {
        if (hasPlot(sender.getUniqueId())) {
            String PlotColor = PlayerPlots.get(sender.getUniqueId()).PlotColor;
            Qwerty(sender,"&fPlot Color: " + PlotColor + ", Plot id: " + PlayerPlots.get(sender.getUniqueId()).PlotId);
            StringBuilder PlayerList = new StringBuilder();

            for (UUID data : plots[getPlotColorIdInArray(PlayerPlots.get(sender.getUniqueId()).PlotColor)][PlayerPlots.get(sender.getUniqueId()).PlotId].Members){
                if (data != null){
                    PlayerList.append(Objects.requireNonNull(Bukkit.getOfflinePlayer(data)).getName()).append(",");
                }
            }

            Qwerty(sender,"&fPlayer List: " + PlayerList );
        } else{
            SendNo(sender);
            Qwerty(sender,"&cYou do not have a plot");
        }
    }
    @Subcommand("leave")
    public void LeavePlots(Player sender){
        if (hasPlot(sender.getUniqueId())){
            byte color = getPlotColorIdInArray(PlayerPlots.get(sender.getUniqueId()).PlotColor);
            byte id = PlayerPlots.get(sender.getUniqueId()).PlotId;
            if (plots[color][id].PlotOwner.equals(sender.getUniqueId())) {

                Location loc = getLocationPlot(PlayerPlots.get(sender.getUniqueId()).PlotColor,id);

                Block block = loc.getBlock();
                block.setType(Material.OAK_SIGN);

                Rotatable signRotatable = (Rotatable) block.getBlockData();
                signRotatable.setRotation(BlockFace.WEST);
                block.setBlockData(signRotatable);


                if (!(block.getState() instanceof Sign sign)) return;
                SignSide side = sign.getSide(Side.FRONT);

                int price = getPriceOfPlot(getSizeOfPlotForColor(color));

                side.setLine(0, "Claimed By:");
                side.setLine(1, "--------");
                side.setLine(2, "Expiration Date:");
                side.setLine(3, "Price of: " + price);

                sign.setColor(DyeColor.GREEN);
                sign.setWaxed(true);
                sign.setGlowingText(true);

                sign.update();

                for (UUID uuid: plots[color][id].Members){
                    if (uuid != null){
                        Qwerty(Bukkit.getPlayer(uuid),"&a" + sender.getName() + " sold his plot!");
                    }
                }


                plots[color][id].Members = new ArrayList<>(1);

                UnclaimPlot(color,id);
                PlayerPlots.remove(sender.getUniqueId());
                File e = new File("plugins/prokits/playerplotdata/" + sender.getUniqueId() + ".yml");
                e.delete();
                SendYes(sender);
                Qwerty(sender,"&aRemoved You From plot " + color + " and id " + id + " (no refund since taxes)");
            } else {
                Qwerty(sender,"sooo if your see this than something went wrong your going to want to make a ticket and ping @sfoow");
            }
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou do not have a plot");
        }
    }

    @Subcommand("addplayer")
    public void AddPlayerToPlot(Player sender,Player targets){
        if (hasPlot(sender.getUniqueId())){
            if (sender.getName().equals(targets.getName())){
                Qwerty(sender,"&cEhhh no");
                SendNo(sender);
                return;
            }
            byte color = getPlotColorIdInArray(PlayerPlots.get(sender.getUniqueId()).PlotColor);
            byte id = PlayerPlots.get(sender.getUniqueId()).PlotId;
            if (plots[color][id].PlotOwner.equals(sender.getUniqueId())) {
                if (plots[color][id].Members.size() <= 8){
                    if (!plots[color][id].Members.contains(targets.getUniqueId())){
                        plots[color][id].Members.add(targets.getUniqueId());
                        Qwerty(sender,"&aAdded " + targets.getName() + " to your plot");
                        Qwerty(targets,"&aYou have been added to " + sender.getName() + "'s plot");
                        SendYes(sender);
                    } else {
                        Qwerty(sender,"&fThat Player is already in the list");
                        SendNo(sender);
                    }
                } else {
                    Qwerty(sender,"&cToo many players in your plot");
                    SendNo(sender);
                }
            } else {
                Qwerty(sender,"sooo if your see this than something went wrong your going to want to make a ticket and ping @sfoow");
            }
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou do not have a plot");
        }
    }

    @Subcommand("removeplayer")
    public void RemovePlayerFromPlot(Player sender,Player targets){
        if (!targets.isOnline()){
            Qwerty(sender,"&cThat player is not online");
            SendNo(sender);
            return;
        }
        if (hasPlot(sender.getUniqueId())){
            byte color = getPlotColorIdInArray(PlayerPlots.get(sender.getUniqueId()).PlotColor);
            byte id = PlayerPlots.get(sender.getUniqueId()).PlotId;
            if (plots[color][id].PlotOwner.equals(sender.getUniqueId())) {
                if (plots[color][id].Members.contains(targets.getUniqueId())){
                    plots[color][id].Members.remove(targets.getUniqueId());
                    Qwerty(sender,"&cRemoved " + targets.getName() + " from plot");
                    Qwerty(targets,"&cYou have been removed from " + sender.getName() + "'s plot");
                } else {
                    Qwerty(sender,"&cInvalid player, that player is not in the plots");

                    StringBuilder PlayerList = new StringBuilder();

                    for (UUID data : plots[color][id].Members){
                        PlayerList.append(Bukkit.getPlayer(data).getName()).append(",");
                    }

                    Qwerty(sender,"&fPossible Players include: " + PlayerList );
                    SendNo(sender);
                }
            } else {
                Qwerty(sender,"sooo if your see this than something went wrong your going to want to make a ticket and ping @sfoow");
            }
        } else {
            SendNo(sender);
            Qwerty(sender,"&cYou do not have a plot");
        }
    }



    private static boolean hasPlot(UUID uuid){
        return PlayerPlots.containsKey(uuid);
    }
}
