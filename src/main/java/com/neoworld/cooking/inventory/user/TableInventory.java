package com.neoworld.cooking.inventory.user;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.data.*;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.DateUtils;
import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.api.inventory.IOpen;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TableInventory extends BaseInventory implements IClickable, IOpen {
    private static final int INV_SIZE = 54;

    private static final int TABLE_SIZE = 9;
    private static final int TABLE_POS = 12;

    @Nullable
    private Recipe recipe;
    private final CookingTable table;

    private Player recentPlayer;

    public TableInventory(@Nullable InventoryHolder prevInventory, CookingTable table) {
        super(prevInventory);
        this.table = table;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE, "");
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        event.titleOverride(Component.text(
                ChatUtils.toColorString("요리작업대" + (table.getLevel() > 0 ? " &a[+"+ table.getLevel() +"]" : ""))));
    }

    @Override
    protected void initialize() {
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&f")
                    .build());
        }

        for (int i : getTableIndex()) {
            inventory.setItem(i, null);
        }

        inventory.setItem(19, getTimeInfoItem());
        inventory.setItem(25, getRecipeInfo());
        inventory.setItem(49, getStartCookItem());
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, ItemStack currentItem) {

        Player player = (Player)event.getWhoClicked();
        recentPlayer = player;

        if (getStartCookItem().isSimilar(currentItem)) {

            updateItem(); // up to date.
            UserContainer containerData = UserContainerProvider.getOrCreateContainer(player);

            if ((recipe != null && recipe.isAvailable()) && table.isEnoughEnergy() && containerData.canAddItem()) {
                closeInventoryEveryone(event.getViewers());
                table.startCook(recipe, player);

                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

                int useEnergy = (int) (Math.random() * (table.getMaxUseEnergy() - table.getMinUseEnergy()) + table.getMinUseEnergy());
                table.setEnergy(Math.max(table.getEnergy() - useEnergy, 0));

                // table Effects.
                player.sendMessage(ChatUtils.translateMessages(
                        "message.table.startcook",
                        recipe.getName(), String.valueOf(table.getEnergy()), String.valueOf(useEnergy)
                ));

                if (table.getBlock() != null) {
                    Block block = table.getBlock();
                    block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().toCenterLocation(), 100, 0.4, 0.4, 0.4, 0.01);
                }

            } else if (!table.isEnoughEnergy()) {
                player.closeInventory();
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                player.sendMessage(ChatUtils.translateMessages(
                        "message.table.no_energy",
                        String.valueOf(table.getEnergy()), String.valueOf(table.getEnoughEnergy())
                ));
            } else if (!containerData.canAddItem()) {
                player.closeInventory();
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                player.sendMessage(ChatUtils.translateMessages("message.table.container_not_enough"));
            } else {
                player.playSound(player, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }

            return;
        }

        if (getTableIndex().contains(event.getSlot()))
            event.setCancelled(false);

        // find after put
        Bukkit.getScheduler().runTaskLater(NWCooking.getInstance(), this::updateItem, 2);
    }

    public void clearTable() {
        for (int i : getTableIndex()) {
            inventory.setItem(i, null);
        }
        updateItem();
    }

    public void updateItem() {
        UserContainer containerData = UserContainerProvider.getOrCreateContainer(recentPlayer);

        ItemStack[] ingredients = getTableIndex().stream()
                .map(inventory::getItem)
                .toArray(ItemStack[]::new);

        this.recipe = RecipeProvider.findFromIngredient(ingredients, true, containerData);

        inventory.setItem(25, getRecipeInfo());
        inventory.setItem(19, getTimeInfoItem());
    }

    // items
    private ItemStack getTimeInfoItem() {
        Recipe r = this.recipe;

        if (this.recipe == null) {
            return new ItemBuilder(Material.BARRIER)
                    .setDisplayName("&c존재하지 않은 레시피")
                    .build();
        }

        return new ItemBuilder(Material.CLOCK)
                .setDisplayName("&f조리시간 : &6" + DateUtils.getTimerFormat(r.getCookingTime(), DateUtils.HOUR_DATE_FORMAT))
                .build();
    }

    private ItemStack getRecipeInfo() {
        if (this.recipe == null) {
            return new ItemBuilder(Material.BARRIER)
                    .setDisplayName("&c존재하지 않은 레시피")
                    .build();
        }

        return this.recipe.getRecipeBookItem();
    }

    private ItemStack getStartCookItem() {
        return new ItemBuilder(Material.FURNACE)
                .setDisplayName("&a&l요리시작")
                .addLore("&f■ 레시피에 맞는 요리를 시작합니다.")
                .build();
    }

    public static List<Integer> getTableIndex() {
        List<Integer> tableIndex = new ArrayList<>();

        for (int i = 0; i < TABLE_SIZE; i++) {
            int pos = (((i / 3) * 9) + i % 3) + TABLE_POS;

            tableIndex.add(pos);
        }

        return tableIndex;
    }
}
