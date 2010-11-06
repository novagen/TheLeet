/*
 * AOAnonVicinityMessagePacket.java
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
 * <p>{@code AOAnonVicinityMessagePacket} is sent from the AO server
 * to the client when an anonymous character says something in vicinity.
 * Often times, these are system messages.
 * This packet is the same as {@link AOVicinityMessagePacket},
 * except that the types are different and this one is anonymous.</p>
 *
 * <p>FIXME: What are the first and second strings in this packet used for?
 * Can the message sent/recieved ever be null?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      SSS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see AOPrivateMessagePacket
 * @see AOVicinityMessagePacket
 * @see AOAnonVicinityMessagePacket
 * @see AOSystemMessagePacket
 * @see AOGroupMessagePacket
 */
public class AOAnonVicinityMessagePacket extends AOMessagePacket {
    
    public static final short TYPE = 35;
    
    private final String m_source;
    private final String m_msg;
    private final String m_str;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of AOAnonVicinityMessagePacket
     *
     * @param source
     *        ???
     * @param msg
     *        the message that will be sent
     */
    public AOAnonVicinityMessagePacket(String source, String msg) {
        this(source, msg, "\0");
    }   // end AOAnonVicinityMessagePacket()
    
    /** 
     * Creates a new instance of AOAnonVicinityMessagePacket
     *
     * @param source
     *        ???
     * @param msg
     *        the message that will be sent
     * @param str
     *        ???
     */
    public AOAnonVicinityMessagePacket(String source, String msg, String str) {
        m_source = source;
        m_msg    = msg;
        m_str    = str;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 6 + m_source.length() + m_msg.length() + m_str.length() );
        serializer.write(m_source);
        serializer.write(m_msg);
        serializer.write(m_str);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOAnonVicinityMessagePacket()
    
    /** 
     * Creates a new instance of AOAnonVicinityMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOAnonVicinityMessagePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_source = parser.parseString();
            m_msg    = parser.parseString();
            m_str    = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOAnonVicinityMessagePacket()
    
    public String getSource() { return m_source; }
    /** Returns the message that was recieved */
    public String getMessage() { return m_msg; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOAnonVicinityMessagePacket: " 
            + m_source + ", " + m_msg + ", " + m_str;
    }   // end toString()
    
    public String display(AOCharacterIDTable charTable, AOGroupTable groupTable) {
        return (m_source.equals("") ? "" : (m_source + ": ")) + m_msg;
    }   // end log()
    
}   // end class AOAnonVicinityMessagePacket
