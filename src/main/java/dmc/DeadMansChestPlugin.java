package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.barrel.BarrelManager;
import dmc.loot.LootManager;
import dmc.map.TreasureMapManager;
import dmc.treasure.TreasureChest;
import dmc.treasure.TreasureManager;
import dmc.utils.UtilFuncs;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Light;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Vector3f;

/**
 * The primary plugin.  Currently there are no permission
 * checks in place and all of the commands are available to
 * any player.
 */
public class DeadMansChestPlugin extends JavaPlugin {
	private static Logger logger;
	public static Random RNG;
	public static Configuration configuration;
	public BarrelManager barrelManager;
	public TreasureMapManager mapManager;
	public TreasureManager treasureManager;
	public LootManager lootManager;

	@Override
	public void onEnable() {
		RNG = new Random(System.currentTimeMillis());
		logger = getLogger();

		saveDefaultConfig();
		configuration = new Configuration(this);

		
		configuration.showInfo();

		barrelManager = new BarrelManager(this);
		getServer().getPluginManager().registerEvents(barrelManager, this);

		mapManager = new TreasureMapManager(this);
		getServer().getPluginManager().registerEvents(mapManager, this);

		lootManager = new LootManager(this);
		getServer().getPluginManager().registerEvents(lootManager, this);

		treasureManager = new TreasureManager(this, lootManager);
		getServer().getPluginManager().registerEvents(treasureManager, this);

		LOG(0,"Dead Man's Chest plugin startup");
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}
	public BarrelManager getBarrelManager() {
		return this.barrelManager;
	}
	public TreasureMapManager getMapManager() {
		return this.mapManager;
	}
	public TreasureManager getTreasureManager() {
		return this.treasureManager;
	}

	@Override
	public void onDisable() {
		LOG(0,"Dead Man's Chest plugin shutdown");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String commandName = command.getName().toLowerCase();
		
    if(!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player");
			return true;
		}

		if(commandName.equalsIgnoreCase("flush")) {
			for(Entity entity : player.getWorld().getEntities()) {
				if( UtilFuncs.isDMCComponent(entity)) {
					LOG(0,"Removing entity %s", UtilFuncs.getDMCComponentType(entity));
					entity.remove();
				}
			}
			barrelManager.flushAllBarrels();
		}
		if(commandName.equalsIgnoreCase("info")) {
			barrelManager.showAllBarrels(player);
		}
		return true;
	}

	@Override
	public String namespace() {
		return Constants.NAME_SPACE;
	}

	
	static public void LOG(int level, String msg, Object... args) {
		try {
			if( level == 0 || level == 10 ) {
				if( configuration == null || (configuration.isDebugOn() && level == 0) || level == 10)
					logger.info(String.format(msg, args));
			}	else {
				logger.warning(String.format(msg, args));
			}
		} catch(Exception ex) {
			logger.severe("Exception writing log: " + ex.getMessage());
		}
	}
	
	static public void LOG(int level, Player player, String msg, Object... args) {
		try {
			String toSend = String.format(msg, args);
			if( level == 0 || level == 10 ) {
				if( configuration == null || (configuration.isDebugOn() && level == 0) || level == 10) {
					logger.info(toSend);
					if( player != null )
						player.sendMessage(toSend);
				}
			} else {
				logger.warning(toSend);
				if( player != null )
					player.sendMessage(toSend);
			}

		} catch(Exception ex) {
			logger.severe("Exception writing log to player: " + ex.getMessage());
			if( player != null )
				player.sendMessage("Exception writing log to player: " + ex.getMessage());
		}
	}

}
