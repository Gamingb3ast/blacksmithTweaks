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
    //TODO: Rework effects, start by looking at this file, and then move on to making the NBT data into a 16-bit attached to a byte for the value. Where each bit represents a different buff (0, 1 for active or not) and the byte represents the value (-100 to 100 inclusive is a percent and ±101, ±102, ±103...+n represent ±110, ±120...±130...+10n)
    //TODO: This will cut down the nbt size from 17 bytes per buff to 16 bytes TOTAL. 153 bytes for legendary down to 16 bytes. 
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
        return name;
    }
    public String getRarity() {
        return rarity.getRarityColor();
    }
    public String getCodeName() {
        return codeName;
    }

    public String getColor() {
        return rarity.getRarityColor();
    }

}
