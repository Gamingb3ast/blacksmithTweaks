package com.gamingb3ast.blacksmithTweaks;

import static com.gamingb3ast.blacksmithTweaks.api.BT_Buff.*;
import static com.gamingb3ast.blacksmithTweaks.api.BT_ItemAPI.*;
import static com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig.buffApplicationMethod;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import com.gamingb3ast.blacksmithTweaks.api.BT_Buff;
import com.gamingb3ast.blacksmithTweaks.api.BT_EffectAPI;
import com.gamingb3ast.blacksmithTweaks.api.BT_ItemAPI;
import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;
import com.gamingb3ast.blacksmithTweaks.network.BT_MessageAnvilRename;
import com.gamingb3ast.blacksmithTweaks.network.BT_MessageShift;
import com.gamingb3ast.blacksmithTweaks.network.BT_ShiftHandler;

import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.MiscUtils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import xonin.backhand.api.core.BackhandUtils;

public class BT_Handler {

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {

        EntityPlayer player = event.player;
        ItemStack item = event.crafting;
        if (player == null || player.worldObj == null || item == null) return;
        ChatComponentText message1 = new ChatComponentText(
            "#---You worked hard to craft a flawless tool with no additional stats, you are now exhausted!---#");
        message1.getChatStyle()
            .setColor(EnumChatFormatting.YELLOW);
        ChatComponentText message2 = new ChatComponentText(
            "#---TIP: Get 30 experience levels to prevent exhaustion when crafting tools!---#");
        message2.getChatStyle()
            .setColor(EnumChatFormatting.GREEN);
        ChatComponentText message3 = new ChatComponentText(
            "#---TIP: When crafting tools, don't hold shift unless you want no buffs!---#");
        message3.getChatStyle()
            .setColor(EnumChatFormatting.GREEN);
        ChatComponentText message4 = new ChatComponentText(
            "#---If you continue to craft flawless tools, you will die from exhaustion!---#");
        message4.getChatStyle()
            .setColor(EnumChatFormatting.RED)
            .setBold(true);

        if (player.worldObj.isRemote) {
            boolean isShiftDown = GuiScreen.isShiftKeyDown();
            if (BT_ShiftHandler.isPlayerShiftDown(event.player.getUniqueID()) != isShiftDown) {
                BT_Mod.network.sendToServer(new BT_MessageShift(isShiftDown));
                BT_ShiftHandler.setPlayerShiftState(event.player.getUniqueID(), isShiftDown); // Update locally
            }
        } else {
            boolean isShiftDown = BT_ShiftHandler.isPlayerShiftDown(player.getUniqueID());
            if (buffApplicationMethod == 1) {
                if (isShiftDown) {
                    if (player.experienceLevel < 30) {
                        if (player.getActivePotionEffect(Potion.digSlowdown) == null) {
                            player.addChatMessage(message1);
                            player.addChatMessage(message2);
                            player.addChatMessage(message3);

                        }
                        if (player.getActivePotionEffect(Potion.blindness) != null) {
                            // Harming
                            player.addPotionEffect(new PotionEffect(7, 1, 0));
                            // Nausea
                            player.addPotionEffect(new PotionEffect(9, 500, 1));
                        }
                        if (player.getActivePotionEffect(Potion.digSlowdown) != null) {
                            // Hunger
                            player.addPotionEffect(new PotionEffect(17, 500, 1));
                            // Weakness
                            player.addPotionEffect(new PotionEffect(18, 1000, 1));
                            // Blindness
                            player.addPotionEffect(new PotionEffect(15, 1000, 0));

                            player.addChatMessage(message4);
                        }
                        // Slowness
                        player.addPotionEffect(new PotionEffect(2, 1000, 2));
                        // Negative Jump Boost
                        player.addPotionEffect(new PotionEffect(8, 1000, -3));
                        // Miner's fatigue
                        player.addPotionEffect(new PotionEffect(4, 1000, 2));

                        player.experienceLevel = Math.max(player.experienceLevel - 1, 0);
                        player.worldObj.spawnEntityInWorld(
                            new EntityXPOrb(player.worldObj, player.posX, player.posY, player.posZ, 1));

                    }

                }
                BT_ItemAPI.addRandomEffect(item);
            } else if (buffApplicationMethod == 2 && isItemBuffable(item)) {
                Container cont = player.openContainer;
                if (isShiftDown) BT_ItemAPI.buffItemsInContainer(cont, player);
                else addRandomEffect(item);
            }

        }
    }

    @SubscribeEvent
    public void onOpenedContainer(PlayerOpenContainerEvent event) {
        EntityPlayer player = event.entityPlayer;
        Container cont = player.openContainer;
        if (buffApplicationMethod == 3) {
            BT_ItemAPI.buffItemsInContainer(cont, player);

        } else if (buffApplicationMethod == 4) {
            ItemStack stack = null;
            if (itemToBuffIndex != -1) stack = cont.getSlot(itemToBuffIndex)
                .getStack();
            if (!itemHasEffect(stack) && isItemBuffable(stack)) {
                addRandomEffect(stack);
            }
        }
    }

    private int itemToBuffIndex;

    @SubscribeEvent
    public void event_ItemTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.itemStack;
        Container container = event.entityPlayer.openContainer;

        if (buffApplicationMethod == 4) {
            List<ItemStack> inventory = container.getInventory();
            if (!itemHasEffect(stack) && isItemBuffable(stack)) {
                itemToBuffIndex = inventory.indexOf(stack);
            }
        }
        if (stack != null) {
            BT_ItemAPI.checkAndUpdateDeprecatedItem(stack);

            if (BT_ItemAPI.itemHasEffect(stack)) {

                NBTTagCompound itemTag = stack.getTagCompound();
                NBTTagCompound effectsTag = (NBTTagCompound) stack.getTagCompound()
                    .getTag("BT_BuffList");
                NBTTagCompound displayTag = itemTag.getCompoundTag("BT_Display");

                // Renaming item
                if (container instanceof ContainerRepair && stack.equals(
                    container.getSlot(2)
                        .getStack())) {
                    displayTag.setString("BT_AnvilName", stack.getDisplayName());
                    itemTag.setTag("BT_Display", displayTag);
                    BT_Mod.network.sendToServer(new BT_MessageAnvilRename(2, itemTag));
                }
                else if (!(container instanceof ContainerRepair && stack.equals(
                        container.getSlot(0)
                                .getStack()))){
                    stack.setStackDisplayName(BT_ItemAPI.getDisplayName(stack));
                }
                // Display tooltips
                byte[] bytes = effectsTag.getByteArray("BT_Values");
                for (int i = 0; i < bytes.length; i++) {
                    if (BT_EffectAPI.isBuffActive(bytes, BT_Buff.values()[i])) {
                        String name = BT_Buff.values()[i].getName();
                        String mainName = BT_EffectAPI.translateBuffName(name);
                        if (bytes[i] > 0)
                            event.toolTip.add(EnumRarityColor.GOOD.getRarityColor() + "+" + bytes[i] + "% " + mainName);
                        else event.toolTip.add(EnumRarityColor.ULTIMATE.getRarityColor() + bytes[i] + "% " + mainName);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void event_AttackEntityEvent(AttackEntityEvent event) {
        EntityPlayer p = event.entityPlayer;
        World w = p.worldObj;
        if (BT_ItemAPI.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote) {
            ItemStack stack = p.getCurrentEquippedItem();
            byte[] bytes = stack.getTagCompound()
                .getCompoundTag("BT_BuffList")
                .getByteArray("BT_Values");

            if (BT_EffectAPI.isBuffActive(bytes, DURABILITY)) {

                float value = BT_EffectAPI.getBuffValue(bytes, DURABILITY) / 100F;
                if (value > 0 && w.rand.nextFloat() < value && stack.getItemDamage() > 0) {
                    stack.setItemDamage(stack.getItemDamage() - 1);
                }
                if (value < 0 && w.rand.nextFloat() < -value) {
                    stack.setItemDamage(stack.getItemDamage() + 1);
                }

            }
        }
    }

    @SubscribeEvent
    public void event_HarvestCheck(BreakEvent event) {
        EntityPlayer p = event.getPlayer();
        World w = p.worldObj;
        if (BT_ItemAPI.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote) {
            ItemStack stack = p.getCurrentEquippedItem();
            byte[] bytes = stack.getTagCompound()
                .getCompoundTag("BT_BuffList")
                .getByteArray("BT_Values");

            if (BT_EffectAPI.isBuffActive(bytes, DURABILITY)) {
                float value = BT_EffectAPI.getBuffValue(bytes, DURABILITY) / 100F;

                if (value > 0 && w.rand.nextFloat() < value && stack.getItemDamage() > 0) {
                    stack.setItemDamage(stack.getItemDamage() - 1);
                }
                if (value < 0 && w.rand.nextFloat() < -value) {
                    stack.setItemDamage(stack.getItemDamage() + 1);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void event_LivingHurtEvent(LivingHurtEvent event) {
        if (event.source instanceof EntityDamageSource) {
            EntityDamageSource edms = (EntityDamageSource) event.source;
            if (edms.damageType.contains("player") && edms.getSourceOfDamage() instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) (edms.getSourceOfDamage());
                ItemStack stack = p.getCurrentEquippedItem();

                if (BT_ItemAPI.itemHasEffect(stack)) {
                    byte[] bytes = stack.getTagCompound()
                        .getCompoundTag("BT_BuffList")
                        .getByteArray("BT_Values");

                    if (BT_EffectAPI.isBuffActive(bytes, DAMAGE)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, DAMAGE) / 100F;

                        if (value < 0) {
                            value = -value;
                            float mainDam = event.ammount * value;
                            event.ammount -= mainDam;
                        } else {
                            float mainDam = event.ammount * value;
                            event.ammount += mainDam;
                        }
                    }
                    if (BT_EffectAPI.isBuffActive(bytes, LIFESTEAL)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, LIFESTEAL) / 100F;

                        if (p.worldObj.rand.nextFloat() <= value) {
                            int heartAmount = p.worldObj.rand.nextInt(3);
                            p.heal(heartAmount);
                            event.ammount += heartAmount;

                        }
                    }
                    if (BT_EffectAPI.isBuffActive(bytes, CRIT)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, CRIT) / 100F;

                        if (p.worldObj.rand.nextFloat() <= value) {
                            event.ammount *= 2.5F;
                        }

                    }
                    if (BT_EffectAPI.isBuffActive(bytes, SPEED)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, SPEED) / 100F;

                        MiscUtils.damageEntityIgnoreEvent(event.entityLiving, edms, event.ammount);
                        int damageResistance = 20;
                        damageResistance -= (int) (value * 40);
                        event.entityLiving.hurtResistantTime = damageResistance;
                        event.entityLiving.hurtTime = damageResistance;
                        p.swingProgress -= value * 100;
                        p.swingProgressInt -= (int) (value * 100);
                        event.setCanceled(true);
                    }
                    if (BT_EffectAPI.isBuffActive(bytes, POISON)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, POISON) / 100F;

                        if (p.worldObj.rand.nextFloat() <= value) {
                            event.entityLiving.addPotionEffect(new PotionEffect(19, 450, 3));
                        }
                    }
                    if (BT_EffectAPI.isBuffActive(bytes, BIND)) {
                        float value = BT_EffectAPI.getBuffValue(bytes, BIND) / 100F;

                        if (p.worldObj.rand.nextFloat() <= value) {
                            event.entityLiving.addPotionEffect((new PotionEffect(2, 200, 1000)));
                        }
                    }
                }
            } else if (edms.damageType.contains("arrow") && edms.getSourceOfDamage() instanceof EntityArrow
                && ((EntityArrow) (edms.getSourceOfDamage())).shootingEntity instanceof EntityPlayer) {
                    EntityPlayer p = (EntityPlayer) ((EntityArrow) (edms.getSourceOfDamage())).shootingEntity;
                    ItemStack[] equippedItems = new ItemStack[2];
                    equippedItems[0] = p.getCurrentEquippedItem();
                    equippedItems[1] = (BT_Mod.backhandLoaded ? BackhandUtils.getOffhandItem(p) : null);

                    for (ItemStack stack : equippedItems) {
                        if (BT_ItemAPI.itemHasEffect(stack) && stack.getItem() instanceof ItemBow) {
                            byte[] bytes = stack.getTagCompound()
                                .getCompoundTag("BT_BuffList")
                                .getByteArray("BT_Values");

                            if (BT_EffectAPI.isBuffActive(bytes, DAMAGE)) {
                                float value = BT_EffectAPI.getBuffValue(bytes, DAMAGE) / 100F;
                                if (value < 0) {
                                    value = -value;
                                    float mainDam = event.ammount * value;
                                    event.ammount -= mainDam;
                                } else {
                                    float mainDam = event.ammount * value;
                                    event.ammount += mainDam;
                                }
                            }
                            // TODO: Eventually add mixin alternatives for everything and also add a mixin based
                            // speed buff for the bow draw speed
                            if (BT_EffectAPI.isBuffActive(bytes, LIFESTEAL)) {
                                float value = BT_EffectAPI.getBuffValue(bytes, LIFESTEAL) / 100F;
                                if (p.worldObj.rand.nextFloat() <= value) {
                                    int heartAmount = p.worldObj.rand.nextInt(3);
                                    p.heal(heartAmount);
                                    event.ammount += heartAmount;

                                }
                            }
                            if (BT_EffectAPI.isBuffActive(bytes, CRIT)) {
                                float value = BT_EffectAPI.getBuffValue(bytes, CRIT) / 100F;
                                if (p.worldObj.rand.nextFloat() <= value) {
                                    event.ammount *= 2.5F;
                                }
                            }
                            if (BT_EffectAPI.isBuffActive(bytes, POISON)) {
                                float value = BT_EffectAPI.getBuffValue(bytes, POISON) / 100F;
                                if (p.worldObj.rand.nextFloat() <= value) {
                                    event.entityLiving.addPotionEffect(new PotionEffect(19, 450, 3));
                                }
                            }
                        }
                    }
                }
            if (event.entityLiving instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) event.entityLiving;
                World w = p.worldObj;

                for (int aSlot = 0; aSlot < 4; aSlot++) {
                    if (BT_ItemAPI.itemHasEffect(p.getCurrentArmor(aSlot))) {
                        ItemStack stack = p.getCurrentArmor(aSlot);
                        byte[] bytes = stack.getTagCompound()
                            .getCompoundTag("BT_BuffList")
                            .getByteArray("BT_Values");

                        if (BT_EffectAPI.isBuffActive(bytes, DURABILITY)) {
                            float value = BT_EffectAPI.getBuffValue(bytes, DURABILITY) / 100F;
                            if (value > 0 && w.rand.nextFloat() < value && stack.getItemDamage() > 0) {
                                stack.setItemDamage(stack.getItemDamage() - 1);
                            }
                            if (value < 0 && w.rand.nextFloat() < -value) {
                                stack.setItemDamage(stack.getItemDamage() + 1);
                            }

                        }
                        if (BT_EffectAPI.isBuffActive(bytes, BIND)) {
                            float value = BT_EffectAPI.getBuffValue(bytes, BIND) / 100F;
                            if (p.worldObj.rand.nextFloat() <= value) {
                                if (edms.getSourceOfDamage() instanceof EntityMob)
                                    ((EntityLiving) edms.getSourceOfDamage())
                                        .addPotionEffect((new PotionEffect(2, 200, 1000)));
                            }
                        }
                    }
                }
            }
        }
    }

    private static float hasteValue = 0.0F;

    @SubscribeEvent
    public void event_BreakSpeed(BreakSpeed event) {
        EntityPlayer p = event.entityPlayer;
        if (BT_ItemAPI.itemHasEffect(p.getCurrentEquippedItem())) {
            ItemStack stack = p.getCurrentEquippedItem();

            byte[] bytes = stack.getTagCompound()
                .getCompoundTag("BT_BuffList")
                .getByteArray("BT_Values");

            if (BT_EffectAPI.isBuffActive(bytes, SPEED)) {
                float value = BT_EffectAPI.getBuffValue(bytes, SPEED) / 100F;
                hasteValue += value;
            }
        }
        float speed = event.originalSpeed;
        if (hasteValue < 0) {
            hasteValue = -hasteValue;
            float mainSpeed = speed * hasteValue;
            event.newSpeed = speed - mainSpeed;
        } else {
            float mainSpeed = speed * hasteValue;
            event.newSpeed = speed + mainSpeed;
        }
    }

    UUID speedUUID = UUID.fromString("c0a80123-4567-89ab-cdef-0123456789ab");

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        EntityPlayer player = event.entity;

        IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (attr == null) return;
        AttributeModifier mod = attr.getModifier(speedUUID);
        if (mod == null) return;
        event.newfov = 1.0F
            + (float) (mod.getAmount() / player.capabilities.getWalkSpeed()) / BT_CoreConfig.FOVEffectsStrength;
    }

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent event) {
        EntityPlayer p = event.player;
        World w = p != null ? p.worldObj : null;
        float playerSpeedModifier = 0.0F;
        float fearFactor = 0.0F;
        hasteValue = 0.0F;

        if (p == null || p.ticksExisted < 80 || w == null) return;

        ItemStack[] equippedItems = new ItemStack[6];
        equippedItems[0] = p.getCurrentArmor(0);
        equippedItems[1] = p.getCurrentArmor(1);
        equippedItems[2] = p.getCurrentArmor(2);
        equippedItems[3] = p.getCurrentArmor(3);
        equippedItems[4] = p.getCurrentEquippedItem();
        equippedItems[5] = (BT_Mod.backhandLoaded ? BackhandUtils.getOffhandItem(p) : null);
        for (ItemStack stack : equippedItems) {
            if (stack != null) {
                BT_ItemAPI.checkAndUpdateDeprecatedItem(stack);
            }
            if (BT_ItemAPI.itemHasEffect(stack)) {
                byte[] bytes = stack.getTagCompound()
                    .getCompoundTag("BT_BuffList")
                    .getByteArray("BT_Values");

                if (BT_EffectAPI.isBuffActive(bytes, SWIFT)) {
                    float value = BT_EffectAPI.getBuffValue(bytes, SWIFT) / 100F;
                    playerSpeedModifier += value;
                }
                if (BT_EffectAPI.isBuffActive(bytes, SLOW)) {
                    float value = BT_EffectAPI.getBuffValue(bytes, SLOW) / 100F;
                    playerSpeedModifier += value;
                    // TODO: Make these effects also increase/decrease jump height, but make this change less
                    // noticeable and limited,
                    // it would be bad if players couldn't even jump up one block. It would be good to prevent
                    // spring jumping
                }
                if (BT_EffectAPI.isBuffActive(bytes, SPEED)) {
                    float value = BT_EffectAPI.getBuffValue(bytes, SPEED) / 100F;
                    hasteValue += value;
                }
                if (BT_EffectAPI.isBuffActive(bytes, FEAR)) {
                    fearFactor = BT_EffectAPI.getBuffValue(bytes, FEAR) / 100F;
                }
            }
        }

        try {
            manageFleeTask(p, w, fearFactor);
        } catch (Exception e) {
            System.err.println(
                "[BlacksmithTweaks] Error with fear buff: | " + e
                    + " Why :( If this happens, please report it, this literally isn't supposed to happen anymore");
        }
        AttributeModifier speedModifier = new AttributeModifier(speedUUID, "BT_SWIFT", playerSpeedModifier, 2);
        IAttributeInstance attr = p.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (playerSpeedModifier != 0.0) {
            if (attr != null) {
                attr.removeModifier(speedModifier);
                attr.applyModifier(speedModifier);
                if (p instanceof EntityPlayerSP) ForgeHooksClient.getOffsetFOV((EntityPlayerSP) p, 1.0F);
            }
        } else attr.removeModifier(speedModifier);

        //Hotbar display name
        ItemStack stack = p.getCurrentEquippedItem();
        if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("BT_Display")) {
            stack.setStackDisplayName(BT_ItemAPI.getDisplayName(stack));
        }
        if (p.openContainer instanceof ContainerRepair && BT_ItemAPI.itemHasEffect(p.openContainer.getSlot(0).getStack())) {
            ContainerRepair anvilContainer = (ContainerRepair) p.openContainer;
            ItemStack result = anvilContainer.getSlot(0).getStack();
            String name = result.getDisplayName();
            if(name.contains(BT_ItemAPI.getFormattedEffectName(result).substring(1)))
                result.setStackDisplayName(name.substring(BT_ItemAPI.getFormattedEffectName(result).length()+1));
        }


    }

    // TODO: Make the fearFactor be a range rather than a constant. With higher values doing cooler stuff.
    private void manageFleeTask(EntityPlayer p, World w, float fearFactor) {

        if (fearFactor < 0) return;
        for (Object obj : w.loadedEntityList) {
            if (!(obj instanceof EntityCreature)) continue;

            EntityCreature mob = (EntityCreature) obj;
            if (!EntityAIFear.field_98218_a.isEntityApplicable(mob)) continue;
            EntityAIFear fearTask = null;

            for (EntityAITasks.EntityAITaskEntry entry : mob.tasks.taskEntries) {
                if (entry.action instanceof EntityAIFear) {
                    fearTask = (EntityAIFear) entry.action;
                    fearTask.setFearFactor(fearFactor);
                    break;
                }
            }
            if (fearTask == null) {
                fearTask = new EntityAIFear(mob, EntityPlayer.class, 20.0F, 1.1D, 1.4D, fearFactor);
                mob.tasks.addTask(1, fearTask);
            }

        }
    }

}
