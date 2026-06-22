package dmc;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.utils.UtilFuncs;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;

import static dmc.DeadMansChestPlugin.LOG;

public class CompositeDisplayMover {
	final ICompositeDisplayHolder holder;
	final Location start;
	final Location end;
	final long duration;
	final String id;

	Sound sound;
	Location soundLocation;
	
	BlockData particle;
	Location particleLocation;
	
	Consumer<CompositeDisplay> finishCallback;
	
	long runTime;
	boolean done = false;
	boolean spawnIfNeeded = false;
	

	public CompositeDisplayMover(String id, ICompositeDisplayHolder holder, Location start, Location end, long milliSeconds) {
		this.id = id;
		this.holder = holder;
		this.start = start.clone();
		this.end = end.clone();
		this.duration = milliSeconds;
		this.spawnIfNeeded = false;
	}
	public CompositeDisplayMover(String id, ICompositeDisplayHolder holder, Location start, Location end, long milliSeconds, boolean spawnIfNeeded) {
		this.id = id;
		this.holder = holder;
		this.start = start.clone();
		this.end = end.clone();
		this.duration = milliSeconds;
		this.spawnIfNeeded = true;
	}

	public void update(long deltaMS) {
		if(done)
			return;

		if( spawnIfNeeded && !holder.getCompositeDisplay().isSpawned()) {
			holder.getCompositeDisplay().spawn();
		}
		
		LOG(0, "Mover %s: duration: %d, runTime: %d, delta: %d", id, duration, runTime, deltaMS);
		runTime += deltaMS;
		if( runTime >= duration ) {
			done = true;
			if(this.finishCallback != null )
				this.finishCallback.accept(holder.getCompositeDisplay());
		} 
		move();
		if( this.sound != null )
			end.getWorld().playSound(soundLocation, this.sound, 1.0f, 0.6f);
		if( this.particle != null )
			end.getWorld().spawnParticle(Particle.BLOCK, particleLocation, 20, 0.4, 0.2, 0.4, this.particle);
	}

	public boolean isDone() {
		return done;
	}

	public CompositeDisplayMover setSound(Sound sound, Location soundLocation) {
		this.sound = sound;
		this.soundLocation = soundLocation.clone();
		return this;
	}

	public CompositeDisplayMover setParticle(BlockData particle, Location particleLoc) {
		this.particle = particle;
		this.particleLocation = particleLoc.clone();
		return this;
	}

	public CompositeDisplayMover setFinishCallback(Consumer<CompositeDisplay> callback) {
		this.finishCallback = callback;
		return this;
	}
	
	private void move() {
		double t = (double)(runTime)/(double)duration;
		Location newLoc = UtilFuncs.lerp(start, end, t);

		holder.getCompositeDisplay().moveTo(newLoc);
	}
	
}
