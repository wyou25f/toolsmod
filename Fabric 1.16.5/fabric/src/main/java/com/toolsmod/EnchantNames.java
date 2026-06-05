package com.toolsmod;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnchantNames {

    public static final Map<String, String> RU = new LinkedHashMap<>();
    public static final Map<String, String> EN = new LinkedHashMap<>();

    static {
        RU.put("острота",                 "minecraft:sharpness");
        RU.put("кара",                    "minecraft:smite");
        RU.put("гибель_членистоногих",    "minecraft:bane_of_arthropods");
        RU.put("отдача",                  "minecraft:knockback");
        RU.put("огненный_аспект",         "minecraft:fire_aspect");
        RU.put("мародёрство",             "minecraft:looting");
        RU.put("резня",                   "minecraft:sweeping_edge");
        RU.put("защита",                  "minecraft:protection");
        RU.put("огнезащита",              "minecraft:fire_protection");
        RU.put("защита_от_взрывов",       "minecraft:blast_protection");
        RU.put("защита_от_снарядов",      "minecraft:projectile_protection");
        RU.put("колючки",                 "minecraft:thorns");
        RU.put("мягкое_падение",          "minecraft:feather_falling");
        RU.put("дыхание",                 "minecraft:respiration");
        RU.put("подводник",               "minecraft:aqua_affinity");
        RU.put("поступь_душ",             "minecraft:soul_speed");
        RU.put("эффективность",           "minecraft:efficiency");
        RU.put("шёлковое_касание",        "minecraft:silk_touch");
        RU.put("удача",                   "minecraft:fortune");
        RU.put("сила",                    "minecraft:power");
        RU.put("отбрасывание",            "minecraft:punch");
        RU.put("пламя",                   "minecraft:flame");
        RU.put("бесконечность",           "minecraft:infinity");
        RU.put("удача_моря",              "minecraft:luck_of_the_sea");
        RU.put("приманка",                "minecraft:lure");
        RU.put("лояльность",              "minecraft:loyalty");
        RU.put("пронзание",               "minecraft:impaling");
        RU.put("рябь",                    "minecraft:riptide");
        RU.put("гроза",                   "minecraft:channeling");
        RU.put("многозарядность",         "minecraft:multishot");
        RU.put("быстрая_перезарядка",     "minecraft:quick_charge");
        RU.put("пробивание",              "minecraft:piercing");
        RU.put("нерушимость",             "minecraft:unbreaking");
        RU.put("починка",                 "minecraft:mending");
        RU.put("проклятие_привязанности", "minecraft:binding_curse");
        RU.put("проклятие_исчезновения",  "minecraft:vanishing_curse");

        EN.put("sharpness",               "minecraft:sharpness");
        EN.put("smite",                   "minecraft:smite");
        EN.put("bane_of_arthropods",      "minecraft:bane_of_arthropods");
        EN.put("knockback",               "minecraft:knockback");
        EN.put("fire_aspect",             "minecraft:fire_aspect");
        EN.put("looting",                 "minecraft:looting");
        EN.put("sweeping_edge",           "minecraft:sweeping_edge");
        EN.put("protection",              "minecraft:protection");
        EN.put("fire_protection",         "minecraft:fire_protection");
        EN.put("blast_protection",        "minecraft:blast_protection");
        EN.put("projectile_protection",   "minecraft:projectile_protection");
        EN.put("thorns",                  "minecraft:thorns");
        EN.put("feather_falling",         "minecraft:feather_falling");
        EN.put("respiration",             "minecraft:respiration");
        EN.put("aqua_affinity",           "minecraft:aqua_affinity");
        EN.put("soul_speed",              "minecraft:soul_speed");
        EN.put("efficiency",              "minecraft:efficiency");
        EN.put("silk_touch",              "minecraft:silk_touch");
        EN.put("fortune",                 "minecraft:fortune");
        EN.put("power",                   "minecraft:power");
        EN.put("punch",                   "minecraft:punch");
        EN.put("flame",                   "minecraft:flame");
        EN.put("infinity",                "minecraft:infinity");
        EN.put("luck_of_the_sea",         "minecraft:luck_of_the_sea");
        EN.put("lure",                    "minecraft:lure");
        EN.put("loyalty",                 "minecraft:loyalty");
        EN.put("impaling",                "minecraft:impaling");
        EN.put("riptide",                 "minecraft:riptide");
        EN.put("channeling",              "minecraft:channeling");
        EN.put("multishot",               "minecraft:multishot");
        EN.put("quick_charge",            "minecraft:quick_charge");
        EN.put("piercing",                "minecraft:piercing");
        EN.put("unbreaking",              "minecraft:unbreaking");
        EN.put("mending",                 "minecraft:mending");
        EN.put("binding_curse",           "minecraft:binding_curse");
        EN.put("vanishing_curse",         "minecraft:vanishing_curse");
    }

    public static String resolve(String input) {
        String low = input.toLowerCase();
        if (RU.containsKey(low)) return RU.get(low);
        if (EN.containsKey(low)) return EN.get(low);
        return low.contains(":") ? low : "minecraft:" + low;
    }
}
