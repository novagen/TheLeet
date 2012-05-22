/*
 * ExtendedMessage.java
 *
 *************************************************************************
 * Copyright 2011 Tyrence
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ao.db.MMDBDatabase;

public class ExtendedMessage {

    public static final String ENCODING = "ISO-8859-1";
    private long categoryId;
    private long instanceId;
    private String message;
    private List<Object> params;

    public ExtendedMessage(long categoryId, long instanceId, String paramString, MMDBDatabase db) {
        this.categoryId = categoryId;
        this.instanceId = instanceId;
        if (db == null) {
            message = paramString;
        } else {
            this.message = db.getMessage(categoryId, instanceId);

            try {
                params = parseParams(new DataInputStream(new ByteArrayInputStream(paramString.getBytes("UTF-8"))), db);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ExtendedMessage(DataInputStream dataInputStream, MMDBDatabase db) {
        this.categoryId = b85g(dataInputStream);
        this.instanceId = b85g(dataInputStream);
        if (db == null) {
        } else {
            this.message = db.getMessage(categoryId, instanceId);

            try {
                params = parseParams(dataInputStream, db);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<Object> parseParams(DataInputStream dataInputStream, MMDBDatabase db) throws IOException {
        List<Object> params = new ArrayList<Object>();

        while (dataInputStream.available() > 0) {
            char paramType = (char) dataInputStream.read();
            switch (paramType) {
                case 'R':
                    // reference
                    params.add(db.getMessage(b85g(dataInputStream), b85g(dataInputStream)));
                    break;

                case 'i':
                case 'u':
                    // long
                    params.add(b85g(dataInputStream));
                    break;

                case 'S':
                    // string
                    int stringLength = dataInputStream.readShort();
                    byte[] bytes = new byte[stringLength];
                    dataInputStream.readFully(bytes);

                    params.add(new String(bytes, ENCODING));
                    break;

                default:
                    throw new RuntimeException("Unknown param type in extended message: '" + paramType + "'");
            }
        }

        return params;
    }

    public static long b85g(InputStream input) {
        try {
            long n = 0;
            for (int i = 0; i < 5; i++) {
                n = (n * 85) + input.read() - 33;
            }
            return n;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFormattedMessage() {
        return String.format(message, params.toArray());
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    public long getCategoryId() {
        return categoryId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public String getMessage() {
        return message;
    }
}
