package com.neoworld.cooking.repeat;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.data.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class AnnouncementLoop {

    public static void init(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {

            for (CookingTable table: CookingTableProvider.getCookingTables()) {
                try {
                    table.evaluateCook();
                } catch(Exception e) {
                    NWCooking.LOGGER.warning("something is wrong in table uuid: " + table.getTableUUID());
                    table.clearCookData();
                    e.printStackTrace();
                }
            }

        }, 0, 20);
    }
}
