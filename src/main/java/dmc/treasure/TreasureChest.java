package dmc.treasure;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.CompositeDisplay;
import dmc.Constants;
import dmc.ICompositeDisplayHolder;
import dmc.utils.UtilFuncs;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Vector3f;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureChest implements ICompositeDisplayHolder {
	Location location;
	ItemStack treasureMap;
	int treasureLevel;
	CompositeDisplay theChest;
	String uniqueId;

	public TreasureChest(Location loc, int treasureLevel) {
		this.location = loc.clone();
		this.treasureLevel = treasureLevel;
		init();
	}
	
	public TreasureChest(ItemStack map, Location loc, int treasureLevel) {
		LOG(0, "New treasure chest at (%d,%d,%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		this.location = loc.clone();
		this.treasureMap = map;
		this.treasureLevel = treasureLevel;
		init();
	}
	

	public TreasureChest spawn() {
		if( !theChest.isSpawned())
			theChest.spawn();

		return this;
	}
	
	@Override
	public CompositeDisplay getCompositeDisplay() {
		return theChest;
	}
	
	private void init() {
		String mapId = null;
		this.uniqueId = UUID.randomUUID().toString();
		if( this.treasureMap != null ) {
			ItemMeta meta = this.treasureMap.getItemMeta();
			if( meta != null )
				mapId = meta.getPersistentDataContainer().get(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING);
		}

		Material treasureType = UtilFuncs.getMaterialForTreasureLevel(treasureLevel);
		
		theChest = new CompositeDisplay(Constants.DMC_CD_TYPE_TREASURE_CHEST, location, true, 1.25f, 1.25f)
			.addBlock(Material.COMPOSTER          , new Vector3f(0f,      0f,      0.1875f), new Vector3f(0,0,0), new Vector3f(1f,     0.5625f, 0.625f))
			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.1875f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f),
							 (d) -> { d.setBrightness(new Display.Brightness(9,0)); })

			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.5f,    0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f),
							 (d) -> { d.setBrightness(new Display.Brightness(9,0)); })

			.addItem(Material.SKELETON_SKULL     , new Vector3f(0.8125f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f),
							 (d) -> { d.setBrightness(new Display.Brightness(9,0)); })

			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
			.addBlock(Material.WAXED_COPPER_BLOCK , new Vector3f(0.0625f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.875f, 0.05f,   0.625f))
			.addBlock(Material.WAXED_COPPER_BLOCK , new Vector3f(0.0625f, 0.5625f + 0.05f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.870f, 0.05f,   0.620f))
			.addBlock(Material.WAXED_COPPER_BLOCK , new Vector3f(0.0625f, 0.5625f + 0.1f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.865f, 0.05f,   0.615f))
			.addBlock(Material.GOLD_BLOCK         , new Vector3f(0f,      0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
			.addBlock(Material.GOLD_BLOCK         , new Vector3f(0.9375f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
			.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.125f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f),
							 (d) -> { d.setBrightness(new Display.Brightness(13,0)); })
			.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.835f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f),
							 (d) -> { d.setBrightness(new Display.Brightness(13,0)); })
			.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.125f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f),
							 (d) -> { d.setBrightness(new Display.Brightness(13,0)); })
			.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.835f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f),
							 (d) -> { d.setBrightness(new Display.Brightness(12,0)); })
			.addBlock(treasureType, new Vector3f(0.5f - (0.125f/2), 0.5625f + 0.1f, .5f - (0.125f/2)), new Vector3f(0,0,0), new Vector3f(0.125f, 0.125f, 0.125f),
							 (d) -> { d.setBrightness(new Display.Brightness(14,0)); })
			.addKey(Constants.DMC_TREASURE_COMPONENT, true)
			.addKey(Constants.DMC_TREASURE_LEVEL, treasureLevel)
			.addKey(Constants.ITEM_TYPE_KEY, Constants.DMC_TREASURE_CHEST_ITEM_TYPE)
			.addKey(Constants.DMC_TREASURE_CHEST_ID_KEY, this.uniqueId);
		
		
		if( mapId != null )
			theChest.addKey(Constants.DMC_MAP_ID_KEY, mapId);
	}
}
