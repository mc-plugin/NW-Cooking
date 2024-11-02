package com.neoworld.cooking.data;

import com.neoworld.cooking.NWCooking;
import com.neoworld.cooking.inventory.user.InfoInventory;
import com.neoworld.cooking.inventory.user.TableInventory;
import com.neoworld.cooking.utils.chat.ChatUtils;
import com.neoworld.cooking.utils.inventory.ItemBuilder;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class CookingTable implements ConfigurationSerializable {

    @NotNull
    final UUID tableUUID;

    @Nullable
    UUID ownerUUID;

    int energy;
    int cookCount;
    int level;

    @Nullable
    String recipeId;
    @Nullable
    Long startedTimestamp;
    @Nullable
    UUID cookUserUUID;

    final TableInventory tableInventory;
    final InfoInventory InfoInventory;

    @Nullable
    Block block;

    protected CookingTable(@NotNull UUID uuid) {
        this.tableUUID = uuid;

        this.energy = NWCooking.config.getInt("defaultEnergy");
        this.cookCount = 0;
        this.level = 1;

        this.tableInventory = new TableInventory(null, this);
        this.InfoInventory = new InfoInventory(null, this);
    }

    public CookingTable(Map<String, Object> serialize) {
        this.tableUUID = UUID.fromString((String) serialize.get("uuid"));

        this.ownerUUID = serialize.get("ownerUUID") != null ? UUID.fromString((String) serialize.get("ownerUUID")) : null;

        this.energy = (int) serialize.getOrDefault("energy", 0);
        this.cookCount = (int) serialize.getOrDefault("cookCount", 0);
        this.level = (int) serialize.getOrDefault("level", 0);;

        if (RecipeProvider.hasRecipe((String) serialize.get("recipeId"))) {
            this.recipeId = (String) serialize.get("recipeId");
            this.startedTimestamp = Long.parseLong(serialize.get("startedTimestamp").toString());
            this.cookUserUUID = UUID.fromString((String) serialize.get("cookUserUUID"));
        }

        this.tableInventory = new TableInventory(null, this);
        this.InfoInventory = new InfoInventory(null, this);
    }

    public boolean isEnoughEnergy() {
        return getEnoughEnergy() <= energy;
    }

    public int getEnoughEnergy() {
        return ((int) (getDefaultMaxUseEnergy() * getDiscountedRate()));
    }

    public void startCook(Recipe recipe, Player player) {
        this.recipeId = recipe.id;
        this.cookUserUUID = player.getUniqueId();
        this.startedTimestamp = System.currentTimeMillis();

        UserContainer container = UserContainerProvider.getOrCreateContainer(player);
        container.addPermission(recipe);

        update();
    }

    public void evaluateCook() throws NullPointerException {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(Optional.ofNullable(getCookUserUUID()).orElseThrow());
        UserContainer container = UserContainerProvider.getOrCreateContainer(offlinePlayer.getUniqueId());
        Recipe recipe = getRecipe();

        if (!isCooking() || getLeftTime() > 0
                || container == null || recipe == null) {
            return;
        }

        boolean isSuccess = Math.random() < 0.5;
        boolean isSpecial = Math.random() < recipe.specialRatio;

        String key = "message.table";
        Sound sound;

        if (isSuccess) {
            key += ".success";
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        } else {
            key += ".fail";
            sound = Sound.ENTITY_VILLAGER_NO;
        }

        if (isSpecial && (isSuccess || recipe.getFailItem() != null)) {
            key += ".special";
        } else {
            key += ".message";
        }

        container.addItem(recipe, isSuccess, isSpecial);

        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            player.sendMessage(ChatUtils.translateMessages(key, recipe.getName()));
            player.playSound(player, sound, 1f ,1f);
        }

        clearCookData();
        tableInventory.clearTable();

        cookCount++;
    }

    public void clearCookData() {
        this.recipeId = null;
        this.cookUserUUID = null;
        this.startedTimestamp = null;

        update();
    }

    public double getDiscountedRate() {

        Function normalize = new Function("normal", 1) {
            @Override
            public double apply(double... args) {
                return Math.max(0.0, Math.min(args[0], 1.0));
            }
        };

        String originExp = NWCooking.config.getString("energyDiscountRateExpression", "1.0");
        Expression expression;
        try {
            expression = new ExpressionBuilder(originExp)
                    .variables("level")
                    .function(normalize)
                    .build()
                    .setVariable("level", level);
            return expression.evaluate();
        } catch (Exception e) {
            NWCooking.LOGGER.warning("energyDiscountRateExpression evaluate fail. more detail is here.");
            e.printStackTrace();

            expression = new ExpressionBuilder("normal(1 / (level + 4) * 5)")
                    .variables("level")
                    .function(normalize)
                    .build()
                    .setVariable("level", level);

            return expression.evaluate();
        }
    }

    @Nullable
    public Recipe getRecipe() {
        if (!RecipeProvider.hasRecipe(this.recipeId)) {
            clearCookData();
            return null;
        } else {
            return RecipeProvider.findFromId(this.recipeId);
        }
    }

    public int getEnergy() {
        return energy;
    }

    public int getLevel() {
        return Math.min(level, getMaxLevel());
    }

    public void setLevel(int level) {
        this.level = level;
        update();
    }

    public void setEnergy(int energy) {
        this.energy = energy;
        update();
    }

    public int getCookCount() {
        return cookCount;
    }

    public void setCookCount(int cookCount) {
        this.cookCount = cookCount;
    }

    @Nullable
    public UUID getCookUserUUID() {
        return cookUserUUID;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public InfoInventory getInfoInventory() {
        return InfoInventory;
    }

    public TableInventory getTableInventory() {
        return tableInventory;
    }

    public @NotNull UUID getTableUUID() {
        return tableUUID;
    }

    public void setOwnerUUID(Player player) {
        this.ownerUUID = player.getUniqueId();
        update();
    }

    public boolean isCooking() {
        boolean isCooking = (recipeId != null && cookUserUUID != null && startedTimestamp != null);

        if (!isCooking && (recipeId != null || cookUserUUID != null || startedTimestamp != null)) {
            clearCookData();
        }

        return isCooking;
    }

    public long getLeftTime() {
        if (startedTimestamp == null || getRecipe() == null)
            return -1;

        return Math.max((startedTimestamp - System.currentTimeMillis()) + getRecipe().cookingTime, 0);
    }

    @Nullable
    public Block getBlock() {
        return block;
    }

    public void setBlock(@Nullable Block block) {
        this.block = block;
    }

    public ItemStack getItem() {
        return new ItemBuilder(Material.FURNACE)
                .setDisplayName("&e요리작업대")
                .setLore(List.of(
                        "&7• 요리작업대 레벨 : &blevel " + (getLevel()),
                        "&7• 잔여화력 : &c" + energy +"&4/&c" + NWCooking.config.getInt("maxEnergy", 0),
                        "&8• 요리작업대가 사용된 횟수 : &7" + getCookCount()
                ))
                .setPersistentData(getKey(), PersistentDataType.STRING, this.tableUUID.toString())
                .build();
    }

    public static ItemStack getRepairItem() {
        return new ItemBuilder(Material.PAPER)
                .setDisplayName("&6&l요리작업대 수리권")
                .addLore("&f■ 요리작업대의 내구도를 &6"+ getRepairAmount() + " &f만큼 수리합니다.")
                .build();
    }

    public static ItemStack getUpgradeItem() {
        return new ItemBuilder(Material.PAPER)
                .setDisplayName("&b&l요리작업대 업그레이드")
                .addLore("&f■ 요리작업대의 레벨 &d1 만큼 올립니다.")
                .build();
    }

    public void update() {
        CookingTableProvider.provider.save();
    }

    public static NamespacedKey getKey() {
        return new NamespacedKey(NWCooking.getInstance(), "table_data");
    }

    public static int getMaxLevel() {
        return NWCooking.config.getInt("maxTableLevel", 5);
    }

    public static int getMaxEnergy() {
        return NWCooking.config.getInt("maxEnergy", 20000);
    }

    public static int getDefaultMaxUseEnergy() {
        return NWCooking.config.getInt("useEnergyMax", 500);
    }

    public int getMaxUseEnergy() {
        return (int) (getDefaultMaxUseEnergy() * getDiscountedRate());
    }

    public static int getDefaultMinUseEnergy() {
        return NWCooking.config.getInt("useEnergyMin", 50);
    }

    public int getMinUseEnergy() {
        return (int) (getDefaultMinUseEnergy() * getDiscountedRate());
    }

    public static int getRepairAmount() {
        return NWCooking.config.getInt("repairAmount", 2500);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> serializedMap = new HashMap<>();

        serializedMap.put("uuid", this.tableUUID.toString());

        if (this.ownerUUID != null)
            serializedMap.put("ownerUUID", this.ownerUUID.toString());

        serializedMap.put("energy", this.energy);
        serializedMap.put("cookCount", this.cookCount);
        serializedMap.put("level", this.level);

        if (RecipeProvider.hasRecipe(recipeId)) {
            serializedMap.put("recipeId", this.recipeId);
            serializedMap.put("startedTimestamp", this.startedTimestamp);
            serializedMap.put("cookUserUUID", this.cookUserUUID.toString());
        }

        return serializedMap;
    }
}
