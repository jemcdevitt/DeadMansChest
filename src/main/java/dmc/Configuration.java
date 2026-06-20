package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import static dmc.DeadMansChestPlugin.LOG;

public class Configuration {
	static public final boolean DEBUG_ON_DEFAULT = false;
	static public final int     BARREL_SPAWN_CHECK_SECONDS = 5; //30;
	static public final int     MAX_BARRELS_SPAWNED = 30;
	static public final int     MIN_DISTANCE_BETWEEN_BARRELS = 128;

	final DeadMansChestPlugin plugin;
	private boolean debugOn = DEBUG_ON_DEFAULT;
	private int barrelSpawnCheckSeconds = BARREL_SPAWN_CHECK_SECONDS;
	private int maxBarrelsSpawned = MAX_BARRELS_SPAWNED;
	private int minDistanceBetweenBarrels = MIN_DISTANCE_BETWEEN_BARRELS;

	Configuration(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		loadConfiguration();
	}

	public boolean isDebugOn() {
		return this.debugOn;
	}
	
	public int getBarrelSpawnCheckSeconds() {
		return this.barrelSpawnCheckSeconds;
	}
	public int getMaxBarrelsSpawned() {
		return this.maxBarrelsSpawned;
	}
	public int getMinDistanceBetweenBarrels() {
		return this.minDistanceBetweenBarrels; 
	}
	


	
	public void showInfo() {
		LOG(0,"Debug is " + (debugOn?"ON":"OFF"));
		LOG(0,"Barrel spawn check in seconds is " + barrelSpawnCheckSeconds);
		LOG(0,"Max barrels allowed is " + maxBarrelsSpawned);
		LOG(0,"Min distance between barrels in blocks is " + minDistanceBetweenBarrels);
	}

	public void loadConfiguration() {
		FileConfiguration cfg = plugin.getConfig();
		if( cfg == null ) {
			LOG(1,"Configuration should not be null");
		} else {

			debugOn = cfg.getBoolean("debug", false);
			barrelSpawnCheckSeconds = cfg.getInt("barrelSpawnCheckSeconds", BARREL_SPAWN_CHECK_SECONDS);
			maxBarrelsSpawned = cfg.getInt("maxBarrelsSpawned", MAX_BARRELS_SPAWNED);
			minDistanceBetweenBarrels=cfg.getInt("minDistanceBetweenBarrels", MIN_DISTANCE_BETWEEN_BARRELS);
		}	
	}


	
	//allow overworld only for now
	//may add a config for other worlds in the
	//future
	public boolean isWorldAllowed(World world) {
		return world.getEnvironment() == World.Environment.NORMAL;
	}
}
