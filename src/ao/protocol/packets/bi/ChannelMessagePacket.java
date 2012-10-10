/*
 * ChannelMessagePacket.java
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

import ao.db.MMDBDatabase;
import ao.protocol.CharacterIDTable;
import ao.protocol.GroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      GISS/GSS
 * <br>DIRECTION:   in/out</p>
 *
 * @author Paul Smith
 */
public class ChannelMessagePacket extends MessagePacket {
    
    public static final short TYPE = 65;
    
    private final byte[]    m_groupID;
    private final int       m_characterID;
    private final String    m_msg;
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;
    private final ExtendedMessage m_ex_msg;
    
    /** 
     * Creates a new instance of ChannelMessagePacket
     * (outgoing only)
     *
     * @param groupID
     *        the ID of the group that the message will be sent to
     * @param msg
     *        the message that will be sent
     */
    public ChannelMessagePacket(byte[] groupID, String msg) {
        this(groupID, msg, "\0");
    }   // end ChannelMessagePacket()
    
    /** 
     * Creates a new instance of ChannelMessagePacket
     * (outgoing only)
     *
     * @param groupID
     *        the ID of the group that the message will be sent to
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public ChannelMessagePacket(byte[] groupID, String msg, String str) {
        this(groupID, -1, msg, str, Direction.TO_SERVER);
    }   // end ChannelMessagePacket()
    
    /** 
     * Creates a new instance of ChannelMessagePacket
     *
     * @param groupID
     *        the ID of the group that the message was sent to
     * @param characterID
     *        the ID of the character that sent this message
     * @param msg
     *        the message that was be sent
     * @param d
     *        the direction that the message was sent
     */
    public ChannelMessagePacket(byte[] groupID, int characterID, String msg, Direction d) {
        this(groupID, characterID, msg, "\0", d);
    }   // end ChannelMessagePacket()
    
    /** 
     * Creates a new instance of ChannelMessagePacket
     *
     * @param groupID
     *        the ID of the group that the message was sent to
     * @param characterID
     *        the ID of the character that sent this message
     * @param msg
     *        the message that was be sent
     * @param str
     *        ???
     * @param d
     *        the direction that the message was sent
     */
    public ChannelMessagePacket(byte[] groupID, int characterID, String msg, String str, Direction d) {
        m_groupID     = groupID;
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = str;
        m_direction   = d;
        m_ex_msg      = null;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 5 + 4 + m_msg.length() + m_str.length() );
        serializer.write40Bit(m_groupID);
        
        if (m_direction == Direction.TO_CLIENT) {
            serializer.write(m_characterID);
        }   // end if
        
        serializer.write(m_msg);
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end ChannelMessagePacket()
    
    /** 
     * Creates a new instance of ChannelMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @param db
     *        the mdb database that will be used for extended messages
     * @param d
     *        the direction that the packet is being sent
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public ChannelMessagePacket(byte[] data, MMDBDatabase db, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            m_direction           = d;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_groupID = parser.parse40Bit();
            
            if (m_direction == Direction.TO_CLIENT) {
                m_characterID = parser.parseInt();
            } else {
                m_characterID = -1;
            }   // end else
            
            m_msg = parser.parseString();
            m_str = parser.parseString();
        
            parser.close();

            if(m_characterID == 0 && m_msg.startsWith("~&") && m_msg.endsWith("~")){
                String e_msg = m_msg.substring(2, m_msg.length() - 2);
                m_ex_msg = new ExtendedMessage(new DataInputStream(new ByteArrayInputStream(e_msg.getBytes("UTF-8"))), db);
            } else {
                m_ex_msg = null;
            }

        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end ChannelMessagePacket()
    
    /** Returns the ID of the group that this message was/will be sent to */
    public byte[] getGroupID() { return m_groupID; }
    /** Returns the ID of the sender of this message */
    public int getCharID() { return m_characterID; }
    /** Returns the message that was/will be sent/recieved */
    public String getMessage() { return m_msg; }
    public ExtendedMessage getExtendedMessage() { return m_ex_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }
    
    @Override
    public String toString() {
        String result = "["+TYPE+"]ChannelMessagePacket: ";
        
        for (byte b : m_groupID) { result += String.format("%02X", b); }
        
        result += ", " + Integer.toHexString( m_characterID );
        result += ", " + m_msg;
        result += ", " + m_str;
        
        return result;
    }   // end toString()
    
    public String display(CharacterIDTable charTable, GroupTable groupTable) {

        String result = "";
        
        if(m_ex_msg == null){
            String charName  = (charTable  == null ? null : charTable.getName(m_characterID));
            String groupName = (groupTable == null ? null : groupTable.getName(m_groupID));
            result = "[";
        
            if (groupName == null) {
                for (byte b : m_groupID) { result += String.format("%02X", b); }
            } else {
                result += groupName;
            }   // end else
        
            result += "] " + (charName == null ? Integer.toHexString( m_characterID ) : charName) + ": ";
            result += m_msg;
        } else {
            result = m_ex_msg.getFormattedMessage();
        }
        return result;
    }   // end log()
}   // end class ChannelMessagePacket
