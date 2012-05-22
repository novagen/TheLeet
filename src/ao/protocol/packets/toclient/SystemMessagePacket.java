/*
 * SystemMessagePacket.java
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

package ao.protocol.packets.toclient;

import ao.db.MMDBDatabase;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class SystemMessagePacket extends Packet {
    
    public static final short TYPE = 37;

    private static final int cat_id = 20000;
    
    private final int m_clientID;
    private final int m_windowID;
    private final int m_messageID;
    private final String m_msg;
    private final ExtendedMessage m_ex_msg;
    private final byte[] m_data;

    /**
     * Creates a new instance of SimpleSystemMessagePacket
     *
     * @param clientID
     *        ???
     * @param windowID
     *        ???
     * @param messageID
     *        ???
     * @param msg
     *        The message being sent
     * @param db
     *        The mdb database used for extended messages.
     */
    public SystemMessagePacket(int clientID, int windowID, int messageID, String msg, MMDBDatabase db){
        m_clientID = clientID;
        m_windowID = windowID;
        m_messageID = messageID;
        m_msg = msg;
        m_ex_msg = new ExtendedMessage(cat_id, m_messageID, m_msg, db);

        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 + 4 + 4 + m_msg.length() );
        serializer.write(m_clientID);
        serializer.write(m_windowID);
        serializer.write(m_messageID);
        serializer.write(m_msg);

        m_data = serializer.getResult();
        serializer.close();
    }   // end SystemMessagePacket()

    /**
     * Creates a new instance of SimpleSystemMessagePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @param db
     *        The mdb database that will be used for extended messages
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public SystemMessagePacket(byte[] data, MMDBDatabase db) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data                = data;
            PacketParser parser = new PacketParser(data);
            
            // Parse the packet
            m_clientID   = parser.parseInt();
            m_windowID   = parser.parseInt();
            m_messageID   = parser.parseInt();
            m_msg = parser.parseString();
        
            parser.close();

            m_ex_msg = new ExtendedMessage(cat_id, m_messageID, m_msg, db);

        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end SystemMessagePacket()
    
    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    public String getMsgType() { return Integer.toHexString(m_messageID); }
    public int getCharID() { return m_clientID; }

    /** Always returns {@code Direction.IN} */
    public Direction getDirection() { return Direction.IN; }

    public String display(){
        return m_ex_msg.getFormattedMessage();
    }
    
    @Override
    public String toString() {
        return "["+TYPE+"]SystemMessagePacket: "
            + Integer.toHexString( m_clientID ) + ", " + m_windowID + ", " + Integer.toHexString(  m_messageID ) + ", " + m_msg;
    }   // end toString()
    
}   // end class SystemMessagePacket
