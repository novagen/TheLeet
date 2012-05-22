/*
 * PrivateChannelKickAllPacket.java
 *
 * Created on September 13, 2010, 3:00 PM
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

package ao.protocol.packets.toserver;

import ao.protocol.packets.*;

public class PrivateChannelKickAllPacket extends Packet  {

    public static final short TYPE = 54;

    private final byte[] m_data = {};
    
    public PrivateChannelKickAllPacket() {
    }   // end PrivateChannelKickAllPacket()

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return Direction.OUT; }

    @Override
    public String toString() {
    	return "["+TYPE+"]PrivateChannelKickAllPacket";
    }   // end toString()
}   // end class PrivateChannelKickAllPacket
