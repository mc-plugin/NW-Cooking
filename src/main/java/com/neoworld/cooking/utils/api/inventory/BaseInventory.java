package com.neoworld.cooking.utils.api.inventory;

import com.google.errorprone.annotations.ForOverride;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseInventory implements InventoryHolder {

    protected Inventory inventory;
    protected final InventoryHolder prevInventory;

    private Component title;

    protected BaseInventory(@Nullable InventoryHolder prevInventory) {
        this.prevInventory = prevInventory;
    }

    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            this.inventory = createInventory();
            initialize();
        }

        return inventory;
    }

    protected abstract Inventory createInventory();

    /**
     * Initialize inventory such as put item, init var, etc.
     * This method must be implemented by subclasses to provide the specific inventory setup.
     *
     * @return The initialized inventory instance.
     */
    protected abstract void initialize();

    /**
     * Synchronizes items when returning to the previous inventory. This method must be implemented by subclasses
     * to handle the specific synchronization logic needed when returning to a previous inventory.
     */
    @ForOverride
    protected void syncItemsOnReturn() {

    }

    public Component getTitle() {
        return title;
    }

    /**
     * Returns to the previous inventory if it exists, otherwise closes the current inventory.
     * If the previous inventory is also an instance of InventoryBase, it will synchronize items upon return.
     */
    public void returnToPreviousInventory(HumanEntity player) {
        if (prevInventory == null) {
            player.closeInventory();
            return;
        }

        player.openInventory(prevInventory.getInventory());

        if (prevInventory instanceof BaseInventory) {
            ((BaseInventory) prevInventory).syncItemsOnReturn();
        }
    }

    public void closeInventoryEveryone(List<HumanEntity> players) {
        for (HumanEntity p : new ArrayList<>(players)) {
            p.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        }
    }

    public enum InventorySizeEnum {
        XS(9), S(18), SM(27), LM(36), L(45), XL(54);

        final int size;

        InventorySizeEnum(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }
}