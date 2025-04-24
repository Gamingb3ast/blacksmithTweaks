package com.gamingb3ast.blacksmithTweaks;

import java.util.List;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.MiscUtils;
import com.gamingb3ast.blacksmithTweaks.network.BT_MessageShift;
import com.gamingb3ast.blacksmithTweaks.network.BT_ShiftHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
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
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraft.entity.item.EntityXPOrb;

import static com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig.buffApplicationMethod;
import static com.gamingb3ast.blacksmithTweaks.BT_Utils.*;


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

		if(player.worldObj.isRemote)
		{
			boolean isShiftDown = GuiScreen.isShiftKeyDown();
			if (BT_ShiftHandler.isPlayerShiftDown(event.player.getUniqueID()) != isShiftDown) {
				BT_Mod.network.sendToServer(new BT_MessageShift(isShiftDown));
				BT_ShiftHandler.setPlayerShiftState(event.player.getUniqueID(), isShiftDown); // Update locally
			}
		}
		if(!player.worldObj.isRemote && item != null)
		{
			boolean isShiftDown = BT_ShiftHandler.isPlayerShiftDown(player.getUniqueID());
			if(buffApplicationMethod == 1) {
				if (isShiftDown) {
					if (player.experienceLevel < 30) {
						if (player.getActivePotionEffect(Potion.digSlowdown) == null) {
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

					}

				}
				BT_Utils.addRandomEffects(item);
			}
			else if(buffApplicationMethod == 2 && isItemBuffable(item))
			{
				Container cont = player.openContainer;
				if (isShiftDown)
					BT_Utils.buffItemsInContainer(cont, player);
				else
					addRandomEffects(item);
			}


		}
	}

	@SubscribeEvent
	public void onOpenedContainer(PlayerOpenContainerEvent event)
	{
		EntityPlayer player = event.entityPlayer;
		Container cont = player.openContainer;
		if(buffApplicationMethod == 3) {
			BT_Utils.buffItemsInContainer(cont, player);

		}
		else if(buffApplicationMethod == 4)
		{
			ItemStack stack = null;
			if(itemToBuffIndex != -1)
				 stack = cont.getSlot(itemToBuffIndex).getStack();
			if(stack != null)
				if(!itemHasEffect(stack) && isItemBuffable(stack))
				{
					addRandomEffects(stack);
				}
		}

	}

	private int itemToBuffIndex;
	@SubscribeEvent
	public void event_ItemTooltipEvent(ItemTooltipEvent event)
	{
		ItemStack stack = event.itemStack;
		if(buffApplicationMethod == 4) {
			List inventory = event.entityPlayer.openContainer.getInventory();
			if (!itemHasEffect(stack) && isItemBuffable(stack)) {
				itemToBuffIndex = inventory.indexOf(stack);
			}
		}
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
					String mainName = BT_Utils.translateEffectName(name);
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
		double critValue = 0.0;
		DamageSource dms = event.source;
		if(dms instanceof EntityDamageSource)
		{
			EntityDamageSource edms = (EntityDamageSource)dms;

			if(edms.damageType.contains("player") && edms.getSourceOfDamage() instanceof EntityPlayer)
			{
				EntityPlayer p = (EntityPlayer) edms.getSourceOfDamage();
				for(int aSlot = 0; aSlot < 4; aSlot++)
				{
					if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot))) {
						ItemStack stack = p.getCurrentArmor(aSlot);
						String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
						DummyData[] d = DataStorage.parseData(dummyDataString);
						for (int i1 = 0; i1 < d.length; ++i1)
						{
							DummyData data = d[i1];
							String name = data.fieldName;
							double value = Double.parseDouble(data.fieldValue);
							if (name.contains("crit")) {
								critValue+=value;
							}
						}
					}
				}
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
							critValue += value;

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
						if(name.contains("poison"))
						{
							if(p.worldObj.rand.nextDouble() <= value || 0 <= value)
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
				if(p.worldObj.rand.nextDouble() <= critValue)
				{
					event.ammount*=2.5F;
				}
			}
			if(event.entityLiving instanceof EntityPlayer)
			{

				EntityPlayer p = (EntityPlayer) event.entityLiving;
				World w = p.worldObj;
				for(int aSlot = 0; aSlot < 4; aSlot++)
				{
					if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot))) {
						ItemStack stack = p.getCurrentArmor(aSlot);
						String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
						DummyData[] d = DataStorage.parseData(dummyDataString);
						for (int i1 = 0; i1 < d.length; ++i1) {
							DummyData data = d[i1];
							String name = data.fieldName;
							double value = Double.parseDouble(data.fieldValue);

							if (name.contains("durability")) {
								if (value > 0 && w.rand.nextDouble() < value && stack.getItemDamage() > 0) {
									stack.setItemDamage(stack.getItemDamage() - 1);
								}
								if (value < 0 && w.rand.nextDouble() < -value) {
									stack.setItemDamage(stack.getItemDamage() + 1);
								}

							}
							if (name.contains("bind"))
							{
								if(p.worldObj.rand.nextDouble() <= value)
                                {
									if (edms.getSourceOfDamage() instanceof EntityMob)
										((EntityLiving)edms.getSourceOfDamage()).addPotionEffect((new PotionEffect(2, 200, 1000)));
                                }
							}/*
							if (name.contains("damage"))
							{
								if (value > 0 && w.rand.nextDouble() < value) {
									if (edms.getSourceOfDamage() instanceof EntityMob)
										((EntityLiving)edms.getSourceOfDamage()).addPotionEffect((new PotionEffect(7, 1, 1)));
                                }
								if (value < 0 && w.rand.nextDouble() < -value) {
									event.ammount -= event.ammount*value;
								}
							}
							if (name.contains("poison"))
							{
								if(p.worldObj.rand.nextDouble() <= value) {
									if (edms.getSourceOfDamage() instanceof EntityMob) {
										((EntityLiving) edms.getSourceOfDamage()).addPotionEffect((new PotionEffect(19, 450, 3)));
									}
								}
							}
							if (name.contains("life"))
							{
								if(p.worldObj.rand.nextDouble() <= value/3)
								{
									p.addPotionEffect(new PotionEffect(6, 1, 1));
									if (edms.getSourceOfDamage() instanceof EntityMob)
										((EntityLiving)edms.getSourceOfDamage()).addPotionEffect(new PotionEffect(7, 1, 1));

								}
							}*/

						}
					}
				}
			}
		}
	}
	private static double hasteValue = 0.0;
	
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
					hasteValue += value;
				}
			}
		}
		float speed = event.originalSpeed;
		if(hasteValue < 0)
		{
			hasteValue = -hasteValue;
			float mainSpeed = (float) (speed*hasteValue);
			event.newSpeed = speed-mainSpeed;
		}else
		{
			float mainSpeed = (float) (speed*hasteValue);
			event.newSpeed = speed+mainSpeed;
		}
	}
	EntityAIAvoidEntity avoidPlayerTask;
	private EntityMob mob = null;
	@SubscribeEvent
	public void PlayerTickEvent(PlayerEvent event)
	{
		EntityPlayer p = event.entityPlayer;
		World w = p.worldObj;
		double speedValue = 0.0;
		double slowValue = 0.0;
		hasteValue = 0.0;



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
					speedValue += value;
				else if (name.contains("slow"))
					slowValue += value;
				if (name.contains("fear")) {
					assignFleeTask(p, w, value);
				} else if (avoidPlayerTask != null && mob != null) {
					removeFleeTask(avoidPlayerTask, mob, p);
				}
			}
		}
		for (int aSlot = 0; aSlot < 4; aSlot++)
		{
			if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot)))
			{
				ItemStack stack = p.getCurrentArmor(aSlot);
				String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
				DummyData[] d = DataStorage.parseData(dummyDataString);
				for (int i1 = 0; i1 < d.length; ++i1)
				{
					DummyData data = d[i1];
					String name = data.fieldName;
					double value = Double.parseDouble(data.fieldValue);
					if (name.contains("swift"))
					{
						speedValue += value;
					}
					if (name.contains("slow"))
					{
						slowValue+= value;
					}
					if (name.contains("speed"))
					{
						hasteValue+=value;
					}
					if (name.contains("fear")) {
						assignFleeTask(p, w, value);
					} else if (avoidPlayerTask != null && mob != null) {
						removeFleeTask(avoidPlayerTask, mob, p);
					}
				}
			}
		}
		if (speedValue > 0)
		{
			p.addPotionEffect(new PotionEffect(1, 1, (int)(speedValue-1)));
		}
		else if (slowValue < 0)
		{
			p.addPotionEffect(new PotionEffect(2, 1, (int)((slowValue+1))));
		}


	}

	private void assignFleeTask(EntityPlayer p, World w, double value)
	{
		if (p.ticksExisted % 50 == 0) {
			for (Object obj : w.loadedEntityList) {
				if (obj instanceof EntityMob) {
					mob = (EntityMob) obj;
					if (mob.getDistanceToEntity(p) <= 10) {
						avoidPlayerTask = new EntityAIAvoidEntity(mob, p.getClass(), (float) 12.0D, value * 1.0D, 1.2D);
						mob.tasks.addTask(1, avoidPlayerTask);
					} else if (mob.getDistanceToEntity(p) <= 15) {
						mob.tasks.removeTask(avoidPlayerTask);
					}
				}
			}
		}
	}
	private void removeFleeTask(EntityAIAvoidEntity task, EntityMob mob, EntityPlayer p)
	{
		boolean okToRemoveTask = true;

		//Armor
		for (int aSlot = 0; aSlot < 4; aSlot++) {
			if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot))) {
				ItemStack stack = p.getCurrentArmor(aSlot);
				String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
				DummyData[] d = DataStorage.parseData(dummyDataString);
				for (int i1 = 0; i1 < d.length; ++i1) {
					DummyData data = d[i1];
					String name = data.fieldName;
					double value = Double.parseDouble(data.fieldValue);
					if (name.contains("fear")) {
						okToRemoveTask = false;
					}
				}
			}
		}

		//Hand
		if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem())) {
			ItemStack stack = p.getCurrentEquippedItem();
			String dummyDataString = stack.getTagCompound().getCompoundTag("BT_TagList").getString("BT_Buffs");
			DummyData[] d = DataStorage.parseData(dummyDataString);
			for (int i1 = 0; i1 < d.length; ++i1) {
				DummyData data = d[i1];
				String name = data.fieldName;
				if (name.contains("fear")) {
					okToRemoveTask = false;
				}
			}
		}
			mob.tasks.removeTask(avoidPlayerTask);
	}



}
