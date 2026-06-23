package dmc.loot;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.CompositeDisplay;
import dmc.CompositeDisplayMover;
import dmc.Constants;
import dmc.DeadMansChestPlugin;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Matrix4f;

import static dmc.DeadMansChestPlugin.LOG;

public class LootManager implements Listener {
	private static final int CHEST_SIZE = 27;

	private final DeadMansChestPlugin plugin;
	private final Map<String,Inventory> activeLoot = new HashMap<>();

	public LootManager(DeadMansChestPlugin plugin) {
		this.plugin = plugin;
	}

	public void openTreasureChest(Player player, Interaction interaction, CompositeDisplay chestDisplay, int treasureLevel) {
		String chestId = interaction.getPersistentDataContainer()
			.get(Constants.DMC_CD_ID_KEY, PersistentDataType.STRING);
		
		if (chestId == null) {
			chestId = interaction.getUniqueId().toString();
		}
		
		Inventory inventory = activeLoot.get(chestId);

		if (inventory == null) {
			TreasureLootHolder holder = new TreasureLootHolder(chestId, treasureLevel, chestDisplay);
			
			inventory = Bukkit.createInventory(holder, CHEST_SIZE, Component.text("Dead Man's Treasure"));

			holder.setInventory(inventory);
			fillInventory(inventory, treasureLevel);
			
			activeLoot.put(chestId, inventory);
		}

		Location loc = interaction.getLocation();
		loc.getWorld().playSound(loc, Sound.BLOCK_CHEST_OPEN, 1.0f, 0.85f);
		
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getInventory().getHolder() instanceof TreasureLootHolder holder)) {
			return;
		}
		
		Inventory inventory = event.getInventory();
		
		if (!isInventoryEmpty(inventory)) {
			return;
		}
		
		String chestId = holder.getChestId();
		activeLoot.remove(chestId);
		
		CompositeDisplay display = holder.getChestDisplay();
		if (display != null) {
			removeTreasureChest(display);
		}
	}


	private void removeTreasureChest(CompositeDisplay display) {
//		LOG(0,"LM_rtc: starting removal");
		Location loc = display.getLocation();
		World world = display.getWorld();
		display.setActive(false);
		world.playSound(loc, Sound.BLOCK_CHEST_CLOSE, 1.0f, 0.85f);
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
				destroyTreasureChest(display);
			}, 2 * 20L);
	}
	private void destroyTreasureChest(CompositeDisplay display) {
//		LOG(0,"LM_dtc: starting destruction");
		Location loc = display.getLocation();
		World world = display.getWorld();
		world.strikeLightningEffect(loc.clone().add(0.5, 0, 0.5));
		world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.85f);
		world.playSound(loc, Sound.BLOCK_SOUL_SAND_BREAK, 0.75f, 0.60f);

		CompositeDisplayMover chestMover = new CompositeDisplayMover("chest", display, loc.clone(), loc.clone().add(0,-1,0), 8_000, false)
			.setSound(Sound.BLOCK_SOUL_SAND_BREAK, loc)
			.setParticle(Particle.SMOKE, null, loc.clone().add(0.5f,0.5f,0.5f), 45);
		
		new BukkitRunnable() {
			long lastTick = System.currentTimeMillis();
			BlockDisplay fire;
			boolean started = false;
			
			@Override
			public void run() {
				start();
				long now = System.currentTimeMillis();
				long delta = now - lastTick;
				lastTick = now;
				if( !chestMover.isDone() ) {
					chestMover.update(delta);
				} else {
					cancel();
					end();
				}
			}

			void start() {
				if( started )
					return;
				fire = world.spawn(loc, BlockDisplay.class, bd -> {
						bd.setPersistent(false);
						bd.setGravity(false);
						bd.setTransformationMatrix(new Matrix4f().identity().scale(1f, 0.45f, 1f));
						bd.setBlock(Bukkit.createBlockData(Material.FIRE));
					});
				started = true;
			}
			void end() {
				if( fire != null )
					fire.remove();
				chestMover.moveToEnd();
				world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 0.85f);
				display.remove();
				cancel();
			}
				
		}.runTaskTimer(plugin, 0, 20);
	}
		
	
	private boolean isInventoryEmpty(Inventory inventory) {
		for (ItemStack item : inventory.getContents()) {
			if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
				return false;
			}
		}
		return true;
	}
	
	private void fillInventory(Inventory inventory, int treasureLevel) {
		List<ItemStack> loot = generateLoot(treasureLevel);
		
		for (ItemStack item : loot) {
			if (item == null || item.getType().isAir()) {
				continue;
			}
			
			inventory.addItem(item);
		}
	}
	
	private List<ItemStack> generateLoot(int treasureLevel) {
		List<LootEntry> table = buildLootTable(treasureLevel);
		
		int rolls = getRollCount(treasureLevel);
		
		List<ItemStack> result = new ArrayList<>();
		
		for (int i = 0; i < rolls; i++) {
			LootEntry entry = weightedPick(table);
			if (entry != null) {
				result.add(entry.createItem());
			}
		}

		ItemStack book = maybeGenerateBook(treasureLevel);
		if( book != null )
			result.add(book);
		return result;
	}

	private int getRollCount(int treasureLevel) {
		int fallback = switch(treasureLevel) {
			case 1 -> 4;
			case 2 -> 6;
			case 3 -> 8;
			default -> 4;
		};

		return plugin.getConfig().getInt("loot.level-" + treasureLevel + ".rolls", fallback);
	}
	

	private ItemStack maybeGenerateBook(int treasureLevel) {
		if( treasureLevel < 2)
			return null;

		int chance = switch(treasureLevel) {
			case 2 -> plugin.getConfig().getInt("loot.level-2.book-chance-percent", 18);
			case 3 -> plugin.getConfig().getInt("loot.level-3.book-chance-percent", 35);
			default -> 0;
		};

		int roll = ThreadLocalRandom.current().nextInt(100);
		if( roll >= chance ) {
			return null;
		}
		List<LootEntry> bookTable = buildBookTable(treasureLevel);
		LootEntry picked = weightedPick(bookTable);
		return picked == null ? null : picked.createItem();
	}
		
	private LootEntry weightedPick(List<LootEntry> table) {
    if (table == null || table.isEmpty()) {
			return null;
    }

    int total = 0;
    for (LootEntry entry : table) {
			total += entry.getWeight();
    }

    int roll = java.util.concurrent.ThreadLocalRandom.current().nextInt(total);

    for (LootEntry entry : table) {
			roll -= entry.getWeight();
			if (roll < 0) {
				return entry;
			}
    }

    return table.get(table.size() - 1);
	}	

	private List<LootEntry> buildLootTable(int level) {
    List<LootEntry> table = new ArrayList<>();

    if (level <= 1) {
			table.add(new LootEntry(Material.IRON_INGOT, 2, 6, 20));
			table.add(new LootEntry(Material.GOLD_NUGGET, 4, 12, 20));
			table.add(new LootEntry(Material.EMERALD, 1, 2, 8));
			table.add(new LootEntry(Material.COOKED_COD, 2, 5, 10));
			table.add(new LootEntry(Material.ARROW, 4, 12, 10));
			table.add(new LootEntry(Material.EXPERIENCE_BOTTLE, 1, 3, 6));
			table.add(new LootEntry(Material.SPRUCE_SAPLING, 1, 2, 17));
			table.add(new LootEntry(Material.DARK_OAK_SAPLING, 2, 5, 17));
			table.add(new LootEntry(Material.PALE_OAK_SAPLING, 2, 5, 17));
			table.add(new LootEntry(Material.MANGROVE_PROPAGULE, 1, 2, 17));
			table.add(new LootEntry(Material.CAKE, 1, 1, 7));
    } else if (level == 2) {
			table.add(new LootEntry(Material.GOLD_INGOT, 2, 6, 20));
			table.add(new LootEntry(Material.IRON_INGOT, 4, 10, 16));
			table.add(new LootEntry(Material.EMERALD, 2, 5, 12));
			table.add(new LootEntry(Material.REDSTONE, 6, 16, 10));
			table.add(new LootEntry(Material.EXPERIENCE_BOTTLE, 3, 7, 8));
			table.add(new LootEntry(Material.NAME_TAG, 1, 1, 4));
    } else {
			table.add(new LootEntry(Material.DIAMOND, 1, 3, 14));
			table.add(new LootEntry(Material.EMERALD, 4, 10, 14));
			table.add(new LootEntry(Material.GOLD_BLOCK, 1, 2, 10));
			table.add(new LootEntry(Material.EXPERIENCE_BOTTLE, 6, 12, 10));
			table.add(new LootEntry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 1));
			table.add(new LootEntry(Material.NETHERITE_SCRAP, 1, 1, 2));
    }

    applyConfigOverrides(table, level);
		applyCustomLootOverrides(table, level);
    return table;
	}

	private List<LootEntry> buildBookTable(int level) {
		List<LootEntry> table = new ArrayList<>();

		if( level == 2 )
			addLevelTwoBooks(table);
		else if( level == 3)
			addLevelThreeBooks(table);

		applyBookOverrides(table, level);
		return table;
	}
	
	private void addLevelTwoBooks(List<LootEntry> table) {
		table.add(new EnchantedBookLootEntry(Enchantment.UNBREAKING, 2, 4));
    table.add(new EnchantedBookLootEntry(Enchantment.UNBREAKING, 3, 2));

    table.add(new EnchantedBookLootEntry(Enchantment.EFFICIENCY, 3, 4));
    table.add(new EnchantedBookLootEntry(Enchantment.EFFICIENCY, 4, 2));

    table.add(new EnchantedBookLootEntry(Enchantment.PROTECTION, 3, 3));
    table.add(new EnchantedBookLootEntry(Enchantment.SHARPNESS, 3, 3));

    table.add(new EnchantedBookLootEntry(Enchantment.FORTUNE, 2, 2));
    table.add(new EnchantedBookLootEntry(Enchantment.SILK_TOUCH, 1, 1));

    table.add(new EnchantedBookLootEntry(Enchantment.MENDING, 1, 1));
	}

	private void addLevelThreeBooks(List<LootEntry> table) {
    table.add(new EnchantedBookLootEntry(Enchantment.UNBREAKING, 3, 5));
    table.add(new EnchantedBookLootEntry(Enchantment.EFFICIENCY, 5, 4));
    table.add(new EnchantedBookLootEntry(Enchantment.PROTECTION, 4, 4));
    table.add(new EnchantedBookLootEntry(Enchantment.SHARPNESS, 5, 4));

    table.add(new EnchantedBookLootEntry(Enchantment.FORTUNE, 3, 3));
    table.add(new EnchantedBookLootEntry(Enchantment.LOOTING, 3, 3));
    table.add(new EnchantedBookLootEntry(Enchantment.POWER, 5, 3));

    table.add(new EnchantedBookLootEntry(Enchantment.SILK_TOUCH, 1, 2));
    table.add(new EnchantedBookLootEntry(Enchantment.MENDING, 1, 2));

    // Very rare special pulls.
    table.add(new EnchantedBookLootEntry(Enchantment.FEATHER_FALLING, 4, 1));
    table.add(new EnchantedBookLootEntry(Enchantment.RESPIRATION, 3, 1));
	}
	
	private void applyConfigOverrides(List<LootEntry> table, int level) {
    String base = "loot.level-" + level;

    boolean removeDefaults = plugin.getConfig().getBoolean("loot.remove-defaults", false);
    if (removeDefaults) {
			table.clear();
    }

    for (String materialName : plugin.getConfig().getStringList(base + ".remove")) {
			Material mat = Material.matchMaterial(materialName);
			if (mat == null) {
				LOG(1,"Unknown loot material in remove list: " + materialName);
				continue;
			}

			table.removeIf(entry -> entry.getMaterial() == mat);
    }

    List<Map<?, ?>> addList = plugin.getConfig().getMapList(base + ".add");

    for (Map<?, ?> raw : addList) {
			String materialName = String.valueOf(raw.get("material"));
			Material mat = Material.matchMaterial(materialName);

			if (mat == null || mat.isAir()) {
				LOG(1,"Unknown loot material in add list: " + materialName);
				continue;
			}

			int min = asInt(raw.get("min"), 1);
			int max = asInt(raw.get("max"), min);
			int weight = asInt(raw.get("weight"), 1);

			table.add(new LootEntry(mat, min, max, weight));
    }
	}

	private void applyBookOverrides(List<LootEntry> table, int level) {
		LOG(0,"LootManager: applyBookOverrides");
    String base = "loot.level-" + level + ".add-books";

    for (Map<?, ?> raw : plugin.getConfig().getMapList(base)) {
			String enchantName = String.valueOf(raw.get("enchantment"));
			// NOTE this is deprecated but the new approach is overly complex and has
			// changed with every release, so we'll wait for now
			Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
			
			if (enchantment == null) {
				LOG(1,"Unknown enchantment in loot config: " + enchantName);
				continue;
			}

			int enchantLevel = asInt(raw.get("level"), 1);
			int weight = asInt(raw.get("weight"), 1);

			table.add(new EnchantedBookLootEntry(enchantment, enchantLevel, weight));
    }
	}

	private void applyCustomLootOverrides(List<LootEntry> table, int level) {
    String base = "loot.level-" + level + ".custom";

    for (Map<?, ?> raw : plugin.getConfig().getMapList(base)) {
			String materialName = String.valueOf(raw.get("material"));
			Material material = Material.matchMaterial(materialName);

			if (material == null || material.isAir()) {
				LOG(1,"Unknown custom loot material: " + materialName);
				continue;
			}

			int min = asInt(raw.get("min"), 1);
			int max = asInt(raw.get("max"), min);
			int weight = asInt(raw.get("weight"), 1);

			String name = raw.get("name") == null
				? null
				: String.valueOf(raw.get("name"));

			List<ConfiguredEnchant> enchants = parseConfiguredEnchants(raw.get("enchants"));

			table.add(new CustomItemLootEntry(material,min,max,weight,name,enchants));
    }
	}
	
	
	private int asInt(Object value, int fallback) {
    if (value instanceof Number num) {
			return num.intValue();
    }

    try {
			return Integer.parseInt(String.valueOf(value));
    } catch (Exception ex) {
			return fallback;
    }
	}


	@SuppressWarnings("unchecked")
	private List<ConfiguredEnchant> parseConfiguredEnchants(Object rawEnchants) {
    List<ConfiguredEnchant> result = new ArrayList<>();

    if (!(rawEnchants instanceof List<?> list)) {
			return result;
    }
		
    for (Object item : list) {
			if (result.size() >= 2) {
				LOG(1,"Custom loot item has more than 2 enchants; ignoring extras.");
				break;
			}
			
			if (!(item instanceof Map<?, ?> map)) {
				continue;
			}
			
			String enchantName = String.valueOf(map.get("enchantment"));
			// NOTE this is deprecated but the new approach is overly complex and has
			// changed with every release, so we'll wait for now
			Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());

			if (enchantment == null) {
				LOG(1,"Unknown enchantment in custom loot: " + enchantName);
				continue;
			}
			
			int enchantLevel = asInt(map.get("level"), 1);
			
			result.add(new ConfiguredEnchant(enchantment, enchantLevel));
    }
		
    return result;
	}	
}
