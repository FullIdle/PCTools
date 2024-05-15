package me.figsq.pctools.pctools.api.enums;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

public enum SpecialType{
    LEGEND,UBEAST,NORMAL,EGG;

    /**
     * 获取宝可梦的类型
     * @return
     */
    public static SpecialType getType(Pokemon pokemon) {
        return pokemon.isLegendary()?
                LEGEND:pokemon.isEgg()?
                EGG:pokemon.getSpecies().isUltraBeast()?
                UBEAST:NORMAL;
    }
}
