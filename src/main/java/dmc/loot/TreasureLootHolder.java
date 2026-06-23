package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.CompositeDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class TreasureLootHolder implements InventoryHolder {
	private final String chestId;
	private final int treasureLevel;
	private final CompositeDisplay chestDisplay;
	private Inventory inventory;

	public TreasureLootHolder(String chestId, int treasureLevel, CompositeDisplay chestDisplay) {
		this.chestId = chestId;
		this.treasureLevel = treasureLevel;
		this.chestDisplay = chestDisplay;
	}

	public String getChestId() {
		return this.chestId;
	}
	public int getTreasureLevel() {
		return this.treasureLevel;
	}
	public CompositeDisplay getChestDisplay() {
		return chestDisplay;
	}
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}
		
}
