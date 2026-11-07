package local.gameshark;

public enum Mode {
    LEGENDARIES("Legendaries", 0xF6BD38),
    SHINIES("Shinies", 0xFF69B4),
    BOSSES("Bosses", 0x4AA4FF),
    ULTRA_BEASTS("Ultra Beasts", 0xB747FF),
    SPECIAL_PALETTES("Special palettes", 0xCC34EB),
    QUEST_GIVERS("Quest givers", 0xD1C708),
    TRAINERS("Trainers", 0x3056E3),
    PLAYERS("Players", 0x888888),
    ULTRA_WORMHOLES("Ultra Wormholes", 0x820AD4),
    LEGENDARY_SUMMONS("Legendary summons", 0xA41536),
    MAX_RAID_DENS("Max Raid Dens", 0xFF2E96),
    POKE_LOOT("Poké Loot", 0xE08338),
    POKE_STOPS("Poké Stops", 0xD9FA68),
    CHESTS("Chests", 0xB5804A),
    CHEST_MINECARTS("Minecart chests", 0xB5804A),
    ZYGARDE_CELLS("Zygarde cells", 0x37CB53),
    CUSTOM("Custom Pokémon", 0xFFFFFF);

    public final String label;
    public final int rgb;

    Mode(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }
}
