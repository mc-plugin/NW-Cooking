package com.neoworld.cooking.inventory.user;

import com.neoworld.cooking.data.Recipe;
import com.neoworld.cooking.data.RecipeProvider;
import com.neoworld.cooking.data.UserContainer;
import com.neoworld.cooking.data.UserContainerProvider;
import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.api.inventory.IPage;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeListInventory extends BaseInventory implements IPage, IClickable {

    public static int INV_SIZE = 54;

    private Player player;
    private int page = 0;

    private boolean isAdminMode = false;

    public RecipeListInventory(Player player, @Nullable InventoryHolder prevInventory) {
        super(prevInventory);
        this.player = player;
        this.isAdminMode = player.isOp();
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE, ChatUtils.toColorString("&c&l레시피 목록"));
    }

    @Override
    protected void initialize() {
        ItemStack pane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("&f")
                .build();

        for (int i = getPageSize(); i < INV_SIZE; i++) {
            inventory.setItem(i, pane);
        }

        inventory.setItem(45, getPageItems(Type.PREVIOUS));
        inventory.setItem(53, getPageItems(Type.NEXT));

        updatePage(page);
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {
        event.setCancelled(true);

        if (player.isOp() && getInfoItem().isSimilar(currentItem)) {
            isAdminMode = !isAdminMode;
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    public Recipe[] getRecipes() {
        if (isAdminMode) {
            return RecipeProvider.getRecipes();
        } else {
            return RecipeProvider.getHaveRecipes(
                    UserContainerProvider.getOrCreateContainer(player)
            );
        }
    }

    @Override
    public int getMaxPage() {
        int size = (int) Math.ceil(getRecipes().length / ((double) getPageSize()));

        return size <= 0 ? 1 : size;
    }

    @Override
    public void onPageLoad(int page) {
        Recipe[] recipes = getRecipes();

        for (int i = 0; i < getPageSize(); i++) {
            int index = getPageSize() * page + i;

            if (index < recipes.length) {
                if (recipes[index].isAvailable()) {
                    inventory.setItem(i, recipes[index].getRecipeBookItem());
                } else {
                    inventory.setItem(i, new ItemBuilder(Material.BARRIER)
                            .setDisplayName("&c활성화 되지 않은 레시피")
                            .addLore("&f> Id : &6" + recipes[index].getId())
                            .build());
                }
            }
            else
                inventory.setItem(i, null);
        }

        updateInventory();
    }

    public ItemStack getInfoItem() {
        ItemBuilder builder = new ItemBuilder(Material.COMPASS)
                .setDisplayName("&6레시피 정보")
                .setLore(List.of(
                        "&f",
                        "&f■ 현재 페이지 : &e" + (getPage() + 1) + " &6/ &e" + getMaxPage()
                ));

        if (player.isOp()) {
            builder = builder.addLore("&f■ 레시피 전체보기 : " + ((isAdminMode) ? "&a활성화" : "&c비활성화"));
        }
        builder = builder.addLore("");

        return builder.build();
    }

    public void updateInventory() {
        inventory.setItem(49, getInfoItem());
    }

    private int getPageSize() {
        return INV_SIZE - 9;
    }
}
