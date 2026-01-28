package com.gamingb3ast.blacksmithTweaks.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import DummyCore.Utils.Notifier;
import com.gamingb3ast.blacksmithTweaks.BT_Buff;
import com.gamingb3ast.blacksmithTweaks.BT_Effect;

public class BT_EffectAPI {

    public static Random rand = new Random(4255467434637L);

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
    public static void setBuffValue(byte[] valueArray, BT_Buff buff, byte value) {
        valueArray[buff.ordinal()]=value;
    }
    public static byte getBuffValue(byte[] valueArray, BT_Buff buff) {
        return valueArray[buff.ordinal()];
    }
    public static boolean isBuffActive(byte[] valueArray, BT_Buff buff) {
        return valueArray[buff.ordinal()] != 0;
    }

}
