/*
 * ChatCommandPacket.java
 *
 * Created on September 11, 2010, 10:30 PM
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
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class ChatCommandPacket extends Packet  {

    public static final short TYPE = 120;

    private final byte[] m_data;
    private final String m_command;

    /**
     * Creates a new instance of ChatCommandPacket
     *
     * @param command
     *        the chat command being sent to the server
     */
    public ChatCommandPacket(String command) {
        m_command = command;
        
        // Serialize the packet
        PacketSerializer serializer = new PacketSerializer(4 + command.length());
        serializer.write(m_command);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end ChatCommandPacket()
    
    /**
     * Creates a new instance of ChatCommandPacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws MalformedPacketException
     *         if the packet is malformed
     */
    public ChatCommandPacket(byte[] data) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        try {
            m_data = data;
            PacketParser parser = new PacketParser(data);
            m_command = parser.parseString();
            
            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.OUT)
            );
        }   // end catch
    }   // end ChatCommandPacket()

    public String getCommand(){
        return m_command;
    }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return Direction.OUT; }

    @Override
    public String toString() {
        String result = "["+TYPE+"]ChatCommandPacket: ";
        result += m_command;

        return result;
    }   // end toString()
}   // end class ChatCommandPacket
