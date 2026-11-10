package net.petemc.hardcorelite.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.petemc.hardcorelite.HardcoreLite;

public class ModTags {
    // Items in this tag restore a heart when eaten (up to the maximum hearts gamerule).
    // Contains enchanted golden apples by default, datapacks can add their own items.
    public static final TagKey<Item> RESTORES_HEARTS = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(HardcoreLite.MOD_ID, "restores_hearts"));
}
