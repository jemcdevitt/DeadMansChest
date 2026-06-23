package dmc.map;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Custom MapRenderer to show our treasure map image data.
 */

import dmc.Constants;
import dmc.DeadMansChestPlugin;
import dmc.utils.UtilFuncs;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static dmc.DeadMansChestPlugin.LOG;


public class TreasureMapRenderer extends MapRenderer {
	static public final Color MARKER_BORDER = new Color(0x3c, 0x2e, 0x22);
	
	private final Location treasureLoc;
	private final DeadMansChestPlugin plugin;
	private final String mapId;
	private TreasureMapImage image ;
	private ItemStack map;
	private List<StainBlob> wetBlobs = new ArrayList<>();
	private boolean renderedBaseMap = false;
	private boolean persistScheduled = false;
	private boolean pixelsPersisted = false;
	private byte[] packedPixels = null;
	private int treasureLevel;

	public TreasureMapRenderer(DeadMansChestPlugin plugin, String mapId, ItemStack map, TreasureMapImage image, Location treasureLoc, int treasureLevel) {
		super(false);
		this.mapId = mapId;
		this.plugin = plugin;
		this.image = image;
		this.treasureLoc = treasureLoc.clone();
		this.map = map;
		this.treasureLevel = treasureLevel;

		generateWetBlobs(treasureLoc.getBlockX() * 734287L ^ treasureLoc.getBlockZ() * 912271L);
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		if( player == null || !player.getWorld().equals(treasureLoc.getWorld())) {
			return;
		}


		if(!renderedBaseMap) {
			if(image.isReady()) {
				paintPreparedMap(canvas);
				renderedBaseMap = true;

				if(!pixelsPersisted && !persistScheduled) {
					packedPixels = image.toPackedBitsArray();
					persistScheduled = true;

					Bukkit.getScheduler().runTask(plugin, () -> {
							boolean saved = persistPixelsToPlayerInventory(player, packedPixels);
							if(saved) {
								pixelsPersisted = true;
							} else {
								persistScheduled = false;
							}
						});
				}
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
		int row = 0;
		int px = 0, py = 0;
		for(py = 0; py < image.getHeight() && maxPixels < nextPixel; py++) {
		 	row = py;
		 	for(px = 0; px < image.getWidth() && maxPixels < nextPixel; px++) {
		 		maxPixels++;
				Color color = image.getColorAt(px, py);
				canvas.setPixelColor(px, py, color);
			}
		}

		for(;py < 128; py++) {
			for(px = 0;px < 128; px++) {
				canvas.setPixelColor(px,py,getWetParchmentColor(px,py));
			}
		}
	}
	
	private void drawTreasureIndicator(MapCanvas canvas, int startX, int startY) {
		for(int y = 0; y < SkullCrossbones.getHeight(); y++) {
			for(int x = 0; x < SkullCrossbones.getWidth(); x++) {
				int pixel = SkullCrossbones.getPixelAt(x,y);
				if( pixel == 0) {
					//transparency, continue;
					continue;
				}
				Color color = MARKER_BORDER;
				if( pixel == 2 ) {
					color = UtilFuncs.getColorForTreasureLevel(treasureLevel);
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
				

	private MapCursor playerCursor(Player player, MapView view) {
		double blocksPerPixel = getBlocksPerPixel(view.getScale());

		double dx = player.getLocation().getX() - view.getCenterX();
		double dz = player.getLocation().getZ() - view.getCenterZ();

		int mapX = UtilFuncs.clamp((int)Math.round(dx / blocksPerPixel), -127, 127);
		int mapZ = UtilFuncs.clamp((int)Math.round(dz / blocksPerPixel), -127, 127);

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
			UtilFuncs.clamp((int)Math.round(mapX), -127, 127),
			UtilFuncs.clamp((int)Math.round(mapZ), -127, 127)
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


	private Color getWetParchmentColor(int x, int y) {
		int baseR = 156;
		int baseG = 166;  //112
		int baseB = 84;

		int noise = pseudoNoise(x, y);
		float dark = blobDarkness(x,y);

		int r = baseR + noise;
		int g = baseG + noise / 2;
		int b = baseB + noise / 3;


		r -= (int)(dark * 55f);
		g -= (int)(dark * 42f);
		b -= (int)(dark * 30f);
		
		return new Color(UtilFuncs.clamp(r,0,255), UtilFuncs.clamp(g,0,255), UtilFuncs.clamp(b, 0,255));
	}

	private int pseudoNoise(int x, int y) {
		int v = x * 734287 + y * 912271 + 137;
		v ^= (v << 13);
		v ^= (v >>> 17);
		v ^= (v << 5);
		return Math.floorMod(v, 21) - 10;
	}

	private float blobDarkness(int x, int y) {
		float total = 0f;

		for(StainBlob blob : wetBlobs) {
			float dx = x - blob.cx;
			float dy = y - blob.cy;
			float dist = (float)Math.sqrt(dx*dx + dy*dy);

			if( dist < blob.radius) {
				float t = 1.0f - (dist / blob.radius);
				t = t * t * (3f - 2f * t);
				total += t * blob.strength;
			}
		}
		return Math.min(total, 0.55f);
	}
	private void generateWetBlobs(long seed) {
		Random r = new Random(seed);

		wetBlobs.clear();
		int blobCount = 5 + r.nextInt(4);

		for(int i = 0; i < blobCount; i++) {
			float cx = r.nextFloat() * 128f;
			float cy = r.nextFloat() * 128f;
			float radius = 14f + r.nextFloat() * 28f;
			float strength = 0.18f + r.nextFloat() * 0.22f;

			wetBlobs.add(new StainBlob(cx, cy, radius, strength));
		}
	}

	private boolean persistPixelsToPlayerInventory(Player player, byte[] packedPixels) {
		PlayerInventory inv = player.getInventory();

		for(int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack item = inv.getItem(slot);
			if(isMatchingTreasureMap(item)) {
				savePixelsToItem(item, packedPixels);
				inv.setItem(slot, item);
				player.updateInventory();
				return true;
			}
		}

		ItemStack offhand = inv.getItemInOffHand();
		if(isMatchingTreasureMap(offhand)) {
			savePixelsToItem(offhand, packedPixels);
			inv.setItemInOffHand(offhand);
			player.updateInventory();
			return true;
		}
		return false;
	}

	private boolean isMatchingTreasureMap(ItemStack item) {
		if( item == null || item.getType() != Material.FILLED_MAP) {
			return false;
		}
		if(!(item.getItemMeta() instanceof MapMeta meta)) {
			return false;
		}

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		String itemType = pdc.get(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING);
		if(!Constants.DMC_MAP_ITEM_TYPE.equals(itemType)) {
			return false;
		}
		String itemMapId = pdc.get(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING);
		return this.mapId != null && this.mapId.equals(itemMapId);
	}

	private boolean savePixelsToItem(ItemStack item, byte[] packedPixels) {
		if(!(item.getItemMeta() instanceof MapMeta meta)) {
			return false;
		}
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_MAP_PIXELS, PersistentDataType.BYTE_ARRAY, packedPixels);
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_MAP_PIXEL_FORMAT, PersistentDataType.INTEGER, 1);
		item.setItemMeta(meta);
		return true;
	}

	private static record StainBlob(float cx, float cy, float radius, float strength) { }
}
