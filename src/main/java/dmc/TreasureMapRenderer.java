package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class TreasureMapRenderer extends MapRenderer {
	private static final Color PARCHMENT_WATER = new Color(170, 139, 82);
	private static final Color PARCHMENT_LAND_LOW = new Color(218, 190, 120);
	private static final Color PARCHMENT_LAND_HIGH = new Color(126, 91, 47);
		
	private final Location treasureLoc;
	private boolean renderedBaseMap = false;

	public TreasureMapRenderer(Location treasureLoc) {
		super(false);
		this.treasureLoc = treasureLoc.clone();
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		if( player == null || !player.getWorld().equals(treasureLoc.getWorld())) {
			return;
		}

		if(!renderedBaseMap) {
			renderPirateMap(canvas, treasureLoc);
			renderedBaseMap = true;
		}

		MapCursorCollection cursors = new MapCursorCollection();
		cursors.addCursor(playerCursor(player, view));
		canvas.setCursors(cursors);
	}

	private void renderPirateMap(MapCanvas canvas, Location center) {
		World world = center.getWorld();
		int seaLevel = world.getSeaLevel() - 1;

		int scale = 4;  //matches Scale.NORMAL
		for(int px = 0; px < 128; px++) {
			for(int py = 0; py < 128; py++) {
				int dx = (px - 64) * scale;
				int dz = (py - 64) * scale;

				int worldX = center.getBlockX() + dx;
				int worldZ = center.getBlockZ() + dz;

				int highestY = world.getHighestBlockYAt(worldX, worldZ);
				Material top = world.getBlockAt(worldX, highestY-1, worldZ).getType();

				Color color = Color.RED; //missing
				if( isWater(top)) {
					color = PARCHMENT_WATER;
				} else if( highestY >= seaLevel + 12 )  {
					color = PARCHMENT_LAND_HIGH;
				} else {
					color = PARCHMENT_LAND_LOW;
				}
				canvas.setPixelColor(px, py, color);
			}
		}

		drawTreasureIndicator(canvas, 64 - (SkullCrossbones.getWidth()/2), 64 - (SkullCrossbones.getHeight()/2));
			
	}

	private void drawTreasureIndicator(MapCanvas canvas, int startX, int startY) {
		for(int y = 0; y < SkullCrossbones.getHeight(); y++) {
			for(int x = 0; x < SkullCrossbones.getWidth(); x++) {
				Color color = SkullCrossbones.getColorAt(x,y);
				if( color == null ) {
					//transparency, continue;
					continue;
				}

				int canvasX = startX + x;
				int canvasY = startY + y;
				if( canvasX < 0 || canvasX >= 128 || canvasY < 0 || canvasY >= 128) {
					continue;
				}
				canvas.setPixelColor(canvasX, canvasY, color);
			}
		}
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

	private MapCursor treasureCursor(Player player, Location treasureLoc, MapView view) {
		int[] marker = calculateMarkerPosition(player.getLocation(), treasureLoc, view);

		byte mapX = (byte)marker[0];
		byte mapZ = (byte)marker[1];

			
		return new MapCursor(mapX, mapZ, (byte)0, MapCursor.Type.RED_X, true);
	}
	private MapCursor playerCursor(Player player, MapView view) {
		double blocksPerPixel = getBlocksPerPixel(view.getScale());

		double dx = player.getLocation().getX() - view.getCenterX();
		double dz = player.getLocation().getZ() - view.getCenterZ();

		int mapX = clamp((int)Math.round(dx / blocksPerPixel), -127, 127);
		int mapZ = clamp((int)Math.round(dz / blocksPerPixel), -127, 127);

		byte direction = yawToMapDirection(player.getLocation().getYaw());
		return new MapCursor((byte)mapX, (byte)mapZ, direction, MapCursor.Type.PLAYER, true);
	}
	private byte yawToMapDirection(float yaw) {
		int dir = Math.round(yaw / 22.5f);
		dir = ((dir % 16) + 16) %16;
		return (byte)dir;
	}

	private int[] calculateMarkerPosition(Location playerLoc, Location treasureLoc, MapView view) {
		double dx = treasureLoc.getX() - playerLoc.getX();
		double dz = treasureLoc.getZ() - playerLoc.getZ();

		double distance = Math.sqrt(dx*dx + dz*dz);

		if( distance < 0.0001) {
			return new int[]{0,0};
		}

		double blocksPerPixel = getBlocksPerPixel(view.getScale());

		double mapX = dx / blocksPerPixel;
		double mapZ = dz / blocksPerPixel;

		double max = Math.max(Math.abs(mapX), Math.abs(mapZ));

		if( max > 127.0 ) {
			double scale = 127.0 / max;
			mapX *= scale;
			mapZ *= scale;
		}

		return new int[] {
			clamp((int)Math.round(mapX), -127, 127),
			clamp((int)Math.round(mapZ), -127, 127)
		};
	}

	private double getBlocksPerPixel(MapView.Scale scale) {
		return switch(scale) {
			case CLOSEST -> 1.0;
			case CLOSE -> 2.0;
			case NORMAL -> 4.0;
			case FAR -> 8.0;
			case FARTHEST -> 16.0;
		};
	}
	private int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

}
