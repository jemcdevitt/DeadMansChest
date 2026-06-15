package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BiomeSearchResult;
import org.joml.Matrix4f;

import static dmc.DeadMansChestPlugin.LOG;
import static dmc.DeadMansChestPlugin.RNG;

public class TreasureMapManager implements Listener {
	static float amplitude = 0.15f;
	static float frequency = 1.5f;
	final DeadMansChestPlugin plugin;
	HashMap<String, MapBarrel> barrels = new HashMap<>();
	long lastTime;
	


	public TreasureMapManager(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		lastTime = System.currentTimeMillis();
	}

	public void flushAllBarrels() {
		for(MapBarrel barrel : barrels.values() ) {
			barrel.removeFromWorld();
		}
		barrels.clear();
	}

	public void showAllBarrels() {
		for(MapBarrel barrel : barrels.values()) {
			barrel.showInfo(true);
		}
	}

	public void spawnCheck() {
		for(World world : plugin.getServer().getWorlds()) {
			if( DeadMansChestPlugin.configuration.isWorldAllowed(world) ) {
				for(Player player : world.getPlayers()) {
					LOG(0,"Doing spawn check: %d of %d", barrels.size(), DeadMansChestPlugin.configuration.maxBarrelsSpawned);
					if( hitMaxBarrels(world)) {
						LOG(0,"Max barrels in world");
						break;
					}
					
					//pick a random loaded chunk within view distance
					Chunk randomChunk = getRandomVisibleChunk(player);
					if(randomChunk == null ) {
						LOG(0,"No random chunk found");
						continue;
					}
					int chunkX = randomChunk.getX() * 16;
					int chunkZ = randomChunk.getZ() * 16;
					int blocksToCheck = RNG.nextInt(25) + 7;
					boolean spawnedABarrel = false;
					
					for( int b = 0; b < blocksToCheck; b++) {
						int blockX = chunkX + RNG.nextInt(16);
						int blockZ = chunkZ + RNG.nextInt(16);
						if( isCoastalWater(world, blockX, blockZ) ) {
							LOG(0,"Found coastal water at %d,%d", blockX, blockZ);
							Location waterBlock = new Location(world, (double)blockX + 0.5, (double)(world.getSeaLevel()-1) + 0.70, (double)blockZ + 0.5);
							if( isFarEnoughFromOthers(waterBlock)) {
								MapBarrel barrel = new MapBarrel(waterBlock);
								barrels.put(barrel.getUniqueId(), barrel);
								barrel.showInfo(false);
								spawnedABarrel = true;
								break;
							} else {
								LOG(0,"Barrel too close to other barrels");
							}
						}
					}
					if( spawnedABarrel ) {
						//don't let them all spawn near one player
						break;
					}
				}
			}
		}
	}
	
	public void update() {
		long now = System.currentTimeMillis();
		long tick = now - lastTime;
		lastTime = now;

		float bounce = (float)(Math.sin((now/1000.0) * frequency) * amplitude);
		for(Iterator<Map.Entry<String,MapBarrel>> iterator = barrels.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String,MapBarrel> entry = iterator.next();
			MapBarrel mb = entry.getValue();
			
			if(!mb.update(tick, bounce)) {
				mb.removeFromWorld();
				iterator.remove();
			}
		}
	}

	
	public void repairTreasureMap(ItemStack item) {
		if( item == null || item.getType() != Material.FILLED_MAP) {
			return;
		}

		if(!(item.getItemMeta() instanceof MapMeta meta)) {
			return;
		}

		PersistentDataContainer pdc = meta.getPersistentDataContainer();

		String itemType = pdc.get(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING);
		if(!Constants.DMC_MAP_ITEM_TYPE.equals(itemType))
			return;

		String worldId = pdc.get(Constants.DMC_MAP_WORLD_ID, PersistentDataType.STRING);
		String uniqueId = pdc.get(Constants.DMC_MAP_ID_KEY, PersistentDataType.STRING);
		Integer x = pdc.get(Constants.DMC_TREASURE_X_VAL, PersistentDataType.INTEGER);
		Integer y = pdc.get(Constants.DMC_TREASURE_Y_VAL, PersistentDataType.INTEGER);
		Integer z = pdc.get(Constants.DMC_TREASURE_Z_VAL, PersistentDataType.INTEGER);

		if( worldId == null || x == null || y == null || z == null ) {
			LOG(1,"Treasure map is incomplete, can't restore");
			return;
		}

		World world = Bukkit.getWorld(UUID.fromString(worldId));
		if( world == null ) {
			LOG(1,"Treasure map is for a world that doesn't exist");
			return;
		}

		Location treasureLoc = new Location(world, x, y, z);
		MapView view = Bukkit.createMap(world);
		view.setCenterX(treasureLoc.getBlockX());
		view.setCenterZ(treasureLoc.getBlockZ());
		view.setScale(MapView.Scale.NORMAL);
		view.setTrackingPosition(true);
		view.setUnlimitedTracking(true);

		for(MapRenderer renderer : view.getRenderers()) {
			view.removeRenderer(renderer);
		}

		view.addRenderer(new TreasureMapRenderer(treasureLoc));

		meta.setMapView(view);

		
		meta.setMapView(view);
		item.setItemMeta(meta);
	}
	//currently this is a global maximum across all
	//worlds allowed, TODO will be to make this on a
	//per world basis.
	public boolean hitMaxBarrels(World world) {
		if (barrels.size() >= DeadMansChestPlugin.configuration.maxBarrelsSpawned)
			return true;
		return false;
	}

	public ItemStack generateTreasureMap(MapBarrel barrel) {

		World world = barrel.getLocation().getWorld();
		int y = world.getSeaLevel() - 1;
		int centerX = barrel.getLocation().getBlockX();
		int centerZ = barrel.getLocation().getBlockZ();
		int chestX = 0;
		int chestZ = 0;
		boolean foundPlace = false;
		Location chestLocation = null;

		//let's try a simple circle around the barrel location
		//at the given radius to see if a spot fits the bill
		int startRadius = RNG.nextInt(900);
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

				if( isWaterBiome(world.getBiome(chestX, y, chestZ))) {
					continue;
				}

				Chunk chunk = world.getChunkAt(chestX, chestZ, false);
				//if null, chunk not generated, we won't cause generation
				if( chunk == null ) {
					continue;
				}
				
				//if chunk is not generated this will return air
				Block highestBlock = world.getHighestBlockAt(chestX, chestZ);
				if(!canBlockHoldTreasureChest(highestBlock)) {
					continue;
				}

				foundPlace = true;
				chestLocation = highestBlock.getLocation().clone();
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
			spawnTreasureMarker(chestLocation);
		} else {
			LOG(0,"No location found for treasure");
			return null;
		}
		ItemStack map = new TreasureMap(chestLocation).createItemStack();
		return map;
	}



	/**********************************************
   *        Event Handlers                      *
	 *********************************************/
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if( !(event.getRightClicked() instanceof Interaction inter)) {
			return;
		}
 		if(!Constants.DMC_BARREL_ITEM_TYPE.equals(inter.getPersistentDataContainer().get(Constants.ITEM_TYPE_KEY,PersistentDataType.STRING))) {
			return;
		}
		String id = inter.getPersistentDataContainer().get(Constants.DMC_BARREL_ID_KEY, PersistentDataType.STRING);
		LOG(0,"Player interacted with barrel %s", id);

		if(id == null ) {
			LOG(1,"Barrel found without an id");
			return;
		}
		MapBarrel barrel = barrels.get(id);
		if( barrel == null ) {
			LOG(1,"Barrel %s not found in manager", id);
			return;
		}

		ItemStack map = generateTreasureMap(barrel);
		Player player = event.getPlayer();
		if( map != null ) {
			if( map != null && player != null )
				player.getInventory().addItem(map);
		} else {
			LOG(1,player,"There was an error generating the treasure location");
		}
		barrel.removeFromWorld();
		barrels.remove(barrel.getUniqueId());
		player.getWorld().playSound(player, Sound.BLOCK_BAMBOO_BREAK, 1.0f, 1.0f);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for(ItemStack item : event.getPlayer().getInventory().getContents()) {
			repairTreasureMap(item);
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

	/**********************************************
   *        Private API                         *
	 *********************************************/
	//this will be replaced with an interaction
	//that will trigger mobs when a player gets close and only
	//when that happens will the loot chest actually be generated.
	private void spawnTreasureMarker(Location loc) {
		loc.clone().add(0,1,0).getBlock().setType(Material.OAK_FENCE);
		loc.clone().add(0,2,0).getBlock().setType(Material.SKELETON_SKULL);
	}

	private boolean canBlockHoldTreasureChest(Block block) {
		if( !block.isSolid()) {
			return false;
		}
		Material mat = block.getType();
		if( Tag.LEAVES.isTagged(mat))
			return false;
		if( Tag.FENCES.isTagged(mat))
			return false;
		if( Tag.WALLS.isTagged(mat))
			return false;
		if( Tag.SNOW.isTagged(mat))
			return false;

		//top slabs would be ok but over complicate this logic
		//so just banning slabs
		if( Tag.SLABS.isTagged(mat))
			return false;
		if( Tag.STAIRS.isTagged(mat))
			return false;

		if( mat == Material.LAVA )
			return false;
		return true;
	}

	private boolean isCoastalWater(World world, int x, int z) {
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

	private boolean isWaterBiome(Biome biome) {
		return biome.toString().toUpperCase().contains("OCEAN") || biome == Biome.RIVER;
	}
	
	protected boolean isFarEnoughFromOthers(Location loc) {
		double minDistSq = plugin.configuration.minDistanceBetweenBarrels * plugin.configuration.minDistanceBetweenBarrels;
		boolean isFarEnough = true;

		for(MapBarrel barrel : barrels.values()) {
			double distanceToSq = barrel.getLocation().distanceSquared(loc);
			if( distanceToSq <= minDistSq) {
				isFarEnough = false;
				break;
			}
		}
		return isFarEnough;
	}

	
	protected Chunk getRandomVisibleChunk(Player player) {
		Location loc = player.getLocation();
		List<Chunk> visible = new ArrayList<>();
		Chunk[] loadedChunks = loc.getWorld().getLoadedChunks();

		for(Chunk chunk :  loadedChunks ) {
			if(chunk.getPlayersSeeingChunk().contains(player)) {
				visible.add(chunk);
			}
		}

		if( visible.size() != 0 ) { //should never be true but check anyway
			int which = RNG.nextInt(visible.size());
			return visible.get(which);
		}
		return null;
	}
}
