package com.neoworld.cooking.data;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.exceptions.DataAlreadyExistsException;
import com.neoworld.cooking.utils.exceptions.ConfigurationEmptyException;
import com.neoworld.cooking.utils.exceptions.ConfigurationLoadFailedException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserContainerProvider {

    public static String FILENAME = "FoodContainers.yaml";
    private YamlConfiguration config;
    private File configFile;

    static Map<UUID, UserContainer> containerMap;

    static UserContainerProvider provider = new UserContainerProvider();

    public static UserContainerProvider getProvider() {
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
        for (UUID uuid: containerMap.keySet())
            config.set(uuid.toString(), containerMap.get(uuid));

        try {
            this.config.save(this.configFile);
        } catch (Exception e) {
            NWCooking.LOGGER.warning("Container saving failed. the reason is '" + e.getMessage() + "'");
        }
    }

    public void load() throws ConfigurationLoadFailedException {
        this.containerMap = new HashMap<>();

        try {
            this.config = YamlConfiguration.loadConfiguration(this.configFile);

            for (String key : config.getKeys(false))
                containerMap.put(UUID.fromString(key), (UserContainer) this.config.get(key));

        } catch (Exception e) {
            this.config = new YamlConfiguration();

            if (e instanceof ConfigurationEmptyException) {
                NWCooking.LOGGER.warning("Container File was empty. initializing Container file.");
                save();
            } else {
                throw new ConfigurationLoadFailedException("load yaml configuration failed", e);
            }
        }
    }

    public static UserContainer getOrCreateContainer(Player player) {
        return getOrCreateContainer(player.getUniqueId());
    }

    public static UserContainer getOrCreateContainer(UUID playerUUID) {
        try {
            return createContainer(playerUUID);
        } catch (DataAlreadyExistsException e) {
            return getContainer(playerUUID);
        }
    }

    public static UserContainer getContainer(Player player) {
        return getContainer(player.getUniqueId());
    }

    public static UserContainer getContainer(UUID uuid) {
        return containerMap.get(uuid);
    }

    public static UserContainer createContainer(UUID uuid) throws DataAlreadyExistsException {
        if (containerMap.containsKey(uuid))
            throw new DataAlreadyExistsException();

        containerMap.put(uuid, new UserContainer(uuid));
        provider.save();

        return containerMap.get(uuid);
    }

}