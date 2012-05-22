/*
 * BotStateException.java
 *
 * Created on July 22, 2007, 12:18 PM
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

/**
 * <p>An AOBotStateException is thrown when a bot is instructed to perform an operation
 * while in the incorrect state for that operation.</p>
 *
 * <p>For instance, an implementation of AOBot should throw an AOBotStateException
 * if the it is instructed to connect to a server, while not in the DISCONNECTED state.</p>
 *
 * @author Paul Smith
 * @see ao.protocol.Client
 */
public class ClientStateException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -1733242975299634046L;
	private final Client.State m_currentState, m_requiredState;
    
    /** 
     * Creates a new instance of AOBotStateException. 
     *
     * @param message
     *        a helpful message explaining why this exception was thrown
     * @param currentState
     *        the state of the bot at the time this exception was thrown
     * @param requiredState
     *        the state that the bot should have been in at the time this exception was thrown
     */
    public ClientStateException(String message, Client.State currentState, Client.State requiredState) {
        super(message);
        m_currentState  = currentState;
        m_requiredState = requiredState;
    }   // end AOBotStateException()
    
    /** Returns the state of the bot at the time this exception was thrown. */
    public Client.State getCurrentState() { return m_currentState; }
    /** Returns the state that the bot should have been in at the time this exception was thrown. */
    public Client.State getRequiredState() { return m_requiredState; }
    
}   // end class AOBotStateException
