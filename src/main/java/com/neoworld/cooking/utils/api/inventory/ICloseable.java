package com.neoworld.cooking.utils.api.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface ICloseable {
    void onCloseEvent(InventoryCloseEvent event);

}