package com.neoworld.cooking.inventory.user;

import com.neoworld.cooking.data.UserContainer;
import com.neoworld.cooking.data.UserContainerProvider;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.api.inventory.IPage;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainerInventory extends BaseInventory implements IPage, IClickable {

    public static int INV_SIZE = 54;

    private Player player;
    private int page = 0;

    public ContainerInventory(Player player, @Nullable InventoryHolder prevInventory) {
        super(prevInventory);
        this.player = player;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE, player.getName() + "의 요리보관함");
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

        inventory.setItem(49, getInfoItem());

        updatePage(page);
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {
        if (event.getClickedInventory() instanceof PlayerInventory)
            return;

        UserContainer container = UserContainerProvider.getOrCreateContainer(player);
        UserContainer.FoodItem[] items = container.getItems();

        Player player = (Player) event.getWhoClicked();
        int index = getPageSize() * page + event.getRawSlot();

        if (currentItem != null && currentItem.isSimilar(getInfoItem()) && items.length > 0) {
            if (container.tryGiveAll(player)) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                onPageLoad(getPage());
            } else {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(ChatUtils.translateMessages("message.container.no_slot"));
            }
        }

        if (event.getRawSlot() > getPageSize())
            return;

        if (index >= 0 && items.length > index) {
            if (container.tryGive(player, index)) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 2f, 1f);
                onPageLoad(getPage());
            } else {
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(ChatUtils.translateMessages("message.container.no_slot"));
            }
        }
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public int getMaxPage() {
        return (int) Math.ceil(
                UserContainerProvider.getOrCreateContainer(player).getContainerSize() / ((double) getPageSize()));
    }

    @Override
    public void onPageLoad(int page) {
        UserContainer container = UserContainerProvider.getOrCreateContainer(player);
        container.updateContainerItems();

        UserContainer.FoodItem[] items = container.getItems();

        for (int i = 0; i < getPageSize(); i++) {
            int index = getPageSize() * page + i;

            if (index >= container.getContainerSize())
                inventory.setItem(i, getLockedItem(index));
            else if (index < items.length) {
                ItemStack origin = items[index].getVisualItem().clone();

                ItemBuilder builder = new ItemBuilder(origin)
                        .addLore("")
                        .addLore("&b> 아이템을 받으려면 클릭하세요. <");

                Component fixedDisplayName =
                        origin.getItemMeta().hasDisplayName()
                                ? origin.getItemMeta().displayName()
                                : Component.translatable(origin.translationKey());

                if (items[index].isSpecial()) {
                    fixedDisplayName = fixedDisplayName
                            .append(Component.empty()
                                    .append(Component.text(" * ", NamedTextColor.GOLD))
                                    .append(Component.text(items[index].getRecipe().getSpecialMultiplier(), NamedTextColor.YELLOW))
                                    .decoration(TextDecoration.ITALIC, false)
                                    .decoration(TextDecoration.BOLD, false)
                            );
                }

                builder.setComponentDisplayName(fixedDisplayName);
                inventory.setItem(i, builder.build());
            }
            else
                inventory.setItem(i, null);
        }

        updateInventory();
    }

    public ItemStack getInfoItem() {
        UserContainer container = UserContainerProvider.getOrCreateContainer(player);
        UserContainer.FoodItem[] items = container.getItems();

        return new ItemBuilder(Material.CHEST)
                .setDisplayName(new StringBuilder("&6모두 받기 &e")
                        .append(items.length != 0 ? "["+items.length+"]" : "").toString())
                .setLore(List.of(
                        "&b> 클릭 시 화면의 아이템을 모두 받습니다. <",
                        "&8※ 공간이 부족할 시, 받을 수 있는 만큼만 받습니다.",
                        "",
                        "&f■ 현재 보관함 레벨 : &e"+(container.getLevel())+" level",
                        "&f■ 현재 페이지 : &e" + (getPage() + 1) + " &6/ &e" + getMaxPage()
                ))
                .build();
    }

    public ItemStack getLockedItem(int absoluteIndex) {
        UserContainer container = UserContainerProvider.getOrCreateContainer(player);
        int requireLevel = (absoluteIndex - container.getDefaultContainerSize()) / container.getContainerUpgradeSize() + 1;

        ItemBuilder builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayName("&4잠김");

        if (requireLevel <= UserContainer.getMaxLevel())
            builder.addLore("&c■ 레벨 &4" + requireLevel + " &c이후 잠금 해제");

        return builder.build();
    }

    public void updateInventory() {
        inventory.setItem(49, getInfoItem());
    }

    private int getPageSize() {
        return INV_SIZE - 9;
    }
}
