/*
 * AOCharListPacket.java
 *
 * Created on May 13, 2007, 2:05 PM
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

package ao.protocol.packets.in;

import ao.protocol.AOCharacter;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>AOCharListPacket is sent from the AO server to the client after 
 * the client has been successfully authenticated. It contains
 * a list of the characters belonging to the account for which the
 * client was authenticated.</p>
 * 
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      isii
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.AOLoginRequestPacket
 * @see ao.protocol.packets.AOLoginErrorPacket
 */
public class AOCharListPacket extends AOPacket {
    
    public static final short TYPE = 7;
    
    private final AOCharacter[] m_characters;
    private final byte[]        m_data;
    
    /** 
     * Creates a new instance of AOCharListPacket 
     *
     * @param characters
     *        a list of characters
     * @throws NullPointerException
     *         if characters or any of element of characters is null
     */
    public AOCharListPacket(AOCharacter[] characters) {
        if (characters == null) { throw new NullPointerException("No list of characters was passed"); }
        
        m_characters = characters;
        
        int[]    ids    = new int[m_characters.length];
        String[] names  = new String[m_characters.length];
        int[]    levels = new int[m_characters.length];
        int[]    online = new int[m_characters.length];
        
        for (int i = 0; i < m_characters.length; ++i) {
            ids[i]    = m_characters[i].getID();
            names[i]  = m_characters[i].getName();
            levels[i] = m_characters[i].getLevel();
            online[i] = m_characters[i].getOnline();
        }   // end for
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer();
        serializer.write(ids);
        serializer.write(names);
        serializer.write(levels);
        serializer.write(online);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOCharListPacket()
    
    /** 
     * Creates a new instance of AOCharListPacket 
     *
     * @param ids
     *        the ids of a list of characters
     * @param names
     *        the names of a list of characters
     * @param levels
     *        the levels of a list of characters
     * @throws NullPointerException
     *         if ids, names, levels, or online is null
     * @throws ArrayIndexOutOfBoundsException
     *         if ids, names, levels, and online are not all the same length
     */
    public AOCharListPacket(int[] ids, String[] names, int[] levels, int[] online) {
        m_characters = new AOCharacter[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            m_characters[i] = new AOCharacter( ids[i], names[i], levels[i], online[i] );
        }   // end for

        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer();
        serializer.write(ids);
        serializer.write(names);
        serializer.write(levels);
        serializer.write(online);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOCharListPacket()

    /** 
     * Creates a new instance of AOCharListPacket 
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOCharListPacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            int[]    ids    = parser.parseIntArray();
            String[] names  = parser.parseStringArray();
            int[]    levels = parser.parseIntArray();
            int[]    online = parser.parseIntArray();
        
            m_characters = new AOCharacter[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                m_characters[i] = new AOCharacter( ids[i], names[i], levels[i], online[i] );
            }   // end for
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOCharListPacket()
    
    /** Returns the number of characters in the list. */
    public int getNumCharacters() { return m_characters.length; }
    /** Returns a specific character. */
    public AOCharacter getCharacter(int index) { return m_characters[index]; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    /** 
     * Returns the character with the given ID 
     * or returns null if the character cannot be found. 
     */
    public AOCharacter findCharacter(int id) {
        for (int i = 0; i < m_characters.length; ++i) {
            if (id == m_characters[i].getID()) { 
                return m_characters[i]; 
            }   // end if
        }   // end for
        
        return null;
    }   // end findCharacter()
    
    /** 
     * Returns the character with the given name 
     * or returns null if the character cannot be found. 
     */
    public AOCharacter findCharacter(String name) {
        for (int i = 0; i < m_characters.length; ++i) {
            if (name.equalsIgnoreCase( m_characters[i].getName() )) { 
                return m_characters[i]; 
            }   // end if
        }   // end for
        
        return null;
    }   // end findCharacter()
    
    public String toString() {
        String result = "AOCharListPacket: ";
        
        if (m_characters.length > 0) {
            result += m_characters[0].toString(true);
        
            for (int i = 1; i < m_characters.length; ++i) {
                result += ", " + m_characters[i].toString(true);
            }   // end for
        }   // end if
        
        return result;
    }   // end toString()
    
}   // end class AOCharListPacket
