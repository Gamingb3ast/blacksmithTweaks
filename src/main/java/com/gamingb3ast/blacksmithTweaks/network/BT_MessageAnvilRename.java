package com.gamingb3ast.blacksmithTweaks.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class BT_MessageAnvilRename implements IMessage {

    private int slotIndex;
    private NBTTagCompound tag;

    public BT_MessageAnvilRename() {}

    public BT_MessageAnvilRename(int slotIndex, NBTTagCompound tag) {
        this.slotIndex = slotIndex;
        this.tag = tag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        slotIndex = buf.readInt();
        boolean hasTag = buf.readBoolean();
        if (hasTag) {
            tag = ByteBufUtils.readTag(buf);
        } else {
            tag = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeBoolean(tag != null);
        if (tag != null) {
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    public static class Handler implements IMessageHandler<BT_MessageAnvilRename, IMessage> {

        @Override
        public IMessage onMessage(final BT_MessageAnvilRename message, MessageContext ctx) {
            // This handler runs on the Netty thread. Many mods modify container/slot state
            // directly here. if hit threading issues, see the note about scheduling.
            if (ctx.side.isServer()) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;

                if (player != null && player.openContainer != null && message.tag != null) {
                    int idx = message.slotIndex;
                    if (idx >= 0 && idx < player.openContainer.inventorySlots.size()) {
                        // getSlot returns a Slot; getStack may be null
                        ItemStack stack = player.openContainer.getSlot(idx)
                            .getStack();
                        if (stack != null) {
                            // set the whole tag compound (replace) — optionally you can merge instead
                            stack.setTagCompound((NBTTagCompound) message.tag.copy());
                            // make sure container syncs to client(s)
                            player.openContainer.detectAndSendChanges();
                        }
                    }
                }
            }
            return null;
        }
    }
}
