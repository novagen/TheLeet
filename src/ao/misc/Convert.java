/*
 * Convert.java
 *
 * Created on Sep 16, 2010, 5:30 PM
 *************************************************************************
 * Copyright 2010 Kevin Kendall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ao.misc;

public class Convert {

    public static int swap(int id) {
        byte[] a = intToByte(id);
        byte[] b = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            b[a.length - i - 1] = a[i];
        }

        return byteToInt(b);
    }

    public static byte[] intToByte(int id) {
        byte[] b = new byte[4];
        b[0] = (byte) (id >> 24);
        b[1] = (byte) ((id << 8) >> 24);
        b[2] = (byte) ((id << 16) >> 24);
        b[3] = (byte) ((id << 24) >> 24);
        return b;
    }

    public static int byteToInt(byte[] id) {
        int i = 0;
        int pos = 0;
        i += unsignedByteToInt(id[pos++]) << 24;
        i += unsignedByteToInt(id[pos++]) << 16;
        i += unsignedByteToInt(id[pos++]) << 8;
        i += unsignedByteToInt(id[pos++]);
        return i;
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    public static long byteToLong(byte[] id) {
        long value = 0;
        for (int i = 0; i < id.length; i++) {
            value = (value << 8) + (id[i] & 0xff);
        }
        return value;
    }
    
    public static long unsignedIntToLong(int x) {
        return x & 0x00000000ffffffffL;
    }
    
    /**
     * Turns a hexadecimal string into a sequence of integers.
     * This function is used to turn the 128 bit encryption key from 
     * a hexadecimal string into a sequence of four integers to encrypt the login data.
     *
     * @param key 
     *        the hexadecimal string that will be turned into a sequence of integers
     * @return 
     *        the sequence of integers created from the hexadecimal string
     */
    public static int[] hexStringToIntArray(String key) {
        int[] keyInts = new int[key.length() / 8];
        for (int i = 0, j = 0; i < keyInts.length; ++i, j += 8) {
            keyInts[i] |= Integer.parseInt( key.substring(j, j + 2), 16 );
            keyInts[i] |= Integer.parseInt( key.substring(j + 2, j + 4), 16 ) <<  8;
            keyInts[i] |= Integer.parseInt( key.substring(j + 4, j + 6), 16 ) << 16;
            keyInts[i] |= Integer.parseInt( key.substring(j + 6, j + 8), 16 ) << 24;
        }
        return keyInts;
    }

    /**
     * Turns a 64 bit block of encrypted binary data into a hexadecimal string.
     *
     * @param block
     *        the 64 bit block that will be used to create the hexadecimal string
     * @return
     *        a hexadecimal string created from the block
     */
    public static String intArrayToHexString(int[] block) {
        String output = "";
        for (int i = 0; i < block.length; i++) {
            output += String.format(
                    "%02x%02x%02x%02x",
                    block[i] & 0xff,
                    block[i] >> 8 & 0xff,
                    block[i] >> 16 & 0xff,
                    block[i] >> 24 & 0xff);
        }
        return output;
    }
    
    /**
     * Turns a sequence of characters into a sequence of integers.
     * This function is used to turn the unencrypted login data
     * from a sequence of characters to a sequence of integers;
     * so that it can be encrypted.
     *
     * @param s 
     *        the sequence of characters that will be turned into a sequence of integers
     * @return 
     *        the sequence of integers created from the sequence of characters
     */
    public static int[] stringToIntArray(String s) {
        int[] ai = new int[s.length() / 4];

        for (int i = 0, j = 0; i < ai.length; ++i, j += 4) {
            ai[i] |= (s.charAt(j) & 0xff);
            ai[i] |= (s.charAt(j + 1) & 0xff) << 8;
            ai[i] |= (s.charAt(j + 2) & 0xff) << 16;
            ai[i] |= (s.charAt(j + 3) & 0xff) << 24;
        }
        return ai;
    }

    public static String intArrayToString(int[] arr) {
        String s = "";
        for (int i : arr) {
            char chars[] = new char[4];
            chars[0] = (char) (i & 0xff);
            chars[1] = (char) (i >>> 8 & 0xff);
            chars[2] = (char) (i >>> 16 & 0xff);
            chars[3] = (char) (i >>> 24);
            s += new String(chars);
        }
        return s;
    }
}
