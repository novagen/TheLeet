/*
 * AOClientNamePacket.java
 *
 * Created on March 22, 2008, 12:25 PM
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

import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>{@code AOClientNamePacket}s are periodically sent from the AO server
 * to the client. One is sent when the client successfully logs in.
 * They are also sent before a client recieves a message from a user.</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      IS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see AOClientLookupPacket
 * @see AOClientLookupResultPacket
 * @see AOClientNamePacket
 * @see AOClientUnknownPacket
 */
public class AOClientNamePacket extends AOPacket {
    
    public static final short TYPE = 20;
    
    private final int    m_id;
    private final String m_name;
    private final byte[] m_data;
    
    /**
     * Creates a new instance of AOClientNamePacket
     * 
     * @param id
     *        the id of the character
     * @param name
     *        the name of the character
     * @throws NullPointerException
     *         if name is null
     */
    public AOClientNamePacket(int id, String name) {
        if (name == null) { throw new NullPointerException("No name was passed."); }
        
        m_id   = id;
        m_name = name;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 6 + m_name.length() );
        serializer.write(m_id);
        serializer.write(m_name);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOClientNamePacket()
    
    /** 
     * Creates a new instance of AOClientNamePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOClientNamePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
            
            // Parse the packet
            m_id   = parser.parseInt();
            m_name = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOClientNamePacket()
    
    /** Returns the id of the character */
    public int getCharacterID() { return m_id; }
    /** Returns the name of the character */
    public String getCharacterName() { return m_name; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOClientNamePacket: " 
            + Integer.toHexString( getCharacterID() ) + ", " + getCharacterName();
    }   // end toString()
    
}   // end class AOClientNamePacket
