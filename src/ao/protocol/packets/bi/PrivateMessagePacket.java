/*
 * PrivateMessagePacket.java
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

import ao.protocol.CharacterIDTable;
import ao.protocol.GroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p>{@code AOPrivateMessagePacket} is sent back and forth between the AO server
 * and client when private messages are sent and received by the client.</p>
 *
 * <p>FIXME: What is the second string in this packet used for?
 * Can the message sent/received ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      ISS
 * <br>DIRECTION:   in/out</p>
 *
 * @author Paul Smith
 */
public class PrivateMessagePacket extends MessagePacket {
    
    public static final short TYPE = 30;
    
    private final int       m_characterID;
    private final String    m_msg;
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;
    
    /** 
     * Creates a new instance of PrivateMessagePacket
     *
     * @param characterID
     *        the ID of the receiver of this message
     * @param msg
     *        the message that will be sent
     * @param d
     *        the direction that the message is being send
     */
    public PrivateMessagePacket(int characterID, String msg, Direction d) {
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = "";
        m_direction   = d;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 + m_msg.length() + m_str.length() );
        serializer.write(m_characterID);
        serializer.write(m_msg);

        //serializer.write(m_str);
        serializer.write((byte)0x0);
        serializer.write((byte)0x1);
        serializer.write((byte)0x0);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end PrivateMessagePacket()
    
    /** 
     * Creates a new instance of PrivateMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public PrivateMessagePacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            m_direction           = d;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_characterID = parser.parseInt();
            m_msg         = parser.parseString();
            m_str         = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end PrivateMessagePacket()
    
    /** Returns the ID of the sender/receiver of this message */
    public int getCharID() { return m_characterID; }
    /** Returns the message that was sent/received */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this tell was received or sent by the client */
    public Direction getDirection() { return m_direction; }
    
    @Override
    public String toString() {
        String temp = "["+TYPE+"]PrivateMessagePacket: "
                + Integer.toHexString( m_characterID ) + ", " 
                + m_msg;
        if(m_str.compareTo("") != 0){
            temp += ", " + m_str;
        }
        return temp;
    }   // end toString()
    
    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        String name = (charTable == null ? null : charTable.getName(m_characterID));
        
        return (m_direction == Direction.IN ? "from " : "to ") 
            + "[" + (name == null ? Integer.toHexString( m_characterID ) : name) + "]: " 
            + m_msg;
    }   // end log()
    
}   // end class PrivateMessagePacket
