package com.neoworld.cooking;

import com.neoworld.cooking.commands.OpenContainer;
import com.neoworld.cooking.commands.RecipeCommand;
import com.neoworld.cooking.commands.TestCommand;
import com.neoworld.cooking.data.*;
import com.neoworld.cooking.listeners.AdminInputListener;
import com.neoworld.cooking.listeners.CookingTableListener;
import com.neoworld.cooking.listeners.ItemListener;
import com.neoworld.cooking.repeat.AnnouncementLoop;
import com.neoworld.cooking.utils.api.AbstractPlugin;
import com.neoworld.cooking.utils.listeners.InventoryEventListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public final class NWCooking extends AbstractPlugin {

    @Override
    public void initialize() {
        // serializations
        ConfigurationSerialization.registerClass(Recipe.class);
        ConfigurationSerialization.registerClass(CookingTable.class);

        ConfigurationSerialization.registerClass(UserContainer.class);
        ConfigurationSerialization.registerClass(UserContainer.FoodItem.class);


        // listeners
        getServer().getPluginManager().registerEvents(new InventoryEventListener(), this);
        getServer().getPluginManager().registerEvents(new CookingTableListener(), this);
        getServer().getPluginManager().registerEvents(new AdminInputListener(), this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);

        // data
        RecipeProvider.getProvider().init(this);
        CookingTableProvider.getProvider().init(this);
        UserContainerProvider.getProvider().init(this);

        // initializer
        AnnouncementLoop.init(this);

        // test
//        Recipe recipe = RecipeProvider.createRecipe("Test");
//
//        if (recipe != null) {
//            ItemStack[] ingredient = new ItemStack[9];
//            ingredient[4] = new ItemStack(Material.BREAD, 2);
//
//            ItemStack s = new ItemBuilder(Material.DIAMOND).build();
//
//            recipe.setIngredient(ingredient);
//            recipe.setSuccessItem(s);
//        }

        this.getCommand("admingive").setExecutor(new TestCommand());
        this.getCommand("container").setExecutor(new OpenContainer());
        this.getCommand("recipemanager").setExecutor(new RecipeCommand());
        PluginCommand recipeManager = this.getCommand("recipemanager");
        recipeManager.setExecutor(new RecipeCommand());
        recipeManager.setTabCompleter(new RecipeCommand());
    }
}
