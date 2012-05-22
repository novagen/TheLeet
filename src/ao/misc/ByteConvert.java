/*
 * ByteConvert.java
 *
 * Created on Sep 16, 2010, 5:30 PM
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
package ao.misc;

public class ByteConvert {

    public static int swap(int id) {
        byte[] a = intToByte(id);
        byte[] b = new byte[a.length];

        for (int i = 0; i < a.length; i++) {
            b[a.length - i - 1] = a[i];
        }

        return byteToInt(b);
    }

    public static byte[] intToByte(int id) {
        byte[] b = new byte[4];
        b[0] = (byte) (id >> 24);
        b[1] = (byte) ((id << 8) >> 24);
        b[2] = (byte) ((id << 16) >> 24);
        b[3] = (byte) ((id << 24) >> 24);
        return b;
    }

    public static int byteToInt(byte[] id) {
        int i = 0;
        int pos = 0;
        i += unsignedByteToInt(id[pos++]) << 24;
        i += unsignedByteToInt(id[pos++]) << 16;
        i += unsignedByteToInt(id[pos++]) << 8;
        i += unsignedByteToInt(id[pos++]) << 0;
        return i;
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    public static long byteToLong(byte[] id) {
        long value = 0;
        for (int i = 0; i < id.length; i++) {
            value = (value << 8) + (id[i] & 0xff);
        }
        return value;
    }
}
