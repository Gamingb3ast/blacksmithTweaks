package com.gamingb3ast.blacksmithTweaks;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MiscUtils;

public class BT_Utils {

    public static void addRandomEffects(ItemStack stk) {
        if (isItemBuffable(stk)) {
            MiscUtils.createNBTTag(stk);
            NBTTagCompound itemTag = stk.getTagCompound();
            NBTTagCompound effectTag = new NBTTagCompound();
            NBTTagCompound displayTag = (itemTag.hasKey("BT_Display") ? itemTag.getCompoundTag("BT_Display") : new NBTTagCompound());

            //Remove old effects
            if (itemTag.hasKey("BT_TagList")) {
                itemTag.removeTag("BT_TagList");
            }
            //Add new effects
            BT_Effect effect = BT_EffectsLib.getRandomEffect();
            List<DummyData> l = effect.getEffects();
            for (DummyData d : l) {
                DataStorage.addDataToString(d);
            }
            String data = DataStorage.getDataString();
            effectTag.setString("BT_Buffs", data);

            //Naming and localization
            //Get prior display info
            if (itemTag.hasKey("BT_Display")) {
                displayTag = itemTag.getCompoundTag("BT_Display");
            }
            if(!stk.getDisplayName().equals(StatCollector.translateToLocal(stk.getUnlocalizedName() + ".name"))) {
                displayTag.setString("BT_AnvilName", stk.getDisplayName());
            }
            displayTag.setString("BT_CodeName", effect.getCodeName()); //Used for localization
            displayTag.setString("BT_EffectName", effect.getRarity() + effect.getName()); //Used for localization, if no localization then just the name of the effect.
            itemTag.setTag("BT_Display", displayTag);
            itemTag.setTag("BT_TagList", effectTag);
            stk.setTagCompound(itemTag);
        }
    }

    public static String getDisplayName(ItemStack stack) {
        String effectName = getNonFormattedEffectName(stack);
        String anvilName = getAnvilName(stack); //Anvil name will ONLY exist if renamed in anvil, uhhhhhhh, idk how to do that for reforged anvil not overwritting but I'll FIGURE IT OUT (Prolly compare to unlocalized name, etc.)
        String originalName = StatCollector.translateToLocal(stack.getUnlocalizedName() + ".name"); //IF ANVIL NAME NULL (Not anviled) Then translate, otherwise use anvilName
        if (!anvilName.equals(originalName) && !anvilName.isEmpty())
            originalName = "§o" + anvilName;
        if(effectName.contains("LANG"))
            effectName = StatCollector.translateToLocal("custom.effect." + getCodeName(stack) + ".name");
        return getColorFormatting(stack)
                        + effectName + " "
                        + originalName;
    }
    public static String getNonFormattedEffectName(ItemStack stack) {
        return stack.getTagCompound()
            .getCompoundTag("BT_Display")
            .getString("BT_EffectName");
    }
    public static String getColorFormatting(ItemStack stack) {
        return stack.getTagCompound()
                .getCompoundTag("BT_Display")
                .getString("BT_EffectName").substring(0, 2);
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
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("BT_TagList")) {
            NBTTagCompound tag = (NBTTagCompound) stack.getTagCompound()
                .getTag("BT_TagList");
            return tag.hasKey("BT_Buffs");
        }
        return false;
    }

    public static void buffItemsInContainer(Container cont, EntityPlayer player) {
        if (cont == null) return;
        for (int i = 0; i < cont.inventorySlots.size(); i++) {
            ItemStack stk = cont.getSlot(i)
                .getStack();
            if (stk != null && stk.getItem() != null) {
                if (isItemBuffable(stk) && !itemHasEffect(stk)) {
                    addRandomEffects(stk);
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

    public static String translateBuffName(String name) {
        return StatCollector.translateToLocal("buff.bt." + name + ".name");
    }

}
