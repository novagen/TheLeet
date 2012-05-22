/*
 * FriendUpdatePacket.java
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

public class FriendUpdatePacket extends MessagePacket {

    public static final short TYPE = 40;

    private final byte[]    m_data;
    private final int       m_characterID;
    private final boolean   m_online;
    private final boolean	m_friend;
    private final String    m_flags;
    private final Direction m_direction;
    
    public FriendUpdatePacket(int characterID, boolean friend) {
        m_characterID = characterID;
        m_friend = friend;
        if(friend){
        	m_flags = "\1";
        } else {
        	m_flags = "\0";
        }
        m_online = false;
        m_direction   = Direction.OUT;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 + m_flags.length() );
        serializer.write(m_characterID);
        serializer.write(m_flags);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end FriendUpdatePacket()
    
    public FriendUpdatePacket(int characterID, String flags) {
        m_characterID = characterID;
        m_flags = flags;
        if(flags.compareTo("\0") == 0){
        	m_friend = false;
        } else {
        	m_friend = true;
        }
        m_online = false;
        m_direction   = Direction.OUT;
        
        // Serialize the packet
        PacketSerializer serializer =
            new PacketSerializer( 4 + 4 + flags.length() );
        serializer.write(m_characterID);
        serializer.write(flags);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end FriendUpdatePacket()
    
    /**
     * Creates a new instance of FriendUpdatePacket
     *
     * @param data
     *        the binary data of this packet (without the type and length bytes)
     * @throws NullPointerException
     *         if data is null
     * @throws AOMalformedPacketException
     *         if the packet is malformed
     */
    public FriendUpdatePacket(byte[] data, Direction d) throws MalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        //FriendUpdatePacket: UInt32 (CharacterID), bool (online/offline), byte (flags)
        //uint, int, ushort + byte
        try {
            m_data = data;
            m_direction = d;
            PacketParser parser = new PacketParser(data);

            // Parse the packet
            m_characterID = parser.parseInt();

            if(parser.parseInt() == 0){
                m_online = false;
            } else {
                m_online = true;
            }
            m_flags = parser.parseString();
            if(m_flags.compareTo("\0") == 0){
            	m_friend = false;
            } else {
            	m_friend = true;
            }

            parser.close();
        } catch (IOException e) {
            throw new MalformedPacketException(
                "The packet could not be parsed.", e, new UnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end FriendUpdatePacket()

    public String getMessage() { return "hi"; }//m_msg; }
    public String getFlags() { return m_flags; }
    public boolean isOnline() { return m_online; }
    public boolean isFriend() { return m_friend; }
    public int getCharID() { return m_characterID; }

    /** Always returns {@value #TYPE} */
    public short getType() { return TYPE; }
    public byte[] getData() { return m_data; }
    /** Returns whether this message was recieved or sent by the client */
    public Direction getDirection() { return m_direction; }

    @Override
    public String toString() {
        String result = "["+TYPE+"]FriendUpdatePacket: ";

        result += Integer.toHexString( m_characterID );
        result += ", ";
        if(m_online){
            result += "Logged in";
        } else {
            result += "Logged off";
        }
        result += ", ";
        if(m_friend){
        	result += "Permanent";
        } else {
        	result += "Temporary";
        }
        
        return result;
    }   // end toString()

    public String display(CharacterIDTable charTable, GroupTable groupTable) {
        String charName  = (charTable  == null ? null : charTable.getName(m_characterID));
        String result    = charName + " Logged ";

        if (m_online) {
            result += "On";
        } else {
            result += "Off";
        }   // end else

        return result;
    }
}   // end class FriendUpdatePacket
