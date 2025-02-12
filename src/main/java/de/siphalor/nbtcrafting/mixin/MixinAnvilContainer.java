package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.anvil.AnvilRecipe;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.container.Property;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AnvilContainer.class)
public abstract class MixinAnvilContainer extends Container {
	@Shadow @Final private PlayerEntity player;

	@Shadow @Final private Inventory inventory;

	@Shadow @Final private Inventory result;

	@Shadow @Final private Property levelCost;

	@Shadow private String newItemName;

	private boolean nbtCrafting_userChangedName = false;

	protected MixinAnvilContainer(ContainerType<?> containerType_1, int int_1) {
		super(containerType_1, int_1);
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo callbackInfo) {
		Optional<AnvilRecipe> optionalAnvilRecipe = player.world.getRecipeManager().getFirstMatch(Core.ANVIL_RECIPE_TYPE, inventory, player.world);
		if(optionalAnvilRecipe.isPresent()) {
			ItemStack resultStack = optionalAnvilRecipe.get().craft(inventory);
			if(nbtCrafting_userChangedName) {
				if (!newItemName.equals(resultStack.getName().getString()))
					resultStack.setCustomName(new LiteralText(newItemName));
				nbtCrafting_userChangedName = false;
			} else {
				newItemName = resultStack.getName().getString();
				if(player instanceof ServerPlayerEntity) {
					if(Core.hasClientMod((ServerPlayerEntity) player)) {
						PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
						packetByteBuf.writeString(newItemName);
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Core.UPDATE_ANVIL_TEXT_S2C_PACKET_ID, packetByteBuf);
					}
				}
			}

			result.setInvStack(0, resultStack);
			levelCost.set(optionalAnvilRecipe.get().levels);
			sendContentUpdates();

			callbackInfo.cancel();
		}
	}

	@Inject(method = "setNewItemName", at = @At("HEAD"))
	public void onNewItemNameSet(String newNewItemName, CallbackInfo callbackInfo) {
		nbtCrafting_userChangedName = true;
	}
}
