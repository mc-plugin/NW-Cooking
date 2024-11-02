package com.neoworld.cooking.data;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.utils.inventory.InventoryUtils;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UserContainer implements ConfigurationSerializable {

    private final UUID uuid;

    private final List<FoodItem> items;
    private final Set<String> permissions;
    private int level;

    protected UserContainer(UUID uuid) {
        this.uuid = uuid;

        this.items = new ArrayList<>();
        this.permissions = new HashSet<>();
        this.level = 1;
    }

    public UserContainer(Map<String, Object> serializedMap) {
        this.uuid = UUID.fromString((String) serializedMap.get("uuid"));

        this.items = ((List<FoodItem>) serializedMap.get("foods"));
        this.items.removeIf(item -> !item.isAvailable());

        this.permissions = ((Set<String>) serializedMap.get("permissions"));

        this.level = (int) serializedMap.getOrDefault("level", 0);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public int getLevel() {
        return Math.min(level, getMaxLevel());
    }

    public void setLevel(int size) {
        this.level = size;
        update();
    }

    public void addPermission(Recipe recipe) {
        this.permissions.add(recipe.id);
    }

    public boolean hasPermission(Recipe recipe) {
        return this.permissions.contains(recipe.id);
    }

    public void removePermission(Recipe recipe) {
        this.permissions.remove(recipe.id);
    }

    public FoodItem[] getItems() {
        this.items.removeIf(item -> !item.isAvailable());
        return items.toArray(FoodItem[]::new);
    }

    public int getContainerSize() {
        return getDefaultContainerSize() + (getContainerUpgradeSize() * level);
    }

    public boolean tryGive(Player player, int index) {
        FoodItem foodItem = items.get(index);
        ItemStack item = foodItem.getItem();

        if (InventoryUtils.canItemGivable(player, item)) {
            player.getInventory().addItem(item);
            removeItem(index);

            return true;
        }

        return false;
    }

    public boolean tryGiveAll(Player player) {
        Iterator<FoodItem> iterator = items.iterator();
        boolean isGiven = false;

        while (iterator.hasNext()) {
            FoodItem i = iterator.next();
            if (InventoryUtils.canItemGivable(player, i.getItem())) {
                player.getInventory().addItem(i.getItem());
                iterator.remove();
                isGiven = true;
            }
        }

        update();
        return isGiven;
    }

    public void addItem(Recipe recipe, boolean isSuccess) {
        addItem(recipe, isSuccess, false);
    }

    public void addItem(Recipe recipe, boolean isSuccess, boolean isSpecial) {
        if (recipe == null || !recipe.isAvailable() ||
                (!isSuccess && recipe.getFailItem() == null))
            return;

        this.items.add(new FoodItem(recipe, isSuccess, isSpecial));
        update();
    }

    public void removeItem(int index) {
        if (index < 0 || index >= items.size())
            return;

        this.items.remove(index);
        update();
    }

    public boolean canAddItem() {
        int cookingTemp = CookingTableProvider.findFormCookPlayerUUID(uuid).length;
        return cookingTemp + items.size() + 1 <= getDefaultContainerSize() + (getContainerUpgradeSize() * level);
    }

    public void updateContainerItems() {
        this.items.removeIf(next -> !next.isAvailable());
    }

    public static ItemStack getUpgradeItem() {
        return new ItemBuilder(Material.PAPER)
                .setDisplayName("&d&l요리보관함 강화권")
                .addLore("&f■ 보관함 레벨은 &a1 &f증가시킵니다.")
                .build();
    }

    public int getDefaultContainerSize() {
        return NWCooking.config.getInt("defaultContainerSize", 27);
    }

    public int getContainerUpgradeSize() {
        return NWCooking.config.getInt("containerUpgradeSize", 9);
    }

    public static int getMaxLevel() {
        return NWCooking.config.getInt("containerMaxLevel", 10);
    }

    public void update() {
        UserContainerProvider.provider.save();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("uuid", this.uuid.toString());

        map.put("foods", items);
        map.put("level", this.level);

        map.put("permissions", this.permissions);

        return map;
    }

    public static class FoodItem implements ConfigurationSerializable {

        final String recipeId;
        final boolean isSuccess;
        final boolean isSpecial;

        public FoodItem(Map<String, Object> map) {
            this(
                    (String) map.get("recipeId"),
                    (Boolean) map.getOrDefault("isSuccess", false),
                    (Boolean) map.getOrDefault("isSpecial", false)
            );
        }

        public FoodItem(Recipe recipe, boolean isSuccess) {
            this(recipe, isSuccess, false);
        }

        public FoodItem(Recipe recipe, boolean isSuccess, boolean isSpecial) {
            this(recipe.id, isSuccess, isSpecial);
        }

        protected FoodItem(String recipeId, boolean isSuccess, boolean isSpecial) {
            this.recipeId = recipeId;
            this.isSuccess = isSuccess;
            this.isSpecial = isSpecial;
        }

        public ItemStack getVisualItem() {
            Recipe recipe = getRecipe();

            if (recipe == null) {
                return null;
            }
            return isSuccess ? recipe.getSuccessItem() : recipe.getFailItem();
        }

        public ItemStack getItem() {
            ItemStack item = getVisualItem();

            if (item == null)
                return null;

            item = item.clone();
            if (isSpecial)
                item.setAmount(item.getAmount() * getRecipe().specialMultiplier);

            return item;
        }

        public String getRecipeId() {
            return recipeId;
        }

        public Recipe getRecipe() {
            return RecipeProvider.findFromId(recipeId);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public boolean isSpecial() {
            return isSpecial;
        }

        public boolean isAvailable() {
            return getRecipe() != null
                    && getVisualItem() != null
                    && getRecipe().isAvailable();
        }

        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("recipeId", recipeId);
            map.put("isSuccess", isSpecial);
            map.put("isSpecial", isSpecial);

            return map;
        }

    }
}
