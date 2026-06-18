package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * This provides the ability to manage a list of Block and Item Display
 * entities as a single component.  It will have an interaction as the
 * core and a single unique id and base location.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CompositeDisplay {
	final String uniqueId;
	final boolean persistent;
	
	Location location;
	Interaction interaction;
	List<Display> parts = new ArrayList<>();


	public CompositeDisplay(Location loc, boolean persistent) {
		this.uniqueId = UUID.randomUUID().toString();
		this.persistent = persistent;
		this.location = loc.clone();
		this.location.setYaw(0);
		this.location.setPitch(0);

		
		this.interaction = this.location.getWorld().spawn(this.location, Interaction.class, it-> {
				it.setPersistent(this.persistent);
				it.setGravity(false);
				it.setInteractionHeight(1.0f);
				it.setInteractionWidth(1.0f);
				it.setResponsive(true);
			});
		addKeys(this.interaction);
	}

	public CompositeDisplay addKey(NamespacedKey key, String val) {
		interaction.getPersistentDataContainer().set(key, PersistentDataType.STRING, val);
		for(Display part : parts ) {
			part.getPersistentDataContainer().set(key, PersistentDataType.STRING, val);
		}
		return this;
	}
	public CompositeDisplay addKey(NamespacedKey key, int val) {
		interaction.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, val);
		for(Display part : parts ) {
			part.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, val);
		}
		return this;
	}
	public CompositeDisplay addKey(NamespacedKey key, boolean val) {
		interaction.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, val);
		for(Display part : parts ) {
			part.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, val);
		}
		return this;
	}

	public CompositeDisplay addBlock(Material mat, Vector3f pos, Vector3f rot, Vector3f scale) {
		Matrix4f xform = new Matrix4f().identity();
		xform.translate(pos);
		xform.rotateX((float)Math.toRadians(rot.x));
		xform.rotateY((float)Math.toRadians(rot.y));
		xform.rotateZ((float)Math.toRadians(rot.z));
		xform.scale(scale);

		Display display = location.getWorld().spawn(location, BlockDisplay.class, bd-> {
				bd.setPersistent(persistent);
				bd.setGravity(false);
				bd.setBlock(Bukkit.createBlockData(mat));
				bd.setInterpolationDelay(0);
				bd.setInterpolationDuration(2);
				bd.setTeleportDuration(2);
				bd.setTransformationMatrix(xform);
			});
		addKeys(display);
		parts.add(display);
		return this;
	}
	public CompositeDisplay addItem(Material mat, Vector3f pos, Vector3f rot, Vector3f scale) {
		Matrix4f xform = new Matrix4f().identity();
		xform.translate(pos);
		 xform.rotateX((float)Math.toRadians(rot.x));
		 xform.rotateY((float)Math.toRadians(rot.y));
		 xform.rotateZ((float)Math.toRadians(rot.z));
		xform.scale(scale);

		ItemStack item = ItemStack.of(mat);

		Display display = this.location.getWorld().spawn(this.location, ItemDisplay.class, bd-> {
				bd.setPersistent(this.persistent);
				bd.setGravity(false);
				bd.setItemStack(item);
				bd.setInterpolationDelay(0);
				bd.setInterpolationDuration(2);
				bd.setTeleportDuration(2);
				bd.setTransformationMatrix(xform);
			});
		addKeys(display);
		parts.add(display);
		return this;
	}

	public void moveTo(Location loc) {
		this.location = loc.clone();
		this.interaction.teleport(this.location);
		for(Display display : parts ) {
			display.teleport(this.location);
		}
	}

	public void remove() {
		this.interaction.remove();
		for(Display display : parts) {
			display.remove();
		}
	}

	private void addKeys(Entity entity) {
		entity.getPersistentDataContainer().set(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING, Constants.DMC_CD_ITEM_TYPE);
		entity.getPersistentDataContainer().set(Constants.DMC_CD_ID_KEY, PersistentDataType.STRING, uniqueId);
	}
		
}
