package com.neoworld.cooking.data;


import com.neoworld.cooking.utils.inventory.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class Recipe implements ConfigurationSerializable {

    protected final String id;
    protected String name;
    protected long cookingTime;

    protected ItemStack[] ingredient;
    protected ItemStack successItem;
    protected ItemStack failItem;

    protected int specialMultiplier;
    protected float specialRatio;

    protected RecipePermission permission;

    protected Recipe(String id) {
        this.id = id.toLowerCase();
        this.name = id;
        this.cookingTime = 60_000L;

        this.successItem = null;
        this.failItem = null;
        this.ingredient = new ItemStack[9];

        this.specialMultiplier = 1;
        this.specialRatio = 0.0f;

        this.permission = RecipePermission.PRIVATE;
    }

    public Recipe(Map<String, Object> serializedMap) {
        this.id = ((String) serializedMap.get("id")).toLowerCase();
        this.name = (String) serializedMap.get("name");
        this.cookingTime = Long.parseLong(serializedMap.get("cookingTime").toString());

        this.successItem = (ItemStack) serializedMap.get("successItem");
        this.failItem = (ItemStack) serializedMap.get("failItem");
        this.ingredient = ((List<ItemStack>) serializedMap.get("ingredient")).toArray(ItemStack[]::new);

        this.specialRatio = Float.parseFloat(serializedMap.get("specialRatio").toString());
        this.specialMultiplier = (int) serializedMap.get("specialMultiplier");

        this.permission = RecipePermission.valueOf((String) serializedMap.get("permission"));
    }

    public long getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(long timestamp) {
        this.cookingTime = timestamp;
        update();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemStack getSuccessItem() {
        return successItem;
    }

    public void setSuccessItem(ItemStack successItem) {
        this.successItem = successItem;
        update();
    }

    @Nullable
    public ItemStack getFailItem() {
        return failItem;
    }

    public void setFailItem(ItemStack failItem) {
        this.failItem = failItem;
        update();
    }

    public ItemStack[] getIngredient() {
        return ingredient;
    }

    public void setIngredient(ItemStack[] ingredient) {
        this.ingredient = ingredient;
        update();
    }

    public float getSpecialRatio() {
        return specialRatio;
    }

    public int getSpecialMultiplier() {
        return specialMultiplier;
    }

    public void setSpecialMultiplier(int specialMultiplier) {
        this.specialMultiplier = specialMultiplier;
        update();
    }

    public void setSpecialRatio(float specialRatio) {
        this.specialRatio = specialRatio;
        update();
    }

    public RecipePermission getPermission() {
        return this.permission;
    }

    public void setPermission(RecipePermission permission) {
        this.permission = permission;
        update();

    }

    public boolean isAvailable() {
        return getSuccessItem() != null
                && !Arrays.stream(ingredient).allMatch(Objects::isNull)
                && cookingTime > 0;
    }

    public ItemStack getRecipeBookItem() {
        Material mat = Material.BARRIER;

        switch (getPermission()) {
            case PUBLIC -> mat = Material.KNOWLEDGE_BOOK;
            case HIDDEN -> mat = Material.ENCHANTED_BOOK;
            case PRIVATE -> mat = Material.WRITABLE_BOOK;
        }

        if (!isAvailable()) {
            mat = Material.BOOK;
        }

        List<Component> lore = new ArrayList<>();
        ItemStack success = getSuccessItem();
        ItemStack fail = getFailItem();

        String divider = "----------------------------------------";

        lore.add(Component.empty());
        lore.add(
                Component.text("성공 아이템", NamedTextColor.GREEN)
                        .append(Component.text(" | ", NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)
                        .append(success.getItemMeta().hasDisplayName()
                                ? success.getItemMeta().displayName()
                                : Component.translatable(success.translationKey()))
        );

        if (success.getItemMeta().hasLore()) {
            lore.add(Component.text(divider, NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.STRIKETHROUGH, true));
            lore.addAll(success.lore());
        }

        if (fail != null) {
            lore.add(Component.empty());
            lore.add(
                    Component.text("실패 아이템", NamedTextColor.RED)
                            .append(Component.text(" | ", NamedTextColor.GRAY))
                            .decoration(TextDecoration.ITALIC, false)
                            .append(fail.getItemMeta().hasDisplayName()
                                    ? fail.getItemMeta().displayName()
                                    : Component.translatable(fail.translationKey()))
            );

            if (fail.getItemMeta().hasLore()) {
                lore.add(Component.text(divider, NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.STRIKETHROUGH, true));
                lore.addAll(fail.lore());
            }
        }

        return new ItemBuilder(mat)
                .setDisplayName("&6" + getName())
                .setComponentLore(lore)
                .addFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
                .build();
    }

    void update() {
        RecipeProvider.getProvider().save();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("id", this.id);
        map.put("name", this.name);
        map.put("cookingTime", this.cookingTime);

        map.put("successItem", this.successItem);
        map.put("failItem", this.failItem);
        map.put("ingredient", this.ingredient);

        map.put("specialRatio", this.specialRatio);
        map.put("specialMultiplier", this.specialMultiplier);

        map.put("permission", this.permission.name());

        return map;
    }

    public enum RecipePermission {
        PRIVATE, PUBLIC, HIDDEN
    }
}
