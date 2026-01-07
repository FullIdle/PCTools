package me.figsq.pctools.pctools.api;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class PapiUtil {
    /**
     * 字符串变量处理及颜色代码处理
     *
     * @param player 可以为null,null则不会进行papi的操作而是颜色代码替换
     */
    public static String papi(OfflinePlayer player, String str) {
        str = str.replace("&", "§");
        if (player == null) return str;
        return PlaceholderAPI.setPlaceholders(player, str);
    }

    public static List<String> papi(OfflinePlayer player, Collection<String> ss, Function<String, String> fun) {
        ArrayList<String> list = new ArrayList<>(ss);
        list.replaceAll(s -> papi(player, fun != null?fun.apply(s):s));
        return list;
    }

    public static String[] papi(OfflinePlayer player, String[] strings, Function<String, String> fun) {
        String[] temp = new String[strings.length];
        boolean b = fun != null;
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            if (b) string = fun.apply(string);
            temp[i] = papi(player, string);
        }
        return temp;
    }
}
