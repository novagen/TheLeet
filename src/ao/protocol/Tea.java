/*
 * Tea.java
 *
 * Created August 2011
 *************************************************************************
 * Copyright 2011 Kevin Kendall
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

package ao.protocol;

public class Tea {

    /**
     * This function implements the 
     * <a href="http://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny Encryption Algorithm (TEA)</a>
     * to encrypt a 64 bit block of data using a 128 bit key.
     *
     * @param block
     *        a two integer (64 bit) block to encrypt
     * @param key
     *        a four integer (128 bit) key to use in encrypting the block 
     *
     * @see <a href="http://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny Encryption Algorithm (TEA)</a>
     */
    public static void encrypt(int[] block, int[] key) {
        int i = block[0];
        int j = block[1];
        int sum = 0;
        int delta = 0x9e3779b9;

        for (int k = 0; k < 32; ++k) {
            sum += delta;
            i += (j << 4 & 0xfffffff0) + key[0] ^ j + sum ^ (j >> 5 & 0x7ffffff) + key[1];
            j += (i << 4 & 0xfffffff0) + key[2] ^ i + sum ^ (i >> 5 & 0x7ffffff) + key[3];
        }   // end for

        block[0] = i;
        block[1] = j;
    }   // end encrypt()

    /**
     * This function implements the 
     * <a href="http://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny Encryption Algorithm (TEA)</a>
     * to decrypt a 64 bit block of data using a 128 bit key.
     *
     * @param block
     *        a two integer (64 bit) block to encrypt
     * @param key
     *        a four integer (128 bit) key used to encrypting the block 
     *
     * @see <a href="http://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm">Tiny Encryption Algorithm (TEA)</a>
     */
    public static void decrypt(int[] block, int[] key) {
        int i = block[0];
        int j = block[1];
        int sum = 0xC6EF3720;
        int delta = 0x9e3779b9;

        for (int k = 0; k < 32; ++k) {
            i -= (i << 4 & 0xfffffff0) + key[2] ^ i + sum ^ (i >> 5 & 0x7ffffff) + key[3];
            j -= (j << 4 & 0xfffffff0) + key[0] ^ j + sum ^ (j >> 5 & 0x7ffffff) + key[1];
            sum -= delta;
        }   // end for

        block[0] = i;
        block[1] = j;
    }   // end decrypt()
}
