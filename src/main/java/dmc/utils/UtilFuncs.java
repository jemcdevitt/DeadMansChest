package dmc.utils;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.Constants;
import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class UtilFuncs {
	static public final Color MARKER_LEVEL_1 = new Color(0x17, 0xc5, 0x44); // #17c544
	static public final Color MARKER_LEVEL_2 = new Color(0xbd, 0x20, 0x08); // #bd2008
	static public final Color MARKER_LEVEL_3 = new Color(0x4b, 0xed, 0xe6); // #4bede6 
	static public final Color MARKER_LEVEL_UNKNOWN = new Color(0xff, 0xff, 0x00);  // #ffff00


	static public final int clamp(int v, int min, int max) {
		return Math.max(min,Math.min(max, v));
	}
	static public final double clamp(double v, double min, double max) {
		return Math.max(min,Math.min(max, v));
	}

	static public boolean isDMCComponent(Entity entity) {
		return getDMCComponentType(entity) != null;
	}

	static public String getDMCComponentType(Entity entity) {
		if( entity == null )
			return null;
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		String type = pdc.get(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING);
		return type;
	}
	
	static public final Location lerp(final Location start, final Location end, final double time) {
		double lx = org.joml.Math.lerp(start.getX(), end.getX(), time);
		double ly = org.joml.Math.lerp(start.getY(), end.getY(), time);
		double lz = org.joml.Math.lerp(start.getZ(), end.getZ(), time);
		double lpitch = org.joml.Math.lerp(start.getPitch(), end.getPitch(), time);
		double lyaw = org.joml.Math.lerp(start.getYaw(), end.getYaw(), time);

		Location newLoc = new Location(start.getWorld(), lx, ly, lz);
		newLoc.setPitch((float)lpitch);
		newLoc.setYaw((float)lyaw);
		return newLoc;
	}
		
	
	static public final boolean isWaterBiome(Biome biome) {
		return biome.toString().toUpperCase().contains("OCEAN") || biome == Biome.RIVER;
	}

	static public final Material getMaterialForTreasureLevel(int level) {
		if( level == 1)
			return Material.EMERALD_BLOCK;
		else if( level == 2)
			return Material.REDSTONE_BLOCK;
		else if( level == 3)
			return Material.DIAMOND_BLOCK;
		return Material.YELLOW_CONCRETE;
	}
	static public final Color getColorForTreasureLevel(int level) {
		if( level == 1)
			return MARKER_LEVEL_1;
		if( level == 2)
			return MARKER_LEVEL_2;
		if( level == 3)
			return MARKER_LEVEL_3;
		return MARKER_LEVEL_UNKNOWN;
	}

	static public final boolean canBlockHoldTreasureChest(Block block) {
		if( !block.isSolid()) {
			return false;
		}
		Material mat = block.getType();
		if( mat == Material.GRASS_BLOCK ||
				mat == Material.DIRT ||
				mat == Material.COARSE_DIRT ||
				mat == Material.PODZOL ||
				mat == Material.SAND ||
				mat == Material.RED_SAND ||
				mat == Material.GRAVEL ||
				mat == Material.MOSS_BLOCK ||
				mat == Material.MUD ||
				mat == Material.ROOTED_DIRT)
			return true;
		return false;
	}

	static final public boolean isCoastalWater(World world, int x, int z) {
		int y = world.getSeaLevel() - 1;

		if( world.getBlockAt(x,y,z).getType() != Material.WATER ) return false;
		if( world.getBlockAt(x,y+1,z).getType() != Material.AIR ) return false;

		boolean hasLandNearby = false;
		boolean hasOceanNearby = false;

		for(int dx = -16; dx <= 16; dx += 4) {
			for(int dz = -16; dz <= 16; dz += 4) {
				Biome biome = world.getBiome(x + dx, y, z + dz);
				Material surface = world.getHighestBlockAt(x + dx, z + dz).getType();

				if( surface != Material.WATER && surface != Material.AIR) {
					hasLandNearby = true;
				}
				if( isWaterBiome(biome)) {
					hasOceanNearby = true;
				}
			}
		}
		return hasLandNearby && hasOceanNearby;
	}
	

}
