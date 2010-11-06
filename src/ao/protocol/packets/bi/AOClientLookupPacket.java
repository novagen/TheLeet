/*
 * AOClientLookupRequestPacket.java
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

package ao.protocol.packets.bi;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>{@code AOClientLookupRequestPacket} is sent to the AO server
 * to request the ID of a character who's name is known.</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   out</p>
 *
 * @author Paul Smith
 * @see AOClientLookupPacket
 * @see AOClientLookupResultPacket
 * @see AOClientNamePacket
 * @see AOClientUnknownPacket
 */
public class AOClientLookupPacket extends AOPacket {
    
    public static final short TYPE = 21;
    
    private final String m_name;
    private final int m_id;
    private final byte[] m_data;
    private final Direction m_direction;
    
    /**
     * Creates a new instance of AOClientLookupRequestPacket
     * 
     * @param name
     *        the name of the character to lookup
     * @throws NullPointerException
     *         if name is null
     */
    public AOClientLookupPacket(String name) {
        if (name == null) { throw new NullPointerException("No name was passed."); }
        
        m_name = name;
        m_direction = Direction.OUT;
        m_id = -1;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 2 + m_name.length() );
        serializer.write(m_name);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOClientLookupRequestPacket()
    
    /** 
     * Creates a new instance of AOClientLookupRequestPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOClientLookupPacket(byte[] data, Direction d) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data = data;
            m_direction = d;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_id   = parser.parseInt();
            m_name = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end AOClientLookupRequestPacket()
    
    /** Returns the name of the character lookup */
    public String getCharacterName() { return m_name; }
    /** Returns the id of the character lookup */
    public int getCharacterID() { return m_id; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.OUT} */
    public Direction getDirection() { return m_direction; }
    
    public String toString() {
    	return "AOClientLookupPacket: " 
        + Integer.toHexString( getCharacterID() ) + ", " + getCharacterName();
    }   // end toString()
    
}   // end class AOClientLookupRequestPacket
