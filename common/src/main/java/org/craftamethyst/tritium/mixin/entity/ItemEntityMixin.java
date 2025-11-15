package org.craftamethyst.tritium.mixin.entity;

import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/11/15 下午1:07
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {
    @Shadow public abstract ItemStack getItem();

    public ItemEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "tick",at = @At("TAIL"))
    private void tick(CallbackInfo ci){
        if(this.getItem().getCount()>this.getItem().getMaxStackSize()) return;
        if(!TritiumConfig.get().entities.entityStacking.enable) return;
        if((this.tickCount % TritiumConfig.get().entities.entityStacking.lagTicks) !=1) return;

        ItemEntity self = (ItemEntity) ((Object) this);
                Set<ItemEntity> matchedEntities = this.level().getEntitiesOfClass(ItemEntity.class,new AABB(this.position(),this.position()).inflate(TritiumConfig.get().entities.entityStacking.range)).stream().filter(e->e!=self && tritium$match(this.getItem(),e.getItem())).collect(Collectors.toSet());
        if(matchedEntities.size()>=TritiumConfig.get().entities.entityStacking.maxEntityCount){
            for (ItemEntity matchedEntity : matchedEntities) {
                int merged = this.getItem().getCount()+matchedEntity.getItem().getCount();
                if(merged>this.getItem().getMaxStackSize()) break;
                this.getItem().setCount(merged);
                matchedEntity.discard();
            }
        }
    }

    @Unique
    public boolean tritium$match(ItemStack a, ItemStack b){
        if(!a.is(b.getItem())) return false;
        return Arrays.equals(a.getTags().toArray(),b.getTags().toArray());
    }
}
