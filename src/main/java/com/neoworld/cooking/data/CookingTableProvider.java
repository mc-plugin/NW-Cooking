package com.neoworld.cooking.data;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.utils.exceptions.ConfigurationEmptyException;
import com.neoworld.cooking.utils.exceptions.ConfigurationLoadFailedException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CookingTableProvider {

    public static String FILENAME = "CookingTableData.yaml";
    private YamlConfiguration config;
    private File configFile;

    static Map<UUID, CookingTable> tableMap;

    static CookingTableProvider provider = new CookingTableProvider();

    public static CookingTableProvider getProvider() {
        return provider;
    }

    public void init(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        this.configFile = new File(NWCooking.getInstance().getDataFolder() + File.separator + FILENAME);

        if ((!this.configFile.exists())) {
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            load();
        } catch (ConfigurationLoadFailedException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        for (UUID uuid: tableMap.keySet())
            config.set(uuid.toString(), tableMap.get(uuid));

        try {
            this.config.save(this.configFile);
        } catch (Exception e) {
            e.printStackTrace();
            NWCooking.LOGGER.warning("Table saving failed. the reason is '" + e.getMessage() + "'");
        }
    }

    public void load() throws ConfigurationLoadFailedException {
        tableMap = new HashMap<>();

        try {
            this.config = YamlConfiguration.loadConfiguration(this.configFile);

            for (String key : config.getKeys(false))
                tableMap.put(UUID.fromString(key), (CookingTable) this.config.get(key));

        } catch (Exception e) {
            this.config = new YamlConfiguration();

            if (e instanceof ConfigurationEmptyException) {
                NWCooking.LOGGER.warning("Table file was empty. initializing table file.");
                save();
            } else {
                throw new ConfigurationLoadFailedException("load yaml configuration failed", e);
            }
        }
    }

    public static CookingTable findFormUUID(UUID uuid) {
        return tableMap.get(uuid);
    }

    public static CookingTable[] findFormCookPlayer(Player player) {
        return findFormCookPlayerUUID(player.getUniqueId());
    }

    public static CookingTable[] findFormCookPlayerUUID(UUID uuid) {
        return tableMap.values().stream()
                .filter(table -> table.cookUserUUID == uuid)
                .toArray(CookingTable[]::new);
    }

    public static CookingTable[] getCookingTables() {
        return tableMap.values().stream()
                .filter(CookingTable::isCooking)
                .toArray(CookingTable[]::new);
    }

    public static CookingTable createTable() {
        UUID uuid = UUID.randomUUID();
        tableMap.put(uuid, new CookingTable(uuid));
        provider.save();

        return tableMap.get(uuid);
    }
}
