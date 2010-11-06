/*
 * AOClientUnknownPacket.java
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
 * <p>???</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:     I
 * <br>DIRECTION:  in</p>
 *
 * @author Paul Smith
 * @see AOClientLookupPacket
 * @see AOClientLookupResultPacket
 * @see AOClientNamePacket
 * @see AOClientUnknownPacket
 */
public class AOClientUnknownPacket extends AOPacket {
    
    public static final short TYPE = 10;
    
    private final int    m_int;
    private final byte[] m_data;
    
    /**
     * Creates a new instance of AOClientUnknownPacket 
     * 
     * @param i
     *        ???
     */
    public AOClientUnknownPacket(int i) {
        m_int = i;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 4 );
        serializer.write(m_int);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOClientUnknownPacket()
    
    /** 
     * Creates a new instance of AOLoginSeedPacket 
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOClientUnknownPacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_int = parser.parseInt();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOClientUnknownPacket()
    
    /** Returns ??? */
    public int getInt() { return m_int; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOClientUnknown: " + m_int;
    }   // end toString()
    
}   // end class AOClientUnknownPacket
