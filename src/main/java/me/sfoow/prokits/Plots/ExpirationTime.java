    package me.sfoow.prokits.Plots;

    import org.bukkit.Bukkit;
    import org.bukkit.entity.Player;

    import static me.sfoow.prokits.Plots.PlotData.*;
    import static me.sfoow.prokits.Plots.PlotsManager.getUnix;
    import static me.sfoow.prokits.Prokits.plugin;

    public class ExpirationTime {



        public static void DoExpirationTimerClock(){
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,() -> {
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
            },72000L,72000L);
        }


    }
