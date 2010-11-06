/*
 * AOLoginSeedPacket.java
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

package ao.protocol.packets.in;

import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

/**
 * <p>AOLoginSeedPacket is sent from the AO server to the client
 * when a connection between them is established. The seed that
 * the server sends to the client is used in the autentication process.
 * Currently the seed is always a 32 character hexadecimal string (128 bit).</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      S
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.AOLoginRequestPacket
 */
public class AOLoginSeedPacket extends AOPacket {
    
    public static final short TYPE = 0;
    
    private final String m_seed;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of AOLoginSeedPacket 
     *
     * @param seed
     *        the seed that will be sent to the client
     * @throws NullPointerException
     *         if seed is null
     */
    public AOLoginSeedPacket(String seed) {
        if (seed == null) { throw new NullPointerException("No seed was passed."); }
        
        m_seed = seed;
        
        // Serialize the packet
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 2 + m_seed.length() );
        serializer.write(m_seed);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOLoginSeedPacket()
    
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
    public AOLoginSeedPacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_seed = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOLoginSeedPacket()
    
    /** Returns the seed that was recieved from the server */
    public String getLoginSeed() { return m_seed; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOLoginSeedPacket: " + m_seed;
    }   // end toString()
    
}   // end class AOLoginSeedPacket
