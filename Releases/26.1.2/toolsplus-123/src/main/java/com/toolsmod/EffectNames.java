package com.toolsmod;

import java.util.LinkedHashMap;
import java.util.Map;

public class EffectNames {

    public static final Map<String, String> RU = new LinkedHashMap<>();
    public static final Map<String, String> EN = new LinkedHashMap<>();

    static {
        RU.put("скорость",             "minecraft:speed");
        RU.put("медлительность",       "minecraft:slowness");
        RU.put("спешка",               "minecraft:haste");
        RU.put("усталость",            "minecraft:mining_fatigue");
        RU.put("сила",                 "minecraft:strength");
        RU.put("лечение",              "minecraft:instant_health");
        RU.put("вред",                 "minecraft:instant_damage");
        RU.put("прыжок",               "minecraft:jump_boost");
        RU.put("тошнота",              "minecraft:nausea");
        RU.put("регенерация",          "minecraft:regeneration");
        RU.put("стойкость",            "minecraft:resistance");
        RU.put("огнестойкость",        "minecraft:fire_resistance");
        RU.put("дыхание_под_водой",    "minecraft:water_breathing");
        RU.put("невидимость",          "minecraft:invisibility");
        RU.put("слепота",              "minecraft:blindness");
        RU.put("ночное_зрение",        "minecraft:night_vision");
        RU.put("голод",                "minecraft:hunger");
        RU.put("слабость",             "minecraft:weakness");
        RU.put("яд",                   "minecraft:poison");
        RU.put("иссушение",            "minecraft:wither");
        RU.put("запас_здоровья",       "minecraft:health_boost");
        RU.put("поглощение",           "minecraft:absorption");
        RU.put("насыщение",            "minecraft:saturation");
        RU.put("свечение",             "minecraft:glowing");
        RU.put("левитация",            "minecraft:levitation");
        RU.put("удача_эффект",         "minecraft:luck");
        RU.put("неудача",              "minecraft:unluck");
        RU.put("медленное_падение",    "minecraft:slow_falling");
        RU.put("слава",                "minecraft:hero_of_the_village");
        RU.put("тьма",                 "minecraft:darkness");
        RU.put("испытание",            "minecraft:trial_omen");
        RU.put("налёт",                "minecraft:raid_omen");
        RU.put("ветер",                "minecraft:wind_charged");
        RU.put("паутина",              "minecraft:weaving");
        RU.put("слизь",                "minecraft:oozing");
        RU.put("заражение",            "minecraft:infested");

        EN.put("speed",                "minecraft:speed");
        EN.put("slowness",             "minecraft:slowness");
        EN.put("haste",                "minecraft:haste");
        EN.put("mining_fatigue",       "minecraft:mining_fatigue");
        EN.put("strength",             "minecraft:strength");
        EN.put("instant_health",       "minecraft:instant_health");
        EN.put("instant_damage",       "minecraft:instant_damage");
        EN.put("jump_boost",           "minecraft:jump_boost");
        EN.put("nausea",               "minecraft:nausea");
        EN.put("regeneration",         "minecraft:regeneration");
        EN.put("resistance",           "minecraft:resistance");
        EN.put("fire_resistance",      "minecraft:fire_resistance");
        EN.put("water_breathing",      "minecraft:water_breathing");
        EN.put("invisibility",         "minecraft:invisibility");
        EN.put("blindness",            "minecraft:blindness");
        EN.put("night_vision",         "minecraft:night_vision");
        EN.put("hunger",               "minecraft:hunger");
        EN.put("weakness",             "minecraft:weakness");
        EN.put("poison",               "minecraft:poison");
        EN.put("wither",               "minecraft:wither");
        EN.put("health_boost",         "minecraft:health_boost");
        EN.put("absorption",           "minecraft:absorption");
        EN.put("saturation",           "minecraft:saturation");
        EN.put("glowing",              "minecraft:glowing");
        EN.put("levitation",           "minecraft:levitation");
        EN.put("luck",                 "minecraft:luck");
        EN.put("unluck",               "minecraft:unluck");
        EN.put("slow_falling",         "minecraft:slow_falling");
        EN.put("hero_of_the_village",  "minecraft:hero_of_the_village");
        EN.put("darkness",             "minecraft:darkness");
        EN.put("trial_omen",           "minecraft:trial_omen");
        EN.put("raid_omen",            "minecraft:raid_omen");
        EN.put("wind_charged",         "minecraft:wind_charged");
        EN.put("weaving",              "minecraft:weaving");
        EN.put("oozing",               "minecraft:oozing");
        EN.put("infested",             "minecraft:infested");
    }

    public static String resolve(String input) {
        String low = input.toLowerCase();
        if (RU.containsKey(low)) return RU.get(low);
        if (EN.containsKey(low)) return EN.get(low);
        return low.contains(":") ? low : "minecraft:" + low;
    }
}
