package com.gamingb3ast.blacksmithTweaks.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;
import com.udojava.evalex.Expression;


import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import DummyCore.Utils.Notifier;

public class BT_EffectAPI {

    public static Random rand = new Random(4255467434637L);
    public static Expression expression;

    public static int totalWeight = 0;
    public static List<BT_Effect> effects_list = new ArrayList<>();

    public static BT_Effect getRandomEffect() {
        return effects_list.get(rand.nextInt(effects_list.size()));
    }

    public static BT_Effect getWeightedRandomEffect() {
        int r = rand.nextInt(totalWeight);
        for (BT_Effect effect : effects_list) {
            if (r < effect.getWeight()) return effect;
            r -= effect.getWeight();
        }
        try {
            return getWeightedRandomEffect(); // Shouldn't stack overflow unless you're REALLY unlucky. It's one in a
                                              // quadrillion. But still, I'll account for it.
        } catch (StackOverflowError e) {
            Notifier.notifyErrorCustomMod(
                "Blacksmith Tweaks",
                "Failed to get weighted random after multiple attempts (How did you manage this??) Using nonWeightedRandom");
            return getRandomEffect();
        }
    }

    public static byte[] setBuffValue(byte[] valueArray, BT_Buff buff, byte value) {
        valueArray[buff.ordinal()] = value;
        return valueArray;
    }
    public static void updateFormula() {
        expression = new Expression(BT_CoreConfig.reforgeCostFormula);
    }

    public static int getReforgeCost(ItemStack stk) {
        if (!BT_ItemAPI.itemHasEffect(stk)) return BT_CoreConfig.firstForgeCost;
        if (BT_ItemAPI.getEffect(stk) == null) {
            return expression.with("effect_count", BigDecimal.valueOf(effects_list.size()))
                    .and("weight_total", BigDecimal.valueOf(totalWeight))
                    .and("weight", BigDecimal.valueOf((double) totalWeight / (double) effects_list.size())) //Average weight instead.
                    .and("buff_count", BigDecimal.valueOf(BT_ItemAPI.getNumItemBuffs(stk)))
                    .setRoundingMode(RoundingMode.HALF_UP).eval(true).intValue();
        }
        else {
            return expression.with("effect_count", BigDecimal.valueOf(effects_list.size()))
                    .and("weight_total", BigDecimal.valueOf(totalWeight))
                    .and("weight", BigDecimal.valueOf(BT_ItemAPI.getEffect(stk).getWeight()))
                    .and("buff_count", BigDecimal.valueOf(BT_ItemAPI.getNumItemBuffs(stk)))
                    .setRoundingMode(RoundingMode.HALF_UP).eval(true).intValue();
        }
    }
    public static byte getBuffValue(byte[] valueArray, BT_Buff buff) {
        return valueArray[buff.ordinal()];
    }

    public static boolean isBuffActive(byte[] valueArray, BT_Buff buff) {
        return valueArray[buff.ordinal()] != 0;
    }
    public static int numBuffsActive(byte[] valueArray) {
        int count = 0;
        for(byte buff : valueArray) {
            if (buff != 0)
                count++;
        }
        return count;
    }

    public static String translateBuffName(String name) {
        return StatCollector.translateToLocal("buff.bt." + name + ".name");
    }
}
