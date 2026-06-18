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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import static dmc.DeadMansChestPlugin.LOG;


public class TreasureMapRenderer extends MapRenderer {
	private final Location treasureLoc;
	private boolean renderedBaseMap = false;
	private TreasureMapImage image ;
	private ItemStack map;

	public TreasureMapRenderer(ItemStack map, TreasureMapImage image, Location treasureLoc) {
		super(false);
		this.image = image;
		this.treasureLoc = treasureLoc.clone();
		this.map = map;
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		if( player == null || !player.getWorld().equals(treasureLoc.getWorld())) {
			return;
		}

		if(!renderedBaseMap) {
			if( image.isReady()) {
				paintPreparedMap(canvas);

				if(!(map.getItemMeta() instanceof MapMeta meta)) {
					return;
				}
				meta.getPersistentDataContainer().set(Constants.DMC_MAP_PIXELS, PersistentDataType.BYTE_ARRAY, image.toPackedBitsArray());
				map.setItemMeta(meta);
				renderedBaseMap = true;
			} else {
				image.update();
				paintBlankParchment(canvas);
			}
		}

		MapCursorCollection cursors = new MapCursorCollection();
		cursors.addCursor(playerCursor(player, view));
		canvas.setCursors(cursors);
	}

	private void paintPreparedMap(MapCanvas canvas) {
		for(int px = 0; px < image.getWidth(); px++ ) {
			for(int py = 0; py < image.getHeight(); py++) {
				Color color = image.getColorAt(px, py);
				canvas.setPixelColor(px, py, color);
			}
		}
		drawTreasureIndicator(canvas, 64 - (SkullCrossbones.getWidth()/2), 64 - (SkullCrossbones.getHeight()/2));
	}

	private void paintBlankParchment(MapCanvas canvas) {
		int maxPixels = 0;
		int nextPixel = image.getNextPixel();
		for(int px = 0; px < image.getWidth() && maxPixels < nextPixel; px++) {
			for(int py = 0; py < image.getHeight() && maxPixels < nextPixel; py++) {
				Color color = image.getColorAt(px, py);
				canvas.setPixelColor(px, py, color);
			}
		}
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
