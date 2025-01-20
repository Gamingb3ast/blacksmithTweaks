package com.gamingb3ast.blacksmithTweaks;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayerMP;

public class BT_MessageShift implements IMessage {
    private boolean isShiftDown;

    // Default constructor required
    public BT_MessageShift() {}

    public BT_MessageShift(boolean isShiftDown) {
        this.isShiftDown = isShiftDown;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isShiftDown = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isShiftDown);
    }

    public static class Handler implements IMessageHandler<BT_MessageShift, IMessage> {
        @Override
        public IMessage onMessage(BT_MessageShift message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                boolean shiftDown = message.isShiftDown;


                // Store the state in a map for use in GUI/containers
                BT_ShiftHandler.setPlayerShiftState(player.getUniqueID(), shiftDown);
            }
            return null;
        }
    }
}
