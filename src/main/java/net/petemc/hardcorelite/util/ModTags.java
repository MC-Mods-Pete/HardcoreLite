package net.petemc.hardcorelite.util;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.petemc.hardcorelite.HardcoreLite;

public class ModTags {
    // Items in this tag restore a heart when eaten (up to the maximum hearts gamerule).
    // Contains enchanted golden apples by default, datapacks can add their own items.
    public static final TagKey<Item> RESTORES_HEARTS = TagKey.of(RegistryKeys.ITEM, new Identifier(HardcoreLite.MOD_ID, "restores_hearts"));
}
