package com.gamingb3ast.blacksmithTweaks.api;


import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;

public class BT_Effect {

    private final EnumRarityColor rarity;
    public byte[] buffValues = new byte[BT_Buff.values().length];
    private final String name;
    private final String codeName;
    private final int weight;

    public BT_Effect(String s, String s1, EnumRarityColor color, DummyData... buffs) {
        this(s, s1, color, 1, buffs);
    }

    public BT_Effect(String s, String s1, EnumRarityColor color, int weight, DummyData... buffs) {
        codeName = s;
        name = s1;
        rarity = color;
        this.weight = weight;
        for (DummyData data : buffs) {
            String name = data.fieldName;

            byte value;
            if(Float.parseFloat(data.fieldValue) > 1)
                value = (byte) (100 + ((Float.parseFloat(data.fieldValue)-1)*500)); //normal from values -1 to 1. After that every 0.01 is 5 times more. So the maximum range is -2.40 to 2.35. (Multiplied by 100 to drop the decimal for storage)
            else if(Float.parseFloat(data.fieldValue) < -1)
                value = (byte) (-100 + ((Float.parseFloat(data.fieldValue)+1)*500));
            else
                value = (byte) (Float.parseFloat(data.fieldValue)*100);
            BT_EffectAPI.setBuffValue(buffValues, BT_Buff.fromName(name), value);
        }

    }
    public BT_Effect registerEffect() {
        BT_EffectAPI.effects_list.add(this);
        BT_EffectAPI.totalWeight += weight;
        return this;
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

    public int getWeight() {
        return weight;
    }
}
