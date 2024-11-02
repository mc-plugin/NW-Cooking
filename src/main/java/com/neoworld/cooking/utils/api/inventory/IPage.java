package com.neoworld.cooking.utils.api.inventory;

import com.neoworld.cooking.utils.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface IPage {

    default ItemStack getPageItems(Type type) {
        switch (type) {
            case INFO -> {
                return new ItemBuilder(Material.COMPASS).setDisplayName("&f현재 페이지 : &e" + getPage() + " &6/ &e" + getMaxPage()).build();
            }
            case NEXT -> {
                return new ItemBuilder(Material.ARROW).setDisplayName("&f다음 페이지").build();
            }
            case PREVIOUS -> {
                return new ItemBuilder(Material.ARROW).setDisplayName("&f이전 페이지").build();
            }
            default -> {
                return null;
            }
        }

    }

    int getPage();
    void setPage(int page);

    int getMaxPage();
    void onPageLoad(int page);

    default boolean updatePage(int page) {
        if (!hasPage(page))
            return false;

        setPage(page);
        onPageLoad(getPage());

        return true;
    }

    default Type matchedItem(@Nullable ItemStack currentItem) {
        return getPageItems(Type.PREVIOUS).isSimilar(currentItem) ? Type.PREVIOUS :
                getPageItems(Type.NEXT).isSimilar(currentItem) ? Type.NEXT :
                getPageItems(Type.INFO).isSimilar(currentItem) ? Type.INFO :
                Type.NOT_MATCHED;
    }
    default boolean hasPage(int page) {
        return page >= 0 && page < getMaxPage();
    }

    enum Type {
        PREVIOUS, NEXT, INFO, NOT_MATCHED
    }
}
