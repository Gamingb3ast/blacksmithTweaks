package com.gamingb3ast.blacksmithTweaks.configs;

import java.io.File;
import java.util.Set;

import com.gamingb3ast.blacksmithTweaks.BT_Effect;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import net.minecraft.item.Item;
import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.Notifier;


public class BT_CoreConfig extends Configuration{

	private static BT_CoreConfig INSTANCE = null;
	private static int buffsCount = 0;

	public static Item[] blacklist;
	public static Item[] whitelist;
	public static int buffApplicationMethod;
	public static String CONFIG_GENERAL = "General";

	public BT_CoreConfig(File configFile)
	{
		super(configFile);
		this.load();
		loadCFG();
		this.save();
	}

	public static BT_CoreConfig initialize(File configFile)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new BT_CoreConfig(configFile);
		}
		else
			throw new IllegalStateException("Cannot initialize Blacksmith Tweaks config twice!");
		return INSTANCE;
	}

    public static BT_CoreConfig instance()
	{
		if(INSTANCE == null)
		{
			throw new IllegalStateException("Instance of Blacksmith Tweaks Config requested before initialization");
		}
		return INSTANCE;
	}

    public void loadCFG()
	{

		ConfigCategory help = this.getCategory("Help");
		help.setComment(" Using this cfg file you can configure black/white lists and other mechanics of the mod "
		+ "\n Use the Effects.cfg file to configure the actual buffs, help and examples can be found at the bottom of the file"
		+ "\n You can create a blacklist and whitelist for items which will be given buffs, simply do this by adding the item name to the list as shown in the config."
		+ "\n Please note that the whitelist overrides the blacklist!");

		//BlackList
		//whitelist = this.get(CONFIG_GENERAL, "whitelist", false, "Is list a whitelist?").getBoolean();
		String[] blacklistItems = this.get(CONFIG_GENERAL, "Blacklist", "minecraft:book", "Items to blacklist. Ignores tinkers construct tools").getString().split(", ");
		//Whitelist
		String[] whitelistItems = this.get(CONFIG_GENERAL, "Whitelist", "minecraft:iron_helmet", "Items to whitelist. Ignores tinkers construct tools").getString().split(", ");
		//Buff application type
		int buffApplicationType = this.get(CONFIG_GENERAL, "Buff Application Type", 2, "This is the method that will be used to apply buffs to tools and armor. The different types are the following:" +
				"\n 1: Classic. Buffs are applied when crafting the item, holding shift will prevent buffs from being applied but will give you negative status effects unless you are above level 30 (Inventory refresh required to see buffs applied to an item)" +
				"\n 2: Crafting Reworked. Buffs are always applied when crafting the item, shifting will do nothing " +
				"\n 3: Container. An alternative system, it runs through every item in the currently opened/updated container and applies buffs to the valid items. Also skips items that were debuffed in the reforging com.gamingb3ast.blacksmithTweaks.anvil" +
				"\n 4: Tooltip Container. Alternative to Container, a hybrid between container update and item hover events, will apply to any valid item even if it was in a chest. The only exception being if you de-buffed the item using the reforging com.gamingb3ast.blacksmithTweaks.anvil (Is a bit more optimized via single slot checking)" +
				"\n 5: Buffs are only applied via reforging com.gamingb3ast.blacksmithTweaks.anvil (This will be more relevant in a future update)" +
				"\n Any other number will result in buff application being disabled" +
				"\n WARNING: SOME OF THESE WILL NOT WORK WITH CERTAIN MODS, THIS CONFIG OPTION EXISTS SO YOU CAN HAVE ALTERNATIVES IN CASE OF BUGS OR CRASHES").getInt();



		buffApplicationMethod = buffApplicationType;
		String applicationString = "Classic";
		switch(buffApplicationType)
		{
			case 1:
                applicationString = "Classic";
                break;
            case 2:
                applicationString = "Crafting Reworked";
                break;
            case 3:
                applicationString = "Container";
                break;
            case 4:
                applicationString = "Tooltip Container";
                break;
            case 5:
                applicationString = "Buffs are only applied via reforging com.gamingb3ast.blacksmithTweaks.anvil";
                break;
            default:
                applicationString = "Buff application disabled";
                break;
		}
		Notifier.notifyCustomMod("Blacksmith Tweaks", "Application method in use:: " + applicationString);

			String mod, item;
			blacklist = new Item[blacklistItems.length];
			for(int i = 0; i < blacklistItems.length; i++)
			{
				Notifier.notifyCustomMod("Blacksmith Tweaks", "Loaded Item " + blacklistItems[i]);
				String[] modidAndName = blacklistItems[i].split(":");
				mod = modidAndName[0];
				item = modidAndName[1];
				blacklist[i] = GameRegistry.findItem(mod, item);

			}
			whitelist = new Item[whitelistItems.length];
			for(int i = 0; i < whitelistItems.length; i++)
			{
				Notifier.notifyCustomMod("Blacksmith Tweaks", "Loaded Item " + whitelistItems[i]);
				String[] modidAndName = whitelistItems[i].split(":");
				mod = modidAndName[0];
				item = modidAndName[1];
				whitelist[i] = GameRegistry.findItem(mod, item);

			}
	}




}
