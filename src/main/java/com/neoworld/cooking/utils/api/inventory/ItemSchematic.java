package com.neoworld.cooking.utils.api.inventory;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@FunctionalInterface
public interface ItemSchematic {
    ItemStack getItem(InventoryHolder inv);

    default boolean isSimilar(ItemStack item, InventoryHolder holder) {
        return item.isSimilar(getItem(holder));
    }
}
