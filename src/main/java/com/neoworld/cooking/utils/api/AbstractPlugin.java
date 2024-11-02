package com.neoworld.cooking.utils.api;

import com.neoworld.cooking.NWCooking;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class AbstractPlugin extends JavaPlugin {

    public static FileConfiguration config;
    public static Logger LOGGER;

    private static AbstractPlugin instance;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();
        LOGGER = getLogger();

        instance = this;

        initialize();
    }

    public abstract void initialize();

    public static AbstractPlugin getInstance() {
        return instance;
    }

}
