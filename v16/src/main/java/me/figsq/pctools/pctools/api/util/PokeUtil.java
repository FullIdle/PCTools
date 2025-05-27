package me.figsq.pctools.pctools.api.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.util.ITranslatable;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.items.HeldItem;
import me.figsq.pctools.pctools.api.Cache;
import me.figsq.pctools.pctools.api.ISearchProperty;
import me.figsq.pctools.pctools.api.PapiUtil;
import me.towdium.pinin.Keyboard;
import me.towdium.pinin.PinIn;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PokeUtil {
    public static Map<Species, Pair<String, List<String>>> specialNAL = new HashMap<>();

    public static void init() {
        specialNAL.clear();


        FileConfiguration config = Cache.plugin.getConfig();

        //special name lore
        for (String key : config.getConfigurationSection("item.special").getKeys(false)) {
            Optional<Species> optional = PixelmonSpecies.fromName(key).getValue();
            if (!optional.isPresent()) {
                continue;
            }
            Species species = optional.get();
            specialNAL.put(
                    species,
                    Pair.of(config.getString("item.special." + key + ".name"),
                            config.getStringList("item.special." + key + ".lore"))
            );
        }
    }

    public static ItemStack getFormatPokePhoto(Pokemon pokemon) {
        if (pokemon == null) {
            return null;
        }
        net.minecraft.server.v1_16_R3.ItemStack photo = (net.minecraft.server.v1_16_R3.ItemStack) ((Object) SpriteItemHelper.getPhoto(pokemon));
        NBTTagCompound tag = photo.getTag() == null ? new NBTTagCompound() : photo.getTag();
        tag.setString("pctoolsUUID", pokemon.getUUID().toString());
        photo.setTag(tag);
        Player player = ((EntityPlayer) ((Object) pokemon.getOwnerPlayer()))
                .getBukkitEntity().getPlayer();
        ItemStack copy = CraftItemStack.asBukkitCopy(photo);
        ItemMeta itemMeta = copy.getItemMeta();
        String name;
        List<String> lore;
        Pair<String, List<String>> pair = specialNAL.get(pokemon.getSpecies());
        if (pair == null) {
            name = pokemon.isEgg() ?
                    Cache.eggName : pokemon.isLegendary() ?
                    Cache.legendName : pokemon.getSpecies().isUltraBeast() ?
                    Cache.uBeastName : Cache.normalName;
            lore = pokemon.isEgg() ?
                    Cache.eggLore : pokemon.isLegendary() ?
                    Cache.legendLore : pokemon.getSpecies().isUltraBeast() ?
                    Cache.uBeastLore : Cache.normalLore;
        } else {
            name = pair.getLeft();
            lore = pair.getRight();
        }

        StoragePosition ps = pokemon.getPosition();
        Function<String, String> fun = s -> s.replace("{box}", String.valueOf(((int) (ps.box - Cache.papiIndexOffset))))
                .replace("{order}", String.valueOf(((int) (ps.order - Cache.papiIndexOffset))));
        itemMeta.setDisplayName(PapiUtil.papi(player, fun.apply(name)));
        itemMeta.setLore(PapiUtil.papi(player, lore, fun));
        copy.setItemMeta(itemMeta);
        return copy;
    }

    public static UUID getFormatItemUUID(ItemStack itemStack) {
        if (itemStack == null) return null;

        net.minecraft.server.v1_16_R3.ItemStack copy = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound nbt = copy.getTag() == null ? new NBTTagCompound() : copy.getTag();
        if (nbt.hasKey("pctoolsUUID")) {
            return UUID.fromString(nbt.getString("pctoolsUUID"));
        }
        return null;
    }

    public static net.minecraft.util.Tuple<PokemonStorage, StoragePosition> computeStorageAndPosition(int clickSlot, PlayerPartyStorage partyStorage, PCBox pcBox) {
        if (Cache.invBackpackSlot.contains(clickSlot))
            return new net.minecraft.util.Tuple<>(partyStorage, new StoragePosition(-1, Cache.invBackpackSlot.indexOf(clickSlot)));
        if (Cache.invPcSlot.contains(clickSlot))
            return new net.minecraft.util.Tuple<>(pcBox, new StoragePosition(pcBox.boxNumber, Cache.invPcSlot.indexOf(clickSlot)));
        return null;
    }

    public static final PinIn pinIn;

    static {
        pinIn = new PinIn();
        pinIn.config().fCh2C(true).commit();
        pinIn.config().fZh2Z(true).commit();
        pinIn.config().fSh2S(true).commit();
        pinIn.config().fAng2An(true).commit();
        pinIn.config().fEng2En(true).commit();
        pinIn.config().fIng2In(true).commit();
        pinIn.config().accelerate(true).commit();
        pinIn.config().keyboard(Keyboard.QUANPIN).commit();

        //searchProperty
        ISearchProperty.addSearchProperty("name", new ISearchProperty() {
            @Override
            public String getName() {
                return "name";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                Pokemon pokemon = (Pokemon) poke;
                String localizedName = pokemon.getLocalizedName();
                String name = pokemon.getSpecies().getName();
                return localizedName.equalsIgnoreCase(arg)
                        || name.equalsIgnoreCase(arg)
                        || pinIn.contains(name, arg)
                        || pinIn.contains(localizedName, arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                UUID uniqueId = player.getUniqueId();
                PlayerPartyStorage party = StorageProxy.getParty(uniqueId);
                PCStorage pc = StorageProxy.getPCForPlayer(uniqueId);
                ArrayList<Pokemon> pokemons = Lists.newArrayList(pc.getAll());
                Collections.addAll(pokemons, party.getAll());
                pokemons.removeIf(Objects::isNull);
                List<String> collect = pokemons.stream().map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //性别
        ISearchProperty.addSearchProperty("gender", new ISearchProperty() {
            @Override
            public String getName() {
                return "gender";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                Gender gender = ((Pokemon) poke).getGender();
                return gender.name().equalsIgnoreCase(arg) ||
                        gender.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Gender.values()).map(ITranslatable::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //属性
        ISearchProperty.addSearchProperty("type1", new ISearchProperty() {
            @Override
            public String getName() {
                return "type1";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                return ((Pokemon) poke).getForm().hasType(Element.parseType(arg));
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Element.getAllTypes().stream().map(Element::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        ISearchProperty.addSearchProperty("type2", new ISearchProperty() {
            @Override
            public String getName() {
                return "type2";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                return ((Pokemon) poke).getForm().hasType(Element.parseType(arg));
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Element.getAllTypes().stream().map(Element::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) {
                    return collect;
                }
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //特性
        ISearchProperty.addSearchProperty("ability", new ISearchProperty() {
            @Override
            public String getName() {
                return "ability";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                Pokemon pokemon = (Pokemon) poke;
                Ability ability = pokemon.getAbility();
                return pokemon.getAbilityName().equalsIgnoreCase(arg) ||
                        ability.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
        //性格
        ISearchProperty.addSearchProperty("nature", new ISearchProperty() {
            @Override
            public String getName() {
                return "nature";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                Nature nature = ((Pokemon) poke).getNature();
                return nature.name().equalsIgnoreCase(arg) ||
                        nature.getLocalizedName().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(Nature.values()).map(Nature::getLocalizedName).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //持有物品
        ISearchProperty.addSearchProperty("helditem", new ISearchProperty() {
            @Override
            public String getName() {
                return "helditem";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                Pokemon pokemon = (Pokemon) poke;
                if (pokemon.getHeldItem() == null ||
                        CraftItemStack.asBukkitCopy((net.minecraft.server.v1_16_R3.ItemStack) ((Object) pokemon.getHeldItem()))
                                .getType().equals(Material.AIR)) {
                    return false;
                }
                HeldItem held = pokemon.getHeldItemAsItemHeld();
                return held.getLocalizedName().equalsIgnoreCase(arg) ||
                        held.getHeldItemType().name().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                List<String> collect = Arrays.stream(EnumHeldItems.values()).map(EnumHeldItems::name).collect(Collectors.toList());
                if (value.isEmpty()) return collect;
                return collect.stream().filter(s -> s.startsWith(value)).collect(Collectors.toList());
            }
        });
        //闪光
        ISearchProperty.addSearchProperty("shiny", new ISearchProperty() {
            @Override
            public String getName() {
                return "shiny";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                return ((Pokemon) poke).isShiny() == Boolean.parseBoolean(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Arrays.asList("false", "true");
            }
        });
        //自定义名
        ISearchProperty.addSearchProperty("nickname", new ISearchProperty() {
            @Override
            public String getName() {
                return "nickname";
            }

            @Override
            public boolean hasProperty(Object poke, String arg) {
                return ((Pokemon) poke).getNickname().equalsIgnoreCase(arg);
            }

            @Override
            public List<String> onTabComplete(Player player, String value) {
                return Collections.emptyList();
            }
        });
    }
}
