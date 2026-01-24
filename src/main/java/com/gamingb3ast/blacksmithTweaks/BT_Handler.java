package com.gamingb3ast.blacksmithTweaks;

import static com.gamingb3ast.blacksmithTweaks.BT_Utils.*;
import static com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig.buffApplicationMethod;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

import com.gamingb3ast.blacksmithTweaks.network.BT_MessageAnvilRename;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import com.gamingb3ast.blacksmithTweaks.network.BT_MessageShift;
import com.gamingb3ast.blacksmithTweaks.network.BT_ShiftHandler;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.MiscUtils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import xonin.backhand.api.core.BackhandUtils;

public class BT_Handler {

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {

        EntityPlayer player = event.player;
        ItemStack item = event.crafting;
        if(player == null || player.worldObj == null || item == null) return;
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
        }
        else {
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
                BT_Utils.addRandomEffects(item);
            } else if (buffApplicationMethod == 2 && isItemBuffable(item)) {
                Container cont = player.openContainer;
                if (isShiftDown) BT_Utils.buffItemsInContainer(cont, player);
                else addRandomEffects(item);
            }

        }
    }

    @SubscribeEvent
    public void onOpenedContainer(PlayerOpenContainerEvent event) {
        EntityPlayer player = event.entityPlayer;
        Container cont = player.openContainer;
        if (buffApplicationMethod == 3) {
            BT_Utils.buffItemsInContainer(cont, player);

        } else if (buffApplicationMethod == 4) {
            ItemStack stack = null;
            if (itemToBuffIndex != -1) stack = cont.getSlot(itemToBuffIndex)
                .getStack();
            if (stack != null) if (!itemHasEffect(stack) && isItemBuffable(stack)) {
                addRandomEffects(stack);
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
        if (stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("BT_TagList")) {

            NBTTagCompound itemTag = stack.getTagCompound(); //This line causes the Anvil Data to go back to the data before the rename, why?
            NBTTagCompound effectsTag = (NBTTagCompound) stack.getTagCompound().getTag("BT_TagList");
            NBTTagCompound displayTag = itemTag.getCompoundTag("BT_Display");

            //Renaming item
            if (container instanceof ContainerRepair && stack.equals(container.getSlot(2).getStack()) && !StatCollector.translateToLocal(stack.getUnlocalizedName()+".name").equals(stack.getDisplayName())) {
                displayTag.setString("BT_AnvilName", stack.getDisplayName());
                itemTag.setTag("BT_Display", displayTag);
                BT_Mod.network.sendToServer(new BT_MessageAnvilRename(2, itemTag)); // slot 2
            }
            else {
                stack.setStackDisplayName(BT_Utils.getDisplayName(stack));
            }
            if (effectsTag.hasKey("BT_Buffs")) {
                String s = effectsTag.getString("BT_Buffs");
                DummyData[] d = DataStorage.parseData(s);
                for (DummyData data : d) {
                    String name = data.fieldName;
                    String mainName = BT_Utils.translateBuffName(name);
                    double da = Double.parseDouble(data.fieldValue);
                    da *= 100;
                    if (da > 0)
                        event.toolTip.add(EnumRarityColor.GOOD.getRarityColor() + "+" + (int) da + "% " + mainName);
                    else event.toolTip.add(EnumRarityColor.ULTIMATE.getRarityColor() + (int) da + "% " + mainName);
                }
            }
        }
    }

    @SubscribeEvent
    public void event_AttackEntityEvent(AttackEntityEvent event) {
        EntityPlayer p = event.entityPlayer;
        World w = p.worldObj;
        if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote) {
            ItemStack stack = p.getCurrentEquippedItem();
            String dummyDataString = stack.getTagCompound()
                .getCompoundTag("BT_TagList")
                .getString("BT_Buffs");
            DummyData[] d = DataStorage.parseData(dummyDataString);
            for (DummyData data : d) {
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
            }
        }
    }

    @SubscribeEvent
    public void event_HarvestCheck(BreakEvent event) {
        EntityPlayer p = event.getPlayer();
        World w = p.worldObj;
        if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem()) && !w.isRemote) {
            ItemStack stack = p.getCurrentEquippedItem();
            String dummyDataString = stack.getTagCompound()
                .getCompoundTag("BT_TagList")
                .getString("BT_Buffs");
            DummyData[] d = DataStorage.parseData(dummyDataString);
            for (DummyData data : d) {
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
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void event_LivingHurtEvent(LivingHurtEvent event) {
        double critValue = 0.0;
        DamageSource dms = event.source;
        if (dms instanceof EntityDamageSource) {
            EntityDamageSource edms = (EntityDamageSource) dms;
            if (edms.damageType.contains("player") && edms.getSourceOfDamage() instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) (edms.getSourceOfDamage());
                ItemStack stack = p.getCurrentEquippedItem();
                if (stack != null && BT_Utils.itemHasEffect(stack)) {
                    String dummyDataString = stack.getTagCompound()
                        .getCompoundTag("BT_TagList")
                        .getString("BT_Buffs");
                    DummyData[] d = DataStorage.parseData(dummyDataString);
                    for (DummyData data : d) {
                        String name = data.fieldName;
                        double value = Double.parseDouble(data.fieldValue);
                        if (name.contains("damage")) {
                            float dam = event.ammount;
                            if (value < 0) {
                                value = -value;
                                float mainDam = (float) (dam * value);
                                event.ammount -= mainDam;
                            } else {
                                float mainDam = (float) (dam * value);
                                event.ammount += mainDam;
                            }
                        }
                        if (name.contains("life")) {
                            if (p.worldObj.rand.nextDouble() <= value) {
                                int heartAmount = p.worldObj.rand.nextInt(3);
                                p.heal(heartAmount);
                                event.ammount += heartAmount;

                            }
                        }
                        if (name.contains("crit")) {
                            if (p.worldObj.rand.nextDouble() <= value) {
                                event.ammount *= 2.5F;
                            }

                        }
                        if (name.contains("speed")) {
                            MiscUtils.damageEntityIgnoreEvent(event.entityLiving, edms, event.ammount);
                            int damageResistance = 20;
                            damageResistance -= (int) (value * 40);
                            event.entityLiving.hurtResistantTime = damageResistance;
                            event.entityLiving.hurtTime = damageResistance;
                            p.swingProgress -= (float) (value * 100);
                            p.swingProgressInt -= (int) (value * 100);
                            event.setCanceled(true);
                        }
                        if (name.contains("poison")) {
                            if (p.worldObj.rand.nextDouble() <= value) {
                                event.entityLiving.addPotionEffect(new PotionEffect(19, 450, 3));
                            }
                        }
                        if (name.contains("bind")) {
                            if (p.worldObj.rand.nextDouble() <= value) {
                                event.entityLiving.addPotionEffect((new PotionEffect(2, 200, 1000)));
                            }
                        }
                    }
                }
            } else if (edms.damageType.contains("arrow") && edms.getSourceOfDamage() instanceof EntityArrow
                && ((EntityArrow)(edms.getSourceOfDamage())).shootingEntity instanceof EntityPlayer) {
                    EntityPlayer p = (EntityPlayer) ((EntityArrow)(edms.getSourceOfDamage())).shootingEntity;
                    ItemStack[] equippedItems = new ItemStack[2];
                    equippedItems[0] = p.getCurrentEquippedItem();
                    equippedItems[1] = (BT_Mod.backhandLoaded ? BackhandUtils.getOffhandItem(p) : null);
                    for (ItemStack stack : equippedItems) {
                        if (stack != null && BT_Utils.itemHasEffect(stack) && stack.getItem() instanceof ItemBow) {
                            String dummyDataString = stack.getTagCompound()
                                .getCompoundTag("BT_TagList")
                                .getString("BT_Buffs");
                            DummyData[] d = DataStorage.parseData(dummyDataString);
                            for (DummyData data : d) {
                                String name = data.fieldName;
                                double value = Double.parseDouble(data.fieldValue);
                                if (name.contains("damage")) {
                                    float dam = event.ammount;
                                    if (value < 0) {
                                        value = -value;
                                        float mainDam = (float) (dam * value);
                                        event.ammount -= mainDam;
                                    } else {
                                        float mainDam = (float) (dam * value);
                                        event.ammount += mainDam;
                                    }
                                }
                                // TODO: Eventually add mixin alternatives for everything and also add a mixin based
                                // speed buff for the bow draw speed
                                if (name.contains("life")) {
                                    if (p.worldObj.rand.nextDouble() <= value) {
                                        int heartAmount = p.worldObj.rand.nextInt(3);
                                        p.heal(heartAmount);
                                        event.ammount += heartAmount;

                                    }
                                }
                                if (name.contains("crit")) {
                                    if (p.worldObj.rand.nextDouble() <= value) {
                                        event.ammount *= 2.5F;
                                    }
                                }
                                if (name.contains("poison")) {
                                    if (p.worldObj.rand.nextDouble() <= value) {
                                        event.entityLiving.addPotionEffect(new PotionEffect(19, 450, 3));
                                    }
                                }
                            }

                        }
                    }

                    if (p.worldObj.rand.nextDouble() <= critValue) {
                        event.ammount *= 2.5F;
                    }
                }
            if (event.entityLiving instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) event.entityLiving;
                World w = p.worldObj;
                for (int aSlot = 0; aSlot < 4; aSlot++) {
                    if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot))) {
                        ItemStack stack = p.getCurrentArmor(aSlot);
                        String dummyDataString = stack.getTagCompound()
                            .getCompoundTag("BT_TagList")
                            .getString("BT_Buffs");
                        DummyData[] d = DataStorage.parseData(dummyDataString);
                        for (DummyData data : d) {
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
                            if (name.contains("bind")) {
                                if (p.worldObj.rand.nextDouble() <= value) {
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
    }

    private static double hasteValue = 0.0;

    @SubscribeEvent
    public void event_BreakSpeed(BreakSpeed event) {
        EntityPlayer p = event.entityPlayer;
        World w = p.worldObj;
        Block b = event.block;
        if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem())) {
            ItemStack stack = p.getCurrentEquippedItem();
            String dummyDataString = stack.getTagCompound()
                .getCompoundTag("BT_TagList")
                .getString("BT_Buffs");
            DummyData[] d = DataStorage.parseData(dummyDataString);
            for (DummyData data : d) {
                String name = data.fieldName;
                double value = Double.parseDouble(data.fieldValue);
                if (name.contains("speed")) {
                    hasteValue += value;
                }
            }
        }
        float speed = event.originalSpeed;
        if (hasteValue < 0) {
            hasteValue = -hasteValue;
            float mainSpeed = (float) (speed * hasteValue);
            event.newSpeed = speed - mainSpeed;
        } else {
            float mainSpeed = (float) (speed * hasteValue);
            event.newSpeed = speed + mainSpeed;
        }
    }
    UUID speedUUID = UUID.fromString("c0a80123-4567-89ab-cdef-0123456789ab");
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        EntityPlayer player = event.entity;

        IAttributeInstance attr =
                player.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (attr == null) return;
        AttributeModifier mod = attr.getModifier(speedUUID);
        if (mod == null) return;

        System.out.println(((attr.getAttributeValue() / player.capabilities.getWalkSpeed() + 1.0F) / 2.0F));
        event.newfov = 1.0F + (float)((attr.getAttributeValue() / player.capabilities.getWalkSpeed() + 1.0F) / 2.0F)/5.0F;
    }



    EntityAIAvoidEntity avoidPlayerTask;
    private EntityMob mob = null;

    @SubscribeEvent
    public void PlayerTickEvent(TickEvent.PlayerTickEvent event) {
        EntityPlayer p = event.player;
        World w = p != null ? p.worldObj : null;
        float playerSpeedModifier = 0.0F;
        hasteValue = 0.0;

        if (p == null || p.ticksExisted < 80 || w == null) return;

        ItemStack[] equippedItems = new ItemStack[6];
        equippedItems[0] = p.getCurrentArmor(0);
        equippedItems[1] = p.getCurrentArmor(1);
        equippedItems[2] = p.getCurrentArmor(2);
        equippedItems[3] = p.getCurrentArmor(3);
        equippedItems[4] = p.getCurrentEquippedItem();
        equippedItems[5] = (BT_Mod.backhandLoaded ? BackhandUtils.getOffhandItem(p) : null);
        for (ItemStack stack : equippedItems) {
            if (stack != null && BT_Utils.itemHasEffect(stack)) {
                String dummyDataString = stack.getTagCompound()
                    .getCompoundTag("BT_TagList")
                    .getString("BT_Buffs");
                DummyData[] d = DataStorage.parseData(dummyDataString);
                for (DummyData data : d) {
                    String name = data.fieldName;
                    float value = (float) Double.parseDouble(data.fieldValue);
                    if (name.contains("swift")) {
                        playerSpeedModifier += value;
                    }
                    if (name.contains("slow")) {
                        playerSpeedModifier += value;
                    }
                    if (name.contains("speed")) {
                        hasteValue += value;
                    }
                    try {
                        if (name.contains("fear")) {
                            assignFleeTask(p, w, value);
                        } else if (avoidPlayerTask != null && mob != null) {
                            removeFleeTask(avoidPlayerTask, mob, p);
                        }
                    } catch (ConcurrentModificationException e) {
                        System.err.println("[BlacksmithTweaks] Error with fear buff: | " + e + " Why :( ");

                    }
                }
            }
        }
        AttributeModifier speedModifier = new AttributeModifier(speedUUID, "BT_SWIFT", playerSpeedModifier, 2);
        IAttributeInstance attr = p.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        if (playerSpeedModifier != 0.0) {
            if (attr != null) {
                attr.removeModifier(speedModifier);
                attr.applyModifier(speedModifier);
                if(p instanceof EntityPlayerSP)
                    ForgeHooksClient.getOffsetFOV((EntityPlayerSP) p, 1.0F);
            }
        }
        else
            attr.removeModifier(speedModifier);

        ItemStack stack = p.getCurrentEquippedItem();
        if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("BT_Display")) {
            stack.setStackDisplayName(BT_Utils.getDisplayName(stack));
        }
    }

    private void assignFleeTask(EntityPlayer p, World w, double value) {
        if (p.ticksExisted % 50 == 0) {
            for (Object obj : w.loadedEntityList) {
                if (obj instanceof EntityMob) {
                    mob = (EntityMob) obj;
                    if (mob.getDistanceToEntity(p) <= 10) {
                        avoidPlayerTask = new EntityAIAvoidEntity(mob, p.getClass(), (float) 12.0D, value, 1.2D);
                        mob.tasks.addTask(1, avoidPlayerTask);
                    } else if (mob.getDistanceToEntity(p) <= 15) {
                        mob.tasks.removeTask(avoidPlayerTask);
                    }
                }
            }
        }
    }

    private void removeFleeTask(EntityAIAvoidEntity task, EntityMob mob, EntityPlayer p) {
        // Armor
        for (int aSlot = 0; aSlot < 4; aSlot++) {
            if (p.getCurrentArmor(aSlot) != null && BT_Utils.itemHasEffect(p.getCurrentArmor(aSlot))) {
                ItemStack stack = p.getCurrentArmor(aSlot);
                String dummyDataString = stack.getTagCompound()
                    .getCompoundTag("BT_TagList")
                    .getString("BT_Buffs");
                DummyData[] d = DataStorage.parseData(dummyDataString);
                for (DummyData data : d) {
                    String name = data.fieldName;
                    double value = Double.parseDouble(data.fieldValue);
                    if (name.contains("fear")) {
                        mob.tasks.removeTask(task);
                        return;
                    }
                }
            }
        }
        // Hand
        if (p.getCurrentEquippedItem() != null && BT_Utils.itemHasEffect(p.getCurrentEquippedItem())) {
            ItemStack stack = p.getCurrentEquippedItem();
            String dummyDataString = stack.getTagCompound()
                .getCompoundTag("BT_TagList")
                .getString("BT_Buffs");
            DummyData[] d = DataStorage.parseData(dummyDataString);
            for (DummyData data : d) {
                String name = data.fieldName;
                if (name.contains("fear")) {
                    mob.tasks.removeTask(task);
                }
            }
        }

    }

}
