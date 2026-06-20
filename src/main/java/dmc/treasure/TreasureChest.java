package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Vector3f;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureChest {
	Location location;
	ItemStack treasureMap;
	CompositeDisplay theChest;

	public TreasureChest(Location loc) {
		this.location = loc.clone();
		init();
	}
	
	public TreasureChest(ItemStack map, Location loc) {
		LOG(0, "New treasure chest at (%d,%d,%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		this.location = loc.clone();
		this.treasureMap = map;
		init();
	}

	private void init() {
		String mapId = null;
		if( this.treasureMap != null ) {
			ItemMeta meta = this.treasureMap.getItemMeta();
			if( meta != null )
				mapId = meta.getPersistentDataContainer().get(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING);
		}
		
		theChest = new CompositeDisplay(location, true, 1.0f, 1.0f)
			.addBlock(Material.COMPOSTER          , new Vector3f(0f,      0f,      0.1875f), new Vector3f(0,0,0), new Vector3f(1f,     0.5625f, 0.625f))
			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.1875f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.5f,    0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.8125f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addBlock(Material.WAXED_COPPER_BLOCK , new Vector3f(0.0625f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.875f, 0.05f,   0.625f))
			.addBlock(Material.GOLD_BLOCK         , new Vector3f(0f,      0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
			.addBlock(Material.GOLD_BLOCK         , new Vector3f(0.9375f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
			.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.125f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
			.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.835f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
			.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.125f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
			.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.835f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
			.addKey(Constants.DMC_TREASURE_COMPONENT, true);
		
		if( mapId != null )
			theChest.addKey(Constants.DMC_MAP_ID_KEY, mapId);

		theChest.spawn();
	}
}
