package com.neoworld.cooking.inventory.user;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.data.CookingTable;
import com.neoworld.cooking.data.Recipe;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.DateUtils;
import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.ChoiceInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.api.inventory.IOpen;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class InfoInventory extends BaseInventory implements IClickable, IOpen {

    public final int INV_SIZE = 36;

    private final CookingTable table;

    public InfoInventory(@Nullable InventoryHolder prevInventory, CookingTable table) {
        super(prevInventory);
        this.table = table;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE, ChatUtils.toColorString("요리작업대 정보"));
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        updateItems();
    }

    @Override
    protected void initialize() {
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&f")
                    .build());
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(NWCooking.getInstance(), this::updateItems, 0, 20);

        inventory.setItem(17, getOwnerItem());
    }

    public void updateItems() {
        if (!inventory.getViewers().isEmpty()) {
            inventory.setItem(9, getCurrentCookItem());
            inventory.setItem(11, getLeftTimeItem());
            inventory.setItem(13, getCookCountItem());
            inventory.setItem(15, getLeftPowerItem());
        }
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, ItemStack currentItem) {
        if (currentItem == null)
            return;

        if (currentItem.isSimilar(getCurrentCookItem()) && table.isCooking()) {
            Player player = ((Player) event.getWhoClicked());

            player.openInventory(
                    new ChoiceInventory(this,
                            new ItemBuilder(Material.FURNACE)
                                    .setDisplayName("&6현재 진행 중인 요리를 취소하시겠습니까?")
                                    .build(),
                            () -> {
                                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                player.sendMessage(ChatUtils.translateMessages("message.table.cancel"));
                                table.clearCookData();
                            },
                            () -> {}
                    ).getInventory());
        }
    }

    // items
    private ItemStack getCurrentCookItem() {

        if (!table.isCooking())
            return new ItemBuilder(Material.BARRIER)
                    .setDisplayName("&c현재 진행중인 요리가 없습니다.")
                    .build();

        Optional<Recipe> recipe = Optional.ofNullable(table.getRecipe());

        return new ItemBuilder(Material.CHEST)
                .setDisplayName("&6현재 진행중인 요리")
                .setLore(List.of(
                        "&c> 취소하려면 클릭하세요. < ",
                        "",
                        "&f■ 요리진행자 : &a" + Bukkit.getOfflinePlayer(table.getCookUserUUID()).getName(),
                        "",
                        "&f■ 레시피명 : &6" + recipe.map(Recipe::getName).orElse("&6UNKNOWN"),
                        "&f■ 소요시간 : &b" + DateUtils.getTimerFormat(recipe.map(Recipe::getCookingTime).orElse(-1L), DateUtils.HOUR_DATE_FORMAT)
                ))
                .build();
    }

    private ItemStack getLeftTimeItem() {
        if (!table.isCooking())
            return new ItemBuilder(Material.BARRIER)
                    .setDisplayName("&c현재 진행중인 요리가 없습니다.")
                    .build();

        return new ItemBuilder(Material.CLOCK)
                .setDisplayName("&e잔여시간")
                .addLore("&f■ 남은 시간 : &b" + DateUtils.getTimerFormat(table.getLeftTime(), DateUtils.HOUR_DATE_FORMAT))
                .build();
    }


    private ItemStack getCookCountItem() {
        return new ItemBuilder(Material.BOOK)
                .setDisplayName("&9요리작업대의 정보")
                .addLore("&f총 제작 횟수 : &b" + table.getCookCount() +" 번")
                .addLore("")
                .addLore("&f■ 요리작업대 레벨 &f: &blevel "+ table.getLevel())
                .addLore("&f■ 화력 소모 배율  &f: &cx"+ String.format("%.2f", table.getDiscountedRate()) + " &4["+table.getMinUseEnergy()+" ~ "+table.getMaxUseEnergy()+"]")
                .build();
    }

    private ItemStack getLeftPowerItem() {
        return new ItemBuilder(Material.COAL)
                .setDisplayName("&e잔여화력 : &6" + table.getEnergy() + "E")
                .build();
    }

    private ItemStack getOwnerItem() {
        return new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("&d최초설치자")
                .addLore("&f■ 작업대를 처음으로 설치한 유저입니다.")
                .setOwner(table.getOwnerUUID())
                .build();
    }
}
