package com.gamingb3ast.blacksmithTweaks;

import java.io.File;
import java.util.Set;
import java.util.Arrays;

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

public class BT_Config extends Configuration{

	private static BT_Config INSTANCE = null;
	private static int buffsCount = 0;

	public static Item[] blacklist;
	public static Item[] whitelist;
	public static int buffApplicationMethod;
	public static String CONFIG_GENERAL = "General";

	public BT_Config(File configFile)
	{
		super(configFile);
		this.load();
		loadCFG();
		this.save();
	}

	public static BT_Config initialize(File configFile)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new BT_Config(configFile);
		}
		else
			throw new IllegalStateException("Cannot initialize Blacksmith Tweaks config twice!");
		return INSTANCE;
	}

    public static BT_Config instance()
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
		help.setComment(" Using this .cfg file you can add your own effects, which will be applied to tools. "
		+ "\n Adding your own effect is very simple. Above you can see the example of how it's done. You need to create a custom category, using # chars, then write the code effect name(make sure your name is unique, otherwise your effect will most likely be ignored)."
		+ "\n Then you need to write this name again, this time without # chars, and add {} after it."
		+ "\n Now, there are 3 fields you need to write in this {}."
		+ "\n The first one is 'name'. This says, what name will be actually shown in-game(the tool prefix)."
		+ "\n Second one is 'color'. You should put one hex number there. You can choose from this number set: 8,f,a,2,9,d,e,6,b,3,c,4. These represent the rarity of your effect. You can learn more about rarities in DummyCore code, in EnumRarityColor file."
		+ "\n The last one is 'dataArray'. This represents the effects, that will be applied to your buff. There are 10 effects by now - 'damage','speed','invulTime','durability', 'swift', 'slow', 'lifesteal', 'fear', 'poison' and 'crit'."
		+ "\n To write this data you need to follow the simple rules: after : put ||, then put an actual name of one of the 10 possible effects. Then put : again, and after that write your value. It can be below 0, should never be an integer unless specified"
		+ "\n This value is persentage-based, and it scales, as 1 = 100%, and 0.25 = 25%. Some effects only except integer values, such as 'swift', 'fear', and 'slow', THESE ARE THE ONLY EFFECTS THAT ACCEPT INTEGER VALUES, PLEASE GIVE THE CORRECT TYPE OF NUMBER TO THE CORRECT EFFECT. "
		+ "\n If you want to add more than one effect, ust put || after the value you have last written, and start writing another data string. But remember, that || represents the beginning of the new datastring, so something like |||| will most likely lead to crash."
		+ "\n You Can also create a blacklist and whitelist for items which will be given buffs, simply do this by adding the item name to the list as shown in the config."
		+ "\n Please note that the whitelist overrides the blacklist!");
		ConfigCategory c = this.getCategory("BT:Effect:Durable");
		c.put("name", new Property("name","Durable",Type.STRING));
		c.put("color", new Property("color","a",Type.STRING));
		DummyData durDat = new DummyData("durability", 0.25D);
		DataStorage.addDataToString(durDat);
		String str = DataStorage.getDataString();
		c.put("dataArray", new Property("dataArray",str,Type.STRING));

		//BlackList
		//whitelist = this.get(CONFIG_GENERAL, "whitelist", false, "Is list a whitelist?").getBoolean();
		String[] blacklistItems = this.get(CONFIG_GENERAL, "Blacklist", "minecraft:book", "Items to blacklist. Ignores tinkers construct tools").getString().split(", ");
		//Whitelist
		String[] whitelistItems = this.get(CONFIG_GENERAL, "Whitelist", "minecraft:iron_helmet", "Items to whitelist. Ignores tinkers construct tools").getString().split(", ");
		//Buff application type
		int buffApplicationType = this.get(CONFIG_GENERAL, "Buff Application Type", 2, "This is the method that will be used to apply buffs to tools and armor. The different types are the following:" +
				"\n 1: Classic. Buffs are applied when crafting the item, holding shift will prevent buffs from being applied but will give you negative status effects unless you are above level 30 (Inventory refresh required to see buffs applied to an item)" +
				"\n 2: Crafting Reworked. Buffs are always applied when crafting the item, shifting will do nothing " +
				"\n 3: Container. An alternative system, it runs through every item in the currently opened/updated container and applies buffs to the valid items. Also skips items that were debuffed in the reforging anvil" +
				"\n 4: Tooltip Container. Alternative to Container, a hybrid between container update and item hover events, will apply to any valid item even if it was in a chest. The only exception being if you de-buffed the item using the reforging anvil (Is a bit more optimized via single slot checking)" +
				"\n 5: Buffs are only applied via reforging anvil (This will be more relevant in a future update)" +
				"\n Any other number will result in buff application being disabled" +
				"\n WARNING: SOME OF THESE WILL NOT WORK WITH CERTAIN MODS, THIS CONFIG OPTION EXISTS SO YOU CAN HAVE ALTERNATIVES IN CASE OF BUGS OR CRASHES").getInt();

		//Create data
		Set s = this.getCategoryNames();
		for(int i = 0; i < s.size()-1; ++i)
		{
			ConfigCategory cat = this.getCategory((String) s.toArray()[i]);
			String codeName = cat.getQualifiedName();
			if(cat.containsKey("name") && cat.containsKey("color") && cat.containsKey("dataArray"))
			{
				String name = cat.get("name").getString();
				String hex = cat.get("color").getString();
				String data = cat.get("dataArray").getString();
				DummyData[] dat = DataStorage.parseData(data);
				BT_Effect aaa = new BT_Effect(codeName, name, EnumRarityColor.getColorByHex(hex), dat).registerEffect();
				Notifier.notifyCustomMod("Blacksmith Tweaks", "Adding a new effect with name "+name+", rarity "+EnumRarityColor.getColorByHex(hex).getName() + " and data "+data);
				++buffsCount;
			}
		}

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
                applicationString = "Buffs are only applied via reforging anvil";
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



		Notifier.notifyCustomMod("Blacksmith Tweaks","Loaded "+buffsCount+" custom effect(s).");
	}




}
