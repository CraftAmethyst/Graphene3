package org.craftamethyst.tritium.mixin.cache;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static final int CACHE_INVALIDATION_TICKS = 200;

    @Unique
    private static final int DEFAULT_COOK_TIME = 200;

    @Unique
    private int tritium$cacheTicks = 0;

    @Unique
    @Nullable
    private RecipeHolder<? extends AbstractCookingRecipe> tritium$cachedRecipe;

    @Unique
    @Nullable
    private ItemStack tritium$cachedInput;

    @Unique
    private boolean tritium$cacheMissed = false;

    @Shadow
    @Final
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    @Shadow
    protected NonNullList<ItemStack> items;

    @Shadow
    private static int getTotalCookTime(Level level, AbstractFurnaceBlockEntity blockEntity) {
        throw new AssertionError();
    }
    @Unique
    private boolean tritium$isInputChanged(ItemStack currentInput) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return true;
        }
        if (tritium$cachedInput == null) {
            return !currentInput.isEmpty();
        }
        if (currentInput.isEmpty()) {
            return true;
        }
        return !ItemStack.isSameItem(tritium$cachedInput, currentInput) ||
                !ItemStack.isSameItemSameComponents(tritium$cachedInput, currentInput);
    }

    @Unique
    @Nullable
    private RecipeHolder<? extends AbstractCookingRecipe> tritium$getCachedRecipe(ItemStack currentInput) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return null;
        }

        tritium$cacheTicks++;
        if (tritium$cacheTicks >= CACHE_INVALIDATION_TICKS || tritium$isInputChanged(currentInput)) {
            tritium$resetCache();
            return null;
        }

        return tritium$cacheMissed ? null : tritium$cachedRecipe;
    }

    @Unique
    private void tritium$updateCache(Level level, ItemStack input) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return;
        }
        tritium$resetCache();

        if (input.isEmpty()) {
            return;
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe =
                this.quickCheck.getRecipeFor(recipeInput, level);

        if (recipe.isPresent()) {
            this.tritium$cachedRecipe = recipe.get();
            this.tritium$cachedInput = input.copy();
            this.tritium$cacheMissed = false;
        } else {
            this.tritium$cacheMissed = true;
            this.tritium$cachedInput = input.copy();
        }

        this.tritium$cacheTicks = 0;
    }

    @Unique
    private void tritium$resetCache() {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return;
        }
        this.tritium$cachedRecipe = null;
        this.tritium$cachedInput = null;
        this.tritium$cacheMissed = false;
        this.tritium$cacheTicks = 0;
    }

    @Unique
    private void tritium$validateAndUpdateCache(Level level, ItemStack currentInput) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return;
        }
        if (level == null || currentInput.isEmpty()) {
            tritium$resetCache();
            return;
        }
        if (tritium$cachedRecipe == null && !tritium$cacheMissed) {
            tritium$updateCache(level, currentInput);
        }
    }

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
            )
    )
    private static int redirectGetTotalCookTime(Level level, AbstractFurnaceBlockEntity blockEntity) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return getTotalCookTime(level, blockEntity);
        }
        return ((AbstractFurnaceBlockEntityMixin) (Object) blockEntity).tritium$getCachedTotalCookTime(level);
    }

    @Unique
    private int tritium$getCachedTotalCookTime(Level level) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return DEFAULT_COOK_TIME;
        }

        ItemStack currentInput = this.items.getFirst();
        if (currentInput.isEmpty()) {
            if (tritium$cachedInput != null || !tritium$cacheMissed) {
                tritium$resetCache();
            }
            return DEFAULT_COOK_TIME;
        }
        tritium$validateAndUpdateCache(level, currentInput);
        RecipeHolder<? extends AbstractCookingRecipe> recipe = tritium$getCachedRecipe(currentInput);
        if (recipe == null && !tritium$cacheMissed) {
            tritium$updateCache(level, currentInput);
            recipe = tritium$cachedRecipe;
        }

        return recipe != null ? recipe.value().getCookingTime() : DEFAULT_COOK_TIME;
    }

    @Inject(
            method = "setItem",
            at = @At("HEAD")
    )
    private void onSetItem(int pIndex, ItemStack pStack, CallbackInfo ci) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return;
        }
        if (pIndex == 0) {
            if (tritium$cachedInput == null || !ItemStack.isSameItemSameComponents(tritium$cachedInput, pStack)) {
                tritium$resetCache();
            }
        }
    }

    @Inject(
            method = "setItems",
            at = @At("HEAD")
    )
    private void onSetItems(NonNullList<ItemStack> pItems, CallbackInfo ci) {
        if (!TritiumConfigBase.Performance.FastFurnace.fastFurnace) {
            return;
        }
        tritium$resetCache();
    }
}