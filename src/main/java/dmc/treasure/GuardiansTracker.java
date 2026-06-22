package dmc.treasure;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import dmc.Constants;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static dmc.DeadMansChestPlugin.LOG;

public class GuardiansTracker {
	Set<String> guardians;
	final String markerId;
	final Interaction marker;

	public GuardiansTracker(Set<String> guardians, Interaction marker, String markerId) {
		this.guardians = guardians;
		this.markerId = markerId;
		this.marker = marker;

		setGuardianList();
	}

	public void showInfo() {
		LOG(0,"GuardiansTracker: marker: %s, count: %d", markerId, getGuardianCount());
		int index = 0;
		for(String str : guardians ) {
			LOG(0,"  Guardian %d: %s", index++, str);
		}
	}

	public int getGuardianCount() {
		return this.guardians.size();
	}

	public void guardianDefeated(String id) {
		guardians.remove(id);
		setGuardianList();
	}

	private void setGuardianList() {
		PersistentDataContainer pdc = marker.getPersistentDataContainer();
		String composite = GuardiansTracker.buildCompositeList(this.guardians);
		LOG(0,"Setting list '%s'", composite);
		pdc.set(Constants.DMC_TREASURE_MARKER_GUARDIAN_LIST, PersistentDataType.STRING, composite);
		pdc.set(Constants.DMC_TREASURE_MARKER_GUARDIAN_LIST_COUNT, PersistentDataType.INTEGER, getGuardianCount());
	}

	static public GuardiansTracker buildFromInteraction(Interaction marker) {
		PersistentDataContainer pdc = marker.getPersistentDataContainer();
		if(!TreasureManager.isTreasureMarker(marker))
			return null;

		String guardianList = pdc.get(Constants.DMC_TREASURE_MARKER_GUARDIAN_LIST, PersistentDataType.STRING);
		String markerId = pdc.get(Constants.DMC_TREASURE_MARKER_ID_KEY, PersistentDataType.STRING);

		LOG(0,"Guardian List: '%s'", guardianList);
		
		Set<String> guardians = decomposeCompositeList(guardianList);
		GuardiansTracker gd = new GuardiansTracker(guardians, marker, markerId);
		return gd;
	}

	static public String buildCompositeList(Set<String> in) {
		StringBuilder sb = new StringBuilder();
		String sep="";;

		if( in != null && in.size() > 0 ) {
			for(String id : in ) {
				sb.append(sep).append(id);
				sep = "|";
			}
		}
		return sb.toString();
	}

	static public Set<String> decomposeCompositeList(String list) {
		String[] parts = list.split("\\|");
		HashSet<String> set = new HashSet<>();
		LOG(0,"GT: %d parts in %s", parts.length, list);
		for(String part : parts) {
			LOG(0,"GT: adding part '%s'", part);
			if(part != null && part.trim().length() != 0 ) {
				set.add(part);
			}
		}
		return set;
	}
		
}
