package com.neoworld.cooking.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtils {

    public static boolean canItemGivable(Player target, ItemStack itemStack) {
        PlayerInventory playerInventory = target.getInventory();
        int size = 0;
        for (ItemStack item : playerInventory.getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                size += itemStack.getMaxStackSize();
            } else if (item.isSimilar(itemStack)) {
                size += item.getMaxStackSize() - item.getAmount();
            }
            if (size >= itemStack.getAmount())
                return true;
        }
        return false;
    }
}
