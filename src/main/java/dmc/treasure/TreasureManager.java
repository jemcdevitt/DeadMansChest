package dmc.treasure;
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
import dmc.loot.LootManager;
import dmc.map.TreasureMapData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Matrix4f;

import static dmc.DeadMansChestPlugin.LOG;

public class TreasureManager implements Listener {
	final private DeadMansChestPlugin plugin;
	final private LootManager lootManager;

	public TreasureManager(DeadMansChestPlugin plugin, LootManager lootManager) {
		this.plugin = plugin;
		this.lootManager = lootManager;
	}

	public void createTreasureMarker(ItemStack map) {
		if( map == null )
			return;
		TreasureMapData mapData = TreasureMapData.fromItem(map);
		if( mapData == null ) {
			return;
		}
		Location location = mapData.toLocation();
		if( location == null ) {
			return;
		}

		if( mapData.wasTreasureMarkerCreated()) {
			return;
		}
		
		TreasureMarker marker = new TreasureMarker(location, mapData.getTreasureLevel());

		mapData.setTreasureMarkerCreated(true);
		mapData.setTreasureMarkerUniqueId(marker.getUniqueId());
		mapData.setToItem(map, null);
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if( !(event.getRightClicked() instanceof Interaction inter)) {
			return;
		}

		Boolean b = inter.getPersistentDataContainer().get(Constants.DMC_CD_IS_ACTIVE_KEY, PersistentDataType.BOOLEAN);
		if(b != null && b == false) {
			return;
		}
		
		if( isTreasureMarker(inter) ) {
			processTreasureMarkerInteraction(inter, event.getPlayer());
		} else if( isTreasureChest(inter) ) {
			processTreasureChestInteraction(inter, event.getPlayer());
		}
		
	}

	private void processTreasureChestInteraction(Interaction inter, Player player) {
		PersistentDataContainer pdc = inter.getPersistentDataContainer();
		CompositeDisplay treasureChest = CompositeDisplay.reconstituteFromInteraction(Constants.DMC_CD_TYPE_TREASURE_CHEST, inter);
		if( treasureChest == null ) {
			return;
		}

		Integer level = pdc.get(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER);
		if( level == null ) {
			level = 2;
		}

		lootManager.openTreasureChest(player, inter, treasureChest, level);
	}
		
	private void processTreasureMarkerInteraction(Interaction inter, Player player) {
		PersistentDataContainer pdc = inter.getPersistentDataContainer();
		
		String id = pdc.get(Constants.DMC_TREASURE_MARKER_ID_KEY, PersistentDataType.STRING);

		
		Integer treasureLevel = pdc.get(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER);
		if( treasureLevel == null ) {
			return;
		}

		Set<String> guardians = spawnGuardians(treasureLevel, inter, player);

		if( guardians == null || guardians.size() == 0 ) {
			allGuardiansDefeated(inter);
		} else {
			GuardiansTracker guardiansTracker = new GuardiansTracker(guardians, inter, id);
		}

		//time to fight
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if( !isGuardianEntity(entity)) {
			return;
		}
		String guardianId = entity.getUniqueId().toString();
		String interactionId = entity.getPersistentDataContainer().get(Constants.DMC_CD_INTERACTION_ID_KEY, PersistentDataType.STRING);
		if( interactionId == null ) {
			return;
		}
		UUID interactionUUID = null;
		try {
			interactionUUID = UUID.fromString(interactionId);
		} catch(Exception ex) {
			return;
		}
		
		
		Entity markerEntity = entity.getWorld().getEntity(interactionUUID);
		if( markerEntity == null ) {
			return;
		}
		if(!(markerEntity instanceof Interaction marker)) {
			return;
		}
		GuardiansTracker gdt = GuardiansTracker.buildFromInteraction(marker);
		if( gdt == null ) {
			return;
		}
		gdt.guardianDefeated(guardianId);

		if(gdt.getGuardianCount() == 0 ) {
			allGuardiansDefeated(marker);
		}
	}
			
	private boolean isGuardianEntity(LivingEntity entity) {
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		return Constants.DMC_GUARDIAN_ITEM_TYPE.equals(pdc.get(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING));
	}
	
	private void allGuardiansDefeated(Interaction marker) {
		CompositeDisplay treasureMarker =  CompositeDisplay.reconstituteFromInteraction(Constants.DMC_CD_TYPE_TREASURE_MARKER, marker);

		Integer treasureLevel = marker.getPersistentDataContainer().get(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER);
		if( treasureLevel == null ) {
			treasureLevel = 2;
		}

		Location markerLocation = marker.getLocation();
		CompositeDisplayMover treasureMarkerMover = new CompositeDisplayMover("marker", treasureMarker.setActive(false), markerLocation.clone(), markerLocation.clone().add(0, -2, 0), 8_000)
			.setSound(Sound.BLOCK_SOUL_SAND_BREAK, markerLocation)
			.setParticle(Particle.BLOCK, Material.SOUL_SAND.createBlockData(), markerLocation);
		
		CompositeDisplayMover treasureChestMover = new CompositeDisplayMover("chest", new TreasureChest(markerLocation.clone().add(0,-1.5,0), treasureLevel),
																																						markerLocation.clone().add(0,-1.5,0), markerLocation.clone(),	6_000, true)
			.setSound(Sound.BLOCK_SOUL_SAND_BREAK, markerLocation)
			.setParticle(Particle.BLOCK, Material.SOUL_SAND.createBlockData(), markerLocation)
			.setFinishCallback((cd)->{ cd.setActive(true);});
									 

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
				if( !treasureMarkerMover.isDone() ) {
					treasureMarkerMover.update(delta);
					if( treasureMarkerMover.isDone()) {
						treasureMarker.remove();
					}
				} else if(!treasureChestMover.isDone()) {
					treasureChestMover.update(delta);
				} else {
					treasureChestMover.moveToEnd();
					cancel();
					end();
				}
			}

			void start() {
				if( started )
					return;
				fire = markerLocation.getWorld().spawn(markerLocation, BlockDisplay.class, bd -> {
						bd.setPersistent(false);
						bd.setGravity(false);
						bd.setTransformationMatrix(new Matrix4f().identity().scale(1f, 0.5f, 1f));
						bd.setBlock(Bukkit.createBlockData(Material.SOUL_FIRE));
					});
				started = true;
			}
			void end() {
				if( fire != null )
					fire.remove();
			}
				
		}.runTaskTimer(plugin, 0, 20);
	}

	//======== support funcs ==============

	static public final boolean isTreasureMarker(Interaction inter) {
		return Constants.DMC_TREASURE_MARKER_ITEM_TYPE.equals(inter.getPersistentDataContainer().get(Constants.ITEM_TYPE_KEY,PersistentDataType.STRING));
	}
	static public final boolean isTreasureChest(Interaction inter) {
		return Constants.DMC_TREASURE_CHEST_ITEM_TYPE.equals(inter.getPersistentDataContainer().get(Constants.ITEM_TYPE_KEY,PersistentDataType.STRING));
	}

	private Set<String> spawnGuardians(int treasureLevel, Interaction marker, Player player) {

		Set<String> guardianIds = new HashSet<>();
		CompositeDisplay treasureMarker =  CompositeDisplay.reconstituteFromInteraction(Constants.DMC_CD_TYPE_TREASURE_MARKER, marker);
		Boolean alreadySpawned = marker.getPersistentDataContainer().get(Constants.DMC_TREASURE_MARKER_GUARDIANS_SPAWNED, PersistentDataType.BOOLEAN);
		if( alreadySpawned != null && alreadySpawned ) {
			if(treasureMarker != null )
				treasureMarker.remove();
			else
				marker.remove();
			return guardianIds;
		}



		if( marker == null || marker.getWorld() == null )
			return guardianIds;




		Location center = marker.getLocation();
		World world = center.getWorld();
		int count= guardianCountForLevel(treasureLevel);
		double radius = radiusForLevel(treasureLevel);

		double startAngle = Math.random() * Math.PI * 2.0;

		for(int i = 0; i < count; i++) {
			double angle = startAngle + ((Math.PI * 2.0) / count) * i;
			Location spawnLoc = findGuardianSpawnLocation(center, angle, radius);
			if( spawnLoc == null) {
				//fallback, don't spawn on center, but keep nearby
				spawnLoc = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
				spawnLoc.setY(world.getHighestBlockYAt(spawnLoc) + 1);
			}
			int which = i;
			Skeleton skeleton = world.spawn(spawnLoc, Skeleton.class, skel -> {
					configureGuardian(skel, treasureLevel, which, marker);
					skel.setAI(false);
					skel.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 10, false, false));
				});

			world.playSound(spawnLoc, Sound.ENTITY_WITHER_SKELETON_AMBIENT, 0.8f, 1.6f);
			world.playSound(spawnLoc, Sound.BLOCK_SOUL_SAND_BREAK, 1.0f, 0.6f);
			world.playSound(spawnLoc, Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.75f);
			world.spawnParticle(Particle.BLOCK, spawnLoc, 20, 0.4, 0.2, 0.4, Material.DIRT.createBlockData());
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
					if(!skeleton.isDead() && skeleton.isValid()) {
						skeleton.setAI(true);
						skeleton.setTarget(player);
					}
				}, 20L);
			guardianIds.add(skeleton.getUniqueId().toString());
		}
		marker.getPersistentDataContainer().set(Constants.DMC_TREASURE_MARKER_GUARDIANS_SPAWNED, PersistentDataType.BOOLEAN, true);
		
		return guardianIds;
	}

	private int guardianCountForLevel(int level) {
		return switch(level) {
			case 1 -> 2;
			case 2 -> 3;
			case 3 -> 4;
			default -> 2;
		};
	}

	private double radiusForLevel(int level) {
		return switch(level) {
			case 1 -> 4.0;
			case 2 -> 5.0;
			case 3 -> 6.0;
			default -> 4.0;
		};
	}

	private Location findGuardianSpawnLocation(Location center, double angle, double radius) {
    World world = center.getWorld();
		
    if (world == null) {
			return null;
    }
		
    // Try several nearby offsets around the desired angle/radius.
    for (int attempt = 0; attempt < 8; attempt++) {
			double angleOffset = Math.toRadians((attempt - 3) * 12.0);
			double testAngle = angle + angleOffset;
			double testRadius = radius + (attempt % 3) - 1; // radius-1, radius, radius+1
			
			int x = center.getBlockX() + (int)Math.round(Math.cos(testAngle) * testRadius);
			int z = center.getBlockZ() + (int)Math.round(Math.sin(testAngle) * testRadius);
			
			int y = world.getHighestBlockYAt(x, z);
			
			Location loc = new Location(world, x + 0.5, y, z + 0.5);
			
			if (isValidGuardianSpawn(loc)) {
				return loc;
			}
    }
		
    return null;
	}

	private boolean isValidGuardianSpawn(Location loc) {
    World world = loc.getWorld();
		
    if (world == null) {
			return false;
    }
		
    Block feet = world.getBlockAt(loc);
    Block head = world.getBlockAt(loc.clone().add(0, 1, 0));
    Block ground = world.getBlockAt(loc.clone().add(0, -1, 0));
		
    if (!feet.isPassable()) {
			return false;
    }
		
    if (!head.isPassable()) {
			return false;
    }
		
    Material groundType = ground.getType();
		
    if (!groundType.isSolid()) {
			return false;
    }
		
    // Avoid silly spawns in water/lava.
    if (feet.getType() == Material.WATER || feet.getType() == Material.LAVA) {
			return false;
    }
		
    return true;
	}

	private void configureGuardian(Skeleton skeleton, int treasureLevel, int index, Interaction markerInteraction) {
    skeleton.setPersistent(true);
    skeleton.setRemoveWhenFarAway(false);
    skeleton.customName(Component.text(guardianName(treasureLevel, index)));
    skeleton.setCustomNameVisible(false);
		
    PersistentDataContainer pdc = skeleton.getPersistentDataContainer();
		
    pdc.set(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING, Constants.DMC_GUARDIAN_ITEM_TYPE);
    pdc.set(Constants.DMC_TREASURE_LEVEL, PersistentDataType.INTEGER, treasureLevel);
		
    String markerId = markerInteraction.getPersistentDataContainer()
			.get(Constants.DMC_CD_ID_KEY, PersistentDataType.STRING);
		
    if (markerId != null) {
			pdc.set(Constants.DMC_TREASURE_MARKER_ID_KEY, PersistentDataType.STRING, markerId);
    }
		pdc.set(Constants.DMC_CD_INTERACTION_ID_KEY, PersistentDataType.STRING, markerInteraction.getUniqueId().toString());
		
    applyGuardianStats(skeleton, treasureLevel, index);
    applyGuardianEquipment(skeleton, treasureLevel, index);
	}

	private void applyGuardianStats(Skeleton skeleton, int level, int index) {
    AttributeInstance maxHealth = skeleton.getAttribute(Attribute.MAX_HEALTH);
    AttributeInstance attackDamage = skeleton.getAttribute(Attribute.ATTACK_DAMAGE);
    AttributeInstance movementSpeed = skeleton.getAttribute(Attribute.MOVEMENT_SPEED);

    double health = switch (level) {
			case 1 -> 24.0;
			case 2 -> 32.0;
			case 3 -> index == 0 ? 48.0 : 38.0;
			default -> 24.0;
    };

    if (maxHealth != null) {
			maxHealth.setBaseValue(health);
			skeleton.setHealth(health);
    }

    if (attackDamage != null) {
			attackDamage.setBaseValue(switch (level) {
					case 1 -> 3.0;
					case 2 -> 4.0;
					case 3 -> index == 0 ? 6.0 : 5.0;
					default -> 3.0;
        });
    }

    if (movementSpeed != null) {
			movementSpeed.setBaseValue(switch (level) {
					case 1 -> 0.23;
					case 2 -> 0.25;
					case 3 -> 0.27;
					default -> 0.23;
        });
    }
	}

	private void applyGuardianEquipment(Skeleton skeleton, int level, int index) {
    EntityEquipment eq = skeleton.getEquipment();

    if (eq == null) {
			return;
    }

    ItemStack weapon;
    ItemStack helmet;
    ItemStack chest;
    ItemStack legs;
    ItemStack boots;

    if (level <= 1) {
			weapon = new ItemStack(Material.STONE_SWORD);
			helmet = new ItemStack(Material.LEATHER_HELMET);
			chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
			legs = new ItemStack(Material.LEATHER_LEGGINGS);
			boots = new ItemStack(Material.LEATHER_BOOTS);
    } else if (level == 2) {
			weapon = new ItemStack(index == 0 ? Material.IRON_SWORD : Material.STONE_SWORD);
			helmet = new ItemStack(Material.CHAINMAIL_HELMET);
			chest = new ItemStack(Material.IRON_CHESTPLATE);
			legs = new ItemStack(Material.CHAINMAIL_LEGGINGS);
			boots = new ItemStack(Material.CHAINMAIL_BOOTS);
    } else {
			// Diamond level: one captain, the rest strong guards.
			boolean captain = index == 0;

			weapon = new ItemStack(captain ? Material.MACE : Material.IRON_SWORD);
			helmet = new ItemStack(captain ? Material.DIAMOND_HELMET : Material.IRON_HELMET);
			chest = new ItemStack(captain ? Material.DIAMOND_CHESTPLATE : Material.IRON_CHESTPLATE);
			legs = new ItemStack(Material.IRON_LEGGINGS);
			boots = new ItemStack(Material.IRON_BOOTS);
    }

    eq.setItemInMainHand(weapon);
    eq.setHelmet(helmet);
    eq.setChestplate(chest);
    eq.setLeggings(legs);
    eq.setBoots(boots);

    // Prevent farming the guardians for their gear.
    eq.setItemInMainHandDropChance(0.0f);
    eq.setHelmetDropChance(0.0f);
    eq.setChestplateDropChance(0.0f);
    eq.setLeggingsDropChance(0.0f);
    eq.setBootsDropChance(0.0f);
	}

	private String guardianName(int level, int index) {
    if (level == 3 && index == 0) {
			return "Dead Man's Captain";
    }

    return switch (level) {
			case 1 -> "Drowned Deckhand";
			case 2 -> "Cursed Raider";
			case 3 -> "Dead Man's Guardian";
			default -> "Treasure Guardian";
    };
	}	
}
