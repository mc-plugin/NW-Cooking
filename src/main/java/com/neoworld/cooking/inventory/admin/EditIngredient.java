package com.neoworld.cooking.inventory.admin;

import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.api.inventory.ItemSchematic;
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

public class EditIngredient extends BaseInventory implements IClickable {
    private final int INV_SIZE = 54;

    private final int TABLE_SIZE = 9;
    private final int TABLE_POS = 12;

    private final RecipeManager manager;

    protected EditIngredient(@NotNull RecipeManager prevInventory) {
        super(prevInventory);
        manager = prevInventory;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE,
                manager.recipe.getName() + " ( ID: " + manager.recipe.getId() + " ) 재료설정");
    }

    @Override
    protected void initialize() {
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, getEmptyBox());
        }

        ItemStack[] ingredient = manager.recipe.getIngredient();

        for (int i = 0; i < TABLE_SIZE; i++) {
            inventory.setItem(getTablePos(i), ingredient[i]);
        }

        inventory.setItem(49, getSaveIngredient());
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {
        if (!event.isCancelled())
            return;

        Player player = ((Player) event.getWhoClicked());

        boolean isTable = false;
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (event.getRawSlot() == getTablePos(i)) {
                isTable = true;
                break;
            }
        }
        event.setCancelled(!isTable);

        if (currentItem != null && !isTable && currentItem.isSimilar(getSaveIngredient())) {
            ItemStack[] newIngredient = new ItemStack[9];

            for (int i = 0; i < TABLE_SIZE; i++)
                newIngredient[i] = inventory.getItem(getTablePos(i));

            manager.recipe.setIngredient(newIngredient);

            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            returnToPreviousInventory(player);
        }
    }

    private int getTablePos(int index) {
        return (((index / 3) * 9) + index % 3) + TABLE_POS;
    }

    private ItemStack getEmptyBox() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("&f")
                .build();
    }

    private ItemStack getSaveIngredient() {
        return new ItemBuilder(Material.CHEST)
                .setDisplayName("&b재료 저장")
                .addLore("&f현재 재료를 저장합니다. &8(저장 하지 않을시 소실됨)")
                .build();
    }
}
