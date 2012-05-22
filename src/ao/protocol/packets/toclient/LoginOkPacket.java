/*
 * LoginOkPacket.java
 *
 * Created on May 13, 2007, 5:36 PM
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
package ao.protocol.packets.toclient;

import ao.protocol.packets.Packet;

/**
 * <p>LoginOkPacket is sent from the AO server to the client
 * if the client was successfully logged in.</p>
 *
 * <p>PACKET TYPE: {@value #TYPE}
 * <br>FORMAT:      empty
 * <br>DIRECTION:   in</p>
 *
 * @author Paul Smith
 * @see ao.protocol.packets.toclient.LoginErrorPacket
 * @see ao.protocol.packets.toserver.LoginSelectPacket
 */
public class LoginOkPacket extends Packet {

    public static final short TYPE = 5;
    private final byte[] m_data = {};

    /** Creates a new instance of LoginOkPacket */
    public LoginOkPacket() {
    }   // end LoginOkPacket()

    /** Always returns {@value #TYPE} */
    public short getType() {
        return TYPE;
    }

    public byte[] getData() {
        return m_data;
    }

    /** Always returns {@code Direction.IN} */
    public Direction getDirection() {
        return Direction.IN;
    }

    @Override
    public String toString() {
        return "[" + TYPE + "]LoginOkPacket";
    }   // end toString()
}   // end class LoginOkPacket

