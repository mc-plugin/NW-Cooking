package com.neoworld.cooking.utils.api.inventory;

import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChoiceInventory extends BaseInventory implements IClickable, IOpen {
    private static final int INV_SIZE = 36;

    private final ItemStack infoItem;

    private final Runnable accept;
    private final ItemStack acceptItem;

    private final Runnable decline;
    private final ItemStack declineItem;

    protected
    ChoiceInventory(@NotNull InventoryHolder prevInventory, ItemStack infoItem,
                    ItemStack acceptItem, Runnable accept,
                    ItemStack declineItem, Runnable decline) {
        super(prevInventory);

        this.infoItem = infoItem;

        this.accept = accept;
        this.acceptItem = acceptItem;

        this.decline = decline;
        this.declineItem = declineItem;
    }

    public ChoiceInventory(@NotNull InventoryHolder prevInventory, ItemStack infoItem, Runnable accept, Runnable decline) {
        this(prevInventory, infoItem,
                new ItemBuilder(Material.LIME_WOOL).setDisplayName("&a확인").build(), accept,
                new ItemBuilder(Material.RED_WOOL).setDisplayName("&4거부").build(), decline);
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE, infoItem.getItemMeta().getDisplayName());
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    @Override
    protected void initialize() {
        inventory.setItem(13, infoItem);
        inventory.setItem(20, acceptItem);
        inventory.setItem(24, declineItem);
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {
        if (currentItem == null)
            return;

        Player player = (Player) event.getWhoClicked();

        if (currentItem.isSimilar(acceptItem)) {
            accept.run();
            returnToPreviousInventory(event.getWhoClicked());
        } else if (currentItem.isSimilar(declineItem)) {
            decline.run();
            returnToPreviousInventory(event.getWhoClicked());
        }
    }

}
