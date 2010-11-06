/*
 * AOSystemMessagePacket.java
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
 * <p>{@code AOSystemMessagePacket} is sent from the AO server
 * to the client to inform the client of some event(s) or technical difficulty.
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see AOPrivateMessagePacket
 * @see AOVicinityMessagePacket
 * @see AOAnonVicinityMessagePacket
 * @see AOSystemMessagePacket
 */
public class AOSystemMessagePacket extends AOMessagePacket {
    
    public static final short TYPE = 36;
    
    private final String m_msg;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of AOSystemMessagePacket
     *
     * @param msg
     *        the message that will be sent
     */
    public AOSystemMessagePacket(String msg) {
        m_msg    = msg;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 2 + m_msg.length() );
        serializer.write(m_msg);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOSystemMessagePacket()
    
    /** 
     * Creates a new instance of AOSystemMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOSystemMessagePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_msg = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOSystemMessagePacket()
    
    /** Returns the message that was recieved */
    public String getMessage() { return m_msg; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOAnonVicinityMessagePacket: " + m_msg;
    }   // end toString()
    
    public String display(AOCharacterIDTable charTable, AOGroupTable groupTable) {
        return "System: " + m_msg;
    }   // end log()
    
}   // end class AOAnonVicinityMessagePacket
