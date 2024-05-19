package me.figsq.pctools.pctools.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.EnumType;
import com.pixelmonmod.pixelmon.items.ItemHeld;
import me.figsq.pctools.pctools.api.ISearchProperty;
import net.minecraft.server.v1_12_R1.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
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
    public static Map<EnumSpecies, Pair<String, List<String>>> specialNAL = new HashMap<>();
    public static Map<String, ISearchProperty> searchProperties = new HashMap<>();
    public static Map<String,String> globalPapiReplace = new LinkedHashMap<>();
    public static Map<String, ConfigurationSection> argsPapiReplace = new LinkedHashMap<>();

    public static void init() {
        //clean
        specialNAL.clear();
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
        //special name lore
        for (String key : config.getConfigurationSection("item.special").getKeys(false)) {
            Optional<EnumSpecies> optional = EnumSpecies.getFromName(key);
            if (!optional.isPresent()) {
                continue;
            }
            EnumSpecies species = optional.get();
            specialNAL.put(
                    species,
                    Pair.of(config.getString("item.special." + key + ".name"),
                            config.getStringList("item.special." + key + ".lore"))
            );
        }


        helpMsg = SomeMethod.papi(null, config.getStringList("msg.help").toArray(new String[0]), null);
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
        for (int i = 0; i < PCBox.POKEMON_PER_BOX; i++) {
            int x = (i + 1) % 6;
            int row = (i + 1) / 6;
            int v = x > 0?(row * 9) + (x - 1):((row - 1) * 9) + 5;
            invPcSlot.add(v);
        }

        //searchProperty
        //名
        searchProperties.put("name", new ISearchProperty() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.getLocalizedName().equalsIgnoreCase(arg)
                        || poke.getSpecies().name.equalsIgnoreCase(arg);
            }
        });
        //性别
        searchProperties.put("gender", new ISearchProperty() {
            @Override
            public String getName() {
                return "gender";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                Gender gender = poke.getGender();
                return gender.name().equalsIgnoreCase(arg) ||
                        gender.getLocalizedName().equalsIgnoreCase(arg);
            }
        });
        //属性
        searchProperties.put("type1", new ISearchProperty() {
            @Override
            public String getName() {
                return "type1";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumType type1 = poke.getBaseStats().getType1();
                return type1.name().equalsIgnoreCase(arg) ||
                        type1.getLocalizedName().equalsIgnoreCase(arg);
            }
        });
        searchProperties.put("type2", new ISearchProperty() {
            @Override
            public String getName() {
                return "type2";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumType type2 = poke.getBaseStats().getType2();
                return type2.name().equalsIgnoreCase(arg) ||
                        type2.getLocalizedName().equalsIgnoreCase(arg);
            }
        });
        //特性
        searchProperties.put("ability", new ISearchProperty() {
            @Override
            public String getName() {
                return "ability";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                AbilityBase ability = poke.getAbility();
                return poke.getAbilityName().equalsIgnoreCase(arg) ||
                        ability.getLocalizedName().equalsIgnoreCase(arg);
            }
        });
        //性格
        searchProperties.put("nature", new ISearchProperty() {
            @Override
            public String getName() {
                return "nature";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                EnumNature nature = poke.getNature();
                return nature.name().equalsIgnoreCase(arg) ||
                        nature.getLocalizedName().equalsIgnoreCase(arg);
            }
        });
        //持有物品
        searchProperties.put("helditem", new ISearchProperty() {
            @Override
            public String getName() {
                return "helditem";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                if (poke.getHeldItem() == null ||
                        CraftItemStack.asBukkitCopy((ItemStack) ((Object) poke.getHeldItem()))
                                .getType().equals(Material.AIR)) {
                    return false;
                }
                ItemHeld held = poke.getHeldItemAsItemHeld();
                return held.getLocalizedName().equalsIgnoreCase(arg) ||
                        held.getHeldItemType().name().equalsIgnoreCase(arg);
            }
        });
        //闪光
        searchProperties.put("shiny", new ISearchProperty() {
            @Override
            public String getName() {
                return "shiny";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.isShiny() == Boolean.parseBoolean(arg);
            }
        });
        //自定义名
        searchProperties.put("nickname", new ISearchProperty() {
            @Override
            public String getName() {
                return "nickname";
            }

            @Override
            public boolean hasProperty(Pokemon poke, String arg) {
                return poke.getNickname().equalsIgnoreCase(arg);
            }
        });
    }
}
