/*
 * AOMessagePacket.java
 *
 * Created on March 28, 2008, 12:52 PM
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

import ao.protocol.AOCharacterIDTable;
import ao.protocol.AOGroupTable;

/**
 * <p>{@code AOMessagePacket} is the base class for 
 * a group of packets that contain a message that is/was
 * sent/received. This class is helpful for logging 
 * purposes, beacuse of the {@link #display(AOCharacterIDTable, AOGroupTable)}
 * method. It could also be helpful to those interested in
 * writing IRC type clients, since this group of packets
 * would be most important.</p>
 *
 * @author Paul Smith
 * @see #display(AOCharacterIDTable, AOGroupTable)
 * @see AOPacket
 * @see AOPrivateMessagePacket
 */
public abstract class AOMessagePacket extends AOPacket {
    
    /** Returns the message that was recieved/sent */
    public abstract String getMessage();
    
    public String display() { return display(null, null); }
    /** 
     * Returns a {@code String} that emulates what the client would see, 
     * if it was running the offical client. 
     */
    public abstract String display(AOCharacterIDTable charTable, AOGroupTable groupTable);
    
}   // end class AOMessagePacket
