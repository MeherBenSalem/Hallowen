package com.nightbeam.hallowed.content;

/** The faces a pumpkin can be carved with. Shears cycle through them in this order. */
public enum Face {
    CLASSIC("classic"),
    GRIN("grin"),
    FANGS("fangs"),
    CAT("cat"),
    SCREAM("scream"),
    WINK("wink"),
    SKULL("skull"),
    BAT("bat");

    public static final int COUNT = values().length;

    public final String id;

    Face(String id) {
        this.id = id;
    }

    public static Face byIndex(int index) {
        return values()[Math.floorMod(index, COUNT)];
    }
}
