package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.awt.Color;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

public class TreasureMap {
	private final Location treasureLoc;
	private final String uniqueId;

	public TreasureMap(Location treasureLoc) {
		this.treasureLoc = treasureLoc;
		this.uniqueId = UUID.randomUUID().toString();
	}

	public ItemStack createItemStack() {
		World world = treasureLoc.getWorld();


		ItemStack map = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta)map.getItemMeta();
		
		MapView view = Bukkit.createMap(world);
		view.setCenterX(treasureLoc.getBlockX());
		view.setCenterZ(treasureLoc.getBlockZ());
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);

		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		TreasureMapImage image = new TreasureMapImage(treasureLoc);

		view.addRenderer(new TreasureMapRenderer(map, image, treasureLoc));

		meta.setMapView(view);
		meta.displayName(Component.text("A soggy pirate's treasure map"));


		meta.getPersistentDataContainer().set(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING, Constants.DMC_MAP_ITEM_TYPE);
		meta.getPersistentDataContainer().set(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING, uniqueId);
		meta.getPersistentDataContainer().set(Constants.DMC_MAP_WORLD_ID, PersistentDataType.STRING, world.getUID().toString());
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_X_VAL, PersistentDataType.INTEGER, treasureLoc.getBlockX());
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_Y_VAL, PersistentDataType.INTEGER, treasureLoc.getBlockY());	
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_Z_VAL, PersistentDataType.INTEGER, treasureLoc.getBlockZ());
		meta.getPersistentDataContainer().set(Constants.DMC_TREASURE_CLAIMED, PersistentDataType.BOOLEAN, Boolean.FALSE);

		map.setItemMeta(meta);
		
		return map;
	}
}
