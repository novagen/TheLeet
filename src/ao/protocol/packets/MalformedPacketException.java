/*
 * MalformedPacketException.java
 *
 * Created on November 5, 2007, 9:09 AM
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

import java.io.IOException;

/**
 * An AOMalformedPacketException is thrown when a packet's binary data
 * is malformed and thus could not be successfully parsed.
 *
 * @author Paul Smith
 */
public class MalformedPacketException extends IOException {
    
    private final UnparsablePacket m_packet;
    
    /** Creates a new instance of AOMalformedPacketException */
    public MalformedPacketException(String message, UnparsablePacket packet) {
        super(message);
        m_packet = packet;
    }   // end AOMalformedPacketException()
    
    /** Creates a new instance of AOMalformedPacketException */
    public MalformedPacketException(String message, Throwable cause, UnparsablePacket packet) {
        //super(message, cause); Unusable in android
    	super(message);
        m_packet = packet;
    }   // end AOMalformedPacketException()
    
    /** Returns the data of the packet that could not be parsed */
    public UnparsablePacket getPacket() { return m_packet; }
    
}   // end class AOMalformedPacketException
