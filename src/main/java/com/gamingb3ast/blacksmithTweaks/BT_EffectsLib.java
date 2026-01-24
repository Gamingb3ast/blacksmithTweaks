package com.gamingb3ast.blacksmithTweaks;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import DummyCore.Utils.Notifier;

public class BT_EffectsLib {

    public static Random rand = new Random(4255467434637L);

    public static Hashtable<String, BT_Effect> effects = new Hashtable<>();
    public static int totalWeight = 0;
    public static List<BT_Effect> effects_list = new ArrayList<>();

    public static List<BT_Effect> tools_effects_list = new ArrayList<>();

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

}
