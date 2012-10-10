/*
 * PacketSerializer.java
 *
 * Created on May 13, 2007, 4:09 PM
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * AOPacketSerializer is a utility class for serializing packets for transmission.
 *
 * @author Paul Smith
 */
public class PacketSerializer {
    
    private ByteArrayOutputStream m_buffer;
    private DataOutputStream      m_output;
    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    /** Creates a new instance of PacketSerializer */
    public PacketSerializer() {
        this(Short.MAX_VALUE);
    }   // end PacketSerializer()
    
    /** 
     * Creates a new instance of AOPacketSerializer.
     * This version of the constructor accepts an estimate
     * of the total length of the packet's data. The estimate
     * is not required to be exact, but a better estimate will 
     * likely improve performance when serializing a packet.
     *
     * @param size 
     *        an estimate of the total length of the packet's binary data
     */
    public PacketSerializer(int size) {
        m_buffer = new ByteArrayOutputStream(size);
        m_output = new DataOutputStream(m_buffer);
    }   // end PacketSerializer()
    
    /** 
     * Serializes a byte (8 bits) 
     * @see PacketParser#parseByte()
     */
    public void write(byte data) {
        try { 
            m_output.writeByte(data);
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** 
     * Serializes a short (16 bits) 
     * @see PacketParser#parseShort()
     */
    public void write(short data) {
        try { 
            m_output.writeShort(data);
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** 
     * Serializes a int (32 bits) 
     * @see PacketParser#parseInt()
     */
    public void write(int data) {
        try { 
            m_output.writeInt(data);
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** 
     * Serializes a long (64 bits) 
     * @see PacketParser#parseLong()
     */
    public void write(long data) {
        try { 
            m_output.writeLong(data);
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** 
     * <p>Serializes a string (16 bit length, 8*length bit character data)</p>
     * 
     * <p>NOTE: Characters are serialized as 8 bit ASCII, rather than Java's normal 16 bit Unicode.</p>
     * 
     * @see #write(short)
     * @see PacketParser#parseString()
     */
    public void write(String data) {
        try {
            m_output.writeUTF(data);
            /**
            byte[] utf8 = data.getBytes(UTF8_CHARSET);
            char[] chars = data.toCharArray();
            int length   = chars.length;
            
            m_output.writeShort(length);
            for (int i = 0; i < length; ++i) {
                m_output.writeByte(chars[i]);
            }   // end for
             */
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** 
     * Serializes a int array (16 bit length, 32*length bit int data)
     *
     * @see #write(short)
     * @see #write(int)
     * @see PacketParser#parseIntArray()
     */
    public void write(int[] data) {
        try { 
            m_output.writeShort( data.length );
            for (int i : data) { write(i); }
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()

    /** 
     * Serializes a string array (16 bit length, data[0], data[1], data[2], ...)
     *
     * @see #write(short)
     * @see #write(String)
     * @see PacketParser#parseStringArray()
     */
    public void write(String[] data) {
        try { 
            m_output.writeShort( data.length );
            for (String s : data) { write(s); }
        } catch (IOException e) { e.printStackTrace(); }
    }   // end write()
    
    /** Returns the net results of all previous calls to write */
    public byte[] getResult() { return m_buffer.toByteArray(); }
    
    /** 
     * Serializes a 40 bit chunck of data
     *
     * @see #write(byte)
     * @see PacketParser#parse40Bit()
     */
    public void write40Bit(byte[] data) {
        if (data.length != 5) { 
            throw new IllegalArgumentException(
                "This chunk of data is composed of " + (data.length*8) + " bits rather than 40 bits"
            ); 
        } else {
            for (byte b : data) { write(b); }
        }   // end else
    }   // end write40Bit()

    /** Closes the serializer's underlying stream */
    public void close() { 
        try { 
            m_output.close();
            m_output = null;
            m_buffer = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            
        }
    }   // end close()
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }   // end dispose()
    
}   // end class PacketSerializer
