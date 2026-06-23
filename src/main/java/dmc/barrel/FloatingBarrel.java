package dmc.barrel;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Provides a bobbing barrel in the water that contains a treasure map.
 */

import dmc.CompositeDisplay;
import dmc.Configuration;
import dmc.Constants;
import dmc.utils.UtilFuncs;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

import static dmc.barrel.BarrelManager.RNG;

import static dmc.DeadMansChestPlugin.LOG;
public class FloatingBarrel {
	final Location loc;
	final String uniqueId;
	CompositeDisplay barrel;
	long ttl;
	int level;

	public FloatingBarrel(Location loc, Configuration config) {

		this.uniqueId = UUID.randomUUID().toString();
		
		this.loc = loc;
		float rx = RNG.nextInt(60) + 3.0f;
		float rz = RNG.nextInt(60) + 3.0f;
		int levelRNG = RNG.nextInt(100);
		level = (levelRNG > 85 ? 3 : levelRNG > 45 ? 2 : 1);

		Material levelBand = UtilFuncs.getMaterialForTreasureLevel(level);
		
		this.barrel = new CompositeDisplay(Constants.DMC_CD_TYPE_BARREL, loc,false,1.0f, 1.0f)
			.addBlock(Material.BARREL,
								new Vector3f(0,0,0),
								new Vector3f(rx, 0, rz),
								new Vector3f(0.25f, 0.25f, 0.25f),
								(d) -> { d.setBrightness(new Display.Brightness(9,0)); })
			.addBlock(levelBand,
								new Vector3f(-0.001f,-0.001f,0.0415f),
								new Vector3f(rx, 0, rz),
								new Vector3f(0.2525f, 0.253f, 0.035f),
								(d) -> { d.setBrightness(new Display.Brightness(12,0)); })
			.addBlock(levelBand,
								new Vector3f(-0.001f,-0.001f,0.165f),
								new Vector3f(rx, 0, rz),
								new Vector3f(0.2525f, 0.253f, 0.035f),
								(d) -> { d.setBrightness(new Display.Brightness(12,0)); })
			.addKey(Constants.ITEM_TYPE_KEY, Constants.DMC_BARREL_ITEM_TYPE)
			.addKey(Constants.DMC_BARREL_ID_KEY, this.uniqueId)
			.addKey(Constants.DMC_TREASURE_LEVEL, level)
			.addKey(Constants.DMC_TREASURE_COMPONENT, true)
			.spawn();

		int minTime = config.getBarrelsConfig().min_lifetime_minutes();
		int maxTime = config.getBarrelsConfig().max_lifetime_minutes();
		
		this.ttl = (RNG.nextInt(maxTime - minTime) + minTime) * 60_000;   
	}


	public Location getLocation() {
		return this.loc;
	}
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	public int getLevel() {
		return this.level;
	}
	
	public void removeFromWorld() {
		barrel.remove();
	}
	
	public boolean update(long tick, float bounce) {
		Location newLoc = loc.clone().add(0,bounce,0);
		barrel.moveTo(newLoc);

		ttl -= tick;
		if(ttl > 0)
			return true;
		LOG(0,"Barrel at (%f,%f,%f) has died", loc.getX(), loc.getY(), loc.getZ());
		return false;
	}

	public void showInfo(Player player, boolean force) {
		LOG(force?10:0,player, "Barrel at (%.2f,%.2f,%.2f), TTL: %d, Level: %d",
				loc.getX(), loc.getY(), loc.getZ(), ttl, level);
	}
	
}
