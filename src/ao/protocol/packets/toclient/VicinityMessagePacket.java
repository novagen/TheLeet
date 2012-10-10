/*
 * VicinityMessagePacket.java
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
 * <p>{@code VicinityMessagePacket} is sent from the AO server
 * to the client when somebody says something in vicinity.</p>
 *
 * <p>FIXME: What is the second string in this packet used for?
 * Can the message sent/recieved ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      ISS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 */
public class VicinityMessagePacket extends MessagePacket {
    
    public static final short TYPE = 34;
    
    private final int    m_characterID;
    private final String m_msg;
    private final String m_str;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of VicinityMessagePacket
     *
     * @param characterID
     *        the ID of the receiver of this message
     * @param msg
     *        the message that will be sent
     */
    public VicinityMessagePacket(int characterID, String msg) {
        this(characterID, msg, "\0");
    }   // end VicinityMessagePacket()
    
    /** 
     * Creates a new instance of VicinityMessagePacket
     *
     * @param characterID
     *        the ID of the receiver of this message
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public VicinityMessagePacket(int characterID, String msg, String str) {
        m_characterID = characterID;
        m_msg         = msg;
        m_str         = str;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 8 + m_msg.length() + m_str.length() );
        serializer.write(m_characterID);
        serializer.write(m_msg);
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end VicinityMessagePacket()
    
    /** 
     * Creates a new instance of VicinityMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public VicinityMessagePacket(byte[] data) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_characterID = parser.parseInt();
            m_msg         = parser.parseString();
            m_str         = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.TO_CLIENT)
            );
        }   // end catch
    }   // end VicinityMessagePacket()
    
    /** Returns the ID of the sender of this message */
    public int getTargetCharacterID() { return m_characterID; }
    /** Returns the message that was received */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.TO_CLIENT} */
    public Direction getDirection() { return Direction.TO_CLIENT; }
    
    @Override
    public String toString() {
        return "["+TYPE+"]VicinityMessagePacket: "
            + Integer.toHexString( m_characterID ) + ", " 
            + m_msg + ", " + m_str;
    }   // end toString()
    
    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        String name = (charTable == null ? null : charTable.getName(m_characterID));
        
        return (name == null ? Integer.toHexString( m_characterID ) : name) + ": " + m_msg;
    }   // end log()
    
}   // end class VicinityMessagePacket
