/*
 * PrivateChannelKickPacket.java
 *
 * Created on July 12, 2010, 2:30 PM
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

package ao.protocol.packets.bi;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class PrivateChannelKickPacket extends Packet  {

    public static final short TYPE = 51;

    private final byte[] m_data;
    private final int m_id;
    private final Direction m_direction;
    
    public PrivateChannelKickPacket(int id) {
        m_direction = Direction.OUT;
        m_id = id;

        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 );
        serializer.write(m_id);

        m_data = serializer.getResult();
        serializer.close();
    }   // end PrivateChannelKickPacket()

    public PrivateChannelKickPacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        try {
            m_data = data;
            m_direction = d;
            PacketParser parser = new PacketParser(data);
            m_id = parser.parseInt();
            
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end PrivateChannelKickPacket()

    public int getGroupID(){
        return m_id;
    }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }

    @Override
    public String toString() {
        String result = "["+TYPE+"]PrivateChannelKickPacket: ";
        result +=  Integer.toHexString( m_id );

        return result;
    }   // end toString()
}   // end class PrivateChannelKickPacket
