package com.gamingb3ast.blacksmithTweaks.api;

import java.util.Arrays;

import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.MiscUtils;
import DummyCore.Utils.Notifier;

public class BT_ItemAPI {

    public static void addRandomEffect(ItemStack stk) {
        if (isItemBuffable(stk)) {
            MiscUtils.createNBTTag(stk);
            NBTTagCompound itemTag = stk.getTagCompound();
            NBTTagCompound effectTag = new NBTTagCompound();
            NBTTagCompound displayTag = (itemTag.hasKey("BT_Display") ? itemTag.getCompoundTag("BT_Display")
                : new NBTTagCompound());

            // Remove old effects
            if (itemTag.hasKey("BT_BuffList")) {
                itemTag.removeTag("BT_BuffList");
            }
            // Add new effects
            BT_Effect effect = BT_EffectAPI.getWeightedRandomEffect();
            effectTag.setByteArray("BT_Values", effect.buffValues);
            // Naming and localization

            if (!stk.getDisplayName()
                .equals(StatCollector.translateToLocal(stk.getUnlocalizedName() + ".name"))
                && !itemTag.hasKey("BT_Display")) {
                displayTag.setString("BT_AnvilName", stk.getDisplayName());
            }
            displayTag.setString("BT_CodeName", effect.getCodeName()); // Used for localization
            displayTag.setString("BT_EffectName", effect.getRarity() + effect.getName()); // Used for localization, if
                                                                                          // no localization then just
                                                                                          // the name of the effect.
            itemTag.setTag("BT_Display", displayTag);
            itemTag.setTag("BT_BuffList", effectTag);
            stk.setTagCompound(itemTag);
        }
    }

    public static void addSpecificEffect(ItemStack stk, BT_Effect effect) {
        if (isItemBuffable(stk)) {
            MiscUtils.createNBTTag(stk);
            NBTTagCompound itemTag = stk.getTagCompound();
            NBTTagCompound effectTag = new NBTTagCompound();
            NBTTagCompound displayTag = (itemTag.hasKey("BT_Display") ? itemTag.getCompoundTag("BT_Display")
                : new NBTTagCompound());

            // Remove old effects
            if (itemTag.hasKey("BT_BuffList")) {
                itemTag.removeTag("BT_BuffList");
            }
            // Add new effects
            effectTag.setByteArray("BT_Values", effect.buffValues);
            // Naming and localization

            if (!stk.getDisplayName()
                .equals(StatCollector.translateToLocal(stk.getUnlocalizedName() + ".name"))
                && !itemTag.hasKey("BT_Display")) {
                displayTag.setString("BT_AnvilName", stk.getDisplayName());
            }
            displayTag.setString("BT_CodeName", effect.getCodeName()); // Used for localization
            displayTag.setString("BT_EffectName", effect.getRarity() + effect.getName()); // Used for localization, if
            // no localization then just
            // the name of the effect.
            itemTag.setTag("BT_Display", displayTag);
            itemTag.setTag("BT_BuffList", effectTag);
            stk.setTagCompound(itemTag);
        }
    }

    public static void addBuff(ItemStack stack, BT_Buff buff, byte value) {
        if (isItemBuffable(stack)) {
            NBTTagCompound itemTag = stack.getTagCompound();

            if (itemHasEffect(stack)) {
                NBTTagCompound effectTag = itemTag.getCompoundTag("BT_BuffList");
                effectTag.setByteArray(
                    "BT_Values",
                    BT_EffectAPI.setBuffValue(effectTag.getByteArray("BT_Values"), buff, value));
            } else {
                MiscUtils.createNBTTag(stack);
                NBTTagCompound effectTag = new NBTTagCompound();
                NBTTagCompound displayTag = (itemTag.hasKey("BT_Display") ? itemTag.getCompoundTag("BT_Display")
                    : new NBTTagCompound());

                effectTag.setByteArray(
                    "BT_Values",
                    BT_EffectAPI.setBuffValue(new byte[BT_Buff.values().length], buff, value));

                if (!stack.getDisplayName()
                    .equals(StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name"))) {
                    displayTag.setString("BT_AnvilName", stack.getDisplayName());
                }
                displayTag.setString("BT_CodeName", "custom"); // Used for localization
                displayTag.setString("BT_EffectName", EnumRarityColor.COMMON.getRarityColor());
                itemTag.setTag("BT_Display", displayTag);
                itemTag.setTag("BT_BuffList", effectTag);
                stack.setTagCompound(itemTag);
            }
        }
    }

    public static void checkAndUpdateDeprecatedItem(ItemStack stack) { // Used to update old worlds to the new data
                                                                       // system.
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("BT_TagList")) {
            try {
                String itemName = stack.getTagCompound()
                    .getCompoundTag("display")
                    .getString("Name");
                String effectName = itemName.substring(4)
                    .split(" ")[0];
                stack.getTagCompound()
                    .getCompoundTag("display")
                    .setString("Name", itemName.substring(itemName.indexOf(" ") + 1));
                if (BT_Effect.getEffectFromName(effectName) != null)
                    addSpecificEffect(stack, BT_Effect.getEffectFromName(effectName));
                else {
                    DummyData[] buffs = DataStorage.parseData(
                        stack.getTagCompound()
                            .getCompoundTag("BT_TagList")
                            .getString("BT_Buffs"));
                    addSpecificEffect(
                        stack,
                        new BT_Effect(
                            "BT:Effect:" + effectName,
                            effectName,
                            EnumRarityColor.getColorByHex(itemName.substring(0, 2)),
                            1,
                            buffs));
                }
            } catch (Exception e) {
                Notifier.notifyErrorCustomMod(
                    "Blacksmith Tweaks",
                    "ERROR: Failed to update deprecated item | \n" + Arrays.toString(e.getStackTrace())
                        + " "
                        + e.getMessage()
                        + "\n Item info | "
                        + stack.getTagCompound()
                            .toString()
                        + "\n Clearing item data, sorry for the inconvenience");
            }
            stack.getTagCompound()
                .removeTag("BT_TagList");
            stack.getTagCompound()
                .removeTag("BT_OriginalName");
        }

    }

    // This can get finicky if the addBuff method was used on an item without an effect.
    public static String getDisplayName(ItemStack stack) {
        try {
            String anvilName = getAnvilName(stack);
            String originalName = StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name");
            if (!anvilName.equals(originalName) && !anvilName.isEmpty()) originalName = "§o" + anvilName;

            if (!getCodeName(stack).equals("custom")) {
                String effectName = getNonFormattedEffectName(stack);
                if (effectName.contains("LANG"))
                    effectName = StatCollector.translateToLocal("custom.effect." + getCodeName(stack) + ".name");
                return getColorFormatting(stack) + effectName + " " + originalName;
            } else return getColorFormatting(stack) + " " + originalName;

        } catch (Exception e) {
            Notifier.notifyErrorCustomMod(
                "Blacksmith Tweaks",
                "ERROR: Failed to get display name | \n" + Arrays.toString(e.getStackTrace())
                    + " "
                    + e.getMessage()
                    + "\n Item info | "
                    + stack.getTagCompound()
                        .toString()
                    + " "
                    + stack.getTagCompound()
                        .getCompoundTag("BT_Display")
                        .toString()
                    + "\n THIS IS A BUG, PLEASE REPORT");
            return "NULL ERROR, CHECK LOGS AND ATTEMPT TO RENAME OR REFORGE";
        }
    }

    public static String getFormattedEffectName(ItemStack stack) {
        String effectName = getNonFormattedEffectName(stack);
        if (effectName.contains("LANG"))
            effectName = StatCollector.translateToLocal("custom.effect." + getCodeName(stack) + ".name");
        return getColorFormatting(stack) + effectName;
    }

    public static String getNonFormattedEffectName(ItemStack stack) {
        return stack.getTagCompound()
            .getCompoundTag("BT_Display")
            .getString("BT_EffectName")
            .substring(2);
    }

    public static String getColorFormatting(ItemStack stack) {
        return stack.getTagCompound()
            .getCompoundTag("BT_Display")
            .getString("BT_EffectName")
            .substring(0, 2);
    }

    public static String getCodeName(ItemStack stack) {
        return stack.getTagCompound()
            .getCompoundTag("BT_Display")
            .getString("BT_CodeName");
    }

    public static String getAnvilName(ItemStack stack) {
        return stack.getTagCompound()
            .getCompoundTag("BT_Display")
            .getString("BT_AnvilName");
    }
    public static BT_Effect getEffect(ItemStack stack) {
        return BT_Effect.getEffectFromName(getCodeName(stack));
    }

    public static boolean isItemOnBlackList(ItemStack stk) {
        for (int i = 0; i < BT_CoreConfig.blacklist.size(); i++) {
            if (ItemStack.areItemStacksEqual(BT_CoreConfig.blacklist.get(i), stk)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemOnWhiteList(ItemStack stk) {
        for (int i = 0; i < BT_CoreConfig.whitelist.size(); i++) {
            if (ItemStack.areItemStacksEqual(BT_CoreConfig.whitelist.get(i), stk)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemBuffable(ItemStack stk) {
        return ((stk != null && stk.getItem() != null
            && !(stk.getItem() instanceof ItemBlock)
            && stk.getItem()
                .isItemTool(stk))
            && (!isItemOnBlackList(stk)) || isItemOnWhiteList(stk)) || isTConstructTool(stk);
    }

    public static boolean itemHasEffect(ItemStack stack) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("BT_BuffList")) {
            NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound()
                .getTag("BT_BuffList");
            return tag.hasKey("BT_Values");
        }
        return false;
    }
    public static byte[] getItemBuffs(ItemStack stack) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("BT_BuffList")) {
            NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound()
                    .getTag("BT_BuffList");
            if (tag.hasKey("BT_Values"))
                return tag.getByteArray("BT_Values");
        }
        return null;
    }
    public static int getNumItemBuffs(ItemStack stack) {
        if (stack != null && stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("BT_BuffList")) {
            NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound()
                    .getTag("BT_BuffList");
            if (tag.hasKey("BT_Values"))
                return BT_EffectAPI.numBuffsActive(tag.getByteArray("BT_Values"));
        }
        return 0;
    }

    public static void buffItemsInContainer(Container cont) {
        if (cont == null) return;
        for (int i = 0; i < cont.inventorySlots.size(); i++) {
            ItemStack stk = cont.getSlot(i)
                .getStack();
            if (stk != null && stk.getItem() != null) {
                if (isItemBuffable(stk) && !itemHasEffect(stk)) {
                    addRandomEffect(stk);
                    cont.detectAndSendChanges();
                }
            }
        }
    }

    public static boolean isTConstructTool(ItemStack stk) {
        if (stk == null || stk.getItem() == null) return false;
        try {
            Class<?> clazz = Class.forName("tconstruct.library.tools.ToolCore");
            Class<? extends Item> toolClazz = stk.getItem()
                .getClass();
            return clazz.isAssignableFrom(toolClazz);
        } catch (Exception e) {
            return false;
        }
    }

}
