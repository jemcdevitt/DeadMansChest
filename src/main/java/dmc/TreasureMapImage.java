package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.awt.Color;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureMapImage {
	static private final int WIDTH = 128;
	static private final int HEIGHT = 128;
	private boolean ready = false;
	private Color[] pixels = new Color[WIDTH * HEIGHT];
	private Location location;
	private int nextPixel = 0;
	private World world;
	private int seaLevel;
	
	public TreasureMapImage(Location loc) {
		this.location = loc.clone();
		this.world = location.getWorld();
		this.seaLevel = world.getSeaLevel() - 1;
	}
	public TreasureMapImage(Location loc, PixelPacker packedBits) {
		this.location = loc.clone();
		this.world = location.getWorld();
		this.seaLevel = world.getSeaLevel() - 1;
		if( packedBits != null) {
			int pixelIndex = 0;
			for(int i = 0; i < (WIDTH * HEIGHT); i++) {
				pixels[i] = getColorForBits(packedBits.unpackPixel(i));
			}
			ready = true;
		}
	}

	
	public int getNextPixel() {
		return this.nextPixel;
	}
	public void update() {
		int pixelsThisTick = 512;
		for(int i = 0; i < pixelsThisTick && nextPixel < (WIDTH * HEIGHT); i++, nextPixel++) {
			int px = nextPixel % WIDTH;
			int py = nextPixel / HEIGHT;
			pixels[py * WIDTH + px] = getMapColor(px, py);
		}

		ready = !(nextPixel < (WIDTH * HEIGHT));
	}

	public int getWidth() {
		return WIDTH;
	}
	public int getHeight() {
		return HEIGHT;
	}
	public Color getColorAt(int px, int py) {
		return pixels[py * WIDTH + px];
	}
	
	public boolean isReady() {
		return this.ready;
	}

	public byte[] toPackedBitsArray() {
		PixelPacker packer = new PixelPacker(WIDTH * HEIGHT);
		int index = 0;
		for(int i = 0; i < WIDTH * HEIGHT; i += 4) {
			for(int b = 0; b < 4; b++) {
				Color pixel = pixels[i + b];
				int which = 0b00000000;
				if( Constants.PARCHMENT_WATER_COLOR.equals(pixel)) {
					which = Constants.PARCHMENT_WATER_MASK;
				}	else if( Constants.PARCHMENT_LAND_LOW_COLOR.equals(pixel)) {
					which = Constants.PARCHMENT_LAND_LOW_MASK;
				}	else if( Constants.PARCHMENT_LAND_HIGH_COLOR.equals(pixel)) {
					which = Constants.PARCHMENT_LAND_HIGH_MASK;
				}
				packer.packPixel(index++, which);
			}
		}
		return packer.getByteArray();
	}

	private Color getMapColor(int px, int py) {
		int scale = 4;
		int dx = (px - 64) * scale;
		int dz = (py - 64) * scale;
		int worldX = location.getBlockX() + dx;
		int worldZ = location.getBlockZ() + dz;

		int highestY = world.getHighestBlockYAt(worldX, worldZ);
		Material top = world.getBlockAt(worldX, highestY-1, worldZ).getType();
		if( isWater(top) )
			return Constants.PARCHMENT_WATER_COLOR;
		if( highestY >= seaLevel + 12 )
			return Constants.PARCHMENT_LAND_HIGH_COLOR;
		return Constants.PARCHMENT_LAND_LOW_COLOR;
	}

	private boolean isWater(Material material) {
		return
			material == Material.WATER ||
			material == Material.KELP ||
			material == Material.KELP_PLANT ||
			material == Material.SEAGRASS ||
			material == Material.TALL_SEAGRASS ||
			material == Material.LILY_PAD;
	}

	
	private Color getColorForBits(int bitMask) {
		bitMask &= 0b00000011;
		if( bitMask == Constants.PARCHMENT_WATER_MASK)
			return Constants.PARCHMENT_WATER_COLOR;
		else if( bitMask == Constants.PARCHMENT_LAND_LOW_MASK)
			return Constants.PARCHMENT_LAND_LOW_COLOR;
		else if( bitMask == Constants.PARCHMENT_LAND_HIGH_MASK)
			return Constants.PARCHMENT_LAND_HIGH_COLOR;
		
		return Color.MAGENTA;
	}
}
