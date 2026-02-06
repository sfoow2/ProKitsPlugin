package me.sfoow.prokits.Plots;

import me.sfoow.prokits.Ect.YamlManager;

public class PlotsDataUitls {

    public static void CreateNewPlotsUtils(YamlManager yaml, byte plotsize, byte plotoffsets, byte plotamount){
        yaml.set("PlotSizes",plotsize);
        yaml.set("PlotOffsets",plotoffsets);

        yaml.set("PlotXZSize",plotamount);

        yaml.set("PlotColorStartingLoc.x",0);
        yaml.set("PlotColorStartingLoc.y",0);
        yaml.set("PlotColorStartingLoc.z",0);

        yaml.set("PlotPos1.x",0);
        yaml.set("PlotPos1.y",0);
        yaml.set("PlotPos1.z",0);

        yaml.set("PlotPos2.x",0);
        yaml.set("PlotPos2.y",0);
        yaml.set("PlotPos2.z",0);

        yaml.set("PlotXZSize",plotamount);

        for (byte x = 0; x < plotamount * plotamount; x++){
            yaml.set("Plot" + x + ".PlotOwner","null");
            yaml.set("Plot" + x + ".TimeLeft","-1");
            yaml.set("Plot" + x + ".Members.0","null");
        }

        yaml.save();
    }


}
