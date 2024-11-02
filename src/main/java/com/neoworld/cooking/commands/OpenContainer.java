package com.neoworld.cooking.commands;

import com.neoworld.cooking.inventory.user.ContainerInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenContainer implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            player.openInventory(new ContainerInventory(player, null).getInventory());
        }

        return true;
    }
}
