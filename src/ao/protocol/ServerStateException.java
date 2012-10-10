/*
 * ServerStateException.java
 *
 * Created on August 02, 2011
 *************************************************************************
 * Copyright 2011 Kevin Kendall
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

import ao.chat.ServerConnection.ServerState;

public class ServerStateException extends RuntimeException {
    
    private final ServerState m_currentState, m_requiredState;
    
    /** 
     * Creates a new instance of AOBotStateException. 
     *
     * @param message
     *        a helpful message explaining why this exception was thrown
     * @param currentState
     *        the state of the server connection at the time this exception was thrown
     * @param requiredState
     *        the state that the server connection should have been in at the time this exception was thrown
     */
    public ServerStateException(String message, ServerState currentState, ServerState requiredState) {
        super(message);
        m_currentState  = currentState;
        m_requiredState = requiredState;
    }   // end AOBotStateException()
    
    /** Returns the state of the bot at the time this exception was thrown. */
    public ServerState getCurrentState() { return m_currentState; }
    /** Returns the state that the bot should have been in at the time this exception was thrown. */
    public ServerState getRequiredState() { return m_requiredState; }
    
}   // end class ServerStateException
