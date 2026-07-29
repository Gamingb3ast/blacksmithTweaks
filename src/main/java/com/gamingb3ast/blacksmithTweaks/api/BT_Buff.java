package com.gamingb3ast.blacksmithTweaks.api;

import java.util.Arrays;

public enum BT_Buff {

    DAMAGE("damage", 1),
    SPEED("speed", 0), //TODO: Figure out a better mechanism for neutral buffs.
    SLOW("slow", -2),
    DURABILITY("durability", 0),
    SWIFT("swift", 2),
    FEAR("fear", 3),
    BIND("bind", 3),
    CRIT("crit", 2),
    LIFESTEAL("lifesteal", 2),
    POISON("poison", 3);

    private final String name;
    private final int weight;

    BT_Buff(String name) {
        this.name = name;
        this.weight = 0;
    }

    BT_Buff(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }


    public String getName() {
        return name;
    }

    public static BT_Buff fromName(String name) {
        for (BT_Buff buff : values()) {
            if (buff.name.equals(name)) {
                return buff;
            }
        }
        throw new IllegalArgumentException("Buff: " + name + " doesn't exist. Someone did something wrong!");
    }
    public static String listBuffs() {
        return "There are " + BT_Buff.values().length + " effects - " + Arrays.toString(BT_Buff.values()) + ".";
    }

}
