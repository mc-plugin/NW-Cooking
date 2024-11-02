package com.neoworld.cooking.listeners;

import com.neoworld.cooking.inventory.admin.RecipeManager;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.DateUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AdminInputListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        if (RecipeManager.hasState(player)) {
            String msg = event.getMessage();
            event.setCancelled(true);

            if (!msg.matches("-?[0-9]+(\\.[0-9]+)?")) {
                player.sendMessage(ChatUtils.translateMessages("message.editor.not_decimal"));
                return;
            }

            double input = Double.parseDouble(msg);
            if (input <= 0) {
                player.sendMessage(ChatUtils.translateMessages("message.editor.must_non_negative"));
                return;
            }

            RecipeManager.State state = RecipeManager.getState(player);
            RecipeManager manager = state.getManager();
            switch (state.getType()) {
                case TIME -> {
                    input *= 1000; // ms to s;

                    manager.recipe.setCookingTime((long) input);
                    RecipeManager.removeState(player);

                    String id = manager.recipe.getId();

                    player.sendMessage(ChatUtils.translateMessages(
                            "message.editor.time.success",
                            id, DateUtils.getTimerFormat(((long) input), DateUtils.HOUR_DATE_FORMAT)
                    ));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                }
                case SPECIAL_RATIO -> {
                    if (input >= 100) {
                        player.sendMessage(ChatUtils.translateMessages("message.editor.special.ratio.range"));
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                        return;
                    }

                    manager.recipe.setSpecialRatio(((float) input / 100));

                    String id = manager.recipe.getId();
                    String formattedInput = String.format("%.2f", input);

                    state.changeSpecialType();

                    player.sendMessage(
                            ChatUtils.translateMessages(
                                "message.editor.special.ratio.success",
                                id, formattedInput
                            ) + "\n&f\n" +
                            ChatUtils.translateMessages(
                                "message.editor.special.multiply"
                            )
                    );
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                    break;
                }
                case SPECIAL_MUL -> {
                    manager.recipe.setSpecialMultiplier((int) input);
                    RecipeManager.removeState(player);

                    String id = manager.recipe.getId();

                    player.sendMessage(ChatUtils.translateMessages(
                            "message.editor.special.multiply.success",
                            id, String.valueOf(input)
                    ));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                    break;
                }
            }

        }

    }
}
