package com.gamingb3ast.blacksmithTweaks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;

public class BT_Effect {

    private final EnumRarityColor rarity;
    private final List<DummyData> buffs = new ArrayList<>();
    private final String name;
    private final String codeName;
    public int type;

    public BT_Effect(String s, String s1, EnumRarityColor color, DummyData... effect) {
        codeName = s;
        name = s1;
        rarity = color;
        type = 0;
        buffs.addAll(Arrays.asList(effect));

    }

    public BT_Effect registerEffect() {
        BT_EffectsLib.effects.put(codeName, this);
        BT_EffectsLib.effects_list.add(this);
        if (this.type == 0) BT_EffectsLib.tools_effects_list.add(this);
        else BT_EffectsLib.armor_effects_list.add(this);
        return this;
    }

    public BT_Effect setArmorType() {
        type = 1;
        return this;
    }

    public List<DummyData> getEffects() {
        return buffs;
    }

    public String getName() {
        return rarity.getRarityColor() + name;
    }

    public String getRealName() {
        return name;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getColor() {
        return rarity.getRarityColor();
    }

}
