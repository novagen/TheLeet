/*
 * AOPacketParser.java
 *
 * Created on May 13, 2007, 3:15 PM
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * AOPacketParser is a utility class for parsing the binary data of packets.
 *
 * @author Paul Smith
 */
public class AOPacketParser {

    private DataInputStream m_input;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    /** 
     * Creates a new instance of AOPacketParser 
     *
     * @param data
     *        the binary data that will be parsed
     */
    public AOPacketParser(byte[] data) {
        m_input = new DataInputStream(new ByteArrayInputStream(data));
    }   // end AOPacketParser()

    /** 
     * Parses the next byte (8 bits) 
     * @see AOPacketSerializer#write(byte)
     */
    public byte parseByte() throws IOException {
        return m_input.readByte();
    }   // end parseByte()

    /** 
     * Parses the next short (16 bits) 
     * @see AOPacketSerializer#write(short)
     */
    public short parseShort() throws IOException {
        return m_input.readShort();
    }   // end parseShort()

    /** 
     * Parses the next int (32 bits) 
     * @see AOPacketSerializer#write(int)
     */
    public int parseInt() throws IOException {
        return m_input.readInt();
    }   // end parseInt()

    /** 
     * Parses the next long (64 bits) 
     * @see AOPacketSerializer#write(long)
     */
    public long parseLong() throws IOException {
        return m_input.readLong();
    }   // end parseLong()

    /** 
     * Parses a string (16 bit length, 8*length bit character data) 
     *
     * <p>NOTE: Characters are parsed as 8 bit ASCII, rather than Java's normal 16 bit Unicode.</p>
     *
     * @see #parseShort()
     * @see AOPacketSerializer#write(String)
     */
    public String parseString() throws IOException {
        short length = parseShort();
        byte[] bytes = new byte[length];
        m_input.readFully(bytes);
        return new String(bytes, "UTF-8");
        //return new String(bytes, UTF8_CHARSET);
    }   // end parseString()

    /** 
     * Parses a int array (16 bit length, 32*length bit int data) 
     *
     * @see #parseShort()
     * @see #parseInt()
     * @see AOPacketSerializer#write(int[])
     */
    public int[] parseIntArray() throws IOException {
        short length = parseShort();
        int[] result = new int[length];

        for (int i = 0; i < length; ++i) {
            result[i] = parseInt();
        }   // end for

        return result;
    }   // end parseIntArray()

    /** 
     * Parses a string array (16 bit length, strings...)
     *
     * @see #parseShort()
     * @see #parseString()
     * @see AOPacketSerializer#write(String[])
     */
    public String[] parseStringArray() throws IOException {
        short length = parseShort();
        String[] result = new String[length];

        for (int i = 0; i < length; ++i) {
            result[i] = parseString();
        }   // end for

        return result;
    }   // end parseStringArray()

    /** 
     * Parses a 40 bit chunck of data
     *
     * @see #parseByte()
     * @see AOPacketSerializer#write40Bit(byte[])
     */
    public byte[] parse40Bit() throws IOException {
        byte[] result = new byte[5];

        for (int i = 0; i < 5; ++i) {
            result[i] = parseByte();
        }   // end for

        return result;
    }   // end parse40Bit()

    /** Closes the parser's underlying stream */
    public void close() {
        try {
            m_input.close();
            m_input = null;
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
    }   // end close()

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }   // end dispose()

}   // end class AOPacketParser

