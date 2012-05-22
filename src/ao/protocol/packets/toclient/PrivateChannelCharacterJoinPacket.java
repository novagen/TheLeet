/*
 * PrivateChannelCharacterJoinPacket.java
 *
 * Created on July 11, 2010, 2:30 PM
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

package ao.protocol.packets.toclient;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class PrivateChannelCharacterJoinPacket extends Packet {
    
    public static final short TYPE = 55;
    
    private final int m_groupID;
    private final int m_id;
    private final byte[] m_data;

    /**
     * Creates a new instance of PrivateChannelCharacterJoinPacket
     *
     * @param groupID
     *        ID of the character who will host the channel
     * @param charID
     *        ID of the character who will be joining the channel
     */
    public PrivateChannelCharacterJoinPacket(int groupID, int charID) {
        m_groupID = groupID;
        m_id = charID;

        // Serialize the packet
        PacketSerializer serializer =
                new PacketSerializer(4 + 4 + 4);
        serializer.write(m_groupID);
        serializer.write(m_id);

        m_data = serializer.getResult();
        serializer.close();
    }   // end PrivateChannelCharacterJoinPacket()

    /**
     * Creates a new instance of PrivateChannelCharacterJoinPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public PrivateChannelCharacterJoinPacket(byte[] data) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            PacketParser parser = new PacketParser(data);
            // Parse the packet
            m_groupID = parser.parseInt();
            m_id = parser.parseInt();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end PrivateChannelCharacterJoinPacket()
    
    public int getGroupID() { return m_groupID; }
    public int getCharID() { return m_id; }
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    @Override
    public String toString() {
        String result = "["+TYPE+"]PrivateChannelCharacterJoinPacket: ";
        
        result += m_groupID + ", " + Integer.toHexString( m_id );
        
        return result;
    }   // end toString()
    
}   // end class PrivateChannelCharacterJoinPacket
