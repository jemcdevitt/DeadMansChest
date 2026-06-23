package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootEntry {
	private final Material material;
	private final int min;
	private final int max;
	private final int weight;

	public LootEntry(Material material, int min, int max, int weight) {
		this.material = material;
		this.min = Math.max(1, min);
		this.max = Math.max(this.min, max);
		this.weight = Math.max(1, weight);
	}

	public Material getMaterial() {
		return material;
	}

	public int getWeight() {
		return weight;
	}

	public ItemStack createItem() {
		int amount = ThreadLocalRandom.current().nextInt(min, max + 1);
		return new ItemStack(material, amount);
	}

	public boolean isRepeatable() {
		return true;
	}
}
