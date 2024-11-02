package com.neoworld.cooking.utils.api.inventory;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChoiceBuilder {

    final InventoryHolder prevInventory;
    ItemStack infoItem;

    Runnable accept;
    ItemStack acceptItem;

    Runnable decline;
    ItemStack declineItem;

    public ChoiceBuilder(@NotNull InventoryHolder prevInventory) {
        this.prevInventory = prevInventory;
    }

    public ChoiceBuilder setInfoItem(ItemStack item) {
        this.infoItem = item;
        return this;
    }

    public ChoiceBuilder setAcceptFunction(Runnable function) {
        this.accept = function;
        return this;
    }

    public ChoiceBuilder setAcceptItem(ItemStack item) {
        this.acceptItem = item;
        return this;
    }

    public ChoiceBuilder setDeclineFunction(Runnable function) {
        this.decline = function;
        return this;
    }

    public ChoiceBuilder setDeclineItem(ItemStack item) {
        this.declineItem = item;
        return this;
    }

    public ChoiceInventory build() {
        return new ChoiceInventory(prevInventory, infoItem, acceptItem, accept, declineItem, decline);
    }
}
