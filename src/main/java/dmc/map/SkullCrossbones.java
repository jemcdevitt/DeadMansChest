package dmc.map;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * A simple bit map to use for the map marker
 */

import java.awt.Color;

public class SkullCrossbones {
	static public final int PIXELS[][] =
	{
		{0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0},
		{0,0,0,1,1,2,2,2,2,2,2,1,1,0,0,0},
		{0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0},
		{0,1,2,2,1,2,2,2,2,2,2,1,2,2,1,0},
		{1,2,2,1,1,1,2,2,2,2,1,1,1,2,2,1},
		{1,2,2,2,1,2,2,2,2,2,2,1,2,2,2,1},
		{1,2,2,2,2,2,2,1,1,2,2,2,2,2,2,1},
		{1,2,2,2,2,2,2,1,1,2,2,2,2,2,2,1},
		{0,1,1,1,2,2,2,2,2,2,2,2,1,1,1,0},
		{0,0,0,0,1,2,1,2,2,1,2,1,0,0,0,0},
		{0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0},
		{0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0},
		{1,2,2,1,1,1,1,1,1,1,1,1,1,2,2,1},
		{0,1,2,2,2,2,2,2,2,2,2,2,2,2,1,0},
		{1,2,2,1,1,1,1,1,1,1,1,1,1,2,2,1},
		{0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,0}
	};

	static public int getWidth() { return 16; }
	static public int getHeight() { return 16; }

	static public int getPixelAt(int x, int y) {
		int pixel = PIXELS[y][x];
		return pixel;
	}
			
}
