package com.gamingb3ast.blacksmithTweaks.configs;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import DummyCore.Utils.Notifier;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

public class BT_CoreConfig extends Configuration {

    private static BT_CoreConfig INSTANCE = null;

    public static ArrayList<ItemStack> blacklist = new ArrayList<ItemStack>();
    public static ArrayList<ItemStack> whitelist = new ArrayList<ItemStack>();
    public static int FOVEffectsStrength;
    public static int buffApplicationMethod;
    public static int firstForgeCost;
    public static String reforgeCostFormula;
    public static String CONFIG_GENERAL = "General";

    public BT_CoreConfig(File configFile) {
        super(configFile);
        this.load();
        loadCFG();
        this.save();
    }

    public static void initialize(File configFile) {
        if (INSTANCE == null) {
            INSTANCE = new BT_CoreConfig(configFile);
        } else throw new IllegalStateException("Cannot initialize Blacksmith Tweaks config twice!");
    }

    public static BT_CoreConfig instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Instance of Blacksmith Tweaks Config requested before initialization");
        }
        return INSTANCE;
    }

    public void loadCFG() {

        ConfigCategory help = this.getCategory("Help");
        help.setComment(
            "Using this cfg file you can configure black/white lists and other mechanics of the mod \n"
                + "Use the Effects.cfg file to configure the actual buffs, help and examples can be found at the bottom of the file\n"
                + "You can create a blacklist and whitelist for items which will be given buffs, simply do this by adding the item name to the list as shown in the config.\n"
                + "Please note that the whitelist overrides the blacklist!\n");

        // FOVEffects
        FOVEffectsStrength = this.get(
            CONFIG_GENERAL,
            "FOVEffectsStrength",
            50,
            "The strength of FOV effects for swift and slow buffs, inversely proportional to FOV changes (bigger number means lesser changes)")
            .getInt();
        // BlackList
        String[] blacklistItems = this
            .get(
                CONFIG_GENERAL,
                "Blacklist",
                "minecraft:book, GalaxySpace:item.BasicItems:1, adventurebackpack:*",
                "Items to blacklist. Ignores tinkers construct tools")
            .getString()
            .split(", ");
        // Whitelist
        String[] whitelistItems = this
            .get(
                CONFIG_GENERAL,
                "Whitelist",
                "minecraft:iron_helmet",
                "Items to whitelist. Ignores tinkers construct tools")
            .getString()
            .split(", ");
        // Buff application type
        buffApplicationMethod = this.get(
            CONFIG_GENERAL,
            "Buff Application Type",
            2,
            "This is the method that will be used to apply buffs to tools and armor. The different types are the following: \n"
                + "1: Classic. Buffs are applied when crafting the item, holding shift will prevent buffs from being applied but will give you negative status effects unless you are above level 30 (Inventory refresh required to see buffs applied to an item)\n"
                + "2: Crafting Reworked. Buffs are always applied when crafting the item, shifting will do nothing \n"
                + "3: Container. An alternative system, it runs through every item in the currently opened/updated container and applies buffs to the valid items. Also skips items that were debuffed in the reforging com.gamingb3ast.blacksmithTweaks.anvil\n"
                + "4: Tooltip Container. Alternative to Container, a hybrid between container update and item hover events, will apply to any valid item even if it was in a chest. The only exception being if you de-buffed the item using the reforging com.gamingb3ast.blacksmithTweaks.anvil (Is a bit more optimized via single slot checking)\n"
                + "5: Buffs are only applied via reforging com.gamingb3ast.blacksmithTweaks.anvil (This will be more relevant in a future update)\n"
                + "Any other number will result in buff application being disabled\n"
                + "WARNING: SOME OF THESE WILL NOT WORK WITH CERTAIN MODS, THIS CONFIG OPTION EXISTS SO YOU CAN HAVE ALTERNATIVES IN CASE OF BUGS OR CRASHES")
            .getInt();
        // first forge cost
        firstForgeCost = this.get(
                CONFIG_GENERAL,
                "First Forge Cost",
                5,
                "The price of forging a non-buffed item in the forging anvil"
        ).getInt();
        // reforge cost
        reforgeCostFormula = this.get(CONFIG_GENERAL,
                "Reforge Cost Formula",
                "MAX(5, MIN(25, buff_count*2*(weight/(weight_total/effect_count))))",
                "The xp cost to reforge an item."
                        + "Uses the EvalEx expression parser\n"
                        + "See: https://github.com/uklimaschewski/EvalEx for syntax/function documentation\n\n"
                        + "Available variables:\n"
                        + "\teffect_count : The number of effects currently registered\n"
                        + "\tweight_total : The total weight of the effects currently registered\n"
                        + "\tweight : The weight of the effect currently being evaluated, if no effect is registered, is treated as the average weight\n"
                        + "\tbuff_count : The amount of buffs the item has\n"
                        + "\tweighted_buff_count : The weighted total of the buffs the item has, can be used as a more interesting substitute for buff_count. Set by [Buff Weights] NOT YET IMPLEMENTED, DO NOT USE THIS." //TODO: Implement buff weights
        ).getString();


        String applicationString = "Buff application disabled";
        switch (buffApplicationMethod) {
            case 1:
                applicationString = "Classic";
                break;
            case 2:
                applicationString = "Crafting Reworked";
                break;
            case 3:
                applicationString = "Container";
                break;
            case 4:
                applicationString = "Tooltip Container";
                break;
            case 5:
                applicationString = "Buffs are only applied via reforging com.gamingb3ast.blacksmithTweaks.anvil";
                break;
        };
        Notifier.notifyCustomMod("Blacksmith Tweaks", "Application method in use:: " + applicationString);

        String mod, item, meta = "0";
        for (String blacklistItem : blacklistItems) {
            try {
                String[] modIDAndName = blacklistItem.split(":");
                mod = modIDAndName[0];
                item = modIDAndName[1];
                if (modIDAndName.length == 3) meta = modIDAndName[2];
                if (item.equals("*")) {
                    for (Object obj : GameData.getItemRegistry()
                        .getKeys()) {
                        String itemName = obj.toString();
                        if (itemName.startsWith(mod + ":")) {
                            blacklist.add(new ItemStack(GameRegistry.findItem(mod, itemName.split(":")[1]), 0));
                            Notifier.notifyCustomMod(
                                "Blacksmith Tweaks",
                                "Blacklisted Item "
                                    + new ItemStack(GameRegistry.findItem(mod, itemName.split(":")[1]), 0)
                                        .getDisplayName());

                        }
                    }
                } else blacklist.add(new ItemStack(GameRegistry.findItem(mod, item), 1, Integer.parseInt(meta)));
                Notifier.notifyCustomMod(
                    "Blacksmith Tweaks",
                    "Blacklisted Item "
                        + new ItemStack(GameRegistry.findItem(mod, item), 1, Integer.parseInt(meta)).getDisplayName());
            } catch (Exception e) {
                Notifier.notifyCustomMod(
                    "Blacksmith Tweaks",
                    "Failed to load item: " + blacklistItem + " " + e.getLocalizedMessage());
            }

        }
        for (String whitelistItem : whitelistItems) {
            try {
                String[] modIDAndName = whitelistItem.split(":");
                mod = modIDAndName[0];
                item = modIDAndName[1];
                if (modIDAndName.length == 3) meta = modIDAndName[2];
                if (item.equals("*")) {
                    for (Object obj : GameData.getItemRegistry()
                        .getKeys()) {
                        String itemName = obj.toString();
                        if (itemName.startsWith(mod + ":")) {
                            whitelist.add(new ItemStack(GameRegistry.findItem(mod, itemName.split(":")[1]), 0));
                            Notifier.notifyCustomMod(
                                "Blacksmith Tweaks",
                                "Whitelisted Item "
                                    + new ItemStack(GameRegistry.findItem(mod, itemName.split(":")[1]), 0)
                                        .getDisplayName());
                        }
                    }
                } else whitelist.add(new ItemStack(GameRegistry.findItem(mod, item), 1, Integer.parseInt(meta)));
                Notifier.notifyCustomMod(
                    "Blacksmith Tweaks",
                    "Whitelisted Item "
                        + new ItemStack(GameRegistry.findItem(mod, item), 1, Integer.parseInt(meta)).getDisplayName());
            } catch (Exception e) {
                Notifier.notifyCustomMod(
                    "Blacksmith Tweaks",
                    "Failed to load item: " + whitelistItem + " " + e.getLocalizedMessage());
            }
        }
    }

}
