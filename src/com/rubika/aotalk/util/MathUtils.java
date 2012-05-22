/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rubika.aotalk.util;

/**
 * Math utility functions.
 */
public final class MathUtils {
	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static float constrain(final float amount, final float low, final float high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static int constrain(final int amount, final int low, final int high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	private MathUtils() {
	}
	
	
	public static boolean stringToBool(String s) {
		if (s.equals("1"))
			return true;
		if (s.equals("0"))
			return false;
		throw new IllegalArgumentException(s + " is not a bool. Only 1 and 0 are.");
	}
	
	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte)(value >>> 24),
				(byte)(value >>> 16),
				(byte)(value >>> 8),
				(byte)value};
	}
	
	public static final int byteArrayToInt(byte [] b) {
		return (b[0] << 24)
		+ ((b[1] & 0xFF) << 16)
		+ ((b[2] & 0xFF) << 8)
		+ (b[3] & 0xFF);
	}
}