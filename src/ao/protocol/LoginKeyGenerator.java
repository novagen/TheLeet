/*
 * LoginKeyGenerator.java
 *
 * Created on May 12, 2007, 6:12 PM
 *************************************************************************
 * Copyright 2008 Paul Smith
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

/**
 * <p>AOLoginKeyGenerator is a utilty class for generating the encrypted login keys
 * that are used to authenticate a bot/user. The algorithm involves
 * Diffie-Hellman Key Exchange, Tiny Encryption Algorithm (TEA), 
 * and likely some other unidentified algorithms.</p>
 *
 * <p><b>References:</b>
 * <ul>
 *      <li><a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange" target="_blank">Diffie-Hellman Key Exchange</a></li>
 *      <li><a href="http://en.wikipedia.org/wiki/Tiny_Encryption_Algorithm"   target="_blank">Tiny Encryption Algorithm (TEA)</a></li>
 *      <li><a href="http://boards.hackersquest.org/viewtopic.php?p=33507"     target="_blank">Class ao.f from JavaBot</a></li>
 *      <li><a href="http://sourceforge.net/projects/aochat"                   target="_blank">Class aochat.loginMessage from a J++ based bot library written by migisan and quaseem</a></li>
 *      <li><a href="http://kiwu.org/aochat/"                                  target="_blank">Script key.py from a Python port of AOChat.php</a></li>
 *      <li><a href="http://auno.org/dev/aochat/AOChat.php"                    target="_blank">AOChat.php</a></li>
 * </ul></p>
 *
 * @see #generateLoginKey(String, String, String)
 * @see ao.protocol.packets.toclient.LoginSeedPacket 
 * @see ao.protocol.packets.toserver.LoginRequestPacket
 * @see ao.protocol.Client#authenticate(String, String)
 */
public class LoginKeyGenerator {
    
    /** 
     * A constant that is sent to the AO server when attempting to authenticate a bot/user.
     * @see ao.protocol.packets.toserver.LoginRequestPacket 
     */
    public static int PROTOCOL_VERSION = 0;
    
    /** Used to generate random numbers whenever necessary. */
    private static Random random = new Random();
    /** See <a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange">Diffie-Hellman Key Exchange</a> */
    private static BigInteger P  = new BigInteger( "eca2e8c85d863dcdc26a429a71a9815ad052f6139669dd659f98ae159d313d13c6bf2838e10a69b6478b64a24bd054ba8248e8fa778703b418408249440b2c1edd28853e240d8a7e49540b76d120d3b1ad2878b1b99490eb4a2a5e84caa8a91cecbdb1aa7c816e8be343246f80c637abc653b893fd91686cf8d32d6cfe5f2a6f", 16 );
    /** See <a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange">Diffie-Hellman Key Exchange</a> */
    private static BigInteger G1 = new BigInteger( "9c32cc23d559ca90fc31be72df817d0e124769e809f936bc14360ff4bed758f260a0d596584eacbbc2b88bdd410416163e11dbf62173393fbc0c6fefb2d855f1a03dec8e9f105bbad91b3437d8eb73fe2f44159597aa4053cf788d2f9d7012fb8d7c4ce3876f7d6cd5d0c31754f4cd96166708641958de54a6def5657b9f2e92", 16 );
    /** See <a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange">Diffie-Hellman Key Exchange</a> */
    private static BigInteger G2 = new BigInteger( "5", 16 );
    
    /**
     * Given a server seed and a user name and password pair, 
     * this function generates an encrypted login key.
     *
     * @param serverSeed
     *        the seed that the AO server provided when the connection to it was first opened
     *        (this is currently a 32 character hexadecimal string (128 bit))
     * @param accountName
     *        the user name that the bot/user provided
     * @param password
     *        the password that the bot/user provided
     * @return 
     *         the generated encrypted login key
     * @throws NullPointerException 
     *         if serverSeed, userName, or password are null
     *
     * @see ao.protocol.packets.toclient.LoginSeedPacket 
     * @see ao.protocol.packets.toserver.LoginRequestPacket
     * @see ao.protocol.Client#authenticate(String, String)
     */
    public static String generateLoginKey(String serverSeed, String accountName, String password) {
        if (serverSeed == null)    { throw new NullPointerException("No server seed was given."); }
        else if (accountName == null) { throw new NullPointerException("No user name was given."); }
        else if (password == null) { throw new NullPointerException("No password was given."); }
        else {
            return generateLoginKey( serverSeed, generateLocalSeed(16), accountName, password, P, G1, G2);
        }   // end else
    }   // end generateLoginKey()

    /**
     * Given a server seed, a local seed, and a user name and password pair, 
     * this function generates an encrypted login key.
     *
     * @param serverSeed
     *        the seed that the AO server provided when the connection to it was first opened
     *        (this is currently a 32 character hexadecimal string (128 bit))
     * @param localSeed
     *        a random 16 character hexadecimal string (64 bit) generated by the client
     * @param accountName
     *        the user name that the bot/user provided
     * @param password
     *        the password that the bot/user provided
     * @return 
     *        the generated encrypted login key
     *
     * @see ao.protocol.packets.toclient.LoginSeedPacket 
     * @see ao.protocol.packets.toclient.LoginRequestPacket
     * @see ao.protocol.Client#authenticate(String, String)
     * @see #generateLocalSeed(int)
     * @see #generateLoginKey(String, String, String)
     * @see <a href="http://en.wikipedia.org/wiki/Diffie-Hellman_key_exchange">Diffie-Hellman Key Exchange</a>
     */
    private static String generateLoginKey(String serverSeed, String localSeed, String accountName, String password, BigInteger prime, BigInteger g1, BigInteger g2) {
        BigInteger bigLocalSeed = new BigInteger(localSeed, 16);
        BigInteger bigDecryptionKey = g2.modPow(bigLocalSeed, prime);
        BigInteger bigEncyrptionKey = g1.modPow(bigLocalSeed, prime);
        
        String encryptionKey      = truncateEncryptionKey(bigEncyrptionKey.toString(16), 32);
        String loginData          = accountName + "|" + serverSeed + "|" + password;
        String encryptedLoginData = encrypt(encryptionKey, loginData);
        
        return bigDecryptionKey.toString(16) + "-" + encryptedLoginData;
    }   // end generateLoginKey()
    
    /**
     * Generates a random local seed. 
     * The seed generated is a {@code length} character hexadecimal string ({@code length*4} bit).
     *
     * @param length 
     *        the number of characters in the generated seed
     * @return 
     *        a random {@code length} character hexadecimal string ({@code length*4} bit)
     *
     * @see #generateLoginKey(String, String, String, String)
     */
    private static String generateLocalSeed(int length) {
        String seed = "";
        
        for (int i = 0; i < length; ++i) {
            seed += Integer.toHexString( random.nextInt(8) );
        }   // end for
        
        return seed;
    }   // end generateLocalSeed()

    /**
     * Truncates or zero fills the encryption key 
     * so that it is always {@code length} characters long.
     * If {@code key.length()} is greater than {@code length},
     * then the first {@code length} characters of the key are returned.
     * If <code>key.length()</code> is less than {@code length},
     * then {@code length - key.length()} zeros are inserted 
     * at the begining of the key and the result is returned.
     *
     * @param key 
     *        the encryption key that will be truncated or zero filled
     * @param length 
     *        the length of the returned encryption key
     * @return
     *        the truncated or zero-filled encryption key
     *
     * @see #generateLoginKey(String, String, String, String)
     * @see #encrypt(String, String)
     */
    private static String truncateEncryptionKey(String key, int length) {
        if (key.length() > length) { 
            return key.substring(0, length); 
        } else {
            while (key.length() < length) {
                key = "0" + key;
            }   // end while
            
            return key;
        }   // end else
    }   // end truncateEncryptionKey()
    
    /**
     * Encrypts a bot/user's login data using a 128 bit encryption key
     * (passed as a 32 character hexadecimal string). 
     *
     * @param encryptionKey 
     *        the 128 bit encryption key used in the encryption process 
     *        (passed as a 32 character hexadecimal string)
     * @param loginData
     *        the login data that will be encrypted
     * @return 
     *        the encrypted login data
     */
    public static String encrypt(String encryptionKey, String loginData) {
        if (encryptionKey.length() < 32) { throw new IllegalArgumentException("The encryption key is too short."); }
        
        String prefix          = generatePrefix(8);
        String loginDataLength = loginDataLengthToString(loginData.length());
        String pad             = generatePad( prefix.length() + loginDataLength.length() + loginData.length() );
        
        String unencrypted = prefix + loginDataLength + loginData + pad;
        String encrypted   = "";
        
        int[] encryptionKeyInts = Convert.hexStringToIntArray(encryptionKey);
        int[] unencryptedInts   = Convert.stringToIntArray(unencrypted);
        
        int[] oldBlock = { 0, 0 };
        int[] newBlock = { 0, 0 };
        
        for (int i = 0; i < unencryptedInts.length; i += 2) {
            newBlock[0] = unencryptedInts[i];
            newBlock[1] = unencryptedInts[i + 1];
            
            if (i != 0) {
                newBlock[0] ^= oldBlock[0];
                newBlock[1] ^= oldBlock[1];
            }   // end if
            
            Tea.encrypt(newBlock, encryptionKeyInts);
            
            oldBlock[0] = newBlock[0];
            oldBlock[1] = newBlock[1];
            
            encrypted += Convert.intArrayToHexString(newBlock);
        }   // end for
        
        return encrypted;
    }   // end encrypt()
    
    /**
     * Generates a random sequence of {@code length} characters.
     *
     * @param length 
     *        the number of characters in the generated string
     * @return 
     *        a random sequence of {@code length} characters
     *
     * @see #encrypt(String, String)
     */
    private static String generatePrefix(int length) {
        char[] chars = new char[length];
        
        for (int i = 0; i < length; ++i) {
            chars[i] = (char)random.nextInt(255);
        }   // end for
        
        return new String(chars);
    }   // end generatePrefix()
    
    /**
     * Turns the length of the actual login data into a sequence of characters.
     * 
     * @param length 
     *        the length of the actual login data
     * @return
     *        a four character string created from the given integer's binary data
     *
     * @see #encrypt(String, String)
     */
    private static String loginDataLengthToString(int length) {
        char chars[] = new char[4];
        chars[0] = (char)(length >>> 24       );
        chars[1] = (char)(length >>> 16 & 0xff);
        chars[2] = (char)(length >>>  8 & 0xff);
        chars[3] = (char)(length        & 0xff);
        return new String(chars);
    }   // end loginDataLengthToString()
    
    /**
     * Creates a sequence of ' ' characters to append 
     * to the unencrypted login data, so that the length
     * of the unecrypted login data is evenly divisible by 8.
     *
     * @param totalLength
     *        the length of the unecrypted login data (without the pad)
     * @return 
     *        a sequence of ' ' characters
     *
     * @see #encrypt(String, String)
     * @see #tea(int[], int[])
     */
    private static String generatePad(int totalLength) {
        String pad    = "";
        int    length = 8 - totalLength % 8;
        
        if (length < 8) {
            for (int i = 0; i < length; ++i) { 
                pad += " ";
            }   // end for
        }   // end if
        
        return pad;
    }   // end generatePad()
}   // end class LoginKeyGenerator
