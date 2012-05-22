/*
 * Bot.java
 *
 * Created on May 12, 2007, 2:46 PM
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

package ao.protocol;

import ao.protocol.packets.Packet;
import java.io.IOException;

/**
 * Bot specifies the basic structure of a bot.
 * All bot implementations should implement the Bot interface.
 *
 * @author Paul Smith
 * @see ao.protocol.packets.utils.PacketFactory
 */
public interface Client extends Runnable {
    
    /** AOBot.State defines the various states that a bot can be in at any point in time. */
    public enum State {
        DISCONNECTED, CONNECTED, AUTHENTICATED, LOGGED_IN;
    }   // end enum State
    
    /** Returns the current state of this bot. */
    State getState();
    
    /** 
     * Returns the character that the bot is logged in as 
     * (or null if the bot is not logged in).
     */
    CharacterInfo getCharacter();
    
    /** Returns the bot's current style sheet. */
    // AOBotStyleSheet getStyleSheet();
    
    /**
     * Reads the next packet from the server. 
     * 
     * @throws AOBotStateException if the bot is in the {@link State#DISCONNECTED} state
     * @throws IOException if an error occurred while attempting to read a packet
     */
    Packet nextPacket() throws IOException;
    /**
     * Sends a packet to the server. 
     * 
     * @throws AOBotStateException if the bot is in the {@link State#DISCONNECTED} state
     * @throws IOException if an error occurred while attempting to send a packet
     */
    void sendPacket(Packet packet) throws IOException;
    
    /**
     * Connects the bot to the server. This is simply a convenience method,
     * {@code connect(server)} is equivalent to
     * {@code connect( server.getURL(), server.getPort() )}
     * 
     * @throws NullPointerException if {@code server} is null
     * @throws BotStateException if the bot is not in the {@link State#DISCONNECTED} state
     * @throws IOException if an error occurred while attempting to connect to the server
     *
     * @see ao.protocol.DimensionAddress
     */
    void connect(DimensionAddress server) throws IOException;
    /**
     * Connects the bot to the server. 
     * 
     * @throws BotStateException if the bot is not in the {@link State#DISCONNECTED} state
     * @throws IOException if an error occurred while attempting to connect to the server
     */
    void connect(String server, int port) throws IOException;
    /**
     * Authenticates a user with the server.
     * 
     * @throws BotStateException if the bot is not in the {@link State#CONNECTED} state
     * @throws IOException if an error occurred while attempting to authenticate the client with the server
     */
    void authenticate(String userName, String password) throws IOException;
    /**
     * Logs a character into the server. 
     * 
     * @throws BotStateException if the bot is not in the {@link State#AUTHENTICATED} state
     * @throws IOException if an error occurred while attempting to log the client into the server
     */
    void login(CharacterInfo character) throws IOException;
    /**
     * Starts the bot. 
     * 
     * @throws BotStateException if the bot is not in the {@link State#LOGGED_IN} state
     */
    void start();
    /** 
     * Disconnects the bot from the server. 
     *
     * @throws IOException if an error occurred while attempting to disconnect the client
     */
    void disconnect() throws IOException;
    
    /** Add a listener to this bot. */
    void addListener(ClientListener l);
    /** Remove a listener from this bot. */
    void removeListener(ClientListener l);
    /** Adds a logger to this bot. */
    void addLogger(ClientLogger logger);
    /** Removes a logger from this bot. */
    void removeLogger(ClientLogger logger);
    
}   // end interface Bot
