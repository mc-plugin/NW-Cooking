package com.neoworld.cooking.utils.inventory;

import com.neoworld.cooking.utils.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemstack) {
        this.item = itemstack;
        this.meta = item.getItemMeta();
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    public ItemBuilder addFlag(ItemFlag... flag) {
        meta.addItemFlags(flag);

        return this;
    }

    public ItemBuilder setOwner(OfflinePlayer player) {
        if (!(meta instanceof SkullMeta) || player == null || item.getType() != Material.PLAYER_HEAD) {
            return this;
        }

        ((SkullMeta) meta).setOwningPlayer(player);
        return this;
    }

    public ItemBuilder setOwner(UUID uuid) {

        if (uuid == null)
            return this;

        return setOwner(Bukkit.getOfflinePlayer(uuid));
    }

    public ItemBuilder addComponentLore(Component lore) {
        List<Component> loreList = meta.lore();
        if (loreList == null)
            loreList = new ArrayList<>();

        loreList.add(lore);
        setComponentLore(loreList);

        return this;
    }

    public ItemBuilder addLore(String lore) {
        List<String> loreList = meta.getLore();
        if (loreList == null)
            loreList = new ArrayList<>();

        loreList.add(ChatUtils.toColorString(lore));
        setLore(loreList);

        return this;
    }

    public ItemBuilder setComponentLore(List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(ChatUtils.toColorList(lore));
        return this;
    }

    public ItemBuilder setDisplayName(String displayName) {
        meta.setDisplayName(ChatUtils.toColorString(displayName));

        return this;
    }

    public ItemBuilder setComponentDisplayName(Component displayName) {
        meta.displayName(displayName);

        return this;
    }

    public ItemBuilder setPersistentData(NamespacedKey key, PersistentDataType type, Object o) {
        meta.getPersistentDataContainer().set(key, type, o);
        return this;
    }

}
