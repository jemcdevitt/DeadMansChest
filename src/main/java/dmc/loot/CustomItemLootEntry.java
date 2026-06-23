package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItemLootEntry extends LootEntry {
	private final String displayName;
	private final List<ConfiguredEnchant> enchants;

	public CustomItemLootEntry(Material material,
														 int min, int max, int weight,
														 String displayName, List<ConfiguredEnchant> enchants) {
		super(material, min, max, weight);
		this.displayName = displayName;
		this.enchants = enchants == null ? List.of() : List.copyOf(enchants);
	}

	@Override
	public ItemStack createItem() {
		ItemStack item = super.createItem();

		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return item;
		}

		if (displayName != null && !displayName.isBlank()) {
			meta.displayName(Component.text(displayName));
		}

		int count = 0;
		for (ConfiguredEnchant configured : enchants) {
			if (count >= 2) {
				break;
			}

			meta.addEnchant(
											configured.enchantment(),
											configured.level(),
											true
											);

			count++;
		}

		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}
	
}
