/*
 * AOSimplePacketFactory.java
 *
 * Created on May 12, 2007, 3:49 PM
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

import ao.protocol.packets.out.AOPrivateGroupPartPacket;
import ao.protocol.packets.out.AOPrivateGroupJoinPacket;
import ao.protocol.packets.*;
import ao.protocol.packets.in.*;
import ao.protocol.packets.out.*;
import ao.protocol.packets.bi.*;

/**
 * {@code AOSimplePacketFactory} is an implementation of 
 * {@link ao.protocol.packets.utils.AOPacketFactory} 
 * that parses packets into all known and implemented packet types.
 *
 * @author Paul Smith
 * @see #toPacket(short, byte[])
 * @see ao.protocol.packets.utils.AOPacketFactory
 * @see ao.protocol.packets.AOPacket
 */
public class AOSimplePacketFactory implements AOPacketFactory {
    
    /** Creates a new instance of AOSimplePacketFactory */
    public AOSimplePacketFactory() {
    }   // end AOSimplePacketFactory()
    
    /** 
     * Converts the raw data of a packet into usable data.
     *
     * @param type
     *        the type of the packet to be parsed
     * @param data
     *        the binary data of the packet to be parsed (without the type and length bytes)
     * @return
     *        a parsed packet or an instance of {@link ao.protocol.packets.AOUnparsablePacket} 
     *        if the packet type is not recognized
     * @throws ao.protocol.packets.AOMalformedPacketException
     *         if the packet could not be successfully parsed 
     *
     * @see ao.protocol.packets.utils.AOPacketFactory#toPacket(short, byte[])
     * @see ao.protocol.packets.AOUnparsablePacket
     * @see ao.protocol.packets.AOMalformedPacketException
     */

    /**
    INCOMING PACKETS
        NULL = -1,
        LOGIN_SEED = 0,
        LOGIN_OK = 5,
        LOGIN_ERROR = 6,
        LOGIN_CHARACTERLIST = 7,
        CLIENT_UNKNOWN = 10,
        CLIENT_NAME = 20,
        VICINITY_MESSAGE = 34,
        ANON_MESSAGE = 35,
        SYSTEM_MESSAGE = 36,
        MESSAGE_SYSTEM = 37,
        PRIVATE_CHANNEL_JOIN = 52,
        PRIVATE_CHANNEL_PART = 53,
        PRIVATE_CHANNEL_CLIENTJOIN = 55,
        PRIVATE_CHANNEL_CLIENTPART = 56,
        CHANNEL_STATUS = 60, ---
        CHANNEL_PART = 61,   ---
        FORWARD = 110, ---
        AMD_MUX_INFO = 1100, ---

    OUTGOING PACKETS
        LOGIN_RESPONSE = 2,
        LOGIN_SELCHAR = 3,
        PRIVATE_CHANNEL_KICKALL = 54,
        CHANNEL_UPDATE = 64,
        CHANNEL_CLIMODE = 66,
        CLIENTMODE_GET = 70,
        CLIENTMODE_SET = 71,
        CHAT_COMMAND = 120,

    BIDIRECTIONAL PACKETS
        NAME_LOOKUP = 21,
        PRIVATE_MESSAGE = 30,
        FRIEND_ADD = 40,
        FRIEND_REMOVE = 41,
        PRIVATE_CHANNEL_INVITE = 50,
        PRIVATE_CHANNEL_KICK = 51,
        PRIVGRP_MESSAGE = 57,
        CHANNEL_MESSAGE = 65,
        PING = 100,
     */
    public AOPacket toPacket(short type, byte[] data) throws AOMalformedPacketException {
        switch (type) {
            //Incoming Packets
            case AOLoginSeedPacket.TYPE:
                return new AOLoginSeedPacket(data);            // TYPE 0
            case AOLoginOkPacket.TYPE:
                return new AOLoginOkPacket();                   // TYPE 5
            case AOLoginErrorPacket.TYPE:
                return new AOLoginErrorPacket(data);            // TYPE 6
            case AOCharListPacket.TYPE:
                return new AOCharListPacket(data);              // TYPE 7
            case AOClientUnknownPacket.TYPE:
                return new AOClientUnknownPacket(data);         // TYPE 10
            case AOChatNoticePacket.TYPE:
                return new AOChatNoticePacket(data);            // TYPE 20
            case AOVicinityMessagePacket.TYPE:
                return new AOVicinityMessagePacket(data);       // TYPE 34
            case AOAnonVicinityMessagePacket.TYPE:
                return new AOAnonVicinityMessagePacket(data);   // TYPE 35
            case AOSystemMessagePacket.TYPE:
                return new AOSystemMessagePacket(data);         // TYPE 36
            case AOClientNamePacket.TYPE:
                return new AOClientNamePacket(data);            // TYPE 37
            case AOPrivateGroupJoinPacket.TYPE:
                return new AOPrivateGroupJoinPacket(data);         // TYPE 52
            case AOPrivateGroupPartPacket.TYPE:
                return new AOPrivateGroupPartPacket(data);         // TYPE 53
            case AOPrivateGroupClientJoinPacket.TYPE:
                return new AOPrivateGroupClientJoinPacket(data);         // TYPE 55
            case AOPrivateGroupClientPartPacket.TYPE:
                return new AOPrivateGroupClientPartPacket(data);         // TYPE 56
            case AOGroupAnnouncePacket.TYPE:
                return new AOGroupAnnouncePacket(data);         // TYPE 60

            //Outgoing Packets
            case AOLoginRequestPacket.TYPE:
                return new AOLoginRequestPacket(data);          // TYPE 2
            case AOLoginSelectPacket.TYPE:
                return new AOLoginSelectPacket(data);           // TYPE 3
            case AOChatCommandPacket.TYPE:
                return new AOChatCommandPacket(data);    // TYPE 120

            //Bidirectional Packets
            case AOClientLookupPacket.TYPE:
                return new AOClientLookupPacket(data, AOPacket.Direction.IN);    // TYPE 21
            case AOPrivateMessagePacket.TYPE:
                return new AOPrivateMessagePacket(data, AOPacket.Direction.IN);   // TYPE 30
            case AOFriendUpdatePacket.TYPE:
                return new AOFriendUpdatePacket(data, AOPacket.Direction.IN);          // TYPE 40
            case AOFriendRemovePacket.TYPE:
                return new AOFriendRemovePacket(data, AOPacket.Direction.IN);          // TYPE 41
            case AOPrivateGroupInvitePacket.TYPE:
                return new AOPrivateGroupInvitePacket(data, AOPacket.Direction.IN);    // TYPE 50
            case AOPrivateGroupKickPacket.TYPE:
                return new AOPrivateGroupKickPacket(data, AOPacket.Direction.IN);      // TYPE 51
            case AOPrivateGroupMessagePacket.TYPE:
                return new AOPrivateGroupMessagePacket(data, AOPacket.Direction.IN);    // TYPE 57
            case AOGroupMessagePacket.TYPE:
                return new AOGroupMessagePacket(data, AOPacket.Direction.IN);   // TYPE 65
            case AOPingPacket.TYPE:
                return new AOPingPacket(data, AOPacket.Direction.IN);   // TYPE 100
                
            //Unparsed Packets
            default:
                return new AOUnparsablePacket(type, data, AOPacket.Direction.IN);
        }   // end switch
    }   // end toPacket()
}   // end class AOSimplePacketFactory
