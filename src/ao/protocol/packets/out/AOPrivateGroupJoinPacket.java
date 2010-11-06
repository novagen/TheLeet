/*
 * AOPrivateGroupJoinPacket.java
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

package ao.protocol.packets.out;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

public class AOPrivateGroupJoinPacket extends AOPacket {

    public static final short TYPE = 52;

    private final byte[] m_data;
    private final int m_id;

    public AOPrivateGroupJoinPacket(int id){
        m_id = id;
        // Serialize the packet
        AOPacketSerializer serializer =
            new AOPacketSerializer( 4 + 4 );
        serializer.write(id);

        m_data = serializer.getResult();
        serializer.close();
    }

    public AOPrivateGroupJoinPacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }

        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);

            // Parse the packet
            m_id = parser.parseInt();

            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.OUT)
            );
        }   // end catch
    }   // end AOClientLookupRequestPacket()

    public int getGroupID() { return m_id; }
    
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }

    public Direction getDirection() { return Direction.OUT; }

    public String toString() {
        String temp = "AOPrivateGroupJoinPacket: " + m_id;
        return temp;
    }   // end toString()

}
