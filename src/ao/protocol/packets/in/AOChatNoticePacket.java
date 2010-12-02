/*
 * AOChatNoticePacket.java
 *
 * Created on September 23, 2010, 12:16 PM
 *************************************************************************
 * Copyright 2010 Kevin Kendall
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

import java.io.IOException;

public class AOChatNoticePacket extends AOPacket {
    
    public static final short TYPE = 37;
    
    private final int m_i1;
    private final int m_i2;
    private final int m_i3;
    private final String m_msg;
    private final byte[] m_data;
    
    public AOChatNoticePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
            
            // Parse the packet
            m_i1   = parser.parseInt();
            m_i2   = parser.parseInt();
            m_i3   = parser.parseInt();
            m_msg  = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOClientNamePacket()
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    public String getMsg() { return m_msg; }
    public int getCharID() { return m_i1; }
    public int getRaw2() { return m_i2; }
    public String getMsgType() { return Integer.toHexString(m_i3); }

    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        return "AOChatNoticePacket: "
            + Integer.toHexString( m_i1 ) + ", " + m_i2 + ", " + Integer.toHexString(  m_i3 ) + ", " + m_msg;
    }   // end toString()
    
}   // end class AOClientNamePacket
