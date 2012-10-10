/*
 * BroadcastMessagePacket.java
 *
 * Created on March 28, 2008, 11:42 AM
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

import ao.protocol.CharacterIDTable;
import ao.protocol.GroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p>{@code BroadcastMessagePacket} is sent from the server
 * to the client when an anonymous character says something in vicinity.
 * Often times, these are system messages.</p>
 *
 * <p>FIXME: What are the first and second strings in this packet used for?
 * Can the message sent/recieved ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      SSS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 */
public class BroadcastMessagePacket extends MessagePacket {

    public static final short TYPE = 35;
    private final String m_source;
    private final String m_msg;
    private final String m_str;
    private final byte[] m_data;

    /** 
     * Creates a new instance of BroadcastMessagePacket
     *
     * @param source
     *        ???
     * @param msg
     *        the message that will be sent
     */
    public BroadcastMessagePacket(String source, String msg) {
        this(source, msg, "\0");
    }   // end AOAnonVicinityMessagePacket()

    /** 
     * Creates a new instance of BroadcastMessagePacket
     *
     * @param source
     *        ???
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public BroadcastMessagePacket(String source, String msg, String str) {
        m_source = source;
        m_msg = msg;
        m_str = str;

        // Serialize the packet
        PacketSerializer serializer =
                new PacketSerializer(4 + m_source.length() + m_msg.length() + m_str.length());
        serializer.write(m_source);
        serializer.write(m_msg);
        //serializer.write(m_str);
        serializer.write((byte)0x0);
        serializer.write((byte)0x1);
        serializer.write((byte)0x0);

        m_data = serializer.getResult();
        serializer.close();
    }   // end BroadcastMessagePacket()

    /** 
     * Creates a new instance of BroadcastMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public BroadcastMessagePacket(byte[] data) throws MalformedPacketException {
        if (data == null) {
            throw new NullPointerException("No binary data was passed.");
        }

        try {
            m_data = data;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_source = parser.parseString();
            m_msg = parser.parseString();
            m_str = parser.parseString();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                    "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.TO_CLIENT));
        }   // end catch
    }   // end BroadcastMessagePacket()

    public String getSource() {
        return m_source;
    }

    /** Returns the message that was recieved */
    public String getMessage() {
        return m_msg;
    }

    public String getStr() {
        return m_str;
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
        return "["+TYPE+"]BroadcastMessagePacket: "
                + m_source + ", " + m_msg + ", " + m_str;
    }   // end toString()

    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        return (m_source.equals("") ? "" : (m_source + ": ")) + m_msg;
    }   // end log()
}   // end class BroadcastMessagePacket

