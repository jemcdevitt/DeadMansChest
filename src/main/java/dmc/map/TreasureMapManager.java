package dmc.map;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Manages the creation, restoration and lifecycle of a treasure map
 */

import dmc.Configuration;
import dmc.Constants;
import dmc.DeadMansChestPlugin;
import dmc.treasure.TreasureMarker;
import dmc.utils.PixelPacker;
import dmc.utils.UtilFuncs;
import java.util.Random;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureMapManager implements Listener {
	final DeadMansChestPlugin plugin;
	final Configuration cfg;
	static Random RNG;

	public TreasureMapManager(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		cfg = plugin.getConfiguration();
		
		RNG = new Random(System.currentTimeMillis()*System.currentTimeMillis());
	}

	public void generateTreasureMap(Player player, Location barrelLocation, Integer treasureLevel) {
		World world = barrelLocation.getWorld();
		int y = world.getSeaLevel() - 1;
		int centerX = barrelLocation.getBlockX();
		int centerZ = barrelLocation.getBlockZ();
		int chestX = 0;
		int chestZ = 0;
		boolean foundPlace = false;
		Location chestLocation = null;

		//let's try a simple circle around the barrel location
		//at the given radius to see if a spot fits the bill
		int startRadius = RNG.nextInt(cfg.getTreasureConfig().max_distance() - cfg.getTreasureConfig().min_distance()) + cfg.getTreasureConfig().min_distance();
		int radius = startRadius;
		boolean radiusWrapCheck = false;
		do {
			int startDir = RNG.nextInt(16);
			int dir = startDir;
			do {
				double angle = dir * 22.5;
				dir = (dir + 1) %16;
				double rads = Math.toRadians(angle);
				chestX = (int)(centerX + (radius+300) * Math.cos(rads));
				chestZ = (int)(centerZ + (radius+300) * Math.sin(rads));

				if( UtilFuncs.isWaterBiome(world.getBiome(chestX, y, chestZ))) {
					continue;
				}

				Chunk chunk = world.getChunkAt(chestX, chestZ, false);
				//if null, chunk not generated, we won't cause generation
				if( chunk == null ) {
					continue;
				}
				
				//if chunk is not generated this will return air
				Block highestBlock = world.getHighestBlockAt(chestX, chestZ);
				if(!UtilFuncs.canBlockHoldTreasureChest(highestBlock)) {
					continue;
				}

				foundPlace = true;
				chestLocation = highestBlock.getLocation().clone().add(0,1,0);
				break;
				
			} while(dir != startDir);
			if( foundPlace)
				break;
			radius = (radius + 8) % 900;
			if(radius < startRadius) {
				//we wrapped past 900
				radiusWrapCheck = true;
			}
		} while(radiusWrapCheck == false || radius < startRadius);
				
		if( foundPlace ) {
			LOG(0,"Found location for treasure at (%d, %d, %d)", chestLocation.getBlockX(), chestLocation.getBlockY(), chestLocation.getBlockZ());
			Chunk chunk = world.getChunkAt(chestLocation.getBlockX(), chestLocation.getBlockZ(), true);  //ensure chunk exists so map doesn't break
			ItemStack map = createTreasureMap(chestLocation, treasureLevel);
			if( map != null ) {
				if( map != null && player != null ) {
					player.getInventory().addItem(map);
					plugin.getTreasureManager().createTreasureMarker(map);
				}
			} else {
				LOG(1,player,"There was an error generating the treasure location");
			}
		} else {
			LOG(0,"No location found for treasure");
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for(ItemStack item : event.getPlayer().getInventory().getContents()) {
			if( item != null ) {
				repairTreasureMap(item);
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();
		for(ItemStack item : inventory.getContents()) {
			repairTreasureMap(item);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		repairTreasureMap(event.getItem());
	}
	

	private ItemStack createTreasureMap(Location location, Integer treasureLevel) {
		String uniqueId = UUID.randomUUID().toString();
		World world = location.getWorld();
		int level = 0;

		if( treasureLevel != null ) {
			level = treasureLevel;
		} else {
			level = RNG.nextInt(3)+1; //levels 1 - 3
		}
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		

		ItemStack map = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta)map.getItemMeta();
		
		MapView view = Bukkit.createMap(world);
		view.setCenterX(x);
		view.setCenterZ(z);
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);

		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		TreasureMapImage image = new TreasureMapImage(location);

		view.addRenderer(new TreasureMapRenderer(plugin, uniqueId, map, image, location, level));

		TreasureMapData mapData = new TreasureMapData(uniqueId, world.getUID(), x, y, z, level, false);
		mapData.setToItem(map, view);
		return map;
	}

	private void repairTreasureMap(ItemStack item) {
		if( item == null)
			return;
		
		TreasureMapData mapData = TreasureMapData.fromItem(item);
		if( mapData == null ) {
			return;
		}

		World world = Bukkit.getWorld(mapData.getWorldId());
		if( world == null ) {
			LOG(1,"Treasure map is for a world that doesn't exist");
			return;
		}

		Location treasureLoc = mapData.toLocation();
		MapView view = Bukkit.createMap(world);
		view.setCenterX(treasureLoc.getBlockX());
		view.setCenterZ(treasureLoc.getBlockZ());
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);

		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		byte[] packedBits = mapData.getPackedBits();
		PixelPacker packer = null;
		if( packedBits != null ) {
			packer = new PixelPacker(packedBits);
		}
		TreasureMapImage image = new TreasureMapImage(treasureLoc, packer);
		view.addRenderer(new TreasureMapRenderer(plugin, mapData.getMapId(), item, image, treasureLoc, mapData.getTreasureLevel()));

		mapData.setToItem(item, view);
	}
}
