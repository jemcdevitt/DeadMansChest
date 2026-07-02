package dmc;
/*
 * Dead Man's Chest
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

import java.awt.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public class Constants {
	static public final int UPDATE_TICKS = 3;
	static public final String NAME_SPACE = "deadmanschest";
	

	static public final NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(NAME_SPACE,"item_type");

	static public final String DMC_BARREL_ITEM_TYPE = "dmc_barrel_item_type";
	static public final String DMC_MAP_ITEM_TYPE = "dmc_map_item_type";
	static public final String DMC_CD_ITEM_TYPE = "dmc_cd_item_type";  //composite display
	static public final String DMC_TREASURE_MARKER_ITEM_TYPE = "dmc_treasure_marker_item_type";
	static public final String DMC_TREASURE_CHEST_ITEM_TYPE = "dmc_treasure_chest_item_type";
	static public final String DMC_GUARDIAN_ITEM_TYPE = "dmc_guardian_item_type";
	static public final String DMC_CD_TYPE_BARREL = "dmc_cd_type_barrel";
	static public final String DMC_CD_TYPE_TREASURE_CHEST = "dmc_cd_type_treasure_chest";
	static public final String DMC_CD_TYPE_TREASURE_MARKER = "dmc_cd_type_treasure_marker";

	static public final NamespacedKey DMC_BARREL_ID_KEY = new NamespacedKey(NAME_SPACE,"dmc_barrel_id_key");
	static public final NamespacedKey DMC_MAP_ID_KEY = new NamespacedKey(NAME_SPACE, "dmc_map_id_key");
	static public final NamespacedKey DMC_CD_ID_KEY = new NamespacedKey(NAME_SPACE, "dmc_cd_id_key");
	static public final NamespacedKey DMC_CD_INTERACTION_ID_KEY = new NamespacedKey(NAME_SPACE, "dmc_cd_interaction_id");
	static public final NamespacedKey DMC_CD_TYPE_KEY = new NamespacedKey(NAME_SPACE, "dmc_cd_type_key");
	static public final NamespacedKey DMC_CD_IS_ACTIVE_KEY = new NamespacedKey(NAME_SPACE, "dmc_cd_is_active_key");
	
	static public final NamespacedKey DMC_TREASURE_COMPONENT = new NamespacedKey(NAME_SPACE, "dmc_treasure_component");
	static public final NamespacedKey DMC_TREASURE_MARKER_ID_KEY = new NamespacedKey(NAME_SPACE, "dmc_treasure_marker_id_key");
	static public final NamespacedKey DMC_TREASURE_CHEST_ID_KEY =  new NamespacedKey(NAME_SPACE, "dmc_treasure_chest_id_key");

	static public final NamespacedKey DMC_MAP_WORLD_ID = new NamespacedKey(NAME_SPACE, "dmc_map_world_id");
	static public final NamespacedKey DMC_TREASURE_X_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_x_id");
	static public final NamespacedKey DMC_TREASURE_Y_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_y_id");
	static public final NamespacedKey DMC_TREASURE_Z_VAL =  new NamespacedKey(NAME_SPACE, "dmc_treasure_z_id");
	static public final NamespacedKey DMC_TREASURE_LEVEL = new NamespacedKey(NAME_SPACE, "dmc_treasure_level");

	static public final NamespacedKey DMC_TREASURE_MARKER_CREATED = new NamespacedKey(NAME_SPACE, "dmc_treasure_marker_created");
	static public final NamespacedKey DMC_TREASURE_MARKER_GUARDIANS_SPAWNED = new NamespacedKey(NAME_SPACE, "dmc_treasure_marker_guardians_spawned");
	static public final NamespacedKey DMC_TREASURE_MARKER_GUARDIAN_LIST = new NamespacedKey(NAME_SPACE, "dmc_treasure_marker_guardian_list");
	static public final NamespacedKey DMC_TREASURE_MARKER_GUARDIAN_LIST_COUNT = new NamespacedKey(NAME_SPACE, "dmc_treasure_marker_guardian_list_count");

	static public final NamespacedKey DMC_TREASURE_MAP_PIXELS = new NamespacedKey(NAME_SPACE, "dmc_treasure_map_pixels");
	static public final NamespacedKey DMC_TREASURE_MAP_PIXEL_FORMAT = new NamespacedKey(NAME_SPACE, "dmc_treasure_map_pixel_format");
	static public final NamespacedKey DMC_TREASURE_MAP_PENDING = new NamespacedKey(NAME_SPACE, "dmc_treasure_map_pending");


	//color constants
	static public final Color PARCHMENT_WATER_COLOR = new Color(156, 166, 84); //(170, 139, 82);
	static public final int PARCHMENT_WATER_MASK = 0b000000001;
	
	static public final Color PARCHMENT_LAND_LOW_COLOR = new Color(218, 190, 120);
	static public final int PARCHMENT_LAND_LOW_MASK = 0b00000010;
	
	static public final Color PARCHMENT_LAND_HIGH_COLOR = new Color(126, 91, 47);
	static public final int PARCHMENT_LAND_HIGH_MASK = 0b00000011;

	
}
