/*
 * AOPrivateMessagePacket.java
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

import ao.protocol.AOCharacterIDTable;
import ao.protocol.AOGroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>{@code AOPrivateMessagePacket} is sent back and forth between the AO server
 * and client when private messages are sent and recieved by the client.</p>
 *
 * <p>FIXME: What is the second string in this packet used for?
 * Can the message sent/recieved ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      ISS
 * <br>DIRECTION:   in/out</p>
 *
 * @author Paul Smith
 * @see AOPrivateMessagePacket
 * @see AOVicinityMessagePacket
 * @see AOAnonVicinityMessagePacket
 * @see AOSystemMessagePacket
 * @see AOGroupMessagePacket
 */
public class AOPrivateMessagePacket extends AOMessagePacket {
    
    public static final short TYPE = 30;
    
    private final int       m_characterID;
    private final String    m_msg;
    private final String    m_str;
    private final byte[]    m_data;
    private final Direction m_direction;
    
    /** 
     * Creates a new instance of AOPrivateMessagePacket 
     *
     * @param characterID
     *        the ID of the reciever of this message
     * @param msg
     *        the message that will be sent
     */
    public AOPrivateMessagePacket(int characterID, String msg, Direction d) {
        this(characterID, msg, "\0", d);
    }   // end AOPrivateMessagePacket()
    
    /** 
     * Creates a new instance of AOPrivateMessagePacket 
     *
     * @param characterID
     *        the ID of the reciever of this message
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public AOPrivateMessagePacket(int characterID, String msg, String str, Direction d) {
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = str;
        m_direction   = d;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 4 + 4 + m_msg.length() + m_str.length() );
        serializer.write(m_characterID);
        serializer.write(m_msg);
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOPrivateMessagePacket()
    
    /** 
     * Creates a new instance of AOLoginSeedPacket 
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOPrivateMessagePacket(byte[] data, Direction d) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            m_direction           = d;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_characterID = parser.parseInt();
            m_msg         = parser.parseString();
            m_str         = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, d)
            );
        }   // end catch
    }   // end AOPrivateMessagePacket()
    
    /** Returns the ID of the sender/reciever of this message */
    public int getCharID() { return m_characterID; }
    /** Returns the message that was sent/recieved */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this tell was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }
    
    public String toString() {
        String temp = "AOPrivateMessagePacket: " 
                + Integer.toHexString( m_characterID ) + ", " 
                + m_msg;
        if(m_str.compareTo("") != 0){
            temp += ", " + m_str;
        }
        return temp;
    }   // end toString()
    
    public String display(AOCharacterIDTable charTable, AOGroupTable groupTable) {
        String name = (charTable == null ? null : charTable.getName(m_characterID));
        
        return (m_direction == Direction.IN ? "from " : "to ") 
            + "[" + (name == null ? Integer.toHexString( m_characterID ) : name) + "]: " 
            + m_msg;
    }   // end log()
    
}   // end class AOPrivateMessagePacket
