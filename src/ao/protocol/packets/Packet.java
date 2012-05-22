/*
 * Packet.java
 *
 * Created on May 12, 2007, 2:30 PM
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
 * {@code AOPacket} is the base class for all packets.
 *
 * @author Paul Smith
 */
public abstract class Packet {
    
    public enum Direction {IN, OUT};
    
    /** Returns the type of this packet. */
    public abstract short getType();
    /** Returns the binary data of this packet (without the type and length bytes). */
    public abstract byte[] getData();
    /** Returns what direction this packet has traveled/will travel */
    public abstract Direction getDirection();
    
    public String toString() {
        return "AOPacket: " + getType() + ", " + new String( getData() );
    }   // end toString()
    
}   // end class AOPacket
