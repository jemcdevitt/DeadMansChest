package dmc.treasure;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Packages the treasure marker
 */

import dmc.CompositeDisplay;
import dmc.Constants;
import dmc.ICompositeDisplayHolder;
import dmc.utils.UtilFuncs;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.joml.Vector3f;

public class TreasureMarker implements ICompositeDisplayHolder {
	final Location location;
	final String uniqueId;
	final int level;
	CompositeDisplay marker;
	

	public TreasureMarker(Location loc, int treasureLevel) {
		this.location = loc.clone();
		this.uniqueId = UUID.randomUUID().toString();
		this.level = treasureLevel;
		
		Material levelGem = UtilFuncs.getMaterialForTreasureLevel(level);;

		marker = new CompositeDisplay(Constants.DMC_CD_TYPE_TREASURE_MARKER, location, true, 1.25f, 1.75f)
			.addBlock(Material.DARK_OAK_FENCE,
								new Vector3f(0.25f,0,0.25f),
								new Vector3f(0, 0, 0),
								new Vector3f(0.5f, 1.0f, 0.5f))
			.addItem(Material.WITHER_SKELETON_SKULL,
							 new Vector3f(0.5f,1.4375f,0.5f),
							 new Vector3f(0, 180f, 0),
							 new Vector3f(1f, 1f, 1f),
							 (d) -> { d.setBrightness(new Display.Brightness(11,0)); })
			//crown
			.addBlock(Material.GOLD_BLOCK,
								new Vector3f(0.25f, 1.4375f, 0.25f),
								new Vector3f(0,0,0),
								new Vector3f(0.5f, 0.05f, 0.5f))
			.addBlock(Material.RAW_GOLD_BLOCK,
								new Vector3f(0.25f, 1.4375f, 0.75f),
								new Vector3f(0, 0, 0),
								new Vector3f(0.05f, 0.15f, 0.05f))
			.addBlock(Material.RAW_GOLD_BLOCK,
								new Vector3f(0.45f, 1.4375f, 0.75f),
								new Vector3f(0, 0, 0),
								new Vector3f(0.1f, 0.175f, 0.05f))
			.addBlock(Material.RAW_GOLD_BLOCK,
								new Vector3f(0.6875f, 1.4375f, 0.75f),
								new Vector3f(0,0,0),
								new Vector3f(0.05f, 0.15f, 0.05f))
			.addBlock(levelGem,
								new Vector3f(0.45f, 1.61f, 0.75f),
								new Vector3f(0,0,0),
								new Vector3f(0.1f, 0.1f, 0.05f),
								(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
			.addBlock(Material.AMETHYST_BLOCK,
								new Vector3f(0.25f, 1.586875f, 0.75f),
								new Vector3f(0,0,0),
								new Vector3f(0.05f, 0.05f, 0.05f),
								(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
			.addBlock(Material.AMETHYST_BLOCK,
								new Vector3f(0.6875f, 1.586875f, 0.75f),
								new Vector3f(0,0,0),
								new Vector3f(0.05f, 0.05f, 0.05f),
								(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
			//decorations
			.addItem(Material.SKELETON_SKULL,
							 new Vector3f(0.25f, 0.25f, 0.5f),
							 new Vector3f(0,215f,0),
							 new Vector3f(0.5f, 0.5f, 0.5f))
			.addItem(Material.SKELETON_SKULL,
							 new Vector3f(0.75f, 0.25f, 0.5f),
							 new Vector3f(0,135f,0),
							 new Vector3f(0.5f, 0.5f, 0.5f))
			.addBlock(Material.SOUL_FIRE,
								new Vector3f(0.18f, 0.25f, 0.4375f),
								new Vector3f(0,0,0),
								new Vector3f(0.15f, 0.2f, 0.15f),
								(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
			.addBlock(Material.SOUL_FIRE,
								new Vector3f(0.68f, 0.25f, 0.4375f),
								new Vector3f(0,0,0),
								new Vector3f(0.15f, 0.2f, 0.15f),
								(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
			
			.addKey(Constants.ITEM_TYPE_KEY, Constants.DMC_TREASURE_MARKER_ITEM_TYPE)
			.addKey(Constants.DMC_TREASURE_MARKER_ID_KEY, this.uniqueId)
			.addKey(Constants.DMC_TREASURE_LEVEL, level)
			.addKey(Constants.DMC_TREASURE_COMPONENT, true)
			.spawn();
	}

	@Override
	public CompositeDisplay getCompositeDisplay() {
		return marker;
	}
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	
	public void removeFromWorld() {
		marker.remove();
	}
	public Location getLocation() {
		return location;
	}
	public int getTreasureLevel() {
		return this.level;
	}
}
