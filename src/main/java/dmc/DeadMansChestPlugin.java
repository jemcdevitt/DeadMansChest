package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.barrel.BarrelManager;
import dmc.map.TreasureMapManager;
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

		// new BukkitRunnable() {
		// 	@Override
		// 	public void run() {
		// 		barrelManager.spawnCheck();
		// 	}
		// }.runTaskTimer(this, 0, configuration.barrelSpawnCheckSeconds * 20);

		// new BukkitRunnable() {
		// 	@Override
		// 	public void run() {
		// 		barrelManager.update();
		// 	}
		// }.runTaskTimer(this, 0, Constants.UPDATE_TICKS);

		
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
			barrelManager.flushAllBarrels();
			barrelManager.flushAllTreasureChests(player.getLocation().getWorld());
		}
		if(commandName.equalsIgnoreCase("info")) {
			barrelManager.showAllBarrels(player);
		}
		if( commandName.equalsIgnoreCase("barrel")) {
			Location pl = player.getLocation();
			Location bl = new Location(pl.getWorld(), pl.getBlockX() + 1, pl.getBlockY(), pl.getBlockZ() + 1);
			CompositeDisplay testDisplay = new CompositeDisplay(bl, false, 1.0f, 1.0f)
				.addBlock(Material.DARK_OAK_FENCE,
									new Vector3f(0.25f,0,0.25f),
									new Vector3f(0, 0, 0),
									new Vector3f(0.5f, 1.0f, 0.5f))
				.addItem(Material.WITHER_SKELETON_SKULL,
								 new Vector3f(0.5f,1.4375f,0.5f),
								 new Vector3f(0, 180f, 0),
								 new Vector3f(1f, 1f, 1f),
									(d) -> { d.setBrightness(new Display.Brightness(11,0)); })
				//crown
				.addBlock(Material.GOLD_BLOCK,
									new Vector3f(0.25f, 1.4375f, 0.25f),
									new Vector3f(0,0,0),
									new Vector3f(0.5f, 0.05f, 0.5f))
				.addBlock(Material.RAW_GOLD_BLOCK,
									new Vector3f(0.25f, 1.4375f, 0.75f),
									new Vector3f(0, 0, 0),
									new Vector3f(0.05f, 0.15f, 0.05f))
				.addBlock(Material.RAW_GOLD_BLOCK,
									new Vector3f(0.45f, 1.4375f, 0.75f),
									new Vector3f(0, 0, 0),
									new Vector3f(0.1f, 0.175f, 0.05f))
				.addBlock(Material.RAW_GOLD_BLOCK,
									new Vector3f(0.6875f, 1.4375f, 0.75f),
									new Vector3f(0,0,0),
									new Vector3f(0.05f, 0.15f, 0.05f))
				.addBlock(Material.EMERALD_BLOCK,
									new Vector3f(0.45f, 1.61f, 0.75f),
									new Vector3f(0,0,0),
									new Vector3f(0.1f, 0.1f, 0.05f),
									(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
				.addBlock(Material.AMETHYST_BLOCK,
									new Vector3f(0.25f, 1.586875f, 0.75f),
									new Vector3f(0,0,0),
									new Vector3f(0.05f, 0.05f, 0.05f),
									(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
				.addBlock(Material.AMETHYST_BLOCK,
									new Vector3f(0.6875f, 1.586875f, 0.75f),
									new Vector3f(0,0,0),
									new Vector3f(0.05f, 0.05f, 0.05f),
									(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
				//decorations
				.addItem(Material.SKELETON_SKULL,
								 new Vector3f(0.25f, 0.25f, 0.5f),
								 new Vector3f(0,215f,0),
								 new Vector3f(0.5f, 0.5f, 0.5f))
				.addItem(Material.SKELETON_SKULL,
								 new Vector3f(0.75f, 0.25f, 0.5f),
								 new Vector3f(0,135f,0),
								 new Vector3f(0.5f, 0.5f, 0.5f))
				.addBlock(Material.SOUL_FIRE,
									new Vector3f(0.18f, 0.25f, 0.4375f),
									new Vector3f(0,0,0),
									new Vector3f(0.15f, 0.2f, 0.15f),
									(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
				.addBlock(Material.SOUL_FIRE,
									new Vector3f(0.68f, 0.25f, 0.4375f),
									new Vector3f(0,0,0),
									new Vector3f(0.15f, 0.2f, 0.15f),
									(d) -> { d.setBrightness(new Display.Brightness(15,0)); })
										
				.spawn();
		}
		if(commandName.equalsIgnoreCase("chest")) {
			
			Location pl = player.getLocation();
			new TreasureChest(new Location(pl.getWorld(), pl.getBlockX(), pl.getBlockY(), pl.getBlockZ()));
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
