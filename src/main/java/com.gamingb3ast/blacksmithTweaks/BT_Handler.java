package com.gamingb3ast.blacksmithTweaks;

import java.util.List;
import java.util.UUID;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;


public class BT_Handler{

	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event) {
		EntityPlayer player = event.player;
		ItemStack item = event.crafting;
		IInventory matrix = event.craftMatrix;
		ChatComponentText message1 = new ChatComponentText("#---You worked hard to craft a flawless tool with no additional stats, you are now exhausted!---#");
		message1.getChatStyle().setColor(EnumChatFormatting.YELLOW);
		ChatComponentText message2 = new ChatComponentText("#---TIP: Get 30 experience levels to prevent exhaustion when crafting tools!---#");
		message2.getChatStyle().setColor(EnumChatFormatting.GREEN);
		ChatComponentText message3 = new ChatComponentText("#---TIP: When crafting tools, don't hold shift unless you want no buffs!---#");
		message3.getChatStyle().setColor(EnumChatFormatting.GREEN);
		ChatComponentText message4 = new ChatComponentText("#---If you continue to craft flawless tools, you will die from exhaustion!---#");
		message4.getChatStyle().setColor(EnumChatFormatting.RED).setBold(true);


		if(!player.worldObj.isRemote && item != null)
		{
			if(GuiScreen.isShiftKeyDown()) {
				if (player.experienceLevel < 30) {
					if(player.getActivePotionEffect(Potion.digSlowdown) == null) {
						player.addChatMessage(message1);
						player.addChatMessage(message2);
						player.addChatMessage(message3);

					}
					if (player.getActivePotionEffect(Potion.blindness) != null) {
						//Harming
						player.addPotionEffect(new PotionEffect(7, 1, 0));
						//Nausea
						player.addPotionEffect(new PotionEffect(9, 500, 1));
					}
					if (player.getActivePotionEffect(Potion.digSlowdown) != null) {
						//Hunger
						player.addPotionEffect(new PotionEffect(17, 500, 1));
						//Weakness
						player.addPotionEffect(new PotionEffect(18, 1000, 1));
						//Blindness
						player.addPotionEffect(new PotionEffect(15, 1000, 0));


						player.addChatMessage(message4);
					}
					//Slowness
					player.addPotionEffect(new PotionEffect(2, 1000, 2));
					//Negative Jump Boost
					player.addPotionEffect(new PotionEffect(8, 1000, -3));
					//Miner's fatigue
					player.addPotionEffect(new PotionEffect(4, 1000, 2));


					player.experienceLevel = Math.max(player.experienceLevel - 1, 0);
					player.worldObj.spawnEntityInWorld(new EntityXPOrb(player.worldObj, player.posX, player.posY, player.posZ, 1));
					Minecraft.getMinecraft().displayGuiScreen(null);




				}

			}
			BT_Utils.addRandomEffects(item);
		}
	}
	
	@SubscribeEvent
	public void event_ItemTooltipEvent(ItemTooltipEvent event)
	{
		ItemStack stack = event.itemStack;
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("BT_TagList"))
		{
			NBTTagCompound tag = (NBTTagCompound)stack.getTagCompound().getTag("BT_TagList");
			if(tag.hasKey("BT_Buffs"))
			{
				String s = tag.getString("BT_Buffs");
				DummyData[] d = DataStorage.parseData(s);
				for(int i = 0; i < d.length; ++i)
				{
					DummyData data = d[i];
					String name = data.fieldName;
					String nameLetter1 = String.valueOf(name.charAt(0)).toUpperCase();
					String mainName = nameLetter1+name.substring(1, name.length());
					double da = Double.parseDouble(data.fieldValue);
					da *= 100;
					if(da > 0)
					event.toolTip.add(EnumRarityColor.GOOD.getRarityColor()+"+"+(int)da+"% "+mainName);
					else
					event.toolTip.add(EnumRarityColor.ULTIMATE.getRarityColor()+(int)da+"% "+mainName);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void event_AttackEntityEvent(AttackEntityEvent event)
	{
		EntityPlayer p = event.entityPlayer;
		World w = p.worldObj;
		if(p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote)
		{
			ItemStack stack = p.getCurrentEquippedItem();
			String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
			DummyData[] d = DataStorage.parseData(dummyDataString);
			for(int i1 = 0; i1 < d.length; ++i1)
			{
				DummyData data = d[i1];
				String name = data.fieldName;
				double value = Double.parseDouble(data.fieldValue);
				if(name.contains("durability"))
				{
					if(value > 0 && w.rand.nextDouble() < value && stack.getItemDamage() > 0)
					{
						stack.setItemDamage(stack.getItemDamage()-1);
					}
					if(value < 0 && w.rand.nextDouble() < -value)
					{
						stack.setItemDamage(stack.getItemDamage()+1);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void event_HarvestCheck(BreakEvent event)
	{
		EntityPlayer p = event.getPlayer();
		World w = p.worldObj;
		if(p != null && p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote)
		{
			ItemStack stack = p.getCurrentEquippedItem();
			String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
			DummyData[] d = DataStorage.parseData(dummyDataString);
			for(int i1 = 0; i1 < d.length; ++i1)
			{
				DummyData data = d[i1];
				String name = data.fieldName;
				double value = Double.parseDouble(data.fieldValue);
				if(name.contains("durability"))
				{
					if(value > 0 && w.rand.nextDouble() < value && stack.getItemDamage() > 0)
					{
						stack.setItemDamage(stack.getItemDamage()-1);
					}
					if(value < 0 && w.rand.nextDouble() < -value)
					{
						stack.setItemDamage(stack.getItemDamage()+1);
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOW)
	public void event_LivingHurtEvent(LivingHurtEvent event)
	{
		DamageSource dms = event.source;
		if(dms instanceof EntityDamageSource)
		{
			EntityDamageSource edms = (EntityDamageSource)dms;
			if(edms.damageType.contains("player") && edms.getSourceOfDamage() instanceof EntityPlayer)
			{
				EntityPlayer p = (EntityPlayer) edms.getSourceOfDamage();
				if(p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()))
				{
					ItemStack stack = p.getCurrentEquippedItem();
					String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
					DummyData[] d = DataStorage.parseData(dummyDataString);
					for(int i1 = 0; i1 < d.length; ++i1)
					{
						DummyData data = d[i1];
						String name = data.fieldName;
						double value = Double.parseDouble(data.fieldValue);
						if(name.contains("damage"))
						{
							float dam = event.ammount;
							if(value < 0)
							{
								value = -value;
								float mainDam = dam*=value;
								event.ammount -= mainDam;
							}else
							{
								float mainDam = dam*=value;
								event.ammount += mainDam;
							}
						}
						if(name.contains("life"))
						{
							if(p.worldObj.rand.nextDouble() <= value)
							{
								p.addPotionEffect(new PotionEffect(6, 1, 1));
								event.entityLiving.addPotionEffect(new PotionEffect(7, 1, 1));

							}
						}
						if(name.contains("crit"))
						{
							if(p.worldObj.rand.nextDouble() <= value)
							{
								event.ammount*=3;
							}
						}
						if(name.contains("speed"))
						{
							MiscUtils.damageEntityIgnoreEvent(event.entityLiving, edms, event.ammount);
							int damageResistance = 20;
							damageResistance -= value*40;
							event.entityLiving.hurtResistantTime = damageResistance;
							event.entityLiving.hurtTime = damageResistance;
							p.swingProgress -= value*100;
							p.swingProgressInt -= value*100;
							event.setCanceled(true);
						}
						if(name.contains("poisonous"))
						{
							if(p.worldObj.rand.nextDouble() <= value)
							{
								event.entityLiving.addPotionEffect(new PotionEffect(19, 450, 3));
							}
						}
						if(name.contains("bind"))
						{
							if(p.worldObj.rand.nextDouble() <= value)
							{
								event.entityLiving.addPotionEffect((new PotionEffect(2, 200, 1000)));
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void event_BreakSpeed(BreakSpeed event)
	{
		EntityPlayer p = event.entityPlayer;
		World w = p.worldObj;
		Block b = event.block;
		if(p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()))
		{
			ItemStack stack = p.getCurrentEquippedItem();
			String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
			DummyData[] d = DataStorage.parseData(dummyDataString);
			for(int i1 = 0; i1 < d.length; ++i1)
			{
				DummyData data = d[i1];
				String name = data.fieldName;
				double value = Double.parseDouble(data.fieldValue);
				if(name.contains("speed"))
				{
					float speed = event.originalSpeed;
					if(value < 0)
					{
						value = -value;
						float mainSpeed = (float) (speed*value);
						event.newSpeed = speed-mainSpeed;
					}else
					{
						float mainSpeed = (float) (speed*value);
						event.newSpeed = speed+mainSpeed;
					}
				}
			}
		}
	}
	EntityAIAvoidEntity avoidPlayerTask;

	@SubscribeEvent
	public void onItemHeld(PlayerEvent event) {
		EntityPlayer p = event.entityPlayer;
		World w = p.worldObj;
		EntityMob mob = null;


		if (p.ticksExisted < 80) return;

		if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem())) {
			ItemStack stack = p.getCurrentEquippedItem();
			String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
			DummyData[] d = DataStorage.parseData(dummyDataString);
			for (int i1 = 0; i1 < d.length; ++i1) {
				DummyData data = d[i1];
				String name = data.fieldName;
				double value = Double.parseDouble(data.fieldValue);
				if (name.contains("swift"))
					p.addPotionEffect(new PotionEffect(1, 1, (int) value - 1));
				else if (name.contains("slow"))
					p.addPotionEffect(new PotionEffect(2, 1, (int) (-1 * (value + 1))));
				if (name.contains("fear")) {
					if(p.ticksExisted % 50 == 0) {
						for (Object obj : w.loadedEntityList) {
							if (obj instanceof EntityMob) {
								mob = (EntityMob) obj;
								if(mob.getDistanceToEntity(p) <= 10) {
									avoidPlayerTask = new EntityAIAvoidEntity(mob, p.getClass(), (float) 12.0D, value * 1.0D, 1.2D);
									mob.tasks.addTask(1, avoidPlayerTask);
								}
								else if(mob.getDistanceToEntity(p) <= 15)
								{
									mob.tasks.removeTask(avoidPlayerTask);
								}
							}
						}
					}

				}
				else if(avoidPlayerTask != null && mob != null) {
					mob.tasks.removeTask(avoidPlayerTask);
                    avoidPlayerTask = null;
				}
			}
		}
	}

}
