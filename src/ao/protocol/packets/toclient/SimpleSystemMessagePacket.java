/*
 * SimpleSystemMessagePacket.java
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
 * <p>{@code SimpleSystemMessagePacket} is sent from the AO server
 * to the client to inform the client of some event(s) or technical difficulty.
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.bi.PrivateMessagePacket
 * @see VicinityMessagePacket
 * @see BroadcastMessagePacket
 * @see SystemMessagePacket
 */
public class SimpleSystemMessagePacket extends MessagePacket {
    
    public static final short TYPE = 36;
    
    private final String m_msg;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of SimpleSystemMessagePacket
     *
     * @param msg
     *        the message that will be sent
     */
    public SimpleSystemMessagePacket(String msg) {
        m_msg    = msg;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 2 + m_msg.length() );
        serializer.write(m_msg);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end SimpleSystemMessagePacket()
    
    /** 
     * Creates a new instance of SimpleSystemMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public SimpleSystemMessagePacket(byte[] data) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_msg = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.TO_CLIENT)
            );
        }   // end catch
    }   // end SimpleSystemMessagePacket()
    
    /** Returns the message that was recieved */
    public String getMessage() { return m_msg; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.TO_CLIENT} */
    public Direction getDirection() { return Direction.TO_CLIENT; }
    
    @Override
    public String toString() {
        return "["+TYPE+"]SimpleSystemMessagePacket: " + m_msg;
    }   // end toString()
    
    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        return "System: " + m_msg;
    }   // end log()
    
}   // end class SimpleSystemMessagePacket
