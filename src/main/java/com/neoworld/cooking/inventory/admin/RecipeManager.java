package com.neoworld.cooking.inventory.admin;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.data.Recipe;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.DateUtils;
import com.neoworld.cooking.utils.api.inventory.BaseInventory;
import com.neoworld.cooking.utils.api.inventory.IClickable;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager extends BaseInventory implements IClickable {

    private final int INV_SIZE = 54;
    private static final int TIMEOUT = 600;

    @NotNull
    public final Recipe recipe;
    private static final Map<Player, State> status = new HashMap<>();

    public RecipeManager(@NotNull Recipe recipe, @Nullable InventoryHolder prevInventory) {
        super(prevInventory);
        this.recipe = recipe;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(this, INV_SIZE,
                recipe.getName() + " ( ID: " + recipe.getId() + " ) 관리");
    }

    @Override
    protected void initialize() {
        for (int i = 0; i < INV_SIZE; i++) {
            inventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&f")
                    .build());
        }

        syncItemsOnReturn();
    }

    @Override
    public void onClickEvent(InventoryClickEvent event, @Nullable ItemStack currentItem) {
        if (currentItem == null)
            return;

        Player player = ((Player) event.getWhoClicked());

        if (currentItem.isSimilar(getEditIngredientItem())) {
            player.openInventory(new EditIngredient(this).getInventory());
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if(currentItem.isSimilar(getEditResultItem())) {
            player.openInventory(new EditResult(this).getInventory());
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
        } else if(currentItem.isSimilar(getEditTimeItem()) && !status.containsKey(player)) {

            status.put(player, new State(EditType.TIME, this));
            registerTimeout(player);

            player.sendMessage(ChatUtils.translateMessages("message.editor.time.input"));
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();

        } else if(currentItem.isSimilar(getEditSpecialItem()) && !status.containsKey(player)) {

            status.put(player, new State(EditType.SPECIAL_RATIO, this));
            registerTimeout(player);

            player.sendMessage(ChatUtils.translateMessages("message.editor.special.ratio.input"));
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            player.closeInventory();

        } else if(currentItem.isSimilar(getEditPermissionItem())) {
            int i = recipe.getPermission().ordinal() + 1;
            recipe.setPermission(Recipe.RecipePermission.values()[i >= Recipe.RecipePermission.values().length ? 0 : i]);
            player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
            syncItemsOnReturn();
        }
    }

    public void registerTimeout(Player player) {
        Bukkit.getScheduler().runTaskLater(NWCooking.getInstance(), () -> {
            if (!hasState(player))
                return;

            removeState(player);
            player.sendMessage(ChatUtils.translateMessages("message.editor.timeout"));
        }, TIMEOUT);
    }

    public static void removeState(Player player) {
        status.remove(player);
    }

    public static void setState(Player player, State state) {
        status.put(player, state);
    }

    public static State getState(Player player) {
        return status.get(player);
    }

    public static boolean hasState(Player player) {
        return status.containsKey(player);
    }

    @Override
    protected void syncItemsOnReturn() {
        inventory.setItem(18, getEditIngredientItem());
        inventory.setItem(20, getEditResultItem());
        inventory.setItem(22, getEditTimeItem());
        inventory.setItem(24, getEditSpecialItem());
        inventory.setItem(26, getEditPermissionItem());
    }

    public ItemStack getEditIngredientItem() {
        return new ItemBuilder(Material.CHEST)
                .setDisplayName("&6재료설정")
                .addLore("&f■ 현재 레시피의 재료를 설정합니다.")
                .build();
    }

    public ItemStack getEditResultItem() {
        return new ItemBuilder(Material.ENDER_CHEST)
                .setDisplayName("&a보상설정")
                .addLore("&f■ 현재 레시피의 보상을 설정합니다.")
                .build();
    }

    public ItemStack getEditTimeItem() {
        return new ItemBuilder(Material.CLOCK)
                .setDisplayName("&e시간설정")
                .setLore(List.of(
                        "&f■ 현재 레시피의 조리시간을 설정합니다.",
                        "",
                        "&f▶ 현재 조리시간 : &b" + DateUtils.getTimerFormat(recipe.getCookingTime(), DateUtils.HOUR_DATE_FORMAT)
                ))
                .build();
    }

    public ItemStack getEditSpecialItem() {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setDisplayName("&d특별설정")
                .setLore(List.of(
                        "&f■ 현재 레시피의 특별보상의 배수를 설정합니다.",
                        "",
                        "&f▶ 현재 확률 : &e" + String.format("%.3f", recipe.getSpecialRatio() * 100) +"%",
                        "&f▶ 현재 배수 : &cx" + recipe.getSpecialMultiplier()
                ))
                .build();
    }

    public ItemStack getEditPermissionItem() {
        String permissionName;

        switch (recipe.getPermission()) {
            case HIDDEN -> permissionName = "&6&l발견 시 등록";
            case PUBLIC -> permissionName = "&a&l공개 권한";
            case PRIVATE -> permissionName = "&c&l비공개 권한";
            default -> permissionName = "&7UNKNOWN";
        }

        return new ItemBuilder(Material.ENCHANTED_BOOK)
                .setDisplayName("&9권한설정")
                .setLore(List.of(
                        "&f■ 현재 레시피의 권한을 설정합니다.",
                        "",
                        "&f▶ 현재 권한 : " + permissionName
                ))
                .build();
    }

    public enum EditType {
        TIME, SPECIAL_RATIO, SPECIAL_MUL
    }

    public static class State {
        EditType type;
        final RecipeManager manager;

        private State(EditType type, RecipeManager manager) {
            this.type = type;
            this.manager = manager;
        }

        public RecipeManager getManager() {
            return manager;
        }

        public EditType getType() {
            return type;
        }

        public void changeSpecialType() {
            this.type = EditType.SPECIAL_MUL;
        }
    }
}