/*
 * AOFriendUpdatePacket.java
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

import ao.protocol.AOCharacterIDTable;
import ao.protocol.AOGroupTable;
import ao.protocol.packets.*;
import ao.protocol.packets.utils.AOPacketParser;
import ao.protocol.packets.utils.AOPacketSerializer;

import java.io.IOException;

public class AOFriendUpdatePacket extends AOMessagePacket {

    public static final short TYPE = 40;

    private final byte[]    m_data;
    private final int       m_characterID;
    private final boolean   m_online;
    private final boolean	m_friend;
    private final String    m_flags;
    private final Direction m_direction;
    
    public AOFriendUpdatePacket(int characterID, boolean friend) {
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
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 4 + 4 + m_flags.length() );
        serializer.write(m_characterID);
        serializer.write(m_flags);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOPrivateMessagePacket()
    
    public AOFriendUpdatePacket(int characterID, String flags) {
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
        AOPacketSerializer serializer = 
            new AOPacketSerializer( 4 + 4 + flags.length() );
        serializer.write(m_characterID);
        serializer.write(flags);
        
        m_data = serializer.getResult();
        serializer.close();
    }   // end AOPrivateMessagePacket()
    
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
    public AOFriendUpdatePacket(byte[] data, Direction d) throws AOMalformedPacketException {
        if (data == null) { throw new NullPointerException("No binary data was passed."); }
        //FriendStatusPacket: UInt32 (CharacterID), bool (online/offline), byte (flags)
        //uint, int, ushort + byte
        try {
            m_data = data;
            m_direction = d;
            AOPacketParser parser = new AOPacketParser(data);

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
            throw new AOMalformedPacketException(
                "The packet could not be parsed.", e, new AOUnparsablePacket(TYPE, data, Direction.IN)
            );
        }   // end catch
    }   // end AOGroupAnnouncePacket()

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

    public String toString() {
        String result = "AOFriendUpdatePacket: ";

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

    public String display(AOCharacterIDTable charTable, AOGroupTable groupTable) {
        String charName  = (charTable  == null ? null : charTable.getName(m_characterID));
        String result    = charName + " Logged ";

        if (m_online) {
            result += "On";
        } else {
            result += "Off";
        }   // end else

        return result;
    }
}   // end class AOGroupMessagePacket
