package org.craftamethyst.tritium.mixin.entity.stack.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;


@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Unique
    private static final int MERGE_COOLDOWN_TICKS = 5;
    @Unique
    private static final int DEFAULT_MAX_STACK = Integer.MAX_VALUE - 100;
    @Unique
    private int tritium$lastMergeTick = -1;
    @Unique
    private int tritium$lastDisplayUpdateTick = -1;
   // @Unique
   // private static final int DISPLAY_UPDATE_INTERVAL = 40;
    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract void setItem(ItemStack stack);

    @Shadow
    public abstract void setExtendedLifetime();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!tritium$shouldProcess()) return;
        ItemEntity self = (ItemEntity) (Object) this;
       // long gameTime = self.level().getGameTime();
       // if (tritium$lastDisplayUpdateTick == -1 || gameTime - tritium$lastDisplayUpdateTick >= DISPLAY_UPDATE_INTERVAL) {
       //     tritium$updateStackDisplay(self);
       //     tritium$lastDisplayUpdateTick = (int) gameTime;
       // }

        if (tritium$shouldAttemptMerge(self)) {
            tritium$lastMergeTick = (int) self.level().getGameTime();
            tritium$tryMergeItems(self);
        }
    }

    @Inject(
            method = "setItem",
            at = @At("TAIL")
    )
    private void onSetItem(ItemStack stack, CallbackInfo ci) {
        if (!tritium$shouldProcess()) return;
        ItemEntity self = (ItemEntity) (Object) this;
        tritium$updateStackDisplay(self);
        tritium$lastDisplayUpdateTick = (int) self.level().getGameTime();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onConstructor(Level level, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
        if (!tritium$shouldProcess()) return;
        ItemEntity self = (ItemEntity) (Object) this;
        tritium$updateStackDisplay(self);
        tritium$lastDisplayUpdateTick = (int) self.level().getGameTime();
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;DDD)V", at = @At("TAIL"))
    private void onConstructorWithVelocity(Level level, double x, double y, double z, ItemStack stack, double dx, double dy, double dz, CallbackInfo ci) {
        if (!tritium$shouldProcess()) return;
        ItemEntity self = (ItemEntity) (Object) this;
        tritium$updateStackDisplay(self);
        tritium$lastDisplayUpdateTick = (int) self.level().getGameTime();
    }

    @Unique
    private boolean tritium$shouldProcess() {
        return TritiumConfigBase.Entities.EntityStacking.enable;
    }

    @Unique
    private boolean tritium$shouldAttemptMerge(ItemEntity self) {
        long gameTime = self.level().getGameTime();
        return tritium$lastMergeTick == -1 || gameTime - tritium$lastMergeTick >= MERGE_COOLDOWN_TICKS;
    }

    @Unique
    private void tritium$tryMergeItems(ItemEntity self) {
        if (!TritiumConfigBase.Entities.EntityStacking.enable) return;

        ItemStack stack = self.getItem();
        int maxStack = tritium$getEffectiveMaxStackSize();

        if (stack.getCount() >= maxStack) return;

        List<ItemEntity> nearby = tritium$findMergeableItems(self);
        if (nearby.isEmpty()) return;

        tritium$performMerge(self, stack, maxStack, nearby);
    }

    @Unique
    private int tritium$getEffectiveMaxStackSize() {
        int configMax = TritiumConfigBase.Entities.EntityStacking.maxStackSize;
        return configMax > 0 ? configMax : DEFAULT_MAX_STACK;
    }

    @Unique
    private List<ItemEntity> tritium$findMergeableItems(ItemEntity self) {
        double mergeDistance = TritiumConfigBase.Entities.EntityStacking.mergeDistance;
        int listMode = TritiumConfigBase.Entities.EntityStacking.listMode;
        List<? extends String> itemList = TritiumConfigBase.Entities.EntityStacking.itemList;
        self.getItem();

        List<ItemEntity> nearby = self.level().getEntitiesOfClass(
                ItemEntity.class,
                self.getBoundingBox().inflate(mergeDistance),
                e -> tritium$isValidMergeTarget(self, e, listMode, itemList)
        );

        nearby.sort(Comparator.comparingDouble(self::distanceToSqr));
        return nearby;
    }

    @Unique
    private void tritium$performMerge(ItemEntity self, ItemStack stack, int maxStack, List<ItemEntity> nearby) {
        int remainingSpace = maxStack - stack.getCount();

        for (ItemEntity other : nearby) {
            if (remainingSpace <= 0) break;

            ItemStack otherStack = other.getItem();
            int transfer = Math.min(otherStack.getCount(), remainingSpace);

            stack.grow(transfer);
            self.setItem(stack);
            self.setExtendedLifetime();

            tritium$handleOtherStackAfterTransfer(other, otherStack, transfer);
            remainingSpace -= transfer;
        }
    }

    @Unique
    private void tritium$handleOtherStackAfterTransfer(ItemEntity other, ItemStack otherStack, int transfer) {
        if (otherStack.getCount() == transfer) {
            other.discard();
        } else {
            otherStack.shrink(transfer);
            other.setItem(otherStack);
            tritium$updateStackDisplay(other);
        }
    }

    @Unique
    private void tritium$updateStackDisplay(ItemEntity entity) {
        if (!TritiumConfigBase.Entities.EntityStacking.enable || !TritiumConfigBase.Entities.EntityStacking.showStackCount) {
            tritium$clearDisplay(entity);
            return;
        }

        ItemStack stack = entity.getItem();
        if (stack.getCount() > 1) {
            tritium$setStackCountDisplay(entity, stack.getCount());
        } else {
            tritium$clearDisplay(entity);
        }
    }


    @Unique
    private void tritium$setStackCountDisplay(ItemEntity entity, int count) {
        Component currentName = entity.getCustomName();
        if (currentName != null) {
            String currentText = currentName.getString();
            if (currentText.startsWith("×") && currentText.length() > 1) {
                try {
                    int currentCount = Integer.parseInt(currentText.substring(1));
                    if (currentCount == count) {
                        return;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        Component countText = Component.literal("×" + count)
                .withStyle(ChatFormatting.DARK_GREEN)
                .withStyle(ChatFormatting.BOLD);
        entity.setCustomName(countText);
        entity.setCustomNameVisible(true);
    }


    @Unique
    private void tritium$clearDisplay(ItemEntity entity) {
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }

    @Unique
    private boolean tritium$isValidMergeTarget(ItemEntity self, ItemEntity other, int listMode, List<? extends String> itemList) {
        if (self == other || other.isRemoved()) return false;

        ItemStack selfStack = self.getItem();
        ItemStack otherStack = other.getItem();

        return tritium$isSameItem(selfStack, otherStack) &&
                tritium$isMergeAllowed(otherStack, listMode, itemList) &&
                (!TritiumConfigBase.Entities.EntityStacking.lockMaxedStacks || otherStack.getCount() < tritium$getEffectiveMaxStackSize()) &&
                (self.getItem().getCount()+other.getItem().getCount()<=self.getItem().getMaxStackSize());
    }

    @Unique
    private boolean tritium$isSameItem(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    @Unique
    private boolean tritium$isMergeAllowed(ItemStack stack, int listMode, List<? extends String> itemList) {
        if (listMode == 0) return true;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        boolean inList = itemList.contains(id.toString());
        return (listMode == 1) == inList;
    }
}