/*
 * AOVicinityMessagePacket.java
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

package ao.protocol.packets.in;

import ao.protocol.AOCharacterIDTable;
import ao.protocol.AOGroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>{@code AOVicinityMessagePacket} is sent from the AO server
 * to the client when somebody says something in vicinity.
 * This packet is the same as {@link AOPrivateMessagePacket},
 * except that the types are different and this one is 
 * unidirectional rather than bidirectional</p>
 *
 * <p>FIXME: What is the second string in this packet used for?
 * Can the message sent/recieved ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      ISS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see AOPrivateMessagePacket
 * @see AOVicinityMessagePacket
 * @see AOAnonVicinityMessagePacket
 * @see AOSystemMessagePacket
 * @see AOGroupMessagePacket
 */
public class AOVicinityMessagePacket extends AOMessagePacket {
    
    public static final short TYPE = 34;
    
    private final int    m_characterID;
    private final String m_msg;
    private final String m_str;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of AOVicinityMessagePacket 
     *
     * @param characterID
     *        the ID of the reciever of this message
     * @param msg
     *        the message that will be sent
     */
    public AOVicinityMessagePacket(int characterID, String msg) {
        this(characterID, msg, "\0");
    }   // end AOVicinityMessagePacket()
    
    /** 
     * Creates a new instance of AOVicinityMessagePacket 
     *
     * @param characterID
     *        the ID of the reciever of this message
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public AOVicinityMessagePacket(int characterID, String msg, String str) {
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = str;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 8 + m_msg.length() + m_str.length() );
        serializer.write(m_characterID);
        serializer.write(m_msg);
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOVicinityMessagePacket()
    
    /** 
     * Creates a new instance of AOVicinityMessagePacket 
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOVicinityMessagePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_characterID = parser.parseInt();
            m_msg         = parser.parseString();
            m_str         = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOVicinityMessagePacket()
    
    /** Returns the ID of the sender of this message */
    public int getTargetCharacterID() { return m_characterID; }
    /** Returns the message that was recieved */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOVicinityMessagePacket: " 
            + Integer.toHexString( m_characterID ) + ", " 
            + m_msg + ", " + m_str;
    }   // end toString()
    
    public String display(AOCharacterIDTable charTable, AOGroupTable groupTable) {
        String name = (charTable == null ? null : charTable.getName(m_characterID));
        
        return (name == null ? Integer.toHexString( m_characterID ) : name) + ": " + m_msg;
    }   // end log()
    
}   // end class AOVicinityMessagePacket 
