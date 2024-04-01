package me.figsq.pctools.pctools.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import me.figsq.pctools.pctools.Main;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Cache {
    //cache
    public static String pCGuiTitle;
    public static Main plugin;
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
    public static Map<EnumSpecies, Pair<String,List<String>>> specialNAL = new HashMap<>();

    public static void init(){
        //clean
        specialNAL.clear();

        FileConfiguration config = plugin.getConfig();
        pCGuiTitle = config.getString("title");

        //name lore
        normalName = config.getString("item.normal.name");
        eggName = config.getString("item.egg.name");
        legendName = config.getString("item.legend.name");
        uBeastName = config.getString("item.uBeast.name");
        normalLore = config.getStringList("item.normal.lore");
        eggLore = config.getStringList("item.egg.lore");
        legendLore = config.getStringList("item.legend.lore");
        uBeastLore = config.getStringList("item.uBeast.lore");
        //special name lore
        ConfigurationSection specialConfig = config.getConfigurationSection("item.special");
        for (String key : specialConfig.getKeys(false)) {
            Optional<EnumSpecies> optional = EnumSpecies.getFromName(key);
            if (!optional.isPresent()) {
                continue;
            }
            EnumSpecies species = optional.get();
            specialNAL.put(
                    species,
                    Pair.of(specialConfig.getString(key+".name"),
                            specialConfig.getStringList(key+".lore"))
            );
        }


        helpMsg = SomeMethod.papi(null,config.getStringList("msg.help").toArray(new String[0]),null);
        cancelPC = config.getBoolean("cancelPC");
        packCanEmpty = config.getBoolean("packCanEmpty");
    }

    static {
        for (int i = 0; i < 6; i++) {
            invBackpackSlot.add(((i+1)*9)-2);
        }
        for (int i = 0; i < PixelmonConfig.computerBoxes; i++) {
            int x = (i+1) % 6;
            int row = (i + 1) / 6;
            if (x > 0) {
                invPcSlot.add((row * 9)+(x-1));
                continue;
            }
            invPcSlot.add(((row-1)*9)+5);
        }
    }
}
