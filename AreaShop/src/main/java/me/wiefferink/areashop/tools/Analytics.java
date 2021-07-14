package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.util.HashMap;
import java.util.Map;

public class Analytics {

    private Analytics() {

    }

    /**
     * Start analytics tracking.
     */
    public static void start() {
        // bStats statistics
        try {
            Metrics metrics = new Metrics(AreaShop.getInstance(), 32);

            // Number of regions
            metrics.addCustomChart(new SingleLineChart("region_count", () -> AreaShop.getInstance().getFileManager().getRegions().size()));

            // Number of rental regions
            metrics.addCustomChart(new SingleLineChart("rental_region_count", () ->AreaShop.getInstance().getFileManager().getRents().size()));

            // Number of buy regions
            metrics.addCustomChart(new SingleLineChart("buy_region_count", () -> AreaShop.getInstance().getFileManager().getBuys().size()));

            // Language
            metrics.addCustomChart(new SimplePie("language", () -> AreaShop.getInstance().getConfig().getString("language")));

            // Pie with region states
            metrics.addCustomChart(new AdvancedPie("region_state", () -> {
                Map<String, Integer> result = new HashMap<>();
                RegionStateStats stats = getStateStats();
                result.put("For Rent", stats.forrent);
                result.put("Rented", stats.rented);
                result.put("For Sale", stats.forsale);
                result.put("Sold", stats.sold);
                result.put("Reselling", stats.reselling);
                return result;
            }));

            // Time series of each region state
            metrics.addCustomChart(new SingleLineChart("forrent_region_count", () -> getStateStats().forrent));
            metrics.addCustomChart(new SingleLineChart("rented_region_count", () -> getStateStats().rented));
            metrics.addCustomChart(new SingleLineChart("forsale_region_count", () -> getStateStats().forsale));
            metrics.addCustomChart(new SingleLineChart("sold_region_count", () -> getStateStats().sold));
            metrics.addCustomChart(new SingleLineChart("reselling_region_count", () -> getStateStats().reselling));

            // TODO track rent/buy/unrent/sell/resell actions (so that it can be reported per collection interval)

            AreaShop.debug("Started bstats.org statistics service");
        } catch (Exception e) {
            AreaShop.debug("Could not start bstats.org statistics service");
        }
    }

    private static class RegionStateStats {
        int forrent = 0;
        int forsale = 0;
        int rented = 0;
        int sold = 0;
        int reselling = 0;
    }

    private static RegionStateStats getStateStats() {
        RegionStateStats result = new RegionStateStats();
        for (GeneralRegion region : AreaShop.getInstance().getFileManager().getRegions()) {
            if (region instanceof RentRegion rent) {
                if (rent.isAvailable()) {
                    result.forrent++;
                } else {
                    result.rented++;
                }
            } else if (region instanceof BuyRegion buy) {
                if (buy.isAvailable()) {
                    result.forsale++;
                } else if (buy.isInResellingMode()) {
                    result.reselling++;
                } else {
                    result.sold++;
                }
            }
        }
        return result;
    }

}
