package org.craftamethyst.tritium;


import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@OnlyIn(Dist.CLIENT)

public class ItemCountRenderer {
   // @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNameTagRender(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();
        if (stack.getCount() <= 1) {
            return;
        }

        Component customName = itemEntity.getCustomName();
        if (customName != null) {
            event.setContent(customName);
        }
    }
}