/*
 * PrivateChannelAcceptPacket.java
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
package ao.protocol.packets.toserver;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class PrivateChannelAcceptPacket extends Packet {

    public static final short TYPE = 52;
    private final byte[] m_data;
    private final int m_id;

    /**
     * Creates a new instance of PrivateChannelAcceptPacket
     *
     * @param id
     *        The id of the character who is hosting the channel
     */
    public PrivateChannelAcceptPacket(int id) {
        m_id = id;
        // Serialize the packet
        PacketSerializer serializer =
                new PacketSerializer(4 + 4);
        serializer.write(id);

        m_data = serializer.getResult();
        serializer.close();
    }   // end PrivateChannelAcceptPacket()

    /**
     * Creates a new instance of PrivateChannelAcceptPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public PrivateChannelAcceptPacket(byte[] data) throws MalformedPacketException {
        if (data == null) {
            throw new NullPointerException("No binary data was passed.");
        }

        try {
            m_data = data;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_id = parser.parseInt();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                    "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.TO_SERVER));
        }   // end catch
    }   // end PrivateChannelJoinPacket()

    public int getGroupID() {
        return m_id;
    }

    public short getType() {
        return TYPE;
    }

    public byte[] getData() {
        return m_data;
    }

    public Direction getDirection() {
        return Direction.TO_SERVER;
    }

    @Override
    public String toString() {
        String temp = "[" + TYPE + "]PrivateChannelJoinPacket: " + m_id;
        return temp;
    }   // end toString()
}
