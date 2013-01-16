/*
 * Decrypt.java
 *
 * Created on September 14, 2011, 1:00 PM
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

import ao.misc.Convert;
import java.math.BigInteger;
import java.util.Random;

public class Decrypt {
    
    /** Used to generate random numbers whenever necessary. */
    private static Random random = new Random();
    /** See <a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange">Diffie-Hellman Key Exchange</a> */
    public static BigInteger P  = new BigInteger( "eca2e8c85d863dcdc26a429a71a9815ad052f6139669dd659f98ae159d313d13c6bf2838e10a69b6478b64a24bd054ba8248e8fa778703b418408249440b2c1edd28853e240d8a7e49540b76d120d3b1ad2878b1b99490eb4a2a5e84caa8a91cecbdb1aa7c816e8be343246f80c637abc653b893fd91686cf8d32d6cfe5f2a6f", 16 );
    public static BigInteger G1 = new BigInteger("9c32cc23d559ca90fc31be72df817d0e124769e809f936bc14360ff4bed758f260a0d596584eacbbc2b88bdd410416163e11dbf62173393fbc0c6fefb2d855f1a03dec8e9f105bbad91b3437d8eb73fe2f44159597aa4053cf788d2f9d7012fb8d7c4ce3876f7d6cd5d0c31754f4cd96166708641958de54a6def5657b9f2e92", 16);
    public static BigInteger G2 = new BigInteger( "5", 16 );
    
    public static String generateServerSeed(String localSeed, String prime, String g){
        BigInteger g1 = new BigInteger(g, 16);
        BigInteger bigLocalSeed = new BigInteger(localSeed, 16);
        BigInteger p = new BigInteger(prime, 16);
        return g1.modPow(bigLocalSeed, p).toString(16);
    }
    
    public static String generateLocalSeed(int length) {
        String seed = "";
        
        for (int i = 0; i < length; ++i) {
            seed += Integer.toHexString( random.nextInt(8) );
        }   // end for
        
        return seed;
    }   // end generateLocalSeed()
    
    public static String decrypt(String decryptionKey, String loginData) {
        if (decryptionKey.length() < 32) { throw new IllegalArgumentException("The decryption key is too short."); }

        String decrypted   = "";
        
        int[] decryptionKeyInts = Convert.hexStringToIntArray(decryptionKey);
        int[] encryptedInts   = Convert.hexStringToIntArray(loginData);
        
        int[] newBlock = { 0, 0 };
        int[] oldBlock = { 0, 0 };
        
        for (int i = 0; i < encryptedInts.length; i += 2) {
            newBlock[0] = encryptedInts[i];
            newBlock[1] = encryptedInts[i + 1];
            
            if (i != 0) {
                newBlock[0] ^= oldBlock[0];
                newBlock[1] ^= oldBlock[1];
            }   // end if
            
            Tea.decrypt(newBlock, decryptionKeyInts);
            
            decrypted += Convert.intArrayToString(newBlock);
        }   // end for

        return decrypted;
    }   // end decrypt()
    
    public static String blockToHexString(int[] block) {
        String output = "";
        for (int i = 0; i < block.length; i++) {
            // output += String.format("%08x, block[i]);
            
            output += String.format(
                "%02x%02x%02x%02x",
                block[i]       & 0xff, 
                block[i] >>  8 & 0xff,
                block[i] >> 16 & 0xff,
                block[i] >> 24 & 0xff
            );
        }   // end for
        return output;
    }   // end blockToHexString()
}
