package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public class ValueDollarPart implements DollarPart {
	public Object value;

	public ValueDollarPart() {
		this.value = null;
	}

	public ValueDollarPart(Object value) {
		this.value = value;
	}

	@Override
	public ValueDollarPart apply(Map<String, CompoundTag> references) throws DollarException {
		return this;
	}
}
