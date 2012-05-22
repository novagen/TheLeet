/*
 * PacketFactory.java
 *
 * Created on May 12, 2007, 3:45 PM
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

package ao.protocol.packets.utils;

import ao.protocol.packets.MalformedPacketException;
import ao.protocol.packets.Packet;

/**
 * Implementations of {@code AOPacketFactory} are used to parse packets recieved
 * from the server in a polymorphic fashion. Using a packet factory 
 * allows a bot to be upgraded to a new protocol with less effort, 
 * and allows new packet types to be easily added/removed.
 *
 * @author Paul Smith
 */
public interface PacketFactory {
    
    /** 
     * Converts the raw data of a packet into usable data. 
     *
     * @param type
     *        the type of the packet to be parsed
     * @param data
     *        the binary data of the packet to be parsed (without the type and length bytes)
     * @return
     *        a parsed packet or an instance of {@link ao.protocol.packets.UnparsablePacket} 
     *        if the packet type is not recognized
     * @throws ao.protocol.packets.MalformedPacketException
     *         if the packet could not be successfully parsed 
     *
     * @see ao.protocol.packets.UnparsablePacket
     * @see ao.protocol.packets.MalformedPacketException
     */
    Packet toPacket(short type, byte[] data) throws MalformedPacketException;
    
}   // end interface AOPacketFactory
