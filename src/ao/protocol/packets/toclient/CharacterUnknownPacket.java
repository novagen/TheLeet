/*
 * CharacterUnknownPacket.java
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
package ao.protocol.packets.toclient;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p>???</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:     I
 * <br>DIRECTION:  in</p>
 *
 * @author Paul Smith
 */
public class CharacterUnknownPacket extends Packet {

    public static final short TYPE = 10;
    private final int m_int;
    private final byte[] m_data;

    /**
     * Creates a new instance of CharacterUnknownPacket
     * 
     * @param i
     *        ???
     */
    public CharacterUnknownPacket(int i) {
        m_int = i;

        // Serialize the packet
        PacketSerializer serializer =
                new PacketSerializer(4);
        serializer.write(m_int);

        m_data = serializer.getResult();
        serializer.close();
    }   // end CharacterUnknownPacket()

    /** 
     * Creates a new instance of CharacterUnknownPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public CharacterUnknownPacket(byte[] data) throws MalformedPacketException {
        if (data == null) {
            throw new NullPointerException("No binary data was passed.");
        }

        try {
            m_data = data;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_int = parser.parseInt();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                    "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.TO_CLIENT));
        }   // end catch
    }   // end CharacterUnknownPacket()

    /** Returns ??? */
    public int getInt() {
        return m_int;
    }

    /** Always returns {@value #TYPE} */
    public short getType() {
        return TYPE;
    }

    public byte[] getData() {
        return m_data;
    }

    /** Always returns {@code Direction.TO_CLIENT} */
    public Direction getDirection() {
        return Direction.TO_CLIENT;
    }

    @Override
    public String toString() {
        return "[" + TYPE + "]CharacterUnknownPacket: " + m_int;
    }   // end toString()
}   // end class CharacterUnknownPacket

