package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class Constants {
	static public final int UPDATE_TICKS = 3;
	static public final String NAME_SPACE = "deadmanschest";
	

	static public final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(NAME_SPACE,"item_type");
	static public final NamespacedKey DMC_BARREL_ID_KEY = new NamespacedKey(NAME_SPACE,"dmc_barrel_id_key");
	static public final NamespacedKey DMC_MAP_ID_KEY = new NamespacedKey(NAME_SPACE, "dmc_map_id_key");

	static public final String DMC_BARREL_ITEM_TYPE = "dmc_barrel_item_type";
	static public final String DMC_MAP_ITEM_TYPE = "dmc_map_item_type";

	static public final NamespacedKey DMC_MAP_WORLD_ID = new NamespacedKey(NAME_SPACE, "dmc_map_world_id");
	static public final NamespacedKey DMC_TREASURE_X_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_x_id");
	static public final NamespacedKey DMC_TREASURE_Y_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_y_id");
	static public final NamespacedKey DMC_TREASURE_Z_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_z_id");
	static public final NamespacedKey DMC_TREASURE_CLAIMED = new NamespacedKey(NAME_SPACE, "dmc_treasure_claimed");
	
}
