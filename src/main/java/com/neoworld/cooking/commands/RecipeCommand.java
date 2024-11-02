package com.neoworld.cooking.commands;

import com.neoworld.cooking.data.Recipe;
import com.neoworld.cooking.data.RecipeProvider;
import com.neoworld.cooking.inventory.admin.RecipeManager;
import com.neoworld.cooking.inventory.user.RecipeListInventory;
import com.neoworld.cooking.utils.chat.ChatUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class RecipeCommand implements CommandExecutor, TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }

        if (args.length == 1 && (args[0].equals("list") || args[0].equals("목록"))) {
            player.openInventory(new RecipeListInventory(player, null).getInventory());
            return true;
        }

        if (!commandSender.isOp()) {
            return true;
        }

        if (args.length >= 2) {
            String recipeId = args[1];

            if (args.length == 2) {
                if (args[0].equals("create") || args[0].equals("생성")) {
                    Recipe recipe = RecipeProvider.createRecipe(recipeId);
                    player.sendMessage(ChatUtils.translateMessages(
                            "message.command.edit.create",
                            recipeId
                    ));
                    return true;
                }
            }

            if (!RecipeProvider.hasRecipe(recipeId)) {
                player.sendMessage(ChatUtils.translateMessages(
                        "message.command.edit.recipenotfound",
                        recipeId
                ));
                return true;
            }

            if (args.length >= 3 && (args[0].equals("rename") || args[0].equals("이름변경"))) {
                Recipe recipe = RecipeProvider.findFromId(recipeId);
                String beforeName = recipe.getName();
                recipe.setName(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                player.sendMessage(ChatUtils.translateMessages(
                        "message.command.edit.rename",
                        recipeId, beforeName, recipe.getName()
                ));
                return true;
            }

            if (args.length == 2) {
                if (args[0].equals("delete") || args[0].equals("삭제")) {
                    RecipeProvider.removeRecipe(recipeId);
                    player.sendMessage(ChatUtils.translateMessages(
                            "message.command.edit.delete",
                            recipeId
                    ));
                    return true;
                }

                if (args[0].equals("edit") || args[0].equals("수정")) {
                    Recipe recipe = RecipeProvider.findFromId(recipeId);
                    player.openInventory(new RecipeManager(recipe, null).getInventory());
                    return true;
                }
            }
        }

        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of(
                    "create", "생성",
                    "delete", "삭제",
                    "rename", "이름변경",
                    "edit", "수정",
                    "list", "목록"
            );
        }

        if (args.length == 2) {
            if (!args[0].equals("create") && !args[0].equals("생성")) {
                return Arrays.stream(RecipeProvider.getRecipes())
                        .map(Recipe::getId)
                        .toList();
            }
        }

        return null;
    }
}
