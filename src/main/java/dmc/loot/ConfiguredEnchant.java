package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import org.bukkit.enchantments.Enchantment;

public record ConfiguredEnchant(Enchantment enchantment, int level) {
}
