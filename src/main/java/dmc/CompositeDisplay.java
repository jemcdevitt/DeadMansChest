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
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static dmc.DeadMansChestPlugin.LOG;

public class CompositeDisplay {
	final String uniqueId;
	final boolean persistent;

	float interactionWidth;
	float interactionHeight;
	Location location;
	Interaction interaction;
	List<DisplayPart> parts = new ArrayList<>();
	List<KeyData> keyData = new ArrayList<>();


	public CompositeDisplay(Location loc, boolean persistent, float interactionWidth, float interactionHeight) {
		this.uniqueId = UUID.randomUUID().toString();
		this.persistent = persistent;
		this.location = loc.clone();
		this.location.setYaw(0);
		this.location.setPitch(0);
		this.interactionWidth = interactionWidth;
		this.interactionHeight = interactionHeight;
	}

	public CompositeDisplay spawn() {
		if( this.interaction != null )
			this.interaction.remove();
		
		this.interaction = this.location.getWorld().spawn(this.location, Interaction.class, it-> {
				it.setPersistent(this.persistent);
				it.setGravity(false);
				it.setInteractionHeight(this.interactionHeight);
				it.setInteractionWidth(this.interactionWidth);
				it.setResponsive(true);
			});
		addKeys(this.interaction);

		for(DisplayPart part : parts ) {
			part.spawn(this.location, this.persistent);
			addKeys(part.getEntity());
		}
		return this;
	}

	public CompositeDisplay addKey(NamespacedKey key, String val) {
		keyData.add(new KeyData(key, val));
		return this;
	}
	public CompositeDisplay addKey(NamespacedKey key, int val) {
		keyData.add(new KeyData(key, val));
		return this;
	}
	public CompositeDisplay addKey(NamespacedKey key, boolean val) {
		keyData.add(new KeyData(key, val));
		return this;
	}
	
	public CompositeDisplay addBlock(Material mat, Vector3f pos, Vector3f rot, Vector3f scale) {
		parts.add(new DisplayPart(mat, pos, rot, scale, true));
		return this;
	}
	public CompositeDisplay addBlock(Material mat, Vector3f pos, Vector3f rot, Vector3f scale, Consumer<Display> userFunc) {
		parts.add(new DisplayPart(mat, pos, rot, scale, true, userFunc));
		return this;
	}
	
	public CompositeDisplay addItem(Material mat, Vector3f pos, Vector3f rot, Vector3f scale) {
		parts.add(new DisplayPart(mat, pos, rot, scale, false));
		return this;
	}
	public CompositeDisplay addItem(Material mat, Vector3f pos, Vector3f rot, Vector3f scale, Consumer<Display> userFunc) {
		parts.add(new DisplayPart(mat, pos, rot, scale, false, userFunc));
		return this;
	}

	public void moveTo(Location loc) {
		this.location = loc.clone();
		this.location.setYaw(0);
		this.location.setPitch(0);
		this.interaction.teleport(this.location);
		for(DisplayPart display : parts ) {
			display.teleport(this.location);
		}
	}

	public void remove() {
		if( interaction != null ) {
			this.interaction.remove();
			this.interaction = null;
		}
		for(DisplayPart display : parts) {
			display.remove();
		}
	}

	private void addKeys(Entity entity) {
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		pdc.set(Constants.ITEM_TYPE_KEY, PersistentDataType.STRING, Constants.DMC_CD_ITEM_TYPE);
		pdc.set(Constants.DMC_CD_ID_KEY, PersistentDataType.STRING, uniqueId);
		for(KeyData data : keyData) {
			data.set(pdc);
		}
			
	}

	static class DisplayPart {
		private Display display;
		private Material mat;
		private Vector3f pos;
		private Vector3f rot;
		private Vector3f scale;
		private boolean isBlock;
		private Consumer<Display> userFunc;

		DisplayPart(Material mat, Vector3f pos, Vector3f rot, Vector3f scale, boolean isBlock) {
			this.mat = mat;
			this.pos = new Vector3f(pos);
			this.rot = new Vector3f(rot);
			this.scale = new Vector3f(scale);
			this.isBlock = isBlock;
		}

		DisplayPart(Material mat, Vector3f pos, Vector3f rot, Vector3f scale, boolean isBlock, Consumer<Display> userFunc) {
			this.mat = mat;
			this.pos = new Vector3f(pos);
			this.rot = new Vector3f(rot);
			this.scale = new Vector3f(scale);
			this.isBlock = isBlock;
			this.userFunc = userFunc;
		}
		
		Display getEntity() {
			return this.display;
		}

		void teleport(Location loc) {
			if(this.display != null)
				this.display.teleport(loc);
		}
		void remove() {
			if(this.display != null )
				this.display.remove();
			this.display = null;
		}
		
		void spawn(Location location, boolean persistent) {
			if( this.display != null ) {
				this.display.remove();
				this.display = null;
			}
			
			if(isBlock )
				spawnBlock(location, persistent);
			else
				spawnItem(location, persistent);
		}

		private void spawnBlock(Location location, boolean persistent) {
			Matrix4f xform = new Matrix4f().identity();
			xform.rotateX((float)Math.toRadians(rot.x));
			xform.rotateY((float)Math.toRadians(rot.y));
			xform.rotateZ((float)Math.toRadians(rot.z));
			xform.translate(pos);
			xform.scale(scale);

			BlockData data = Bukkit.createBlockData(mat);

			this.display = location.getWorld().spawn(location, BlockDisplay.class, bd-> {
					bd.setPersistent(persistent);
					bd.setGravity(false);
					bd.setBlock(data);
					bd.setInterpolationDelay(0);
					bd.setInterpolationDuration(2);
					bd.setTeleportDuration(2);
					bd.setTransformationMatrix(xform);
				});

			if( userFunc != null ) {
				LOG(0,"Custom updates to the block: " + mat.toString());
				userFunc.accept(this.display);
			}
			
		}
		private void spawnItem(Location location, boolean persistent) {
			Matrix4f xform = new Matrix4f().identity();
			xform.translate(pos);
			xform.rotateX((float)Math.toRadians(rot.x));
			xform.rotateY((float)Math.toRadians(rot.y));
			xform.rotateZ((float)Math.toRadians(rot.z));
			xform.scale(scale);

			ItemStack item = ItemStack.of(mat);

			this.display = location.getWorld().spawn(location, ItemDisplay.class, bd-> {
					bd.setPersistent(persistent);
					bd.setGravity(false);
					bd.setItemStack(item);
					bd.setInterpolationDelay(0);
					bd.setInterpolationDuration(2);
					bd.setTeleportDuration(2);
					bd.setTransformationMatrix(xform);
				});
			if( userFunc != null ) {
				LOG(0,"Custom updates to the item: " + mat.toString());
				userFunc.accept(this.display);
			}
		}
	}

	static class KeyData {
		private NamespacedKey key;
		private Object value;

		KeyData(NamespacedKey key, Object value) {
			this.key = key;
			this.value = value;
		}

		public void set(PersistentDataContainer pdc) {
			if( value instanceof String strVal) {
				pdc.set(key, PersistentDataType.STRING, strVal);
			} else if( value instanceof Integer intVal) {
				pdc.set(key, PersistentDataType.INTEGER, intVal);
			} else if( value instanceof Boolean boolVal) {
				pdc.set(key, PersistentDataType.BOOLEAN, boolVal);
			} else {
				throw new RuntimeException("Value of type " + value.getClass().getName() + " not supported");
			}
		}
	}
}
