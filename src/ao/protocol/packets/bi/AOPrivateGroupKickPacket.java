/*
 * AOPrivateGroupKickPacket.java
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
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

public class AOPrivateGroupKickPacket extends AOPacket  {

    public static final short TYPE = 51;

    private final byte[] m_data;
    private final int m_id;
    private final Direction m_direction;
    
    public AOPrivateGroupKickPacket(int id) {
        m_direction = Direction.OUT;
        m_id = id;

        // Serialize the packet
        AOPacketSerializer serializer =
            new AOPacketSerializer( 4 + 4 );
        serializer.write(m_id);

        m_data = serializer.getResult();
        serializer.close();
    }   // end AOGroupMessagePacket()

    public AOPrivateGroupKickPacket(byte[] data, Direction d) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        try {
            m_data = data;
            m_direction = d;
            AOPacketParser parser = new AOPacketParser(data);
            m_id = parser.parseInt();
            
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end AOGroupAnnouncePacket()

    public int getGroupdID(){
        return m_id;
    }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }

    public String toString() {
        String result = "AOPrivateGroupKickPacket: ";
        result +=  Integer.toHexString( m_id );

        return result;
    }   // end toString()
}   // end class AOGroupInvitePacket
