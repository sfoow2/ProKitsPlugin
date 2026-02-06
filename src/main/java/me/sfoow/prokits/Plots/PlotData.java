package me.sfoow.prokits.Plots;

import me.sfoow.prokits.Ect.YamlManager;
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
import org.bukkit.util.Vector;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static me.sfoow.prokits.Ect.FaweUtils.setCuboidFast;
import static me.sfoow.prokits.Ect.utils.Broadcast;
import static me.sfoow.prokits.Plots.ExpirationTime.DoExpirationTimerClock;
import static me.sfoow.prokits.Plots.PlayerPlotData.PlayerPlots;
import static me.sfoow.prokits.Plots.PlotsDataUitls.CreateNewPlotsUtils;
import static me.sfoow.prokits.Plots.PlotsManager.getPriceOfPlot;
import static me.sfoow.prokits.Plots.PlotsManager.getUnix;
import static me.sfoow.prokits.Prokits.plugin;

public class PlotData {

    public static YamlManager PlotsYaml;
    private final static String PlotsPath = "plugins/prokits/plots/";
    final static String[] ListOfAllPlots = new String[]{"red","blue"};

    public static PlayerPlot[][] plots;
    public static ColorPlotData[] GlobalPlots;

    public static class PlayerPlot {
        public UUID PlotOwner = null;
        public long TimeLeft = -1;
        public ArrayList<UUID> Members = null;
    }

    static class ColorPlotData {
        public Location StartLocation = null;
        public byte PlotOffSet = 0;
        public byte PlotSize = 0;
        public byte PlotXZSize = 0;

        public Location PlotColorCoordinate1 = null;
        public Location PlotColorCoordinate2 = null;
    }

    public static void UnclaimPlot(byte color, byte id){
        PutClainSign(color,id);

        UUID owner = plots[color][id].PlotOwner;
        if (owner != null) {
            return;
        };

        String st = plots[color][id].PlotOwner.toString();

        plots[color][id] = new PlayerPlot();

        Location l1 = getLocationPlot(ListOfAllPlots[color],id).add(new Vector(1,0,1));

        byte let = (byte) (GlobalPlots[color].PlotSize - 3);
        Location l2 = l1.clone().add(new Vector(let,0,let));
        l2.setWorld(Bukkit.getWorld("plots"));

        Location l3 = getPlotYBottom(l2.clone()).add(new Vector(0,1,0));
        l3.setWorld(Bukkit.getWorld("plots"));

        setCuboidFast(l1,l3, Material.GRASS_BLOCK);

        Location l4 = getPlotYTop(l2.clone()).add(new Vector(0,-1,0));
        l4.setWorld(Bukkit.getWorld("plots"));

        setCuboidFast(l1,l4, Material.AIR);


        if (st != null){
            new File("plugins/prokits/playerplotdata/" + st + ".yml").delete();
        }

    }

    public static void PutClainSign(byte color, byte id){

        Location loc = getLocationPlot(ListOfAllPlots[color],id);

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

    }


    private static Location getPlotYBottom(Location l1){
        while (!l1.getBlock().getType().equals(Material.BEDROCK)){
            l1.add(new Vector(0,-1,0));
        }
        return l1;
    }

    private static Location getPlotYTop(Location l1){
        while (!l1.getBlock().getType().equals(Material.BARRIER)){
            l1.add(new Vector(0,1,0));
        }
        return l1;
    }

    public static byte getPlotColorIdInArray(String st){
        if (st.equals("red")) {
            return 0;
        } else if (st.equals("blue")) {
            return 1;
        }
        return -1;
    }


    public static byte getSizeOfPlotForColor(byte x){
        return GlobalPlots[x].PlotSize;
    }

    public static void LoadUpYamlDataForPlots() {


        plots = new PlayerPlot[ListOfAllPlots.length][];
        GlobalPlots = new ColorPlotData[ListOfAllPlots.length];

        for (String st : ListOfAllPlots) {

            File file = new File(PlotsPath + st + ".yml");
            if (!file.exists()) {
                CreateNewPlotsUtils(
                        new YamlManager(file.getPath()),
                        (byte) 10,
                        (byte) 6,
                        (byte) 6
                );
                return;
            }

            PlotsYaml = new YamlManager(file.getPath());

            byte colorPlotId = getPlotColorIdInArray(st);

            // ✅ CREATE GlobalPlots object FIRST
            GlobalPlots[colorPlotId] = new ColorPlotData();


            GlobalPlots[colorPlotId].StartLocation = getLocationFromYaml(PlotsYaml, "PlotColorStartingLoc");

            GlobalPlots[colorPlotId].PlotSize = (byte) (PlotsYaml.getByte("PlotSizes") + 2);
            GlobalPlots[colorPlotId].PlotOffSet = PlotsYaml.getByte("PlotOffsets");
            GlobalPlots[colorPlotId].PlotXZSize = PlotsYaml.getByte("PlotXZSize");
            GlobalPlots[colorPlotId].PlotColorCoordinate1 = getLocationFromYaml(PlotsYaml, "PlotPos1");
            GlobalPlots[colorPlotId].PlotColorCoordinate2 = getLocationFromYaml(PlotsYaml, "PlotPos2");

            byte plotXZSize = PlotsYaml.getByte("PlotXZSize");
            int totalPlots = plotXZSize * plotXZSize;

            // ✅ CREATE array
            plots[colorPlotId] = new PlayerPlot[totalPlots];

            // ✅ CREATE each PlayerPlot object
            for (int i = 0; i < totalPlots; i++) {

                plots[colorPlotId][i] = new PlayerPlot();

                String uuid = PlotsYaml.getString("Plot" + i + ".PlotOwner");
                if (Objects.equals(uuid, "null") || uuid == null){
                    plots[colorPlotId][i].PlotOwner = null;
                } else {
                    plots[colorPlotId][i].PlotOwner = UUID.fromString(uuid);
                }

                plots[colorPlotId][i].TimeLeft = Long.parseLong(PlotsYaml.getString("Plot" + i + ".TimeLeft"));

                byte c = 0;

                for (int x = 0; x < 95; x++){
                    if (PlotsYaml.getString("Plot" + i + ".Members." + x) != null){
                        c++;
                    } else {
                        break;
                    }
                }

                ArrayList<UUID> memberlist = new ArrayList<>(c);

                for (int x = 0; x < 95; x++){
                    if (PlotsYaml.getString("Plot" + i + ".Members." + x) != null){
                        String val = PlotsYaml.getString("Plot" + i + ".Members." + x);
                        if (val.equals("null")){
                            memberlist.add(null);
                        } else {
                            memberlist.add(UUID.fromString(val));
                        }
                    } else {
                        break;
                    }
                }

                plots[colorPlotId][i].Members = memberlist;
            }
        }


        setupPlotColorGetMinMax();
        Bukkit.getScheduler().runTaskLater(plugin, ExpirationTime::DoExpirationTimerClock,5L);
        Bukkit.getScheduler().runTaskLater(plugin,() ->{
            for (String st : ListOfAllPlots) {
                byte color = getPlotColorIdInArray(st);
                byte xz = GlobalPlots[color].PlotXZSize;

                for (int x = 0; x < xz * xz; x++) {
                    if (plots[color][x].PlotOwner != null &&
                            plots[color][x].TimeLeft > 0 &&
                            plots[color][x].TimeLeft <= getUnix()) {

                        UnclaimPlot(color, (byte) x);
                    } else if (plots[color][x].PlotOwner == null){
                        PutClainSign(color, (byte) x);
                    }
                }
            }
        },25L);
    }


    public static void SaveYamlDataForPlots() {

        for (String st : ListOfAllPlots) {

            File file = new File(PlotsPath + st + ".yml");
            YamlManager yaml = new YamlManager(file.getPath());

            byte colorPlotId = getPlotColorIdInArray(st);
            if (GlobalPlots == null){
                return;
            }
            ColorPlotData data = GlobalPlots[colorPlotId];

            if (data == null) continue;

            // ===== GLOBAL PLOT DATA =====
//            yaml.set("PlotSizes", (byte) (data.PlotSize - 2));
//            yaml.set("PlotOffsets", data.PlotOffSet);
//            yaml.set("PlotXZSize", data.PlotXZSize);

//            setLocationToYaml(yaml, "PlotColorStartingLoc", data.StartLocation);
//            setLocationToYaml(yaml, "PlotPos1", data.PlotColorCoordinate1);
//            setLocationToYaml(yaml, "PlotPos2", data.PlotColorCoordinate2);

            // ===== PLAYER PLOTS =====
            int totalPlots = data.PlotXZSize * data.PlotXZSize;

            for (int i = 0; i < totalPlots; i++) {

                PlayerPlot plot = plots[colorPlotId][i];

                // Owner
                if (plot.PlotOwner == null) {
                    yaml.set("Plot" + i + ".PlotOwner", "null");
                } else {
                    yaml.set("Plot" + i + ".PlotOwner", plot.PlotOwner.toString());
                }

                // Time
                yaml.set("Plot" + i + ".TimeLeft", String.valueOf(plot.TimeLeft));

                // Members
                if (plot.Members == null || plot.Members.isEmpty()) {
                    yaml.set("Plot" + i + ".Members.0", "null");
                } else {
                    for (int m = 0; m < plot.Members.size(); m++) {
                        UUID member = plot.Members.get(m);
                        yaml.set(
                                "Plot" + i + ".Members." + m,
                                member == null ? "null" : member.toString()
                        );
                    }
                }
            }

            yaml.save();
        }
    }


    private static void setLocationToYaml(YamlManager yaml, String path, Location loc) {
        if (loc == null) {
            yaml.set(path + ".x", 0);
            yaml.set(path + ".y", 0);
            yaml.set(path + ".z", 0);
            return;
        }

        yaml.set(path + ".x", loc.getBlockX());
        yaml.set(path + ".y", loc.getBlockY());
        yaml.set(path + ".z", loc.getBlockZ());
    }


    @Command("dotp")
    @CommandPermission("op")
    public void DoTpCommand(Player sender, String colo, byte tp){
        sender.teleport(getLocationPlot(colo,tp));
    }



    private static void setupPlotColorGetMinMax() {

        ColorPlotsMinX = new int[ListOfAllPlots.length];
        ColorPlotsMaxX = new int[ListOfAllPlots.length];
        ColorPlotsMinZ = new int[ListOfAllPlots.length];
        ColorPlotsMaxZ = new int[ListOfAllPlots.length];

        for (String st : ListOfAllPlots) {
            byte id = getPlotColorIdInArray(st);

            Location pos1 = GlobalPlots[id].PlotColorCoordinate1;
            Location pos2 = GlobalPlots[id].PlotColorCoordinate2;

            ColorPlotsMinX[id] = Math.min(pos1.getBlockX(), pos2.getBlockX());
            ColorPlotsMaxX[id] = Math.max(pos1.getBlockX(), pos2.getBlockX());
            ColorPlotsMinZ[id] = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
            ColorPlotsMaxZ[id] = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        }
    }


    private static int[] ColorPlotsMinX;
    private static int[] ColorPlotsMaxX;

    private static int[] ColorPlotsMinZ;
    private static int[] ColorPlotsMaxZ;

    public static byte getPlotColorFromLoc(Location loc){
        for (String st : ListOfAllPlots) {
            byte id = getPlotColorIdInArray(st);

            int minX = ColorPlotsMinX[id];
            int maxX = ColorPlotsMaxX[id];
            int minZ = ColorPlotsMinZ[id];
            int maxZ = ColorPlotsMaxZ[id];

            if (loc.getX() >= minX && loc.getX() <= maxX && loc.getZ() >= minZ && loc.getZ() <= maxZ) {
                return id;
            }
        }
        return -1;
    }

    private static Location getLocationFromYaml(YamlManager yaml, String st){
        int x = yaml.getInt(st+".x");
        int y = yaml.getInt(st+".y");
        int z = yaml.getInt(st+".z");

        return new Location(Bukkit.getWorld("plots"),x,y,z).add(new Vector(0.5,0,0.5));
    }

    public static Location getLocationPlot(String plotColor, byte id) {
        byte plotcolorid = getPlotColorIdInArray(plotColor);
        byte PlotXZSize = GlobalPlots[plotcolorid].PlotXZSize;

        byte STEP = (byte) (GlobalPlots[plotcolorid].PlotSize + GlobalPlots[plotcolorid].PlotOffSet);
        int index = Byte.toUnsignedInt(id);

        int row = index / PlotXZSize; // X direction
        int col = index % PlotXZSize; // Z direction

        Location start = GlobalPlots[plotcolorid].StartLocation;

        return start.clone().add(row * STEP,0, col * STEP - 1);
    }


    public static Byte getPlotIdFromLocation(String plotColor, Location loc) {

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

        // Local position inside this plot cell
        int localX = dx % step;
        int localZ = dz % step;

        // Shrink plot by 2 blocks and shift by +1 X/Z
        // Valid area: [1 .. plotSize - 2]
        if (localX < 1 || localZ < 1) return null;
        if (localX > plotSize - 2 || localZ > plotSize - 2) return null;

        int id = row * plotXZSize + col;
        return (byte) id;
    }

    public static void DoPlotsBackup(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
            SaveYamlDataForPlots();
        },6000L,6000L);
    }



}
