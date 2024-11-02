package com.neoworld.cooking.inventory.admin;

import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EditResult extends BaseInventory implements IClickable {
    private final int INV_SIZE = 54;

    private final int TABLE_SIZE = 9;

    private final RecipeManager manager;

    protected EditResult(@NotNull RecipeManager prevInventory) {
        super(prevInventory);
        manager = prevInventory;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE,
                manager.recipe.getName() + " ( ID: " + manager.recipe.getId() + " ) 보상설정");
    }

    @Override
    protected void initialize() {
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&f")
                    .build());
        }

        putTable(20, getSuccessTileItem(), manager.recipe.getSuccessItem());
        putTable(24, getFailTileItem(), manager.recipe.getFailItem());

        inventory.setItem(49, getSaveRewardItem());
    }

    private void putTable(int tablePos, ItemStack tableItem, ItemStack puttedItem) {

        int leftTopPos = tablePos - 10;

        for (int i = 0; i < TABLE_SIZE; i++) {
            int pos = (((i / 3) * 9) + i % 3) + leftTopPos;

            if (i == TABLE_SIZE / 2) {
                inventory.setItem(pos, puttedItem);
                continue;
            }

            inventory.setItem(pos, tableItem);
        }
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {

        if (event.getRawSlot() == 20 || event.getRawSlot() == 24) {
            event.setCancelled(false);
        }

        if (currentItem == null)
            return;

        Player player = ((Player) event.getWhoClicked());

        if (currentItem.isSimilar(getSaveRewardItem())) {
            manager.recipe.setSuccessItem(inventory.getItem(20));
            manager.recipe.setFailItem(inventory.getItem(24));

            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            returnToPreviousInventory(player);
        }
    }

    private ItemStack getSaveRewardItem() {
        return new ItemBuilder(Material.CHEST)
                .setDisplayName("&b보상 저장")
                .addLore("&f현재 보상을 저장합니다. &8(저장 하지 않을시 소실됨)")
                .build();
    }

    private ItemStack getSuccessTileItem() {
        return new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .setDisplayName("&a성공 보상")
                .build();
    }

    private ItemStack getFailTileItem() {
        return new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayName("&c실패 보상")
                .build();
    }
}
