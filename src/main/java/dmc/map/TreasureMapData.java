package dmc.map;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Contains the specific information a treasure map carries.
 */

import dmc.Constants;
import java.util.Arrays;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureMapData {
	String mapId;
	UUID worldId;
	int treasureX;
	int treasureY;
	int treasureZ;
	int treasureLevel;
	boolean treasureMarkerCreated;
	boolean pending;
	String treasureMarkerUniqueId;
	byte[] packedBits;


	public TreasureMapData(String mapId, UUID worldId, int treasureX, int treasureY, int treasureZ, int treasureLevel, boolean treasureMarkerCreated) {
		this.mapId = mapId;
		this.worldId = worldId;
		this.treasureX = treasureX;
		this.treasureY = treasureY;
		this.treasureZ = treasureZ;
		this.treasureLevel = treasureLevel;
		this.treasureMarkerCreated = treasureMarkerCreated;
		this.pending = false;
	}


	public String getMapId() {
		return this.mapId;
	}
	public UUID getWorldId() {
		return this.worldId;
	}
	public int getTreasureX() {
		return this.treasureX;
	}
	public int getTreasureY() {
		return this.treasureY;
	}
	public int getTreasureZ() {
		return this.treasureZ;
	}

	public boolean isPending() {
		return this.pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public void setTreasureLocation(Location location) {
		if( location == null )
			return;
		this.worldId = location.getWorld().getUID();
		this.treasureX = location.getBlockX();
		this.treasureY = location.getBlockY();
		this.treasureZ = location.getBlockZ();
	}
	
	public byte[] getPackedBits() {
		return this.packedBits;
	}
	public void setPackedBits(byte[] bits) {
		this.packedBits = Arrays.copyOf(bits, bits.length);
	}

	public void setTreasureMarkerUniqueId(String id) {
		this.treasureMarkerUniqueId = id;
	}

	public String getTreasureMarkerUniqueId() {
		return this.treasureMarkerUniqueId;
	}
	
	public boolean wasTreasureMarkerCreated() {
		return this.treasureMarkerCreated;
	}
	
	public void setTreasureMarkerCreated(boolean f) {
		this.treasureMarkerCreated = f;
	}
	public int getTreasureLevel() {
		return this.treasureLevel;
	}
	
	public Location toLocation() {
		World world = Bukkit.getWorld(worldId);

		if( world == null )
			return null;

		return new Location(world, treasureX, treasureY, treasureZ);
	}

	public void setToItem(ItemStack item,MapView view ) {
		if( item == null || item.getType() != Material.FILLED_MAP)
			return;

		if( !(item.getItemMeta() instanceof MapMeta meta)) {
			return;
		}
		meta.displayName(Component.text("A soggy pirate's treasure map"));

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING, Constants.DMC_MAP_ITEM_TYPE);
		pdc.set(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING, mapId);
		pdc.set(Constants.DMC_MAP_WORLD_ID, PersistentDataType.STRING, worldId.toString());
		pdc.set(Constants.DMC_TREASURE_X_VAL, PersistentDataType.INTEGER, treasureX);
		pdc.set(Constants.DMC_TREASURE_Y_VAL, PersistentDataType.INTEGER, treasureY);
		pdc.set(Constants.DMC_TREASURE_Z_VAL, PersistentDataType.INTEGER, treasureZ);
		pdc.set(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER, treasureLevel);
		pdc.set(Constants.DMC_TREASURE_MARKER_CREATED, PersistentDataType.BOOLEAN, treasureMarkerCreated);
		pdc.set(Constants.DMC_TREASURE_MAP_PENDING, PersistentDataType.BOOLEAN, pending);
		
		if( treasureMarkerUniqueId != null )
			pdc.set(Constants.DMC_TREASURE_MARKER_ID_KEY, PersistentDataType.STRING, treasureMarkerUniqueId);
		
		if( packedBits != null )
			pdc.set(Constants.DMC_TREASURE_MAP_PIXELS, PersistentDataType.BYTE_ARRAY, packedBits);

		if( view != null )
			meta.setMapView(view);
		item.setItemMeta(meta);
	}
	
	public static TreasureMapData fromItem(ItemStack item) {
		if( item == null || item.getType() != Material.FILLED_MAP) {
			return null;
		}

		if( !(item.getItemMeta() instanceof MapMeta meta)) {
			LOG(0, "Item meta is not MapMeta");
			return null;
		}

		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		String itemType = pdc.get(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING);
		if(!Constants.DMC_MAP_ITEM_TYPE.equals(itemType)) {
			return null;
		}

		String mapId = pdc.get(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING);
		String worldIdText = pdc.get(Constants.DMC_MAP_WORLD_ID, PersistentDataType.STRING);
		Integer x = pdc.get(Constants.DMC_TREASURE_X_VAL, PersistentDataType.INTEGER);
		Integer y = pdc.get(Constants.DMC_TREASURE_Y_VAL, PersistentDataType.INTEGER);
		Integer z = pdc.get(Constants.DMC_TREASURE_Z_VAL, PersistentDataType.INTEGER);
		Integer level = pdc.get(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER);
		Boolean markerCreated = pdc.get(Constants.DMC_TREASURE_MARKER_CREATED, PersistentDataType.BOOLEAN);
		Boolean pending = pdc.get(Constants.DMC_TREASURE_MAP_PENDING, PersistentDataType.BOOLEAN);
		String treasureMarkerId = pdc.get(Constants.DMC_TREASURE_MARKER_ID_KEY, PersistentDataType.STRING);
		byte[] packedBits = pdc.get(Constants.DMC_TREASURE_MAP_PIXELS, PersistentDataType.BYTE_ARRAY);

		if( mapId == null ||
				worldIdText == null ||
				x == null ||
				y == null ||
				z == null ||
				level == null ||
				markerCreated == null) {
			LOG(0,"Somthing missing: mapId: %s, worldIdText: %s, x: %s, y: %s, z: %s, level: %s, marker: %s",
					mapId==null?"N":"Y",
					worldIdText==null?"N":"Y",
					x==null?"N":"Y",
					y==null?"N":"Y",
					z==null?"N":"Y",
					level==null?"N":"Y",
					markerCreated==null?"N":"Y");
			return null;
		}

		UUID worldId = null;

		try {
			worldId = UUID.fromString(worldIdText);
		} catch(IllegalArgumentException ex) {
			return null;
		}

		TreasureMapData data = new TreasureMapData(mapId, worldId, x, y, z, level, markerCreated);
		data.setPending(pending != null && pending);
		if( packedBits != null )
			data.setPackedBits(packedBits);
		if( treasureMarkerId != null )
			data.setTreasureMarkerUniqueId(treasureMarkerId);
		return data;
	}
}
