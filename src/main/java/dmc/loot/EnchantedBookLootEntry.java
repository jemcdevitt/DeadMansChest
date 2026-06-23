package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class EnchantedBookLootEntry extends LootEntry {
	private final Enchantment enchantment;
	private final int level;

	public EnchantedBookLootEntry(Enchantment enchantment, int level, int weight) {
		super(Material.ENCHANTED_BOOK, 1, 1, weight);
		this.enchantment = enchantment;
		this.level = level;
	}

	@Override
	public ItemStack createItem() {
		ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();

		meta.addStoredEnchant(enchantment, level, true);
		book.setItemMeta(meta);
		return book;
	}

}
