package com.neoworld.cooking.data;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.utils.exceptions.ConfigurationEmptyException;
import com.neoworld.cooking.utils.exceptions.ConfigurationLoadFailedException;
import com.neoworld.cooking.utils.exceptions.ConfigurationWrongPathException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RecipeProvider {

    public static String FILENAME = "RecipeData.yaml";
    private YamlConfiguration config;
    private File configFile;

    static List<Recipe> recipes;
    static RecipeProvider provider = new RecipeProvider();

    public static RecipeProvider getProvider() {
        return provider;
    }

    public void init(Plugin plugin) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        this.configFile = new File(NWCooking.getInstance().getDataFolder() + File.separator + FILENAME);

        if ((!this.configFile.exists())) {
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            load();
        } catch (ConfigurationLoadFailedException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        config.set("root", recipes);
        try {
            this.config.save(this.configFile);
        } catch (Exception e) {
            NWCooking.LOGGER.warning("Recipe saving failed. the reason is '" + e.getMessage() + "'");
        }
    }

    public void load() throws ConfigurationLoadFailedException {
        String path = "root";

        try {
            this.config = YamlConfiguration.loadConfiguration(this.configFile);

            if (this.config.saveToString().isEmpty()) {
                throw new ConfigurationEmptyException();
            } else if (!this.config.contains(path)) {
                throw new ConfigurationWrongPathException("cannot find 'root' path");
            } else if (!this.config.isList(path)) {
                throw new ConfigurationWrongPathException("'root' path is not List, Root path must be List.");
            }

            recipes = (List<Recipe>) this.config.getList(path);
        } catch (Exception e) {
            this.config = new YamlConfiguration();
            recipes = new ArrayList<>();

            if (e instanceof ConfigurationEmptyException) {
                NWCooking.LOGGER.warning("Recipe file was empty. initializing recipe file.");
                save();
            } else {
                throw new ConfigurationLoadFailedException("load yaml configuration failed", e);
            }
        }
    }

    // register methods

    public static Recipe[] getRecipes() {
        return recipes.toArray(Recipe[]::new);
    }

    public static Recipe[] getHaveRecipes(UserContainer user) {
        return recipes.stream()
                .filter(Recipe::isAvailable)
                .filter(r -> r.getPermission() == Recipe.RecipePermission.PUBLIC
                        || user != null && user.hasPermission(r))
                .toArray(Recipe[]::new);
    }

    public static Recipe createRecipe(String id) {
        Recipe recipe = null;

        if (recipes.stream().noneMatch(r -> r.id.equals(id))) {
            recipe = new Recipe(id);

            recipes.add(recipe);
            getProvider().save();
        }

        return recipe;
    }

    public static boolean removeRecipe(String id) {
        boolean result = recipes.remove(recipes.stream()
                .filter(recipe -> recipe.id.equals(id))
                .findFirst()
                .orElse(null));

        if (result)
            getProvider().save();

        return result;
    }

    // research methods

    public static Recipe findFromIngredient(ItemStack[] ingredient) {
        return findFromIngredient(ingredient, true, null);
    }

    public static Recipe findFromIngredient(ItemStack[] ingredient, boolean includeHidden) {
        return findFromIngredient(ingredient, includeHidden, null);
    }

    public static Recipe findFromIngredient(ItemStack[] ingredient, boolean includeHidden, @Nullable UserContainer user) {
        return unSafeRecipeFromIngredient(ingredient)
                .filter(r -> r.getPermission() == Recipe.RecipePermission.PUBLIC
                            || (includeHidden && r.getPermission() == Recipe.RecipePermission.HIDDEN)
                            || user != null && user.hasPermission(r))
                .findFirst().orElse(null);
    }

    public static Stream<Recipe> unSafeRecipeFromIngredient(ItemStack[] ingredient) {
        return recipes.stream()
                .filter(recipe -> Arrays.equals(recipe.ingredient, ingredient,
                        (a, b) -> {
                            ItemStack itemA = Optional.ofNullable(a).orElse(ItemStack.empty());
                            ItemStack itemB = Optional.ofNullable(b).orElse(ItemStack.empty());
                            return (itemA.isSimilar(itemB) && itemA.getAmount() == itemB.getAmount()) ? 0 : 1;
                        }))
                .filter(Recipe::isAvailable);
    }

    public static Recipe findFromId(String id) {
        return recipes.stream()
                .filter(recipe -> id.equals(recipe.id))
                .findFirst().orElse(null);
    }

    public static boolean hasRecipe(String id) {
        if (id == null)
            return false;

        return recipes.stream()
                .anyMatch(recipe -> id.equals(recipe.id));
    }
}
