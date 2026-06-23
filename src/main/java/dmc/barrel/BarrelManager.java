package dmc.barrel;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * Handles the spawning of barrels
 */

import dmc.Configuration;
import dmc.Constants;
import dmc.DeadMansChestPlugin;
import dmc.utils.UtilFuncs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static dmc.DeadMansChestPlugin.LOG;

public class BarrelManager implements Listener {
	static float amplitude = 0.15f;
	static float frequency = 1.5f;
	static Random RNG;
	
	final DeadMansChestPlugin plugin;
	final Configuration config;
	long lastTime;

	private HashMap<String, FloatingBarrel> barrels = new HashMap<>();

	public BarrelManager(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		this.lastTime = System.currentTimeMillis();
		RNG = new Random(this.lastTime);

		new BukkitRunnable() {
			@Override
			public void run() {
				BarrelManager.this.doSpawnCheck();
			}
		}.runTaskTimer(plugin, 0, config.getBarrelsConfig().seconds_between_spawn_checks() * 20);

		new BukkitRunnable() {
			@Override
			public void run() {
				BarrelManager.this.doUpdate();
			}
		}.runTaskTimer(plugin, 0, Constants.UPDATE_TICKS);
		
	}

	public void flushAllBarrels() {
		LOG(0,"BarrelManager: flushing all %d barrels", barrels.size());
		for(FloatingBarrel barrel : barrels.values() ) {
			barrel.removeFromWorld();
		}
		barrels.clear();
	}

	public void showAllBarrels(Player player) {
		for(FloatingBarrel barrel : barrels.values()) {
			barrel.showInfo(player, true);
		}
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

		if(id == null ) {
			LOG(1,"Barrel found without an id");
			return;
		}
		FloatingBarrel barrel = barrels.get(id);
		if( barrel == null ) {
			LOG(1,"Barrel %s not found in manager", id);
			return;
		}

		barrel.removeFromWorld();
		barrels.remove(barrel.getUniqueId());
		
		Player player = event.getPlayer();
		player.getWorld().playSound(player, Sound.BLOCK_BAMBOO_BREAK, 2.0f, 0.5f);
		Integer treasureLevel = inter.getPersistentDataContainer().get(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER);
		if( treasureLevel == null ) {
			LOG(1,"Barrel didn't have a defined treasure level, defaulting to 1");
			treasureLevel = 1;
		}
		
		plugin.getMapManager().generateTreasureMap(player, barrel.getLocation(), treasureLevel);

	}
	

	private void doUpdate() {
		long now = System.currentTimeMillis();
		long tick = now - lastTime;
		lastTime = now;

		float bounce = (float)(Math.sin((now/1000.0) * frequency) * amplitude);
		for(Iterator<Map.Entry<String,FloatingBarrel>> iterator = barrels.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String,FloatingBarrel> entry = iterator.next();
			FloatingBarrel mb = entry.getValue();
			
			if(!mb.update(tick, bounce)) {
				mb.removeFromWorld();
				iterator.remove();
			}
		}
	}

	private void doSpawnCheck() {
		for(World world : plugin.getServer().getWorlds()) {
			if( DeadMansChestPlugin.configuration.isWorldAllowed(world) ) {
				for(Player player : world.getPlayers()) {
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
							Location waterBlock = new Location(world, (double)blockX + 0.5, (double)(world.getSeaLevel()-1) + 0.70, (double)blockZ + 0.5);
							if( isFarEnoughFromOthers(waterBlock)) {
								FloatingBarrel barrel = new FloatingBarrel(waterBlock, config);
								barrels.put(barrel.getUniqueId(), barrel);
								barrel.showInfo(null, false);
								spawnedABarrel = true;
								break;
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

	//currently this is a global maximum across all
	//worlds allowed, TODO will be to make this on a
	//per world basis.
	public boolean hitMaxBarrels(World world) {
		if (barrels.size() >= config.getBarrelsConfig().max_active())
			return true;
		return false;
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
				if( UtilFuncs.isWaterBiome(biome)) {
					hasOceanNearby = true;
				}
			}
		}
		return hasLandNearby && hasOceanNearby;
	}

	protected boolean isFarEnoughFromOthers(Location loc) {
		double minDistSq = config.getBarrelsConfig().min_distance_between_barrels() * config.getBarrelsConfig().min_distance_between_barrels();
		boolean isFarEnough = true;

		for(FloatingBarrel barrel : barrels.values()) {
			double distanceToSq = barrel.getLocation().distanceSquared(loc);
			if( distanceToSq <= minDistSq) {
				isFarEnough = false;
				break;
			}
		}
		return isFarEnough;
	}

	
}
