package com.gamingb3ast.blacksmithTweaks.configs;

import java.io.File;
import java.util.Set;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

import com.gamingb3ast.blacksmithTweaks.BT_Effect;
import com.gamingb3ast.blacksmithTweaks.BT_Mod;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.EnumRarityColor;
import DummyCore.Utils.Notifier;

public class BT_EffectsConfig extends Configuration {

    private static BT_EffectsConfig INSTANCE = null;
    private static int buffsCount = 0;

    public BT_EffectsConfig(File configFile) {
        super(configFile);
        this.load();
        if (this.getCategoryNames()
            .isEmpty()) {
            registerEffects(configFile);
        }
        loadCFG();
        this.save();
    }

    public static void initialize(File configFile) {
        if (INSTANCE == null) {
            INSTANCE = new BT_EffectsConfig(configFile);
        } else throw new IllegalStateException("Cannot initialize Blacksmith Tweaks config twice!");
    }

    public static BT_EffectsConfig instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Instance of Blacksmith Tweaks Config requested before initialization");
        }
        return INSTANCE;
    }

    public void loadCFG() {

        ConfigCategory help = this.getCategory("Help");
        help.setComment(
            "Using this .cfg file you can add your own effects, which will be applied to tools. \n"
                + "Adding your own effect is very simple. Below you can see the example of how it's done. You need to create a custom category, using # chars, then write the code effect name(make sure your name is unique, otherwise your effect will most likely be ignored).\n"
                + "Then you need to write this name again, this time without # chars, and add {} after it.\n"
                + "Now, there are 3 fields you need to write in this {}.\n"
                + "The first one is 'name'. This says, what name will be actually shown in-game(the tool prefix).\n"
                + "The second one is 'color'. You should put one hex number there. You can choose from this number set: 8,f,a,2,9,d,e,6,b,3,c,4. These can be used to represent the rarity of your effect. You can learn more about rarities in DummyCore code, in EnumRarityColor file.\n"
                + "The third one is 'weight'. This one is optional and can be used to make the effect appear more frequently or infrequently. Only accepts integers, negative numbers are switched to positive. This is just a weighted average system. If not specified will use '1'.\n"
                + "The last one is 'dataArray'. This represents the effects, that will be applied to your buff. There are 9 effects by now - 'damage','speed','durability', 'swift', 'slow', 'lifesteal', 'fear', 'poison' and 'crit'.\n"
                + "To write this data you need to follow the simple rules: after : put ||, then put an actual name of one of the 10 possible effects. Then put : again, and after that write your value. It can be below 0, should never be an integer unless specified\n"
                + "This value is percentage-based, and it scales, as 1 = 100%, and 0.25 = 25%. Some effects only except integer values, such as 'swift', 'fear', and 'slow', THESE ARE THE ONLY EFFECTS THAT ACCEPT INTEGER VALUES, PLEASE GIVE THE CORRECT TYPE OF NUMBER TO THE CORRECT EFFECT. \n"
                + "If you want to add more than one effect, just put || after the value you have last written, and start writing another data string. But remember, that || represents the beginning of the new datastring, so something like |||| will most likely lead to crash.\n");

        ConfigCategory exampleCat = this.getCategory("BT:Effect:exampleEffect");
        exampleCat.setComment("This is an example effect, any effect using this identifier will not be loaded");
        exampleCat.put("name", new Property("name", "Example", Type.STRING));
        exampleCat.put("color", new Property("color", "8", Type.STRING));
        exampleCat.put("weight", new Property("weight", "1", Type.INTEGER));
        DummyData durDat = new DummyData("durability", 1F);
        DummyData slowDat = new DummyData("slow", 0.1F);
        DataStorage.addDataToString(durDat);
        DataStorage.addDataToString(slowDat);
        String str = DataStorage.getDataString();
        exampleCat.put("dataArray", new Property("dataArray", str, Type.STRING));

        // Create data
        Set<String> s = this.getCategoryNames();
        for (int i = 0; i < s.size() - 1; ++i) {
            ConfigCategory cat = this.getCategory((String) s.toArray()[i]);
            String codeName = cat.getQualifiedName();
            if (codeName.equals("BT:Effect:exampleEffect")) break;
            if (cat.containsKey("name") && cat.containsKey("color") && cat.containsKey("dataArray")) {
                String name = cat.get("name")
                    .getString();
                String hex = cat.get("color")
                    .getString();
                String data = cat.get("dataArray")
                    .getString();
                DummyData[] dat = DataStorage.parseData(data);
                int weight = 1;
                if (cat.containsKey("weight")) weight = Math.abs(
                    cat.get("weight")
                        .getInt());
                new BT_Effect(codeName, name, EnumRarityColor.getColorByHex(hex), weight, dat).registerEffect();
                Notifier.notifyCustomMod(
                    "Blacksmith Tweaks",
                    "Adding a new effect with name " + name
                        + ", rarity "
                        + EnumRarityColor.getColorByHex(hex)
                            .getName()
                        + ", weight "
                        + weight
                        + " and data "
                        + data);
                ++buffsCount;
            }
        }

        if (buffsCount != 0) {
            Notifier.notifyCustomMod("Blacksmith Tweaks", "Loaded " + buffsCount + " effect(s).");
        } else throw new IllegalStateException(
            "[Blacksmith Tweaks] No buffs were loaded, THIS IS EXPECTED FOR FIRST LAUNCHES OF THE CONFIG!! PLEASE RESTART THE GAME TO FIX THIS ISSUE!!");

    }

    /**
     * Will recreate the default config file with all the normal effects
     */
    // TODO: Come on dude, you can do better, remake the register effect method and have this shit not be so damn ugly
    public void registerEffects(File configFile) {
        if (BT_Mod.effectConfigExists) {
            ConfigCategory ea0Cat = this.getCategory("BT:Effect:Durable");
            ea0Cat.put("name", new Property("name", "LANG", Type.STRING));
            ea0Cat.put("color", new Property("color", "f", Type.STRING));
            ea0Cat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData ea0Data = new DummyData("durability", .15F);
            DataStorage.addDataToString(ea0Data);
            String ea0Str = DataStorage.getDataString();
            ea0Cat.put("dataArray", new Property("dataArray", ea0Str, Type.STRING));

            ConfigCategory eaaCat = this.getCategory("BT:Effect:Damaged");
            eaaCat.put("name", new Property("name", "LANG", Type.STRING));
            eaaCat.put("color", new Property("color", "8", Type.STRING));
            eaaCat.put("weight", new Property("weight", "10", Type.INTEGER));
            DummyData eaaData = new DummyData("damage", -0.2F);
            DataStorage.addDataToString(eaaData);
            String eaaStr = DataStorage.getDataString();
            eaaCat.put("dataArray", new Property("dataArray", eaaStr, Type.STRING));

            ConfigCategory eabCat = this.getCategory("BT:Effect:Dull");
            eabCat.put("name", new Property("name", "LANG", Type.STRING));
            eabCat.put("color", new Property("color", "8", Type.STRING));
            eabCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eabData = new DummyData("damage", -0.20F);
            DataStorage.addDataToString(eabData);
            String eabStr = DataStorage.getDataString();
            eabCat.put("dataArray", new Property("dataArray", eabStr, Type.STRING));

            ConfigCategory eacCat = this.getCategory("BT:Effect:Sluggish");
            eacCat.put("name", new Property("name", "LANG", Type.STRING));
            eacCat.put("color", new Property("color", "8", Type.STRING));
            eacCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eacData = new DummyData("speed", -0.46F);
            DummyData eacSlow = new DummyData("slow", -0.25F);
            DataStorage.addDataToString(eacData);
            DataStorage.addDataToString(eacSlow);
            String eacStr = DataStorage.getDataString();
            eacCat.put("dataArray", new Property("dataArray", eacStr, Type.STRING));

            ConfigCategory eadCat = this.getCategory("BT:Effect:Slow");
            eadCat.put("name", new Property("name", "LANG", Type.STRING));
            eadCat.put("color", new Property("color", "8", Type.STRING));
            eadCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eadData = new DummyData("speed", -0.3F);
            DataStorage.addDataToString(eadData);
            String eadStr = DataStorage.getDataString();
            eadCat.put("dataArray", new Property("dataArray", eadStr, Type.STRING));

            ConfigCategory eaeCat = this.getCategory("BT:Effect:Lazy");
            eaeCat.put("name", new Property("name", "LANG", Type.STRING));
            eaeCat.put("color", new Property("color", "8", Type.STRING));
            eaeCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eaeData = new DummyData("speed", -0.16F);
            DataStorage.addDataToString(eaeData);
            String eaeStr = DataStorage.getDataString();
            eaeCat.put("dataArray", new Property("dataArray", eaeStr, Type.STRING));

            ConfigCategory eafCat = this.getCategory("BT:Effect:Cracky");
            eafCat.put("name", new Property("name", "LANG", Type.STRING));
            eafCat.put("color", new Property("color", "8", Type.STRING));
            eafCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eafData = new DummyData("durability", -0.2F);
            DataStorage.addDataToString(eafData);
            String eafStr = DataStorage.getDataString();
            eafCat.put("dataArray", new Property("dataArray", eafStr, Type.STRING));

            ConfigCategory eagCat = this.getCategory("BT:Effect:Broken");
            eagCat.put("name", new Property("name", "LANG", Type.STRING));
            eagCat.put("color", new Property("color", "8", Type.STRING));
            eagCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eagData = new DummyData("damage", -0.4F);
            DummyData eagSpeed = new DummyData("speed", -0.4F);
            DataStorage.addDataToString(eagData);
            DataStorage.addDataToString(eagSpeed);
            String eagStr = DataStorage.getDataString();
            eagCat.put("dataArray", new Property("dataArray", eagStr, Type.STRING));

            ConfigCategory eahCat = this.getCategory("BT:Effect:Annoying");
            eahCat.put("name", new Property("name", "LANG", Type.STRING));
            eahCat.put("color", new Property("color", "8", Type.STRING));
            eahCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eahData = new DummyData("damage", -0.2F);
            DummyData eahSpeed = new DummyData("speed", -0.3F);
            DataStorage.addDataToString(eahData);
            DataStorage.addDataToString(eahSpeed);
            String eahStr = DataStorage.getDataString();
            eahCat.put("dataArray", new Property("dataArray", eahStr, Type.STRING));

            ConfigCategory eaiCat = this.getCategory("BT:Effect:Shoddy");
            eaiCat.put("name", new Property("name", "LANG", Type.STRING));
            eaiCat.put("color", new Property("color", "8", Type.STRING));
            eaiCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eaiData = new DummyData("damage", -0.2F);
            DummyData eaiSpeed = new DummyData("speed", -0.3F);
            DataStorage.addDataToString(eaiData);
            DataStorage.addDataToString(eaiSpeed);
            String eaiStr = DataStorage.getDataString();
            eaiCat.put("dataArray", new Property("dataArray", eaiStr, Type.STRING));

            ConfigCategory eajCat = this.getCategory("BT:Effect:Terrible");
            eajCat.put("name", new Property("name", "LANG", Type.STRING));
            eajCat.put("color", new Property("color", "8", Type.STRING));
            eajCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eajData = new DummyData("damage", -0.2F);
            DummyData eajSpeed = new DummyData("speed", -0.15F);
            DummyData eajDurability = new DummyData("durability", -0.08F);
            DataStorage.addDataToString(eajData);
            DataStorage.addDataToString(eajSpeed);
            DataStorage.addDataToString(eajDurability);
            String eajStr = DataStorage.getDataString();
            eajCat.put("dataArray", new Property("dataArray", eajStr, Type.STRING));

            ConfigCategory eakCat = this.getCategory("BT:Effect:Unhappy");
            eakCat.put("name", new Property("name", "LANG", Type.STRING));
            eakCat.put("color", new Property("color", "8", Type.STRING));
            eakCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eakData = new DummyData("damage", -0.15F);
            DummyData eakSpeed = new DummyData("speed", -0.2F);
            DummyData eakDurability = new DummyData("durability", -0.1F);
            DataStorage.addDataToString(eakData);
            DataStorage.addDataToString(eakSpeed);
            DataStorage.addDataToString(eakDurability);
            String eakStr = DataStorage.getDataString();
            eakCat.put("dataArray", new Property("dataArray", eakStr, Type.STRING));

            ConfigCategory ealCat = this.getCategory("BT:Effect:Heavy");
            ealCat.put("name", new Property("name", "LANG", Type.STRING));
            ealCat.put("color", new Property("color", "f", Type.STRING));
            ealCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData ealData = new DummyData("damage", 0.15F);
            DummyData ealSpeed = new DummyData("speed", -0.3F);
            DummyData ealSlow = new DummyData("slow", -0.3F);
            DataStorage.addDataToString(ealData);
            DataStorage.addDataToString(ealSpeed);
            DataStorage.addDataToString(ealSlow);
            String ealStr = DataStorage.getDataString();
            ealCat.put("dataArray", new Property("dataArray", ealStr, Type.STRING));

            ConfigCategory eamCat = this.getCategory("BT:Effect:Light");
            eamCat.put("name", new Property("name", "LANG", Type.STRING));
            eamCat.put("color", new Property("color", "f", Type.STRING));
            eamCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eamData = new DummyData("damage", -0.15F);
            DummyData eamSpeed = new DummyData("speed", 0.3F);
            DummyData eamSwift = new DummyData("swift", 0.10F);
            DataStorage.addDataToString(eamData);
            DataStorage.addDataToString(eamSpeed);
            DataStorage.addDataToString(eamSwift);
            String eamStr = DataStorage.getDataString();
            eamCat.put("dataArray", new Property("dataArray", eamStr, Type.STRING));

            ConfigCategory eanCat = this.getCategory("BT:Effect:Ruthless");
            eanCat.put("name", new Property("name", "LANG", Type.STRING));
            eanCat.put("color", new Property("color", "f", Type.STRING));
            eanCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eanData = new DummyData("damage", 0.2F);
            DummyData eanSpeed = new DummyData("speed", -0.15F);
            DummyData eanFear = new DummyData("fear", 1);
            DataStorage.addDataToString(eanData);
            DataStorage.addDataToString(eanSpeed);
            DataStorage.addDataToString(eanFear);
            String eanStr = DataStorage.getDataString();
            eanCat.put("dataArray", new Property("dataArray", eanStr, Type.STRING));

            ConfigCategory eaoCat = this.getCategory("BT:Effect:Shameful");
            eaoCat.put("name", new Property("name", "LANG", Type.STRING));
            eaoCat.put("color", new Property("color", "f", Type.STRING));
            eaoCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eaoData = new DummyData("damage", -0.2F);
            DummyData eaoSpeed = new DummyData("speed", -0.4F);
            DummyData eaoDurability = new DummyData("durability", 0.15F);
            DataStorage.addDataToString(eaoData);
            DataStorage.addDataToString(eaoSpeed);
            DataStorage.addDataToString(eaoDurability);
            String eaoStr = DataStorage.getDataString();
            eaoCat.put("dataArray", new Property("dataArray", eaoStr, Type.STRING));

            ConfigCategory eapCat = this.getCategory("BT:Effect:Bulky");
            eapCat.put("name", new Property("name", "LANG", Type.STRING));
            eapCat.put("color", new Property("color", "a", Type.STRING));
            eapCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eapData = new DummyData("damage", 0.15F);
            DummyData eapSpeed = new DummyData("speed", -0.2F);
            DummyData eapDurability = new DummyData("durability", 0.1F);
            DummyData eapSlow = new DummyData("slow", -0.3F);
            DummyData eapBind = new DummyData("bind", 0.4);
            DataStorage.addDataToString(eapData);
            DataStorage.addDataToString(eapSpeed);
            DataStorage.addDataToString(eapDurability);
            DataStorage.addDataToString(eapSlow);
            DataStorage.addDataToString(eapBind);
            String eapStr = DataStorage.getDataString();
            eapCat.put("dataArray", new Property("dataArray", eapStr, Type.STRING));

            ConfigCategory eaqCat = this.getCategory("BT:Effect:Nasty");
            eaqCat.put("name", new Property("name", "LANG", Type.STRING));
            eaqCat.put("color", new Property("color", "a", Type.STRING));
            eaqCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eaqData = new DummyData("damage", 0.05F);
            DummyData eaqSpeed = new DummyData("speed", 0.1F);
            DummyData eaqDurability = new DummyData("durability", 0.1F);
            DataStorage.addDataToString(eaqData);
            DataStorage.addDataToString(eaqSpeed);
            DataStorage.addDataToString(eaqDurability);
            String eaqStr = DataStorage.getDataString();
            eaqCat.put("dataArray", new Property("dataArray", eaqStr, Type.STRING));

            ConfigCategory earCat = this.getCategory("BT:Effect:Sharp");
            earCat.put("name", new Property("name", "LANG", Type.STRING));
            earCat.put("color", new Property("color", "2", Type.STRING));
            earCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData earData = new DummyData("damage", 0.2F);
            DataStorage.addDataToString(earData);
            String earStr = DataStorage.getDataString();
            earCat.put("dataArray", new Property("dataArray", earStr, Type.STRING));

            ConfigCategory easCat = this.getCategory("BT:Effect:Pointy");
            easCat.put("name", new Property("name", "LANG", Type.STRING));
            easCat.put("color", new Property("color", "2", Type.STRING));
            easCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData easData = new DummyData("damage", 0.1F);
            DataStorage.addDataToString(easData);
            String easStr = DataStorage.getDataString();
            easCat.put("dataArray", new Property("dataArray", easStr, Type.STRING));

            ConfigCategory eatCat = this.getCategory("BT:Effect:Hurtful");
            eatCat.put("name", new Property("name", "LANG", Type.STRING));
            eatCat.put("color", new Property("color", "2", Type.STRING));
            eatCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eatData = new DummyData("damage", 0.15F);
            DataStorage.addDataToString(eatData);
            String eatStr = DataStorage.getDataString();
            eatCat.put("dataArray", new Property("dataArray", eatStr, Type.STRING));

            ConfigCategory eauCat = this.getCategory("BT:Effect:Strong");
            eauCat.put("name", new Property("name", "LANG", Type.STRING));
            eauCat.put("color", new Property("color", "2", Type.STRING));
            eauCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eauData = new DummyData("durability", 0.15F);
            DataStorage.addDataToString(eauData);
            String eauStr = DataStorage.getDataString();
            eauCat.put("dataArray", new Property("dataArray", eauStr, Type.STRING));

            ConfigCategory eavCat = this.getCategory("BT:Effect:Forceful");
            eavCat.put("name", new Property("name", "LANG", Type.STRING));
            eavCat.put("color", new Property("color", "2", Type.STRING));
            eavCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eavData = new DummyData("durability", 0.15F);
            DummyData eavBind = new DummyData("bind", 0.4);
            DataStorage.addDataToString(eavData);
            DataStorage.addDataToString(eavBind);
            String eavStr = DataStorage.getDataString();
            eavCat.put("dataArray", new Property("dataArray", eavStr, Type.STRING));

            ConfigCategory eawCat = this.getCategory("BT:Effect:Quick");
            eawCat.put("name", new Property("name", "LANG", Type.STRING));
            eawCat.put("color", new Property("color", "2", Type.STRING));
            eawCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eawData = new DummyData("speed", 0.2F);
            DummyData eawSwift = new DummyData("swift", 0.15F);
            DummyData eawDamage = new DummyData("damage", -0.1);
            DataStorage.addDataToString(eawData);
            DataStorage.addDataToString(eawSwift);
            DataStorage.addDataToString(eawDamage);
            String eawStr = DataStorage.getDataString();
            eawCat.put("dataArray", new Property("dataArray", eawStr, Type.STRING));

            ConfigCategory eaxCat = this.getCategory("BT:Effect:Nimble");
            eaxCat.put("name", new Property("name", "LANG", Type.STRING));
            eaxCat.put("color", new Property("color", "2", Type.STRING));
            eaxCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eaxData = new DummyData("speed", 0.15F);
            DummyData eaxSwift = new DummyData("swift", 0.20F);
            DummyData eaxDamage = new DummyData("damage", -0.05);
            DataStorage.addDataToString(eaxData);
            DataStorage.addDataToString(eaxSwift);
            DataStorage.addDataToString(eaxDamage);
            String eaxStr = DataStorage.getDataString();
            eaxCat.put("dataArray", new Property("dataArray", eaxStr, Type.STRING));

            ConfigCategory eayCat = this.getCategory("BT:Effect:Zealous");
            eayCat.put("name", new Property("name", "LANG", Type.STRING));
            eayCat.put("color", new Property("color", "2", Type.STRING));
            eayCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eayCrit = new DummyData("crit", 0.25F);
            DummyData eayFear = new DummyData("fear", 1);
            DummyData eayLifesteal = new DummyData("lifesteal", 0.30);
            DataStorage.addDataToString(eayCrit);
            DataStorage.addDataToString(eayFear);
            DataStorage.addDataToString(eayLifesteal);
            String eayStr = DataStorage.getDataString();
            eayCat.put("dataArray", new Property("dataArray", eayStr, Type.STRING));

            ConfigCategory eazCat = this.getCategory("BT:Effect:Keen");
            eazCat.put("name", new Property("name", "LANG", Type.STRING));
            eazCat.put("color", new Property("color", "2", Type.STRING));
            eazCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData eazCrit = new DummyData("crit", 0.30F);
            DataStorage.addDataToString(eazCrit);
            String eazStr = DataStorage.getDataString();
            eazCat.put("dataArray", new Property("dataArray", eazStr, Type.STRING));

            ConfigCategory massiveCat = this.getCategory("BT:Effect:Massive");
            massiveCat.put("name", new Property("name", "LANG", Type.STRING));
            massiveCat.put("color", new Property("color", "a", Type.STRING));
            massiveCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData massiveDat1 = new DummyData("speed", -0.15F);
            DummyData massiveDat2 = new DummyData("bind", 0.6);
            DataStorage.addDataToString(massiveDat1);
            DataStorage.addDataToString(massiveDat2);
            String massiveStr = DataStorage.getDataString();
            massiveCat.put("dataArray", new Property("dataArray", massiveStr, Type.STRING));

            ConfigCategory largeCat = this.getCategory("BT:Effect:Large");
            largeCat.put("name", new Property("name", "LANG", Type.STRING));
            largeCat.put("color", new Property("color", "a", Type.STRING));
            largeCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData largeDat1 = new DummyData("speed", -0.1F);
            DummyData largeDat2 = new DummyData("bind", 0.5);
            DataStorage.addDataToString(largeDat1);
            DataStorage.addDataToString(largeDat2);
            String largeStr = DataStorage.getDataString();
            largeCat.put("dataArray", new Property("dataArray", largeStr, Type.STRING));

            ConfigCategory demonicCat = this.getCategory("BT:Effect:Demonic");
            demonicCat.put("name", new Property("name", "LANG", Type.STRING));
            demonicCat.put("color", new Property("color", "9", Type.STRING));
            demonicCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData demonicDat1 = new DummyData("speed", 0.4F);
            DummyData demonicDat2 = new DummyData("damage", 0.15F);
            DummyData demonicDat3 = new DummyData("crit", 0.15F);
            DummyData demonicDat4 = new DummyData("lifesteal", 0.40F);
            DummyData demonicDat5 = new DummyData("fear", 1);
            DummyData demonicDat6 = new DummyData("swift", 0.25F);
            DataStorage.addDataToString(demonicDat1);
            DataStorage.addDataToString(demonicDat2);
            DataStorage.addDataToString(demonicDat3);
            DataStorage.addDataToString(demonicDat4);
            DataStorage.addDataToString(demonicDat5);
            DataStorage.addDataToString(demonicDat6);
            String demonicStr = DataStorage.getDataString();
            demonicCat.put("dataArray", new Property("dataArray", demonicStr, Type.STRING));

            ConfigCategory agileCat = this.getCategory("BT:Effect:Agile");
            agileCat.put("name", new Property("name", "LANG", Type.STRING));
            agileCat.put("color", new Property("color", "9", Type.STRING));
            agileCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData agileDat1 = new DummyData("speed", 0.3F);
            DummyData agileDat2 = new DummyData("crit", 0.15F);
            DummyData agileDat3 = new DummyData("swift", 0.18F);
            DataStorage.addDataToString(agileDat1);
            DataStorage.addDataToString(agileDat2);
            DataStorage.addDataToString(agileDat3);
            String agileStr = DataStorage.getDataString();
            agileCat.put("dataArray", new Property("dataArray", agileStr, Type.STRING));

            ConfigCategory deadlyCat = this.getCategory("BT:Effect:Deadly");
            deadlyCat.put("name", new Property("name", "LANG", Type.STRING));
            deadlyCat.put("color", new Property("color", "9", Type.STRING));
            deadlyCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData deadlyDat1 = new DummyData("damage", 0.1F);
            DummyData deadlyDat2 = new DummyData("speed", 0.16F);
            DataStorage.addDataToString(deadlyDat1);
            DataStorage.addDataToString(deadlyDat2);
            String deadlyStr = DataStorage.getDataString();
            deadlyCat.put("dataArray", new Property("dataArray", deadlyStr, Type.STRING));

            ConfigCategory unpleasantCat = this.getCategory("BT:Effect:Unpleasant");
            unpleasantCat.put("name", new Property("name", "LANG", Type.STRING));
            unpleasantCat.put("color", new Property("color", "9", Type.STRING));
            unpleasantCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData unpleasantDat1 = new DummyData("damage", 0.05F);
            DummyData unpleasantDat2 = new DummyData("durability", 0.15F);
            DummyData unpleasantDat3 = new DummyData("fear", 1);
            DataStorage.addDataToString(unpleasantDat1);
            DataStorage.addDataToString(unpleasantDat2);
            DataStorage.addDataToString(unpleasantDat3);
            String unpleasantStr = DataStorage.getDataString();
            unpleasantCat.put("dataArray", new Property("dataArray", unpleasantStr, Type.STRING));

            ConfigCategory rapidCat = this.getCategory("BT:Effect:Rapid");
            rapidCat.put("name", new Property("name", "LANG", Type.STRING));
            rapidCat.put("color", new Property("color", "9", Type.STRING));
            rapidCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData rapidDat1 = new DummyData("speed", 0.25F);
            DummyData rapidDat2 = new DummyData("durability", 0.10F);
            DataStorage.addDataToString(rapidDat1);
            DataStorage.addDataToString(rapidDat2);
            String rapidStr = DataStorage.getDataString();
            rapidCat.put("dataArray", new Property("dataArray", rapidStr, Type.STRING));

            ConfigCategory godlyCat = this.getCategory("BT:Effect:Godly");
            godlyCat.put("name", new Property("name", "LANG", Type.STRING));
            godlyCat.put("color", new Property("color", "d", Type.STRING));
            godlyCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData godlyDat1 = new DummyData("damage", 0.15F);
            DummyData godlyDat2 = new DummyData("crit", 0.25F);
            DummyData godlyDat3 = new DummyData("lifesteal", 0.50F);
            DummyData godlyDat4 = new DummyData("durability", 0.15F);
            DummyData godlyDat5 = new DummyData("swift", 0.5F);
            DataStorage.addDataToString(godlyDat1);
            DataStorage.addDataToString(godlyDat2);
            DataStorage.addDataToString(godlyDat3);
            DataStorage.addDataToString(godlyDat4);
            DataStorage.addDataToString(godlyDat5);
            String godlyStr = DataStorage.getDataString();
            godlyCat.put("dataArray", new Property("dataArray", godlyStr, Type.STRING));

            ConfigCategory rustyCat = this.getCategory("BT:Effect:Rusty");
            rustyCat.put("name", new Property("name", "LANG", Type.STRING));
            rustyCat.put("color", new Property("color", "d", Type.STRING));
            rustyCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData rustyDat1 = new DummyData("damage", -0.08F);
            DummyData rustyDat2 = new DummyData("durability", -0.1F);
            DummyData rustyDat3 = new DummyData("poison", 0.60F);
            DataStorage.addDataToString(rustyDat1);
            DataStorage.addDataToString(rustyDat2);
            DataStorage.addDataToString(rustyDat3);
            String rustyStr = DataStorage.getDataString();
            rustyCat.put("dataArray", new Property("dataArray", rustyStr, Type.STRING));

            ConfigCategory superiorCat = this.getCategory("BT:Effect:Superior");
            superiorCat.put("name", new Property("name", "LANG", Type.STRING));
            superiorCat.put("color", new Property("color", "d", Type.STRING));
            superiorCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData superiorDat1 = new DummyData("damage", 0.15F);
            DummyData superiorDat2 = new DummyData("crit", 0.3F);
            DummyData superiorDat3 = new DummyData("durability", 0.15F);
            DummyData superiorDat4 = new DummyData("swift", 0.45F);
            DataStorage.addDataToString(superiorDat1);
            DataStorage.addDataToString(superiorDat2);
            DataStorage.addDataToString(superiorDat3);
            DataStorage.addDataToString(superiorDat4);
            String superiorStr = DataStorage.getDataString();
            superiorCat.put("dataArray", new Property("dataArray", superiorStr, Type.STRING));

            ConfigCategory dangerousCat = this.getCategory("BT:Effect:Dangerous");
            dangerousCat.put("name", new Property("name", "LANG", Type.STRING));
            dangerousCat.put("color", new Property("color", "d", Type.STRING));
            dangerousCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData dangerousDat1 = new DummyData("damage", 0.20F);
            DummyData dangerousDat2 = new DummyData("lifesteal", 0.60F);
            DummyData dangerousDat3 = new DummyData("crit", 0.22F);
            DummyData dangerousDat4 = new DummyData("fear", 1);
            DummyData dangerousDat5 = new DummyData("speed", 0.3F);
            DummyData dangerousDat6 = new DummyData("swift", 0.35F);
            DummyData dangerousDat7 = new DummyData("poison", 0.60F);
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
            savageCat.put("name", new Property("name", "LANG", Type.STRING));
            savageCat.put("color", new Property("color", "d", Type.STRING));
            savageCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData savageDat1 = new DummyData("damage", 0.15F);
            DummyData savageDat2 = new DummyData("speed", 0.42F);
            DummyData savageDat3 = new DummyData("durability", 0.20F);
            DummyData savageDat4 = new DummyData("swift", 0.60F);
            DummyData savageDat5 = new DummyData("poison", 0.60F);
            DataStorage.addDataToString(savageDat1);
            DataStorage.addDataToString(savageDat2);
            DataStorage.addDataToString(savageDat3);
            DataStorage.addDataToString(savageDat4);
            DataStorage.addDataToString(savageDat5);
            String savageStr = DataStorage.getDataString();
            savageCat.put("dataArray", new Property("dataArray", savageStr, Type.STRING));

            ConfigCategory murderousCat = this.getCategory("BT:Effect:Murderous");
            murderousCat.put("name", new Property("name", "LANG", Type.STRING));
            murderousCat.put("color", new Property("color", "d", Type.STRING));
            murderousCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData murderousDat1 = new DummyData("damage", 0.17F);
            DummyData murderousDat2 = new DummyData("crit", 0.35F);
            DummyData murderousDat3 = new DummyData("fear", 1);
            DummyData murderousDat4 = new DummyData("speed", 0.18F);
            DummyData murderousDat5 = new DummyData("swift", 0.65F);
            DummyData murderousDat6 = new DummyData("poison", 0.60F);
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
            legendaryCat.put("name", new Property("name", "LANG", Type.STRING));
            legendaryCat.put("color", new Property("color", "6", Type.STRING));
            legendaryCat.put("weight", new Property("weight", "1", Type.INTEGER));
            DummyData legendaryDat1 = new DummyData("damage", 0.25F);
            DummyData legendaryDat2 = new DummyData("lifesteal", 0.70F);
            DummyData legendaryDat3 = new DummyData("speed", 0.5F);
            DummyData legendaryDat4 = new DummyData("fear", 1);
            DummyData legendaryDat5 = new DummyData("crit", 0.45F);
            DummyData legendaryDat6 = new DummyData("durability", 0.35F);
            DummyData legendaryDat7 = new DummyData("swift", 1.0F);
            DummyData legendaryDat8 = new DummyData("poison", 0.60F);
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
