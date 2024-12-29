package me.figsq.pctools.pctools.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class Cache {
    //cache
    public static String pCGuiTitle;
    public static JavaPlugin plugin;
    public static final ArrayList<Integer> invBackpackSlot = new ArrayList<>();
    public static final ArrayList<Integer> invPcSlot = new ArrayList<>();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static String normalName;
    public static String eggName;
    public static String legendName;
    public static String uBeastName;
    public static boolean cancelPC;
    public static List<String> normalLore;
    public static List<String> eggLore;
    public static List<String> legendLore;
    public static List<String> uBeastLore;
    public static String[] helpMsg;
    public static boolean packCanEmpty;
    public static Double papiIndexOffset;
    public static Map<String,String> globalPapiReplace = new LinkedHashMap<>();
    public static Map<String, ConfigurationSection> argsPapiReplace = new LinkedHashMap<>();
    public static Map<String, ISearchProperty> searchProperties = new HashMap<>();

    public static void init() {
        //clean
        globalPapiReplace.clear();
        argsPapiReplace.clear();

        FileConfiguration config = plugin.getConfig();
        pCGuiTitle = config.getString("title");
        papiIndexOffset = config.getDouble("papiIndexOffset");

        //name lore
        normalName = config.getString("item.normal.name");
        eggName = config.getString("item.egg.name");
        legendName = config.getString("item.legend.name");
        uBeastName = config.getString("item.uBeast.name");
        normalLore = config.getStringList("item.normal.lore");
        eggLore = config.getStringList("item.egg.lore");
        legendLore = config.getStringList("item.legend.lore");
        uBeastLore = config.getStringList("item.uBeast.lore");

        helpMsg = PapiUtil.papi(null, config.getStringList("msg.help").toArray(new String[0]), null);
        cancelPC = config.getBoolean("cancelPC");
        packCanEmpty = config.getBoolean("packCanEmpty");
        //papiReplace
        {
            ConfigurationSection global = config.getConfigurationSection("papiReplace.global");
            for (String key : global.getKeys(false)) {
                globalPapiReplace.put(key,global.getString(key));
            }
            ConfigurationSection args = config.getConfigurationSection("papiReplace.args");
            for (String key : args.getKeys(false)) {
                argsPapiReplace.put(key,args.getConfigurationSection(key));
            }
        }
    }

    static {
        for (int i = 0; i < 6; i++) {
            invBackpackSlot.add(((i + 1) * 9) - 2);
        }
        for (int i = 0; i < 30; i++) {
            int x = (i + 1) % 6;
            int row = (i + 1) / 6;
            int v = x > 0?(row * 9) + (x - 1):((row - 1) * 9) + 5;
            invPcSlot.add(v);
        }
    }
}
