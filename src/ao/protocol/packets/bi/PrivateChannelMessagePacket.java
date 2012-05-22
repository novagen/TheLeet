/*
 * PrivateChannelMessagePacket.java
 *
 * Created on July 10, 2010, 2:10 PM
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

import ao.protocol.CharacterIDTable;
import ao.protocol.GroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class PrivateChannelMessagePacket extends MessagePacket {

    public static final short TYPE = 57;

    private final int    m_groupID;
    private final int       m_characterID;
    private final String    m_msg;
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;

    /**
     * Creates a new instance of PrivateChannelMessagePacket
     * (outgoing only)
     *
     * @param groupID
     *        the ID of the group that the message will be sent to
     * @param msg
     *        the message that will be sent
     */
    public PrivateChannelMessagePacket(int groupID, String msg) {
        this(groupID, msg, "\0");
    }   // end PrivateChannelMessagePacket()

    /**
     * Creates a new instance of PrivateChannelMessagePacket
     * (outcoming only)
     *
     * @param groupID
     *        the ID of the group that the message will be sent to
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public PrivateChannelMessagePacket(int groupID, String msg, String str) {
        this(groupID, -1, msg, str, Direction.OUT);
    }   // end PrivateChannelMessagePacket()

    /**
     * Creates a new instance of PrivateChannelMessagePacket
     *
     * @param groupID
     *        the ID of the group that the message was sent to
     * @param characterID
     *        the ID of the character that sent this message
     * @param msg
     *        the message that was be sent
     */
    public PrivateChannelMessagePacket(int groupID, int characterID, String msg, Direction d) {
        this(groupID, characterID, msg, "\0", d);
    }   // end PrivateChannelMessagePacket()

    /**
     * Creates a new instance of PrivateChannelMessagePacket
     *
     * @param groupID
     *        the ID of the group that the message was sent to
     * @param characterID
     *        the ID of the character that sent this message
     * @param msg
     *        the message that was be sent
     * @param str
     *        ???
     */
    public PrivateChannelMessagePacket(int groupID, int characterID, String msg, String str, Direction d) {
        m_groupID     = groupID;
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = str;
        m_direction   = d;

        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 + 4 + m_msg.length() + m_str.length() );
        serializer.write(m_groupID);

        if (m_direction == Direction.IN) {
            serializer.write(m_characterID);
        }   // end if

        serializer.write(m_msg);
        //serializer.write(m_str);
        serializer.write((byte)0x0);
        serializer.write((byte)0x1);
        serializer.write((byte)0x0);

        m_data = serializer.getResult();
        serializer.close();
    }   // end PrivateChannelMessagePacket()

        /**
     * Creates a new instance of PrivateChannelMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public PrivateChannelMessagePacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }

        try {
            m_data                = data;
            m_direction           = d;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_groupID = parser.parseInt();

            if (m_direction == Direction.IN) {
                m_characterID = parser.parseInt();
            } else {
                m_characterID = -1;
            }   // end else

            m_msg = parser.parseString();
            m_str = parser.parseString();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end PrivateChannelMessagePacket()

    /** Returns the ID of the group that this message was/will be sent to */
    public int getGroupID() { return m_groupID; }
    /** Returns the ID of the sender of this message */
    public int getCharID() { return m_characterID; }
    /** Returns the message that was/will be sent/recieved */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }

    @Override
    public String toString() {
        String result = "["+TYPE+"]PrivateChannelMessagePacket: ";

        result += Integer.toHexString(  m_groupID );
        result += ", " + Integer.toHexString( m_characterID );
        result += ", " + m_msg;
        result += ", " + m_str;

        return result;
    }   // end toString()

    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        String charName  = (charTable  == null ? null : charTable.getName(m_characterID));
        String groupName = (charTable == null ? null : charTable.getName(m_groupID));

        String result    = "[";
        result += groupName;
        result += "] " + (charName == null ? Integer.toHexString( m_characterID ) : charName) + ": ";
        result += m_msg;

        return result;
    }   // end log()

}   // end class PrivateChannelMessagePacket