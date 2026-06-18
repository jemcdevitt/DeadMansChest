package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	public static Configuration configuration;
	public static Random RNG;
	public static TreasureMapManager barrelManager;
	

	@Override
	public void onEnable() {
		RNG = new Random(System.currentTimeMillis());
		logger = getLogger();

		saveDefaultConfig();
		configuration = new Configuration(this);

		
		configuration.showInfo();

		barrelManager = new TreasureMapManager(this);
		getServer().getPluginManager().registerEvents(barrelManager, this);

		new BukkitRunnable() {
			@Override
			public void run() {
				barrelManager.spawnCheck();
			}
		}.runTaskTimer(this, 0, configuration.barrelSpawnCheckSeconds * 20);

		new BukkitRunnable() {
			@Override
			public void run() {
				barrelManager.update();
			}
		}.runTaskTimer(this, 0, Constants.UPDATE_TICKS);

		
		LOG(0,"Dead Man's Chest plugin startup");
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
			barrelManager.showAllBarrels();
		}
		if(commandName.equalsIgnoreCase("chest")) {
			
			Location pl = player.getLocation();
			new TreasureChest(new Location(pl.getWorld(), pl.getBlockX(), pl.getBlockY(), pl.getBlockZ()));
			/*
			new CompositeDisplay(new Location(pl.getWorld(), pl.getBlockX(), pl.getBlockY(), pl.getBlockZ()), false)
				.addBlock(Material.COMPOSTER          , new Vector3f(0f,      0f,      0.1875f), new Vector3f(0,0,0), new Vector3f(1f,     0.5625f, 0.625f))
				.addItem(Material.SKELETON_SKULL     , new Vector3f(0.1875f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
				.addItem(Material.SKELETON_SKULL     , new Vector3f(0.5f,    0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
				.addItem(Material.SKELETON_SKULL     , new Vector3f(0.8125f, 0.375f,  0.8125f), new Vector3f(0,180f,0), new Vector3f(0.25f,  0.25f,   0.25f))
				.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
				.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.8125f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
				.addItem(Material.COPPER_CHAIN       , new Vector3f(0.04f,   0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
				.addItem(Material.COPPER_CHAIN       , new Vector3f(0.945f, 0.285f,  0.1875f), new Vector3f(0,0,0), new Vector3f(0.25f,  0.6f,    0.25f))
				.addBlock(Material.WAXED_COPPER_BLOCK , new Vector3f(0.0625f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.875f, 0.05f,   0.625f))
				.addBlock(Material.GOLD_BLOCK         , new Vector3f(0f,      0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
				.addBlock(Material.GOLD_BLOCK         , new Vector3f(0.9375f, 0.5625f, 0.1875f), new Vector3f(0,0,0), new Vector3f(0.065f, 0.05f,   0.625f))
				.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.125f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
				.addBlock(Material.REDSTONE_BLOCK     , new Vector3f(0.835f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
				.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.125f,  0.0625f, 0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
				.addBlock(Material.EMERALD_BLOCK      , new Vector3f(0.835f,  0.5f,    0.78f),   new Vector3f(0,0,0), new Vector3f(0.05f,  0.05f,   0.05f))
				.addKey(Constants.DMC_TREASURE_COMPONENT, true);
			*/
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
				if( configuration == null || (configuration.debugOn && level == 0) || level == 10)
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
			if( level == 0 ) {
				if( configuration == null || configuration.debugOn) {
					logger.info(toSend);
					player.sendMessage(toSend);
				}
			} else {
				logger.warning(toSend);
				player.sendMessage(toSend);
			}

		} catch(Exception ex) {
			logger.severe("Exception writing log to player: " + ex.getMessage());
			player.sendMessage("Exception writing log to player: " + ex.getMessage());
		}
	}

}
