/*
 * FriendRemovePacket.java
 *
 * Created on July 11, 2010, 2:30 PM
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

package ao.protocol.packets.bi;

import ao.protocol.CharacterIDTable;
import ao.protocol.GroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.PacketParser;
import ao.protocol.packets.utils.PacketSerializer;

import java.io.IOException;

public class FriendRemovePacket extends MessagePacket {

    public static final short TYPE = 41;

    private final byte[]    m_data;
    private final int       m_characterID;
    private final Direction m_direction;
    
    public FriendRemovePacket(int characterID) {
        m_characterID = characterID;
        m_direction   = Direction.OUT;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 );
        serializer.write(m_characterID);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end FriendRemovePacket()
    
    /**
     * Creates a new instance of FriendRemovePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public FriendRemovePacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        
        try {
            m_data = data;
            m_direction = d;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_characterID = parser.parseInt();

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end FriendRemovePacket()

    public String getMessage() { return "hi"; }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }
    public int getCharID() { return m_characterID; }

    @Override
    public String toString() {
        String result = "["+TYPE+"]FriendRemovePacket: ";
        result += Integer.toHexString( m_characterID );
        return result;
    }   // end toString()

    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        String charName  = (charTable  == null ? null : charTable.getName(m_characterID));
        String result    = charName + " removed from friends ";
        return result;
    }
}   // end class FriendRemovePacket
