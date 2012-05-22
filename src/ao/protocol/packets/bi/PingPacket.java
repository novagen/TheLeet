/*
 * LoginSeedPacket.java
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
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p></p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in/out</p>
 *
 * @author Paul Smith
 */
public class PingPacket extends Packet {
    
    public static final short TYPE = 100;
    
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;
    
    /** 
     * Creates a new instance of PingPacket
     *
     * @param str
     *        ???
     * @throws NullPointerException
     *         if str is null
     */
    public PingPacket(String str, Direction d) {
        if (str == null) { throw new NullPointerException("No string was passed."); }
        
        m_str       = str;
        m_direction = d;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 2 + m_str.length() );
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end PingPacket()
    
    /** 
     * Creates a new instance of PingPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public PingPacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            m_direction           = d;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_str = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end PingPacket()
    
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns which direction this ping has traveled/will travel */
    public Direction getDirection() { return m_direction; }
    
    @Override
    public String toString() {
        return "["+TYPE+"]PingPacket: " + m_str;
    }   // end toString()
    
}   // end class PingPacket
