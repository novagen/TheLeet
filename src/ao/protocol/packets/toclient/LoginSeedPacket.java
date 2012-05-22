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
package ao.protocol.packets.toclient;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p>LoginSeedPacket is sent from the AO server to the client
 * when a connection between them is established. The seed that
 * the server sends to the client is used in the autentication process.
 * Currently the seed is always a 32 character hexadecimal string (128 bit).</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.toserver.LoginRequestPacket
 */
public class LoginSeedPacket extends Packet {

    public static final short TYPE = 0;
    private final String m_seed;
    private final byte[] m_data;

    /** 
     * Creates a new instance of LoginSeedPacket
     *
     * @param seed
     *        the seed that will be sent to the client
     * @throws NullPointerException
     *         if seed is null
     */
    public LoginSeedPacket(String seed) {
        if (seed == null) {
            throw new NullPointerException("No seed was passed.");
        }

        m_seed = seed;

        // Serialize the packet
        PacketSerializer serializer =
                new PacketSerializer(2 + m_seed.length());
        serializer.write(m_seed);

        m_data = serializer.getResult();
        serializer.close();
    }   // end LoginSeedPacket()

    /** 
     * Creates a new instance of LoginSeedPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public LoginSeedPacket(byte[] data) throws MalformedPacketException {
        if (data == null) {
            throw new NullPointerException("No binary data was passed.");
        }

        try {
            m_data = data;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_seed = parser.parseString();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                    "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN));
        }   // end catch
    }   // end LoginSeedPacket()

    /** Returns the seed that was recieved from the server */
    public String getLoginSeed() {
        return m_seed;
    }

    /** Always returns {@value #TYPE} */
    public short getType() {
        return TYPE;
    }

    public byte[] getData() {
        return m_data;
    }

    /** Always returns {@code Direction.IN} */
    public Direction getDirection() {
        return Direction.IN;
    }

    @Override
    public String toString() {
        return "[" + TYPE + "]LoginSeedPacket: " + m_seed;
    }   // end toString()
}   // end class LoginSeedPacket

