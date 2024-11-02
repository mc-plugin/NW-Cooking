package com.neoworld.cooking.commands;

import com.neoworld.cooking.data.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TestCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 1) {
                switch (args[0]) {
                    case "table", "요리작업대" ->
                            player.getInventory().addItem(CookingTableProvider.createTable().getItem());
                    case "table-upgrade", "요리작업대-강화권" -> player.getInventory().addItem(CookingTable.getUpgradeItem());
                    case "table-repair", "요리작업대-수리권" -> player.getInventory().addItem(CookingTable.getRepairItem());
                    case "container-upgrade", "요리보관함-강화권" ->
                            player.getInventory().addItem(UserContainer.getUpgradeItem());
                }

            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of(
                    "table", "요리작업대",
                    "table-upgrade", "요리작업대-강화권",
                    "table-repair", "요리작업대-수리권",
                    "container-upgrade", "요리보관함-강화권"
            );
        }

        return null;
    }
}
