package com.neoworld.cooking.utils.chat;

import com.neoworld.cooking.NWCooking;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils {
    public static String toColorString(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static String translateMessages(String key, String... args) {
        String msg = NWCooking.config.getString(key);

        if (msg == null)
            return key;

        for (int i = 0; i < args.length; i++) {
            msg = msg.replaceAll("%"+ (i+1) +"s", args[i]);
        }

        return toColorString(msg);
    }

    public static List<String> toColorList(List<String> list) {
        List<String> stringList = new ArrayList<>();

        for (String string : list) {
            stringList.add(toColorString(string));
        }

        return stringList;
    }
}