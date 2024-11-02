package com.neoworld.cooking.listeners;

import com.neoworld.cooking.data.UserContainer;
import com.neoworld.cooking.data.UserContainerProvider;
import com.neoworld.cooking.utils.chat.ChatUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack currentItem = player.getInventory().getItemInMainHand();

        if (currentItem.isSimilar(UserContainer.getUpgradeItem())) {
            UserContainer container = UserContainerProvider.getContainer(player);
            int beforeLevel = container.getLevel();

            if (UserContainer.getMaxLevel() < beforeLevel + 1) {
                player.sendMessage(ChatUtils.translateMessages("message.container.max_level"));
                player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            container.setLevel(beforeLevel + 1);
            player.sendMessage(ChatUtils.translateMessages(
                    "message.container.level_up",
                    String.valueOf(beforeLevel), String.valueOf(container.getLevel())
            ));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

            currentItem.setAmount(currentItem.getAmount() - 1);
        }
    }
}
