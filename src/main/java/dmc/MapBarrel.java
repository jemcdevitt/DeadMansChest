package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Matrix4f;

import static dmc.DeadMansChestPlugin.RNG;
import static dmc.DeadMansChestPlugin.LOG;

public class MapBarrel {
	final Location loc;
	final String uniqueId;
	Display barrel;
	Interaction interaction;
	long ttl;

	MapBarrel(Location loc) {

		this.uniqueId = UUID.randomUUID().toString();
		
		this.loc = loc;
		this.barrel = loc.getWorld().spawn(loc, BlockDisplay.class, bd -> {
				bd.setPersistent(false);
				bd.setGravity(false);
				bd.setBlock(Bukkit.createBlockData(Material.BARREL));
				bd.setInterpolationDelay(0);
				bd.setInterpolationDuration(2);
				bd.setTeleportDuration(2);
				bd.setTransformationMatrix(new Matrix4f()
																	 .identity()
																	 .rotateX((float)Math.toRadians(RNG.nextInt(60)+3.0f))
																	 .rotateZ((float)Math.toRadians(RNG.nextInt(60)+3.0f))
																	 .scale(0.25f, 0.30f, 0.25f));
			});

		this.interaction = loc.getWorld().spawn(loc, Interaction.class, it -> {
				it.setPersistent(false);
				it.setGravity(false);
				it.setInteractionHeight(1.0f);
				it.setInteractionWidth(1.0f);
				it.setResponsive(true);
			});


		this.barrel.getPersistentDataContainer().set(Constants.ITEM_TYPE_KEY,PersistentDataType.STRING, Constants.DMC_BARREL_ITEM_TYPE);
		this.barrel.getPersistentDataContainer().set(Constants.DMC_BARREL_ID_KEY,PersistentDataType.STRING, this.uniqueId);
		this.interaction.getPersistentDataContainer().set(Constants.ITEM_TYPE_KEY,PersistentDataType.STRING, Constants.DMC_BARREL_ITEM_TYPE);
		this.interaction.getPersistentDataContainer().set(Constants.DMC_BARREL_ID_KEY,PersistentDataType.STRING, this.uniqueId);

		this.ttl = (RNG.nextInt(8) + 4) * 60_000;   //4-12 minutes
	}

	public Location getLocation() {
		return this.loc;
	}
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	
	public void removeFromWorld() {
		barrel.remove();
		interaction.remove();
	}
	
	public boolean update(long tick, float bounce) {
		Location newLoc = loc.clone().add(0,bounce,0);
		barrel.teleport(newLoc);
		interaction.teleport(newLoc);

		ttl -= tick;
		if(ttl > 0)
			return true;
		LOG(0,"Barrel at (%f,%f,%f) has died", loc.getX(), loc.getY(), loc.getZ());
		return false;
	}

	public void showInfo(boolean force) {
		LOG(force?10:0,"Barrel %s at (%f,%f,%f), TTL: %d",
				uniqueId, loc.getX(), loc.getY(), loc.getZ(), ttl);
	}
}
