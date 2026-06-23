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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dmc.DeadMansChestPlugin.LOG;

public class CompositeDisplayMover {
	final ICompositeDisplayHolder holder;
	final Location start;
	final Location end;
	final long duration;
	final String id;

	Sound sound;
	Location soundLocation;
	
	BlockData particleBlockData;
	Particle particle;
	Location particleLocation;
	int particleCount;
	
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
		this.spawnIfNeeded = spawnIfNeeded;
	}

	public void update(long deltaMS) {
		if(done)
			return;

		if( spawnIfNeeded && !holder.getCompositeDisplay().isSpawned()) {
			holder.getCompositeDisplay().spawn();
		}
		
		runTime += deltaMS;
		if( runTime >= duration ) {
			done = true;
			if(this.finishCallback != null )
				this.finishCallback.accept(holder.getCompositeDisplay());
		} 
		move();
		if( sound != null )
			end.getWorld().playSound(soundLocation, sound, 1.0f, 0.6f);
		if( particle != null )
			end.getWorld().spawnParticle(particle, particleLocation, particleCount, 0.4, 0.2, 0.4, particleBlockData);
	}

	public boolean isDone() {
		return done;
	}

	public CompositeDisplayMover moveToEnd() {
		holder.getCompositeDisplay().moveTo(end);
		return this;
	}
		
	public CompositeDisplayMover setSound(Sound sound, Location soundLocation) {
		this.sound = sound;
		this.soundLocation = soundLocation.clone();
		return this;
	}

	public CompositeDisplayMover setParticle(@NotNull Particle particle, @Nullable BlockData particleData, @NotNull Location particleLoc) {
		return setParticle(particle, particleData, particleLoc, 20);
	}
	public CompositeDisplayMover setParticle(@NotNull Particle particle, @Nullable BlockData particleData, @NotNull Location particleLoc, int count) {
		this.particle = particle;
		this.particleBlockData = particleData;
		this.particleLocation = particleLoc.clone();
		this.particleCount = count;
		return this;
	}

	public CompositeDisplayMover setFinishCallback(Consumer<CompositeDisplay> callback) {
		this.finishCallback = callback;
		return this;
	}
	
	private void move() {
		double t = UtilFuncs.clamp((double)(runTime)/(double)duration, 0.0, 1.0);

		Location newLoc = UtilFuncs.lerp(start, end, t);

		holder.getCompositeDisplay().moveTo(newLoc);
	}
	
}
