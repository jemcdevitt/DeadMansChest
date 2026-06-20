package dmc.utils;
/*
 * Copyright (c) 2026, Jere McDevitt
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 *
 * The the map needs to retain the generating image otherwise it will
 * try to regen it every time the player first looks at on login.  Our map
 * is made up of 3 colors only, so this class allows us to compress the 128x128
 * pixel image from 16k down to 4k and save it in the maps persistent data container.
 */

import java.util.Arrays;

public class PixelPacker {
	private final static int BITS_PER_PIXEL = 2;
	private final static int PIXELS_PER_BYTE = 4;
	private final static int PIXEL_MASK = 0b00000011;
	private int pixelCount;
	private byte[] packedPixels;

	public PixelPacker(int pixelCount) {
		this.pixelCount = pixelCount;
		this.packedPixels = new byte[(pixelCount + PIXELS_PER_BYTE - 1) / PIXELS_PER_BYTE];
		Arrays.fill(this.packedPixels, (byte)0);
	}
	public PixelPacker(byte[] pixels) {
		this.pixelCount = pixels.length * PIXELS_PER_BYTE;
		this.packedPixels = Arrays.copyOf(pixels, pixels.length);
	}

	public byte[] getByteArray() {
		return Arrays.copyOf(packedPixels, packedPixels.length);
	}

	public int getPixelCount() {
		return pixelCount;
	}

	public int unpackPixel(int index) {
		checkIndex(index);
		
		int byteIndex = index / PIXELS_PER_BYTE;
		int packIndex = index % PIXELS_PER_BYTE;
		int shift = packIndex * BITS_PER_PIXEL;
		
		int packedByte = packedPixels[byteIndex] & 0xff;
		return (packedByte >> shift) & PIXEL_MASK;
	}
	
	public void packPixel(int index, int pixel) {
		checkIndex(index);

		int value = pixel & PIXEL_MASK;
		
		int byteIndex = index / PIXELS_PER_BYTE;
		int packIndex = index % PIXELS_PER_BYTE;
		int shift = packIndex * BITS_PER_PIXEL;

		int packedByte = packedPixels[byteIndex] & 0xff;

		packedByte &= ~(PIXEL_MASK << shift);
		packedByte |= value << shift;
		
		packedPixels[byteIndex] = (byte)packedByte;
	}

	private void checkIndex(int index) {
		if(index < 0 || index >= pixelCount) {
			throw new IndexOutOfBoundsException("Pixel index out of range: " + index);
		}
	}
	
}
