package com.github.crimsondawn45.fabricshieldlib.mixin;

import com.github.crimsondawn45.fabricshieldlib.lib.event.ShieldBlockCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin that allows custom shields to block damage.
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Inject(at = @At(value = "HEAD"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
		LivingEntity entity = (LivingEntity)(Object)this;
		ItemStack activeItem = entity.getActiveItem();
		
		if(!(entity.isInvulnerableTo(source) || entity.world.isClient || !entity.isAlive() || (source.isFire() && entity.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)))) {
			if (amount > 0.0F && ((LivingEntityAccessor)entity).fabricshieldlib$invokeBlockedByShield(source)) {
				
				/*
				 * TODO: make sure ActionResult.FAIL results in this event being cancelled, should behave like the attack went through the shield.
				 */
                ActionResult result = ShieldBlockCallback.EVENT.invoker().block(entity, source, amount, entity.getActiveHand(), activeItem);

                if(result == ActionResult.FAIL) {
                    callbackInfo.cancel();
                }

				//Handle Shield
				((LivingEntityAccessor)entity).fabricshieldlib$invokeDamageShield(amount);
				amount = 0.0F;

				if (!source.isProjectile()) {
					Entity sourceEntity = source.getSource();
					
					if (sourceEntity instanceof LivingEntity) {
					   ((LivingEntityAccessor)entity).fabricshieldlib$invokeTakeShieldHit((LivingEntity)sourceEntity);
					}
				}
			}
		}
	}
}
