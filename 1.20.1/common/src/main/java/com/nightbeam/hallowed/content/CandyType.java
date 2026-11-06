package com.nightbeam.hallowed.content;

import net.minecraft.world.item.Rarity;

/** The eight candies: food values, how long they take to eat and how rare they are. Effects live in {@code CandyItem}. */
public enum CandyType {
    CANDY_CORN("candy_corn", 1, 0.1f, 0.6f, Rarity.COMMON, 12),
    CARAMEL_APPLE("caramel_apple", 6, 0.6f, 1.6f, Rarity.COMMON, 6),
    GHOST_GUMMY("ghost_gummy", 2, 0.2f, 0.8f, Rarity.COMMON, 8),
    PUMPKIN_TRUFFLE("pumpkin_truffle", 4, 0.8f, 1.2f, Rarity.COMMON, 8),
    WITCH_LOLLIPOP("witch_lollipop", 2, 0.2f, 1.6f, Rarity.UNCOMMON, 5),
    BAT_WING_LICORICE("bat_wing_licorice", 2, 0.3f, 1.0f, Rarity.UNCOMMON, 5),
    EYEBALL_JAWBREAKER("eyeball_jawbreaker", 3, 0.4f, 3.2f, Rarity.UNCOMMON, 4),
    SOUL_TOFFEE("soul_toffee", 4, 0.5f, 1.2f, Rarity.RARE, 2);

    public final String id;
    public final int nutrition;
    public final float saturation;
    public final float eatSeconds;
    public final Rarity rarity;
    /** Relative weight when a treat or a drop picks a candy at random. */
    public final int weight;

    CandyType(String id, int nutrition, float saturation, float eatSeconds, Rarity rarity, int weight) {
        this.id = id;
        this.nutrition = nutrition;
        this.saturation = saturation;
        this.eatSeconds = eatSeconds;
        this.rarity = rarity;
        this.weight = weight;
    }
}
