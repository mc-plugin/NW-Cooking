package com.neoworld.cooking.utils.api.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface IClickable {

    void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem);
}
