/*
 * AOBotListener.java
 *
 * Created on July 24, 2007, 8:07 PM
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

import java.util.EventListener;
import ao.protocol.packets.AOPacket;

/**
 * AOBotListener provides functionality for responding to bot events.
 *
 * @author Paul Smith
 * @see ao.protocol.AOBot
 */
public interface AOBotListener extends EventListener {
    
    /** Called after a bot is connected. */
    void connected(AOBot bot);
    /** Called after a bot is authenticated. */
    void authenticated(AOBot bot);
    /** Called after a bot is logged in. */
    void loggedIn(AOBot bot);
    /** Called after a bot is started. */
    void started(AOBot bot);
    /** Called after a bot is disconnected. */
    void disconnected(AOBot bot);
    /** Called after a bot receives a packet. */
    void packet(AOBot bot, AOPacket packet);
    /** Called after a bot receives a exception. */
    void exception(AOBot bot, Exception e);
    
}   // end interface AOBotListener
