package com.neoworld.cooking.utils.listeners;

import com.neoworld.cooking.utils.api.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryEventListener implements Listener {

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BaseInventory base)
            event.titleOverride(base.getTitle());

        if (holder instanceof IOpen openable)
            openable.onOpen(event);

    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof ICloseable closeable) {
            closeable.onCloseEvent(event);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        ItemStack clickedItem = event.getCurrentItem();

        if (holder instanceof IPage pageHolder) {
            switch (pageHolder.matchedItem(clickedItem)) {
                case PREVIOUS -> {
                    event.setCancelled(true);
                    if (pageHolder.getPage() > 0) {
                        int updatePage = pageHolder.getPage() - 1;

                        pageHolder.onPageLoad(updatePage);
                        pageHolder.setPage(updatePage);
                    }
                }
                case NEXT -> {
                    event.setCancelled(true);
                    if (pageHolder.getPage() < pageHolder.getMaxPage() - 1) {
                        int updatePage = pageHolder.getPage() + 1;

                        pageHolder.onPageLoad(updatePage);
                        pageHolder.setPage(updatePage);
                    }
                }
            }
        }

        if (holder instanceof IClickable clickable) {
            if (!(event.getClickedInventory() instanceof PlayerInventory))
                event.setCancelled(true);
            clickable.onClickEvent(event, clickedItem);
        }
    }
}