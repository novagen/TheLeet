/*
 * AOLoginSeedPacket.java
 *
 * Created on May 13, 2007, 5:36 PM
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
 * <p></p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in/out</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.AOLoginRequestPacket
 */
public class AOPingPacket extends AOPacket {
    
    public static final short TYPE = 100;
    
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;
    
    /** 
     * Creates a new instance of AOPingPacket
     *
     * @param str
     *        ???
     * @throws NullPointerException
     *         if str is null
     */
    public AOPingPacket(String str, Direction d) {
        if (str == null) { throw new NullPointerException("No string was passed."); }
        
        m_str       = str;
        m_direction = d;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 2 + m_str.length() );
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOPingPacket()
    
    /** 
     * Creates a new instance of AOPingPacket 
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOPingPacket(byte[] data, Direction d) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            m_direction           = d;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_str = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOPingPacket()
    
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns which direction this ping has traveled/will travel */
    public Direction getDirection() { return m_direction; }
    
    public String toString() {
        return "AOPingPacket: " + m_str;
    }   // end toString()
    
}   // end class AOPingPacket
