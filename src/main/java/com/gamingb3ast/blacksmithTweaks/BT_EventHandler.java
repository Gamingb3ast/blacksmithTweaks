package com.gamingb3ast.blacksmithTweaks;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.gamingb3ast.blacksmithTweaks.anvil.BT_TileAnvil;
import com.gamingb3ast.blacksmithTweaks.api.BT_ItemAPI;
import com.gamingb3ast.blacksmithTweaks.configs.BT_CoreConfig;

import DummyCore.Events.DummyEvent_OnClientGUIButtonPress;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BT_EventHandler {

    @SubscribeEvent
    public void onClientPacketRecieved(DummyEvent_OnClientGUIButtonPress event) {
        if (event.client_ParentClassPath.equalsIgnoreCase("com.gamingb3ast.blacksmithTweaks.anvil.BT_GuiAnvil")) {
            TileEntity tile = event.presser.worldObj.getTileEntity(event.x, event.y, event.z);
            if (tile instanceof BT_TileAnvil && BT_CoreConfig.buffApplicationMethod <= 5
                && BT_CoreConfig.buffApplicationMethod > 0) {
                BT_TileAnvil anvil = (BT_TileAnvil) tile;
                ItemStack stk = anvil.getStackInSlot(0)
                    .copy();
                BT_ItemAPI.addRandomEffect(stk);
                anvil.setInventorySlotContents(0, null);
                anvil.setInventorySlotContents(1, stk);
                anvil.markDirty();
                int cost = Integer.parseInt(event.additionalData[0].fieldValue);
                if (!event.presser.capabilities.isCreativeMode) event.presser.experienceLevel -= cost;
            }
        }
    }

}
