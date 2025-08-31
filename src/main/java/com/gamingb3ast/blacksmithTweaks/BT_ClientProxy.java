package com.gamingb3ast.blacksmithTweaks;

import com.gamingb3ast.blacksmithTweaks.anvil.BT_AnvilRenderer;
import com.gamingb3ast.blacksmithTweaks.anvil.BT_ContainerAnvil;
import com.gamingb3ast.blacksmithTweaks.anvil.BT_GuiAnvil;
import com.gamingb3ast.blacksmithTweaks.anvil.BT_TileAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BT_ClientProxy extends BT_ServerProxy
{
	@Override
	public void preload()
	{
		RenderingRegistry.registerBlockHandler(new BT_AnvilRenderer());
	}
	
	@Override
	public int getAnvilRenderID()
	{
		return BT_AnvilRenderer.getStaticRenderId();
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity t = world.getTileEntity(x, y, z);
		if(t instanceof BT_TileAnvil)
		{
			return new BT_GuiAnvil(new BT_ContainerAnvil(player.inventory,t),t);
		}
		return null;
	}
}
