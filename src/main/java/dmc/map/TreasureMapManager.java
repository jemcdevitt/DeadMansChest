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
import dmc.utils.PixelPacker;
import dmc.utils.UtilFuncs;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureMapManager implements Listener {
	private static final int SEARCH_CANDIDATES_PER_TICK = 32;
	private static final int SEARCH_RADIUS_STEP = 8;
	private static final int SEARCH_DIRECTIONS = 16;
	
	final DeadMansChestPlugin plugin;
	final Configuration cfg;
	static Random RNG;

	private final Set<String> activePendingSearches = new HashSet<>();
	
	public TreasureMapManager(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		cfg = plugin.getConfiguration();
		
		RNG = new Random(System.currentTimeMillis()*System.currentTimeMillis());
	}

	public void generateTreasureMap(Player player, Location barrelLocation, Integer treasureLevel, String pirateName, String pirateAdjective) {
		if( player == null || barrelLocation == null || barrelLocation.getWorld() == null) {
			return;
		}
		ItemStack map = createPendingTreasureMap(barrelLocation, treasureLevel, pirateName, pirateAdjective);
		if( map == null ) {
			LOG(1, player, "There was an error creating the treasure map");
			return;
		}
		player.getInventory().addItem(map);
		TreasureMapData mapData = TreasureMapData.fromItem(map);
		if( mapData != null) {
			startPendingSearch(player, map, mapData);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		for(ItemStack item : player.getInventory().getContents()) {
			if( item != null ) {
				repairTreasureMap(item, player);
			}
		}
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if(!(event.getPlayer() instanceof Player player)) {
			return;
		}

		Inventory inventory = event.getInventory();
		for(ItemStack item : inventory.getContents()) {
			repairTreasureMap(item, player);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		repairTreasureMap(event.getItem(), event.getPlayer());
	}
	

	private ItemStack createPendingTreasureMap(Location origin, Integer treasureLevel, String pirateName, String pirateAdjective) {
		String uniqueId = UUID.randomUUID().toString();

		World world = origin.getWorld();
		int level = treasureLevel != null ? treasureLevel : RNG.nextInt(3)+1;

		ItemStack map = new ItemStack(Material.FILLED_MAP);
		
		MapView view = createPendingMapView(world, uniqueId, map, origin, level);

		TreasureMapData mapData = new TreasureMapData(uniqueId, world.getUID(), origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), level, false, pirateName, pirateAdjective);
		mapData.setPending(true);
		mapData.setToItem(map, view);
		return map;
	}

	private MapView createPendingMapView(World world, String mapId, ItemStack map, Location origin, int treasureLevel) {
		MapView view = Bukkit.createMap(world);

		view.setCenterX(origin.getBlockX());
		view.setCenterZ(origin.getBlockZ());
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);

		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		view.addRenderer(new TreasureMapRenderer(plugin, mapId,  map, world, treasureLevel));
		return view;
	}

	private MapView createResolvedMapView(World world, String mapId, ItemStack map, Location treasureLocation, int treasureLevel, PixelPacker packer) {
		MapView view= Bukkit.createMap(world);
		view.setCenterX(treasureLocation.getBlockX());
		view.setCenterZ(treasureLocation.getBlockZ());
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);
		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		TreasureMapImage image = new TreasureMapImage(treasureLocation, packer);
		view.addRenderer(new TreasureMapRenderer(plugin, mapId, map, image, treasureLocation, treasureLevel));
		return view;
	}

	private void repairTreasureMap(ItemStack item, Player player) {
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

		Location mapLoc = mapData.toLocation();
		if( mapLoc == null) {
			return;
		}
		
		MapView view = null;
		if( mapData.isPending()) {
			view = createPendingMapView(world, mapData.getMapId(), item, mapLoc, mapData.getTreasureLevel());
			if( player != null) {
				startPendingSearch(player, item, mapData);
			}
		} else {
			PixelPacker packer = null;
			byte[] packedBits = mapData.getPackedBits();
			if( packedBits != null) {
				packer = new PixelPacker(packedBits);
			}
			view = createResolvedMapView(world, mapData.getMapId(), item, mapLoc, mapData.getTreasureLevel(), packer);
		}
		mapData.setToItem(item,view);
	}

	private void startPendingSearch(Player player, ItemStack map, TreasureMapData mapData) {
		if( player == null || map == null || mapData == null || !mapData.isPending()) {
			return;
		}
		if( activePendingSearches.contains(mapData.getMapId())) {
			return;
		}

		Location origin = mapData.toLocation();
		if( origin == null || origin.getWorld() == null) {
			return;
		}

		activePendingSearches.add(mapData.getMapId());
		new PendingTreasureSearch(player.getUniqueId(), mapData.getMapId(), origin, mapData.getTreasureLevel()).runTaskTimer(plugin, 20L, 1L);
	}

	private void completePendingMap(Player player, String mapId, Location treasureLocation, int treasureLevel) {
		if( player == null || treasureLocation == null ) {
			return;
		}

		boolean updated = false;
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		for(int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			TreasureMapData mapData = TreasureMapData.fromItem(item);
			if( mapData == null || !mapId.equals(mapData.getMapId()) || !mapData.isPending()) {
				continue;
			}

			MapView view = createResolvedMapView(treasureLocation.getWorld(), mapId, item, treasureLocation, treasureLevel, null);
			mapData.setTreasureLocation(treasureLocation);
			mapData.setPending(false);
			mapData.setToItem(item, view);
			inventory.setItem(i, item);
			plugin.getTreasureManager().createTreasureMarker(item);
			updated = true;
		}

		if( updated ) {
			player.sendMessage("The soggy map dries enough to reveal a cursed mark...");
		} else {
			LOG(0,"Pending treasure map %s resolved, but no matching map found in %s's inventory", mapId, player.getName());
		}
	}


	private class PendingTreasureSearch extends BukkitRunnable {
		private final UUID playerId;
		private final String mapId;
		private final Location origin;
		private final int treasureLevel;
		private final World world;
		private final int y;
		private final int centerX;
		private final int centerZ;
		private final int minRadius;
		private final int maxRadius;
		private final int totalRings;

		private int radius;
		private int ringsChecked = 0;
		private int startDir = RNG.nextInt(SEARCH_DIRECTIONS);
		private int dir = startDir;

		PendingTreasureSearch(UUID playerId, String mapId, Location origin, int treasureLevel) {
			this.playerId = playerId;
			this.mapId = mapId;
			this.origin = origin.clone();
			this.treasureLevel = treasureLevel;
			this.world = origin.getWorld();
			this.y = world.getSeaLevel() - 1;
			this.centerX = origin.getBlockX();
			this.centerZ = origin.getBlockZ();

			int min = cfg.getTreasureConfig().min_distance();
			int max = cfg.getTreasureConfig().max_distance();

			if( max < min ) {
				max = min;
			}
			this.minRadius = min;
			this.maxRadius = max;
			this.totalRings = Math.max(1, ((maxRadius - minRadius) / SEARCH_RADIUS_STEP) + 1);
			this.radius = maxRadius <= minRadius ? minRadius : RNG.nextInt(maxRadius - minRadius + 1) + minRadius;
		}

		@Override
		public void run() {
			Player player = Bukkit.getPlayer(playerId);
			if( player == null || !player.isOnline()) {
				activePendingSearches.remove(mapId);
				cancel();
				return;
			}

			for(int i = 0; i < SEARCH_CANDIDATES_PER_TICK; i++) {
				Location treasureLocation = nextValidTreasureLocation();
				if( treasureLocation != null ) {
					LOG(0,"Found location for treasure at (%d, %d, %d)", treasureLocation.getBlockX(),treasureLocation.getBlockY(),treasureLocation.getBlockZ());
					completePendingMap(player, mapId, treasureLocation, treasureLevel);
					activePendingSearches.remove(mapId);
					cancel();
					return;
				}

				if(isExhausted() ) {
					LOG(0,"No location found for pending treasure map %s", mapId);
					activePendingSearches.remove(mapId);
					cancel();
					return;
				}
			}
		}
		private Location nextValidTreasureLocation() {
			int checkDir = dir;
			dir = (dir + 1) % SEARCH_DIRECTIONS;
			if( dir == startDir ) {
				ringsChecked++;
				radius += SEARCH_RADIUS_STEP;
				if(radius > maxRadius) {
					radius = minRadius;
				}
				startDir = RNG.nextInt(SEARCH_DIRECTIONS);
				dir = startDir;
			}

			double angle = checkDir * 22.5;
			double rads = Math.toRadians(angle);
			int chestX = (int)(centerX + radius * Math.cos(rads));
			int chestZ = (int)(centerZ + radius * Math.sin(rads));

			// Avoid generating chunks or doing biome/surface checks against chunks
			// the world has not naturally created yet.
			Chunk chunk = world.getChunkAt(chestX >> 4, chestZ >> 4, false);
			if( chunk == null ) {
				return null;
			}

			if( UtilFuncs.isWaterBiome(world.getBiome(chestX, y, chestZ))) {
				return null;
			}

			Block highestBlock = world.getHighestBlockAt(chestX, chestZ);
			if(!UtilFuncs.canBlockHoldTreasureChest(highestBlock)) {
				return null;
			}

			return highestBlock.getLocation().clone().add(0,1,0);
		}

		private boolean isExhausted() {
			return ringsChecked >= totalRings;
		}
	}
}
