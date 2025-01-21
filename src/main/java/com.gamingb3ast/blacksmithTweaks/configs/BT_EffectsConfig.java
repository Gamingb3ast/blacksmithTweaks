package com.gamingb3ast.blacksmithTweaks.configs;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.Notifier;
import com.gamingb3ast.blacksmithTweaks.BT_Effect;
import com.gamingb3ast.blacksmithTweaks.BT_Mod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
import java.io.File;
import java.io.IOException;
import java.util.Set;


public class BT_EffectsConfig extends Configuration{

	private static BT_EffectsConfig INSTANCE = null;
	private static int buffsCount = 0;


	public BT_EffectsConfig(File configFile)
	{
		super(configFile);
		this.load();
		registerEffects(configFile);
		loadCFG();
		this.save();
	}

	public static BT_EffectsConfig initialize(File configFile)
	{
		if (INSTANCE == null)
		{
			INSTANCE = new BT_EffectsConfig(configFile);
		}
		else
			throw new IllegalStateException("Cannot initialize Blacksmith Tweaks config twice!");
		return INSTANCE;
	}

    public static BT_EffectsConfig instance()
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
		+ "\n Adding your own effect is very simple. Below you can see the example of how it's done. You need to create a custom category, using # chars, then write the code effect name(make sure your name is unique, otherwise your effect will most likely be ignored)."
		+ "\n Then you need to write this name again, this time without # chars, and add {} after it."
		+ "\n Now, there are 3 fields you need to write in this {}."
		+ "\n The first one is 'name'. This says, what name will be actually shown in-game(the tool prefix)."
		+ "\n Second one is 'color'. You should put one hex number there. You can choose from this number set: 8,f,a,2,9,d,e,6,b,3,c,4. These represent the rarity of your effect. You can learn more about rarities in DummyCore code, in EnumRarityColor file."
		+ "\n The last one is 'dataArray'. This represents the effects, that will be applied to your buff. There are 9 effects by now - 'damage','speed','durability', 'swift', 'slow', 'lifesteal', 'fear', 'poison' and 'crit'."
		+ "\n To write this data you need to follow the simple rules: after : put ||, then put an actual name of one of the 10 possible effects. Then put : again, and after that write your value. It can be below 0, should never be an integer unless specified"
		+ "\n This value is percentage-based, and it scales, as 1 = 100%, and 0.25 = 25%. Some effects only except integer values, such as 'swift', 'fear', and 'slow', THESE ARE THE ONLY EFFECTS THAT ACCEPT INTEGER VALUES, PLEASE GIVE THE CORRECT TYPE OF NUMBER TO THE CORRECT EFFECT. "
		+ "\n If you want to add more than one effect, just put || after the value you have last written, and start writing another data string. But remember, that || represents the beginning of the new datastring, so something like |||| will most likely lead to crash.");


        ConfigCategory exampleCat = this.getCategory("BT:Effect:exampleEffect");
		exampleCat.setComment("This is an example effect, any effect using this identifier will not be loaded");
		exampleCat.put("name", new Property("name","Example",Type.STRING));
		exampleCat.put("color", new Property("color","8",Type.STRING));
		DummyData durDat = new DummyData("durability", 1D);
		DummyData slowDat = new DummyData("slow", 1);
		DataStorage.addDataToString(durDat);
		DataStorage.addDataToString(slowDat);
		String str = DataStorage.getDataString();
		exampleCat.put("dataArray", new Property("dataArray",str,Type.STRING));


		//Create data
		Set s = this.getCategoryNames();
		for(int i = 0; i < s.size()-1; ++i)
		{
			ConfigCategory cat = this.getCategory((String) s.toArray()[i]);
			String codeName = cat.getQualifiedName();
			if(codeName.equals("BT:Effect:exampleEffect"))
				break;
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

		if(buffsCount!= 0) {
			Notifier.notifyCustomMod("Blacksmith Tweaks", "Loaded " + buffsCount + " effect(s).");
		}
		else
			throw new IllegalStateException("[Blacksmith Tweaks] No buffs were loaded, THIS IS EXPECTED FOR FIRST LAUNCHES OF THE CONFIG!! PLEASE RESTART THE GAME TO FIX THIS ISSUE!1");

	}
	/**
	Will recreate the default config file with all the normal effects
	 */
	public void registerEffects(File configFile)
	{
		if(BT_Mod.effectConfigExists) {
			ConfigCategory eaaCat = this.getCategory("BT:Effect:Damaged");
			eaaCat.put("name", new Property("name", "Damaged", Type.STRING));
			eaaCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eaaData = new DummyData("damage", -0.2D);
			DataStorage.addDataToString(eaaData);
			String eaaStr = DataStorage.getDataString();
			eaaCat.put("dataArray", new Property("dataArray", eaaStr, Type.STRING));

			ConfigCategory eabCat = this.getCategory("BT:Effect:Dull");
			eabCat.put("name", new Property("name", "Dull", Type.STRING));
			eabCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eabData = new DummyData("damage", -0.20D);
			DataStorage.addDataToString(eabData);
			String eabStr = DataStorage.getDataString();
			eabCat.put("dataArray", new Property("dataArray", eabStr, Type.STRING));

			ConfigCategory eacCat = this.getCategory("BT:Effect:Sluggish");
			eacCat.put("name", new Property("name", "Sluggish", Type.STRING));
			eacCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eacData = new DummyData("speed", -0.46D);
			DummyData eacSlow = new DummyData("slow", -1);
			DataStorage.addDataToString(eacData);
			DataStorage.addDataToString(eacSlow);
			String eacStr = DataStorage.getDataString();
			eacCat.put("dataArray", new Property("dataArray", eacStr, Type.STRING));

			ConfigCategory eadCat = this.getCategory("BT:Effect:Slow");
			eadCat.put("name", new Property("name", "Slow", Type.STRING));
			eadCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eadData = new DummyData("speed", -0.3D);
			DataStorage.addDataToString(eadData);
			String eadStr = DataStorage.getDataString();
			eadCat.put("dataArray", new Property("dataArray", eadStr, Type.STRING));

			ConfigCategory eaeCat = this.getCategory("BT:Effect:Lazy");
			eaeCat.put("name", new Property("name", "Lazy", Type.STRING));
			eaeCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eaeData = new DummyData("speed", -0.16D);
			DataStorage.addDataToString(eaeData);
			String eaeStr = DataStorage.getDataString();
			eaeCat.put("dataArray", new Property("dataArray", eaeStr, Type.STRING));

			ConfigCategory eafCat = this.getCategory("BT:Effect:Cracky");
			eafCat.put("name", new Property("name", "Cracky", Type.STRING));
			eafCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eafData = new DummyData("durability", -0.2D);
			DataStorage.addDataToString(eafData);
			String eafStr = DataStorage.getDataString();
			eafCat.put("dataArray", new Property("dataArray", eafStr, Type.STRING));

			ConfigCategory eagCat = this.getCategory("BT:Effect:Broken");
			eagCat.put("name", new Property("name", "Broken", Type.STRING));
			eagCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eagData = new DummyData("damage", -0.4D);
			DummyData eagSpeed = new DummyData("speed", -0.4D);
			DataStorage.addDataToString(eagData);
			DataStorage.addDataToString(eagSpeed);
			String eagStr = DataStorage.getDataString();
			eagCat.put("dataArray", new Property("dataArray", eagStr, Type.STRING));

			ConfigCategory eahCat = this.getCategory("BT:Effect:Annoying");
			eahCat.put("name", new Property("name", "Annoying", Type.STRING));
			eahCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eahData = new DummyData("damage", -0.2D);
			DummyData eahSpeed = new DummyData("speed", -0.3D);
			DataStorage.addDataToString(eahData);
			DataStorage.addDataToString(eahSpeed);
			String eahStr = DataStorage.getDataString();
			eahCat.put("dataArray", new Property("dataArray", eahStr, Type.STRING));

			ConfigCategory eaiCat = this.getCategory("BT:Effect:Shoddy");
			eaiCat.put("name", new Property("name", "Shoddy", Type.STRING));
			eaiCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eaiData = new DummyData("damage", -0.2D);
			DummyData eaiSpeed = new DummyData("speed", -0.3D);
			DataStorage.addDataToString(eaiData);
			DataStorage.addDataToString(eaiSpeed);
			String eaiStr = DataStorage.getDataString();
			eaiCat.put("dataArray", new Property("dataArray", eaiStr, Type.STRING));

			ConfigCategory eajCat = this.getCategory("BT:Effect:Terrible");
			eajCat.put("name", new Property("name", "Terrible", Type.STRING));
			eajCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eajData = new DummyData("damage", -0.2D);
			DummyData eajSpeed = new DummyData("speed", -0.15D);
			DummyData eajDurability = new DummyData("durability", -0.08D);
			DataStorage.addDataToString(eajData);
			DataStorage.addDataToString(eajSpeed);
			DataStorage.addDataToString(eajDurability);
			String eajStr = DataStorage.getDataString();
			eajCat.put("dataArray", new Property("dataArray", eajStr, Type.STRING));

			ConfigCategory eakCat = this.getCategory("BT:Effect:Unhappy");
			eakCat.put("name", new Property("name", "Unhappy", Type.STRING));
			eakCat.put("color", new Property("color", "8", Type.STRING)); // BROKEN
			DummyData eakData = new DummyData("damage", -0.15D);
			DummyData eakSpeed = new DummyData("speed", -0.2D);
			DummyData eakDurability = new DummyData("durability", -0.1D);
			DataStorage.addDataToString(eakData);
			DataStorage.addDataToString(eakSpeed);
			DataStorage.addDataToString(eakDurability);
			String eakStr = DataStorage.getDataString();
			eakCat.put("dataArray", new Property("dataArray", eakStr, Type.STRING));

			ConfigCategory ealCat = this.getCategory("BT:Effect:Heavy");
			ealCat.put("name", new Property("name", "Heavy", Type.STRING));
			ealCat.put("color", new Property("color", "f", Type.STRING)); // COMMON
			DummyData ealData = new DummyData("damage", 0.15D);
			DummyData ealSpeed = new DummyData("speed", -0.3D);
			DummyData ealSlow = new DummyData("slow", -1);
			DataStorage.addDataToString(ealData);
			DataStorage.addDataToString(ealSpeed);
			DataStorage.addDataToString(ealSlow);
			String ealStr = DataStorage.getDataString();
			ealCat.put("dataArray", new Property("dataArray", ealStr, Type.STRING));

			ConfigCategory eamCat = this.getCategory("BT:Effect:Light");
			eamCat.put("name", new Property("name", "Light", Type.STRING));
			eamCat.put("color", new Property("color", "f", Type.STRING)); // COMMON
			DummyData eamData = new DummyData("damage", -0.15D);
			DummyData eamSpeed = new DummyData("speed", 0.3D);
			DummyData eamSwift = new DummyData("swift", 1);
			DataStorage.addDataToString(eamData);
			DataStorage.addDataToString(eamSpeed);
			DataStorage.addDataToString(eamSwift);
			String eamStr = DataStorage.getDataString();
			eamCat.put("dataArray", new Property("dataArray", eamStr, Type.STRING));

			ConfigCategory eanCat = this.getCategory("BT:Effect:Ruthless");
			eanCat.put("name", new Property("name", "Ruthless", Type.STRING));
			eanCat.put("color", new Property("color", "f", Type.STRING)); // COMMON
			DummyData eanData = new DummyData("damage", 0.2D);
			DummyData eanSpeed = new DummyData("speed", -0.15D);
			DummyData eanFear = new DummyData("fear", 1);
			DataStorage.addDataToString(eanData);
			DataStorage.addDataToString(eanSpeed);
			DataStorage.addDataToString(eanFear);
			String eanStr = DataStorage.getDataString();
			eanCat.put("dataArray", new Property("dataArray", eanStr, Type.STRING));

			ConfigCategory eaoCat = this.getCategory("BT:Effect:Shameful");
			eaoCat.put("name", new Property("name", "Shameful", Type.STRING));
			eaoCat.put("color", new Property("color", "f", Type.STRING)); // COMMON
			DummyData eaoData = new DummyData("damage", -0.2D);
			DummyData eaoSpeed = new DummyData("speed", -0.4D);
			DummyData eaoDurability = new DummyData("durability", 0.15D);
			DataStorage.addDataToString(eaoData);
			DataStorage.addDataToString(eaoSpeed);
			DataStorage.addDataToString(eaoDurability);
			String eaoStr = DataStorage.getDataString();
			eaoCat.put("dataArray", new Property("dataArray", eaoStr, Type.STRING));

			ConfigCategory eapCat = this.getCategory("BT:Effect:Bulky");
			eapCat.put("name", new Property("name", "Bulky", Type.STRING));
			eapCat.put("color", new Property("color", "a", Type.STRING)); // GOOD
			DummyData eapData = new DummyData("damage", 0.15D);
			DummyData eapSpeed = new DummyData("speed", -0.2D);
			DummyData eapDurability = new DummyData("durability", 0.1D);
			DummyData eapSlow = new DummyData("slow", -1);
			DummyData eapBind = new DummyData("bind", 0.4);
			DataStorage.addDataToString(eapData);
			DataStorage.addDataToString(eapSpeed);
			DataStorage.addDataToString(eapDurability);
			DataStorage.addDataToString(eapSlow);
			DataStorage.addDataToString(eapBind);
			String eapStr = DataStorage.getDataString();
			eapCat.put("dataArray", new Property("dataArray", eapStr, Type.STRING));

			ConfigCategory eaqCat = this.getCategory("BT:Effect:Nasty");
			eaqCat.put("name", new Property("name", "Nasty", Type.STRING));
			eaqCat.put("color", new Property("color", "a", Type.STRING)); // GOOD
			DummyData eaqData = new DummyData("damage", 0.05D);
			DummyData eaqSpeed = new DummyData("speed", 0.1D);
			DummyData eaqDurability = new DummyData("durability", 0.1D);
			DataStorage.addDataToString(eaqData);
			DataStorage.addDataToString(eaqSpeed);
			DataStorage.addDataToString(eaqDurability);
			String eaqStr = DataStorage.getDataString();
			eaqCat.put("dataArray", new Property("dataArray", eaqStr, Type.STRING));

			ConfigCategory earCat = this.getCategory("BT:Effect:Sharp");
			earCat.put("name", new Property("name", "Sharp", Type.STRING));
			earCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData earData = new DummyData("damage", 0.2D);
			DataStorage.addDataToString(earData);
			String earStr = DataStorage.getDataString();
			earCat.put("dataArray", new Property("dataArray", earStr, Type.STRING));

			ConfigCategory easCat = this.getCategory("BT:Effect:Pointy");
			easCat.put("name", new Property("name", "Pointy", Type.STRING));
			easCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData easData = new DummyData("damage", 0.1D);
			DataStorage.addDataToString(easData);
			String easStr = DataStorage.getDataString();
			easCat.put("dataArray", new Property("dataArray", easStr, Type.STRING));

			ConfigCategory eatCat = this.getCategory("BT:Effect:Hurtful");
			eatCat.put("name", new Property("name", "Hurtful", Type.STRING));
			eatCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eatData = new DummyData("damage", 0.15D);
			DataStorage.addDataToString(eatData);
			String eatStr = DataStorage.getDataString();
			eatCat.put("dataArray", new Property("dataArray", eatStr, Type.STRING));

			ConfigCategory eauCat = this.getCategory("BT:Effect:Strong");
			eauCat.put("name", new Property("name", "Strong", Type.STRING));
			eauCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eauData = new DummyData("durability", 0.15D);
			DataStorage.addDataToString(eauData);
			String eauStr = DataStorage.getDataString();
			eauCat.put("dataArray", new Property("dataArray", eauStr, Type.STRING));

			ConfigCategory eavCat = this.getCategory("BT:Effect:Forceful");
			eavCat.put("name", new Property("name", "Forceful", Type.STRING));
			eavCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eavData = new DummyData("durability", 0.15D);
			DummyData eavBind = new DummyData("bind", 0.4);
			DataStorage.addDataToString(eavData);
			DataStorage.addDataToString(eavBind);
			String eavStr = DataStorage.getDataString();
			eavCat.put("dataArray", new Property("dataArray", eavStr, Type.STRING));

			ConfigCategory eawCat = this.getCategory("BT:Effect:Quick");
			eawCat.put("name", new Property("name", "Quick", Type.STRING));
			eawCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eawData = new DummyData("speed", 0.2D);
			DummyData eawSwift = new DummyData("swift", 1);
			DummyData eawDamage = new DummyData("damage", -0.1);
			DataStorage.addDataToString(eawData);
			DataStorage.addDataToString(eawSwift);
			DataStorage.addDataToString(eawDamage);
			String eawStr = DataStorage.getDataString();
			eawCat.put("dataArray", new Property("dataArray", eawStr, Type.STRING));

			ConfigCategory eaxCat = this.getCategory("BT:Effect:Nimble");
			eaxCat.put("name", new Property("name", "Nimble", Type.STRING));
			eaxCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eaxData = new DummyData("speed", 0.15D);
			DummyData eaxSwift = new DummyData("swift", 1);
			DummyData eaxDamage = new DummyData("damage", -0.05);
			DataStorage.addDataToString(eaxData);
			DataStorage.addDataToString(eaxSwift);
			DataStorage.addDataToString(eaxDamage);
			String eaxStr = DataStorage.getDataString();
			eaxCat.put("dataArray", new Property("dataArray", eaxStr, Type.STRING));

			ConfigCategory eayCat = this.getCategory("BT:Effect:Zealous");
			eayCat.put("name", new Property("name", "Zealous", Type.STRING));
			eayCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eayCrit = new DummyData("crit", 0.25D);
			DummyData eayFear = new DummyData("fear", 1);
			DummyData eayLifesteal = new DummyData("lifesteal", 0.30);
			DataStorage.addDataToString(eayCrit);
			DataStorage.addDataToString(eayFear);
			DataStorage.addDataToString(eayLifesteal);
			String eayStr = DataStorage.getDataString();
			eayCat.put("dataArray", new Property("dataArray", eayStr, Type.STRING));

			ConfigCategory eazCat = this.getCategory("BT:Effect:Keen");
			eazCat.put("name", new Property("name", "Keen", Type.STRING));
			eazCat.put("color", new Property("color", "2", Type.STRING)); // UNCOMMON
			DummyData eazCrit = new DummyData("crit", 0.30D);
			DataStorage.addDataToString(eazCrit);
			String eazStr = DataStorage.getDataString();
			eazCat.put("dataArray", new Property("dataArray", eazStr, Type.STRING));

			ConfigCategory massiveCat = this.getCategory("BT:Effect:Massive");
			massiveCat.put("name", new Property("name", "Massive", Type.STRING));
			massiveCat.put("color", new Property("color", "a", Type.STRING));
			DummyData massiveDat1 = new DummyData("speed", -0.15D);
			DummyData massiveDat2 = new DummyData("bind", 0.6);
			DataStorage.addDataToString(massiveDat1);
			DataStorage.addDataToString(massiveDat2);
			String massiveStr = DataStorage.getDataString();
			massiveCat.put("dataArray", new Property("dataArray", massiveStr, Type.STRING));

			ConfigCategory largeCat = this.getCategory("BT:Effect:Large");
			largeCat.put("name", new Property("name", "Large", Type.STRING));
			largeCat.put("color", new Property("color", "a", Type.STRING));
			DummyData largeDat1 = new DummyData("speed", -0.1D);
			DummyData largeDat2 = new DummyData("bind", 0.5);
			DataStorage.addDataToString(largeDat1);
			DataStorage.addDataToString(largeDat2);
			String largeStr = DataStorage.getDataString();
			largeCat.put("dataArray", new Property("dataArray", largeStr, Type.STRING));

			ConfigCategory demonicCat = this.getCategory("BT:Effect:Demonic");
			demonicCat.put("name", new Property("name", "Demonic", Type.STRING));
			demonicCat.put("color", new Property("color", "9", Type.STRING));
			DummyData demonicDat1 = new DummyData("speed", 0.4D);
			DummyData demonicDat2 = new DummyData("damage", 0.15D);
			DummyData demonicDat3 = new DummyData("crit", 0.15D);
			DummyData demonicDat4 = new DummyData("lifesteal", 0.40D);
			DummyData demonicDat5 = new DummyData("fear", 1);
			DummyData demonicDat6 = new DummyData("swift", 1);
			DataStorage.addDataToString(demonicDat1);
			DataStorage.addDataToString(demonicDat2);
			DataStorage.addDataToString(demonicDat3);
			DataStorage.addDataToString(demonicDat4);
			DataStorage.addDataToString(demonicDat5);
			DataStorage.addDataToString(demonicDat6);
			String demonicStr = DataStorage.getDataString();
			demonicCat.put("dataArray", new Property("dataArray", demonicStr, Type.STRING));

			ConfigCategory agileCat = this.getCategory("BT:Effect:Agile");
			agileCat.put("name", new Property("name", "Agile", Type.STRING));
			agileCat.put("color", new Property("color", "9", Type.STRING));
			DummyData agileDat1 = new DummyData("speed", 0.3D);
			DummyData agileDat2 = new DummyData("crit", 0.15D);
			DummyData agileDat3 = new DummyData("swift", 1);
			DataStorage.addDataToString(agileDat1);
			DataStorage.addDataToString(agileDat2);
			DataStorage.addDataToString(agileDat3);
			String agileStr = DataStorage.getDataString();
			agileCat.put("dataArray", new Property("dataArray", agileStr, Type.STRING));

			ConfigCategory deadlyCat = this.getCategory("BT:Effect:Deadly");
			deadlyCat.put("name", new Property("name", "Deadly", Type.STRING));
			deadlyCat.put("color", new Property("color", "9", Type.STRING));
			DummyData deadlyDat1 = new DummyData("damage", 0.1D);
			DummyData deadlyDat2 = new DummyData("speed", 0.16D);
			DataStorage.addDataToString(deadlyDat1);
			DataStorage.addDataToString(deadlyDat2);
			String deadlyStr = DataStorage.getDataString();
			deadlyCat.put("dataArray", new Property("dataArray", deadlyStr, Type.STRING));

			ConfigCategory unpleasantCat = this.getCategory("BT:Effect:Unpleasant");
			unpleasantCat.put("name", new Property("name", "Unpleasant", Type.STRING));
			unpleasantCat.put("color", new Property("color", "9", Type.STRING));
			DummyData unpleasantDat1 = new DummyData("damage", 0.05D);
			DummyData unpleasantDat2 = new DummyData("durability", 0.15D);
			DummyData unpleasantDat3 = new DummyData("fear", 1);
			DataStorage.addDataToString(unpleasantDat1);
			DataStorage.addDataToString(unpleasantDat2);
			DataStorage.addDataToString(unpleasantDat3);
			String unpleasantStr = DataStorage.getDataString();
			unpleasantCat.put("dataArray", new Property("dataArray", unpleasantStr, Type.STRING));

			ConfigCategory rapidCat = this.getCategory("BT:Effect:Rapid");
			rapidCat.put("name", new Property("name", "Rapid", Type.STRING));
			rapidCat.put("color", new Property("color", "9", Type.STRING));
			DummyData rapidDat1 = new DummyData("speed", 0.25D);
			DummyData rapidDat2 = new DummyData("durability", 0.10D);
			DataStorage.addDataToString(rapidDat1);
			DataStorage.addDataToString(rapidDat2);
			String rapidStr = DataStorage.getDataString();
			rapidCat.put("dataArray", new Property("dataArray", rapidStr, Type.STRING));

			ConfigCategory godlyCat = this.getCategory("BT:Effect:Godly");
			godlyCat.put("name", new Property("name", "Godly", Type.STRING));
			godlyCat.put("color", new Property("color", "d", Type.STRING));
			DummyData godlyDat1 = new DummyData("damage", 0.15D);
			DummyData godlyDat2 = new DummyData("crit", 0.25D);
			DummyData godlyDat3 = new DummyData("lifesteal", 0.50D);
			DummyData godlyDat4 = new DummyData("durability", 0.15D);
			DummyData godlyDat5 = new DummyData("swift", 2);
			DataStorage.addDataToString(godlyDat1);
			DataStorage.addDataToString(godlyDat2);
			DataStorage.addDataToString(godlyDat3);
			DataStorage.addDataToString(godlyDat4);
			DataStorage.addDataToString(godlyDat5);
			String godlyStr = DataStorage.getDataString();
			godlyCat.put("dataArray", new Property("dataArray", godlyStr, Type.STRING));

			ConfigCategory rustyCat = this.getCategory("BT:Effect:Rusty");
			rustyCat.put("name", new Property("name", "Rusty", Type.STRING));
			rustyCat.put("color", new Property("color", "d", Type.STRING));
			DummyData rustyDat1 = new DummyData("damage", -0.08D);
			DummyData rustyDat2 = new DummyData("durability", -0.1D);
			DummyData rustyDat3 = new DummyData("poison", 0.60D);
			DataStorage.addDataToString(rustyDat1);
			DataStorage.addDataToString(rustyDat2);
			DataStorage.addDataToString(rustyDat3);
			String rustyStr = DataStorage.getDataString();
			rustyCat.put("dataArray", new Property("dataArray", rustyStr, Type.STRING));

			ConfigCategory superiorCat = this.getCategory("BT:Effect:Superior");
			superiorCat.put("name", new Property("name", "Superior", Type.STRING));
			superiorCat.put("color", new Property("color", "d", Type.STRING));
			DummyData superiorDat1 = new DummyData("damage", 0.15D);
			DummyData superiorDat2 = new DummyData("crit", 0.3D);
			DummyData superiorDat3 = new DummyData("durability", 0.15D);
			DummyData superiorDat4 = new DummyData("swift", 1);
			DataStorage.addDataToString(superiorDat1);
			DataStorage.addDataToString(superiorDat2);
			DataStorage.addDataToString(superiorDat3);
			DataStorage.addDataToString(superiorDat4);
			String superiorStr = DataStorage.getDataString();
			superiorCat.put("dataArray", new Property("dataArray", superiorStr, Type.STRING));

			ConfigCategory dangerousCat = this.getCategory("BT:Effect:Dangerous");
			dangerousCat.put("name", new Property("name", "Dangerous", Type.STRING));
			dangerousCat.put("color", new Property("color", "d", Type.STRING));
			DummyData dangerousDat1 = new DummyData("damage", 0.20D);
			DummyData dangerousDat2 = new DummyData("lifesteal", 0.60D);
			DummyData dangerousDat3 = new DummyData("crit", 0.22D);
			DummyData dangerousDat4 = new DummyData("fear", 1);
			DummyData dangerousDat5 = new DummyData("speed", 0.3D);
			DummyData dangerousDat6 = new DummyData("swift", 1);
			DummyData dangerousDat7 = new DummyData("poison", 0.60D);
			DataStorage.addDataToString(dangerousDat1);
			DataStorage.addDataToString(dangerousDat2);
			DataStorage.addDataToString(dangerousDat3);
			DataStorage.addDataToString(dangerousDat4);
			DataStorage.addDataToString(dangerousDat5);
			DataStorage.addDataToString(dangerousDat6);
			DataStorage.addDataToString(dangerousDat7);
			String dangerousStr = DataStorage.getDataString();
			dangerousCat.put("dataArray", new Property("dataArray", dangerousStr, Type.STRING));

			ConfigCategory savageCat = this.getCategory("BT:Effect:Savage");
			savageCat.put("name", new Property("name", "Savage", Type.STRING));
			savageCat.put("color", new Property("color", "d", Type.STRING));
			DummyData savageDat1 = new DummyData("damage", 0.15D);
			DummyData savageDat2 = new DummyData("speed", 0.42D);
			DummyData savageDat3 = new DummyData("durability", 0.20D);
			DummyData savageDat4 = new DummyData("swift", 2);
			DummyData savageDat5 = new DummyData("poison", 0.60D);
			DataStorage.addDataToString(savageDat1);
			DataStorage.addDataToString(savageDat2);
			DataStorage.addDataToString(savageDat3);
			DataStorage.addDataToString(savageDat4);
			DataStorage.addDataToString(savageDat5);
			String savageStr = DataStorage.getDataString();
			savageCat.put("dataArray", new Property("dataArray", savageStr, Type.STRING));

			ConfigCategory murderousCat = this.getCategory("BT:Effect:Murderous");
			murderousCat.put("name", new Property("name", "Murderous", Type.STRING));
			murderousCat.put("color", new Property("color", "d", Type.STRING));
			DummyData murderousDat1 = new DummyData("damage", 0.17D);
			DummyData murderousDat2 = new DummyData("crit", 0.35D);
			DummyData murderousDat3 = new DummyData("fear", 1);
			DummyData murderousDat4 = new DummyData("speed", 0.18D);
			DummyData murderousDat5 = new DummyData("swift", 2);
			DummyData murderousDat6 = new DummyData("poison", 0.60D);
			DummyData murderousDat7 = new DummyData("bind", 0.4);
			DataStorage.addDataToString(murderousDat1);
			DataStorage.addDataToString(murderousDat2);
			DataStorage.addDataToString(murderousDat3);
			DataStorage.addDataToString(murderousDat4);
			DataStorage.addDataToString(murderousDat5);
			DataStorage.addDataToString(murderousDat6);
			DataStorage.addDataToString(murderousDat7);
			String murderousStr = DataStorage.getDataString();
			murderousCat.put("dataArray", new Property("dataArray", murderousStr, Type.STRING));

			ConfigCategory legendaryCat = this.getCategory("BT:Effect:Legendary");
			legendaryCat.put("name", new Property("name", "Legendary", Type.STRING));
			legendaryCat.put("color", new Property("color", "6", Type.STRING));
			DummyData legendaryDat1 = new DummyData("damage", 0.25D);
			DummyData legendaryDat2 = new DummyData("lifesteal", 0.70D);
			DummyData legendaryDat3 = new DummyData("speed", 0.5D);
			DummyData legendaryDat4 = new DummyData("fear", 1);
			DummyData legendaryDat5 = new DummyData("crit", 0.45D);
			DummyData legendaryDat6 = new DummyData("durability", 0.35D);
			DummyData legendaryDat7 = new DummyData("swift", 2);
			DummyData legendaryDat8 = new DummyData("poison", 0.60D);
			DummyData legendaryDat9 = new DummyData("bind", 0.4);
			DataStorage.addDataToString(legendaryDat1);
			DataStorage.addDataToString(legendaryDat2);
			DataStorage.addDataToString(legendaryDat3);
			DataStorage.addDataToString(legendaryDat4);
			DataStorage.addDataToString(legendaryDat5);
			DataStorage.addDataToString(legendaryDat6);
			DataStorage.addDataToString(legendaryDat7);
			DataStorage.addDataToString(legendaryDat8);
			DataStorage.addDataToString(legendaryDat9);
			String legendaryStr = DataStorage.getDataString();
			legendaryCat.put("dataArray", new Property("dataArray", legendaryStr, Type.STRING));
		}
	}




}
