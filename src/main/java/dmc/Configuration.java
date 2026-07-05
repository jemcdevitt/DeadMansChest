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
	
	static public final int     BARRELS_SECONDS_BETWEEN_SPAWN_CHECKS = 30;
	static public final int     BARRELS_MAX_ACTIVE = 30;
	static public final int     BARRELS_MIN_LIFETIME_MINUTES = 4;
	static public final int     BARRELS_MAX_LIFETIME_MINUTES = 12;
	static public final int     BARRELS_MIN_DISTANCE_BETWEEN_BARRELS = 128;

	static public final int     TREASURE_MIN_DISTANCE = 300;
	static public final int     TREASURE_MAX_DISTANCE = 1200;

	final DeadMansChestPlugin plugin;
	private boolean debugOn = DEBUG_ON_DEFAULT;

	private BarrelsConfig  barrelsConfig;
	private TreasureConfig treasureConfig;
	private MessagesConfig messagesConfig;

	Configuration(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
		loadConfiguration();
	}

	public boolean isDebugOn() {
		return this.debugOn;
	}

	public BarrelsConfig getBarrelsConfig() {
		return this.barrelsConfig;
	}

	public TreasureConfig getTreasureConfig() {
		return this.treasureConfig;
	}
	public MessagesConfig getMessagesConfig() {
		return this.messagesConfig;
	}

	
	public void showInfo() {
		LOG(10,"Debug is " + (debugOn?"ON":"OFF"));
		barrelsConfig.showInfo();
		treasureConfig.showInfo();
		messagesConfig.showInfo();
	}

	public void loadConfiguration() {
		FileConfiguration cfg = plugin.getConfig();
		if( cfg == null ) {
			LOG(1,"Configuration should not be null");
		} else {
			debugOn = cfg.getBoolean("debug", false);
			barrelsConfig = BarrelsConfig.get(cfg);
			treasureConfig = TreasureConfig.get(cfg);
			messagesConfig = MessagesConfig.get(cfg);
		}	
	}


	
	//allow overworld only for now
	//may add a config for other worlds in the
	//future
	public boolean isWorldAllowed(World world) {
		return world.getEnvironment() == World.Environment.NORMAL;
	}

	static public record TreasureConfig(int min_distance, int max_distance) {
		static TreasureConfig get(FileConfiguration cfg) {
			int min_distance = cfg.getInt("treasure.min-distance", TREASURE_MIN_DISTANCE);
			int max_distance = cfg.getInt("treasure.max-distance", TREASURE_MAX_DISTANCE);
			return new TreasureConfig(min_distance, max_distance);
		}

		void showInfo() {
			LOG(10,"Treasure: min_distance: %d, max_distance: %d", min_distance(), max_distance());
		}
	}
	
	static public record BarrelsConfig(int max_active, int min_lifetime_minutes, int max_lifetime_minutes, int seconds_between_spawn_checks, int min_distance_between_barrels) {
		static BarrelsConfig get(FileConfiguration cfg) {
			int max_active = cfg.getInt("barrels.max-active", BARRELS_MAX_ACTIVE);
			int max_lifetime_minutes = cfg.getInt("barrels.max-lifetime-minutes", BARRELS_MAX_LIFETIME_MINUTES);
			int min_lifetime_minutes = cfg.getInt("barrels.min-lifetime-minutes", BARRELS_MIN_LIFETIME_MINUTES);
			int seconds_between_spawn_checks = cfg.getInt("barrels.seconds-between-spawn-checks", BARRELS_SECONDS_BETWEEN_SPAWN_CHECKS);
			int min_distance_between_barrels = cfg.getInt("barrels.min-distance-between-barrels", BARRELS_MIN_DISTANCE_BETWEEN_BARRELS);
			return new BarrelsConfig(max_active, min_lifetime_minutes, max_lifetime_minutes, seconds_between_spawn_checks, min_distance_between_barrels);
		}

		void showInfo() {
			LOG(10,"Barrels:%n  max_active: %d%n  min_lifetime_minutes: %d%n  max_lifetime_minutes: %d%n  seconds_between_spawn_checks: %d%n  min_distance_between_barrels: %d",
					max_active(), min_lifetime_minutes(), max_lifetime_minutes(), seconds_between_spawn_checks(), min_distance_between_barrels());
		}

	}

	static public record MessagesConfig(TreasureRecoveredConfig treasure_recovered) {	
		static MessagesConfig get(FileConfiguration cfg) {
			TreasureRecoveredConfig trc = TreasureRecoveredConfig.get(cfg);
			return new MessagesConfig(trc);
		}

		void showInfo() {
			LOG(10,"messages:");
			treasure_recovered().showInfo();
		}
	}

	static public record TreasureRecoveredConfig(boolean enabled, boolean broadcast, String text) {
		static TreasureRecoveredConfig get(FileConfiguration cfg) {
			String text = cfg.getString("messages.treasure-recovered.text", "%player% has recovered the treasure of %adjective% pirate %pirate_name% and broken the curse.");
			boolean enabled = cfg.getBoolean("messages.treasure-recovered.enabled",true);
			boolean broadcast = cfg.getBoolean("messages.treasure-recovered.broadcast", false);
			return new TreasureRecoveredConfig(enabled, broadcast, text);
		}

		void showInfo() {
			LOG(10,"treasure-recovered: enabled: %s, broadcast: %s, text: \"%s\"",
					enabled(), broadcast(), text());
		}
	}
}
