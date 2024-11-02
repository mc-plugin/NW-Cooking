package com.neoworld.cooking.listeners;

import com.neoworld.cooking.data.CookingTable;
import com.neoworld.cooking.data.CookingTableProvider;
import com.neoworld.cooking.inventory.user.TableInventory;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.DateUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class CookingTableListener implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.FURNACE ||
                !(block.getState() instanceof TileState))
            return;

        // block data
        TileState state = ((TileState) block.getState());
        PersistentDataContainer blockContainer = state.getPersistentDataContainer();

        UUID uuid = getUUIDFromItem(event.getItemInHand());

        if (uuid != null) {
            CookingTable data = CookingTableProvider.findFormUUID(uuid);

            if (data == null) {
                data = CookingTableProvider.createTable();
            }

            if (data.getOwnerUUID() == null) {
                data.setOwnerUUID(event.getPlayer());
            }

            blockContainer.set(CookingTable.getKey(),
                    PersistentDataType.STRING,
                    uuid.toString()
            );

            state.update();
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        UUID uuid = getUUIDFromBlock(block);

        if (uuid != null) {

            event.setDropItems(false);
            CookingTable data = CookingTableProvider.findFormUUID(uuid);

            if (data.isCooking()) {
                event.setCancelled(true);
                sendActionFailMessage(player, data);
                return;
            }

            for (int i : TableInventory.getTableIndex()) {
                ItemStack item = data.getTableInventory().getInventory().getItem(i);
                if (item != null)
                    block.getWorld().dropItemNaturally(block.getLocation(), item.clone());
            }

            data.getTableInventory().clearTable();
            block.getWorld().dropItemNaturally(block.getLocation(), data.getItem());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null)
            return;

        UUID uuid = getUUIDFromBlock(block);
        if (uuid != null) {
            event.setCancelled(true);

            CookingTable data = CookingTableProvider.findFormUUID(uuid);

            if (data == null) {
                data = CookingTableProvider.createTable();
            }
            data.setBlock(block);

            ItemStack currentItem = player.getInventory().getItemInMainHand();
            if (currentItem.isSimilar(CookingTable.getRepairItem())) {
                int beforeEnergy = data.getEnergy();

                if (CookingTable.getMaxEnergy() <= data.getEnergy()) {
                    player.sendMessage(ChatUtils.translateMessages("message.table.full_energy"));
                    player.playSound(player, Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    return;
                }

                data.setEnergy(Math.min(data.getEnergy() + CookingTable.getRepairAmount(), CookingTable.getMaxEnergy()));
                player.sendMessage(ChatUtils.translateMessages(
                        "message.table.charge_energy",
                        String.valueOf(beforeEnergy), String.valueOf(data.getEnergy())
                ));
                player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f);

                currentItem.setAmount(currentItem.getAmount() - 1);
                return;
            }

            if (currentItem.isSimilar(CookingTable.getUpgradeItem())) {
                int beforeLevel = data.getLevel();

                if (CookingTable.getMaxLevel() <= beforeLevel) {
                    player.sendMessage(ChatUtils.translateMessages("message.table.max_level"));
                    player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }

                data.setLevel(data.getLevel() + 1);
                player.sendMessage(ChatUtils.translateMessages(
                        "message.table.charge_energy",
                        String.valueOf(beforeLevel), String.valueOf(data.getLevel())
                ));
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                currentItem.setAmount(currentItem.getAmount() - 1);
                return;
            }

            if (player.isSneaking())
                player.openInventory(data.getInfoInventory().getInventory());
            else {
                if (!data.isCooking()) {
                    player.openInventory(data.getTableInventory().getInventory());
                } else {
                    sendActionFailMessage(player, data);
                }
            }
        }

    }

    private UUID getUUIDFromPDC(PersistentDataContainer pdc) {
        if (!pdc.has(CookingTable.getKey()))
            return null;

        String originUUID = pdc.get(CookingTable.getKey(), PersistentDataType.STRING);
        return originUUID != null ? UUID.fromString(originUUID) : null;
    }

    private UUID getUUIDFromBlock(Block block) {
        if (!(block.getState() instanceof TileState))
            return null;

        TileState state = ((TileState) block.getState());
        PersistentDataContainer container = state.getPersistentDataContainer();

        return getUUIDFromPDC(container);
    }

    private UUID getUUIDFromItem(ItemStack item) {
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return getUUIDFromPDC(container);
    }

    private void sendActionFailMessage(Player player, CookingTable data) {
        player.sendMessage(ChatUtils.translateMessages(
                "message.table.already_cooking",
                DateUtils.getTimerFormat(data.getLeftTime(), DateUtils.HOUR_DATE_FORMAT)
        ));
    }
}
