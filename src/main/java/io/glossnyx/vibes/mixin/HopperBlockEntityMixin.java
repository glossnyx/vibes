package io.glossnyx.vibes.mixin;

import io.glossnyx.vibes.network.ServerNetworking;
import io.glossnyx.vibes.util.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
class HopperBlockEntityMixin extends BlockEntity {
	@Shadow private DefaultedList<ItemStack> inventory;

	public HopperBlockEntityMixin(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(method = "setStack", at = @At("HEAD"))
	private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
		ServerNetworking.INSTANCE.changePositionProvider(stack, this, inventory.get(slot));
	}

	@Inject(method = "removeStack(II)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
	private void onRemoveStack(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
		ServerNetworking.INSTANCE.changePositionProvider(cir.getReturnValue(), world);
	}

	@Redirect(
		method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;remove()V")
	)
	private static void onRemoveItem(ItemEntity entity) {
		if (!entity.world.isClient && ItemsKt.isPlaying(entity.getStack())) entity.removed = true;
		else entity.remove();
	}
}
