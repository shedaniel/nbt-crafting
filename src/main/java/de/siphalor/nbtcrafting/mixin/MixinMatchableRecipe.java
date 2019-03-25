package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.Core;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

@Mixin(targets = "net/minecraft/recipe/RecipeFinder$MatchableRecipe")
public abstract class MixinMatchableRecipe {
	
	private RecipeFinder owner;

    @Shadow(aliases = "field_7552", remap = false)
	private List<Ingredient> ingredients;
	
	@Shadow(aliases = "field_7551", remap = false)
	private int[] inputs;
	
	@Shadow(aliases = "field_7558", remap = false)
	private BitSet bitSet;
	
	@Shadow
	protected abstract int method_7420(final boolean bool, final int int_1, final int int_2);
	
	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(
		method = "<init>(Lnet/minecraft/recipe/RecipeFinder;Lnet/minecraft/recipe/Recipe;)V",
		at = @At("RETURN")
	)
	public void onConstruct(RecipeFinder recipeFinder, Recipe<?> recipe, CallbackInfo ci) {
		this.bitSet.clear();
		for(int j = 0; j < ingredients.size(); j++) {
			Ingredient ingredient = (Ingredient) ingredients.get(j);
			for (int i = 0; i < inputs.length; i++) {
				if(ingredient.method_8093(RecipeFinder.getStackFromId(inputs[i])))
					this.bitSet.set(method_7420(true, i, j));
			}
		}
	}

	/**
	 * @reason Builds the idToAmountMap but nbt dependent
	 * @author Siphalor
	 */
	@Overwrite
	private int[] method_7422() {
		owner = Core.lastRecipeFinder;
		IntCollection ints = new IntAVLTreeSet();
		for(int id : owner.idToAmountMap.keySet()) {
			for (Iterator<Ingredient> iterator = ingredients.iterator(); iterator.hasNext();) {
				Ingredient ingredient = (Ingredient) iterator.next();
				if(ingredient.method_8093(RecipeFinder.getStackFromId(id)))
					ints.add(id);
			}
		}
		return ints.toIntArray();
	}
}