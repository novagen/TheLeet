/*
 * UnparsablePacket.java
 *
 * Created on May 13, 2007, 12:40 PM
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

package ao.protocol.packets;

/**
 * AOUnparsablePacket is used to encapsulate packet data
 * that could not successfully be parsed for whatever reason.
 *
 * @author Paul Smith
 */
public class UnparsablePacket extends Packet {
    
    private final short     m_type;
    private final byte[]    m_data;
    private final Direction m_direction;
    
    /** 
     * Creates a new instance of AOUnparsablePacket 
     *
     * @param type
     *        the type of this packet
     * @param data
     *        the binary data of this packet
     */
    public UnparsablePacket(short type, byte[] data, Direction d) {
        m_type      = type;
        m_data      = data;
        m_direction = d;
    }   // end AOUnparsablePacket()
    
    public short getType() { return m_type; }
    public byte[] getData() { return m_data; }
    public Direction getDirection() { return m_direction; }
    
    public String toString() {
        return "AOUnparsablePacket: " + getType() + ", " + new String( getData() );
    }   // end toString()
    
}   // end class AOUnparsablePacket
