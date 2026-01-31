package com.gamingb3ast.blacksmithTweaks.api;

import java.util.Arrays;

public enum BT_Buff {

    DAMAGE("damage"),
    SPEED("speed"),
    SLOW("slow"),
    DURABILITY("durability"),
    SWIFT("swift"),
    FEAR("fear"),
    BIND("bind"),
    CRIT("crit"),
    LIFESTEAL("lifesteal"),
    POISON("poison");

    private final String name;

    BT_Buff(String name) {
        this.name = name;
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
