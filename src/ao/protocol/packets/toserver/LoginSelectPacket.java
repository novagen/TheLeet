/*
 * LoginSelectPacket.java
 *
 * Created on May 12, 2007, 10:37 PM
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

package ao.protocol.packets.toserver;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

/**
 * <p>LoginSelectPacket is sent from the client to the AO Server
 * in response to a ChararacterListPacket, in order to login the client.</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      I
 * <br>DIRECTION:   out</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.toclient.CharacterListPacket
 * @see ao.protocol.packets.toclient.LoginErrorPacket
 * @see ao.protocol.packets.toclient.LoginOkPacket
 */
public class LoginSelectPacket extends Packet {
    
    public static final short TYPE = 3;
    
    private final int    m_id;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of LoginSelectPacket
     *
     * @param id
     *        the ID of the character that the client will attempt to login as
     */
    public LoginSelectPacket(int id) {
        m_id = id;
        
        // Serialize the packet
        PacketSerializer serializer = new PacketSerializer(4);
        serializer.write(m_id);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end LoginSelectPacket()
    
    /** 
     * Creates a new instance of LoginSelectPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public LoginSelectPacket(byte[] data) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            PacketParser parser = new PacketParser(data);
        
            // Parse the packet
            m_id = parser.parseInt();
        
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.OUT)
            );
        }   // end catch
    }   // end LoginSelectPacket()
    
    /** Returns the ID of the character that the client will attempt to login as */
    public int getCharacterID() { return m_id; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.OUT} */
    public Direction getDirection() { return Direction.OUT; }
    
    @Override
    public String toString() {
        return "["+TYPE+"]LoginSelectPacket: " + Integer.toHexString( getCharacterID() );
    }   // end toString()
    
}   // end class LoginSelectPacket
