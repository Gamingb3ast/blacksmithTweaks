package com.gamingb3ast.blacksmithTweaks;


import java.util.List;
import java.util.UUID;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MiscUtils;
import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

public class BT_Utils {
	
	public static void addRandomEffects(ItemStack stk)
	{
		int type = getISType(stk);
		if(type != -1)
		{
			if(type == 1)
			{
				MiscUtils.createNBTTag(stk);
				NBTTagCompound itemTag = stk.getTagCompound();
				NBTTagCompound buffsTag = new NBTTagCompound();
				String originalName = null;
				if(itemTag.hasKey("BT_TagList"))
				{
					originalName = itemTag.getString("BT_OriginalName");
					itemTag.removeTag("BT_TagList");
				}
				BT_Effect effect = BT_EffectsLib.getRandomEffect(type);
				List<DummyData> l = effect.getEffects();
				for(int i = 0; i < l.size(); ++i)
				{
					DummyData d = l.get(i);
					DataStorage.addDataToString(d);
				}
				String data = DataStorage.getDataString();
				buffsTag.setString("BT_Buffs", data);
				buffsTag.setString("BT_UUID", UUID.randomUUID().toString());
				if(originalName == null || originalName.isEmpty())
					itemTag.setString("BT_OriginalName", stk.getDisplayName());
				NBTTagCompound display = new NBTTagCompound();
				if(itemTag.hasKey("display"))
				{
					display = itemTag.getCompoundTag("display");
				}
				if(originalName == null || originalName.isEmpty())
					display.setString("Name", effect.getColor()+effect.getName()+" "+stk.getDisplayName());
				else
					display.setString("Name", effect.getColor()+effect.getName()+" "+originalName);
				itemTag.setTag("display", display);
				itemTag.setTag("BT_TagList", buffsTag);
				stk.setTagCompound(itemTag);
							}
		}
	}


	public static int getISType(ItemStack stk)
	{
		if(isItemBuffable(stk))
		{
			return 1;
		}
		return -1;
	}
	public static boolean isItemOnBlackList(ItemStack stk)
	{
		boolean output = false;
		for(int i = 0; i < BT_CoreConfig.blacklist.length; i++)
		{
			if(BT_CoreConfig.blacklist[i] == stk.getItem()) {
				output = true;
			}
		}
		return output;

	}

	public static boolean isItemOnWhiteList(ItemStack stk)
	{
		boolean output = false;
		for(int i = 0; i < BT_CoreConfig.whitelist.length; i++)
		{
			if(BT_CoreConfig.whitelist[i] == stk.getItem()) {
				output = true;
			}
		}
		return output;

	}
	public static boolean isItemBuffable(ItemStack stk)
	{
		boolean enable = false;
		if(!enable)
			enable = isTConstructTool(stk);

		return ((stk != null && stk.getItem() != null && !(stk.getItem() instanceof ItemBlock) && stk.getItem().isItemTool(stk)) && !isItemOnBlackList(stk)) || isItemOnWhiteList(stk) || enable;

	}
	
	public static boolean itemHasEffect(ItemStack stack)
	{
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("BT_TagList"))
		{
			NBTTagCompound tag = (NBTTagCompound)stack.getTagCompound().getTag("BT_TagList");
			if(tag.hasKey("BT_Buffs"))
			{
				return true;
			}
		}
		return false;
	}


	public static void buffItemsInContainer(Container cont, EntityPlayer player)
	{
		if(cont == null) return;
        for(int i = 0; i < cont.inventorySlots.size(); i++)
        {
            ItemStack stk = cont.getSlot(i).getStack();
            if(stk!= null && stk.getItem()!= null)
            {
                if(isItemBuffable(stk) && !itemHasEffect(stk))
                {
                    addRandomEffects(stk);
                    cont.detectAndSendChanges();
                }
            }
        }
	}
	
	public static boolean isTConstructTool(ItemStack stk)
	{
		if(stk == null || stk.getItem() == null)return false;
		try
		{
			Class clazz = Class.forName("tconstruct.library.tools.ToolCore");
			Class toolClazz = stk.getItem().getClass();
			return clazz.isAssignableFrom(toolClazz);
		}catch(Exception e)
		{
			return false;
		}
	}
	public static String translateEffectName(String name)
	{
		return StatCollector.translateToLocal("effect.bt." + name + ".name");
	}


}
