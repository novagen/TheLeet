/*
 * PluginUser.java
 *
 * Created on Sep 16, 2010, 4:15 PM
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

package ao.plugins;

import ao.chat.ChatClient;
import ao.db.Database;
import ao.protocol.PacketListener;
import ao.protocol.packets.Packet;

public interface PluginUser {
    /** Prints a string. */
    void print(String name, String msg);
    /** Returns a reference to the database object */
    Database getDatabase();
    /** Displays a exception. */
    void exception(String name, Exception e);
    /** Sends a packet */
    void sendPacket(Packet packet);
    /** Returns a reference to the chatbot */
    ChatClient getClient();
    /** Adds a client listener */
    void addListener(PacketListener listener);
}
