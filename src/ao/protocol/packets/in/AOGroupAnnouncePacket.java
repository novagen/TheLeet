/*
 * AOGroupAnnouncePacket.java
 *
 * Created on March 28, 2008, 1:47 PM
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

import java.io.IOException;

/**
 * <p></p>
 *
 * <p>FIXME: What is the last string used for?</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      GSIS
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see AOGroupMessagePacket
 */
public class AOGroupAnnouncePacket extends AOPacket {
    
    public static final short TYPE = 60;
    
    private final byte[] m_groupID;
    private final String m_groupName;
    private final int    m_groupStatus;
    private final String m_str;
    private final byte[] m_data;
    
    /** 
     * Creates a new instance of AOGroupAnnouncePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public AOGroupAnnouncePacket(byte[] data) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            AOPacketParser parser = new AOPacketParser(data);
        
            // Parse the packet
            m_groupID = parser.parse40Bit();
            m_groupName  = parser.parseString();
            m_groupStatus     = parser.parseInt();
            m_str  = parser.parseString();
        
            parser.close();
        } catch (IOException e) {
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOGroupAnnouncePacket()
    
    public byte[] getGroupID() { return m_groupID; }
    public String getGroupName() { return m_groupName; }
    public int getGroupStatus() { return m_groupStatus; }
    public String getStr() { return m_str; }
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }
    
    public String toString() {
        String result = "AOGroupAnnouncePacket: ";
        
        for (byte b : m_groupID) { result += String.format("%02X", b); }
        
        result += ", " + m_groupName;
        result += ", " + Integer.toHexString(m_groupStatus);
        result += ", " + m_str;
        
        return result;
    }   // end toString()
    
}   // end class AOGroupAnnouncePacket
